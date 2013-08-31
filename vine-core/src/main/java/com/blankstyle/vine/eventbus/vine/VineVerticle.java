/*
* Copyright 2013 the original author or authors.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package com.blankstyle.vine.eventbus.vine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Future;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.impl.DefaultFutureResult;
import org.vertx.java.core.json.JsonObject;

import com.blankstyle.vine.context.ConnectionContext;
import com.blankstyle.vine.context.SeedContext;
import com.blankstyle.vine.context.VineContext;
import com.blankstyle.vine.eventbus.ReliableBusVerticle;
import com.blankstyle.vine.eventbus.ReliableEventBus;
import com.blankstyle.vine.eventbus.TimeoutException;
import com.blankstyle.vine.eventbus.WrappedReliableEventBus;
import com.blankstyle.vine.messaging.ConnectionPool;
import com.blankstyle.vine.messaging.DefaultChannel;
import com.blankstyle.vine.messaging.DefaultConnectionPool;
import com.blankstyle.vine.messaging.Dispatcher;
import com.blankstyle.vine.messaging.JsonMessage;
import com.blankstyle.vine.messaging.ReliableChannel;
import com.blankstyle.vine.messaging.ReliableEventBusConnection;

/**
 * A vine verticle.
 *
 * The Vine verticle is the primary entry point for communicating with a vine.
 * It listens for messages on a unique address, dispatches incoming messaages to
 * the first worker, and tracks messages for completion. If a message fails to
 * be processed through the vine in a sufficient amount of time the message will
 * be resent down the vine.
 *
 * Note that the vine does *not* monitor the status of worker processes. The root
 * (in local mode) or the stem (in remote mode) will monitor the status of worker
 * processes and start or stop them as necessary.
 *
 * @author Jordan Halterman
 */
public class VineVerticle extends ReliableBusVerticle implements Handler<Message<JsonObject>> {

  /**
   * The vine context.
   */
  private VineContext context;

  /**
   * The message process time expiration.
   */
  private long messageExpiration;

  /**
   * The maximum process queue size.
   */
  private int maxQueueSize;

  /**
   * The vine verticle address.
   */
  private String address;

  /**
   * The current message correlation ID.
   */
  private long currentID;

  /**
   * A collection of channels to which to dispatch messages.
   */
  private Collection<ReliableChannel> channels;

  /**
   * A reliable eventbus implementation.
   */
  private ReliableEventBus eventBus;

  /**
   * A map of correlation IDs to feed messages.
   */
  private Map<Long, Message<JsonObject>> feedRequests = new HashMap<Long, Message<JsonObject>>();

  /**
   * A map of correlation IDs to message objects.
   */
  private Map<Long, JsonMessage> inProcess = new HashMap<Long, JsonMessage>();

  /**
   * A map of correlation IDs to future results.
   */
  private Map<Long, Future<JsonObject>> futureResults = new HashMap<Long, Future<JsonObject>>();

  private String[] tagNames;

  @Override
  public void setVertx(Vertx vertx) {
    super.setVertx(vertx);
    EventBus eventBus = vertx.eventBus();
    if (eventBus instanceof ReliableEventBus) {
      this.eventBus = (ReliableEventBus) eventBus;
    }
    else {
      this.eventBus = new WrappedReliableEventBus(eventBus, vertx);
    }
  }

  @Override
  protected void start(ReliableEventBus eventBus) {
    address = getMandatoryStringConfig("address");
    context = new VineContext(config);
    messageExpiration = context.getDefinition().getMessageExpiration();
    maxQueueSize = context.getDefinition().getMaxQueueSize();
    setupTagNames();
    setupChannels();
    eventBus.registerHandler(address, this);
  }

  /**
   * Creates an array of tag names for validation of message results.
   */
  private void setupTagNames() {
    Collection<SeedContext> seeds = context.getSeedContexts();
    List<String> tags = new ArrayList<String>();
    Iterator<SeedContext> iter = seeds.iterator();
    while (iter.hasNext()) {
      tags.add(iter.next().getAddress());
    }
    tagNames = tags.toArray(new String[tags.size()]);
  }

  /**
   * Sets up vine channels.
   */
  private void setupChannels() {
    channels = new ArrayList<ReliableChannel>();
    Collection<ConnectionContext> connections = context.getConnectionContexts();
    Iterator<ConnectionContext> iter = connections.iterator();
    while (iter.hasNext()) {
      ConnectionContext connectionContext = iter.next();
      try {
        JsonObject grouping = connectionContext.getGrouping();
        Dispatcher dispatcher = (Dispatcher) Class.forName(grouping.getString("dispatcher")).newInstance();

        // Set options on the dispatcher. All non-"dispatcher" values
        // are considered to be dispatcher options.
        Iterator<String> fieldNames = grouping.getFieldNames().iterator();
        while (fieldNames.hasNext()) {
          String fieldName = fieldNames.next();
          if (fieldName != "dispatcher") {
            String value = grouping.getString(fieldName);
            dispatcher.setOption(fieldName, value);
          }
        }

        // Create a connection pool from which the dispatcher will dispatch messages.
        ConnectionPool connectionPool = new DefaultConnectionPool();
        String[] addresses = connectionContext.getAddresses();
        for (String address : addresses) {
          connectionPool.add(new ReliableEventBusConnection(address, eventBus));
        }

        // Initialize the dispatcher and add a channel to the channels list.
        dispatcher.init(connectionPool);
        channels.add(new DefaultChannel(dispatcher));
      }
      catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
        logger.error("Failed to find grouping handler.");
      }
    }
  }

  @Override
  public void handle(final Message<JsonObject> message) {
    String action = getMandatoryString("action", message);

    if (action == null) {
      sendError(message, "An action must be specified.");
      return;
    }

    switch (action) {
      case "feed":
        doFeed(message);
        break;
      case "receive":
        doReceive(message);
        break;
      default:
        sendError(message, String.format("Invalid action %s.", action));
    }
  }

  /**
   * Feeds a message to the vine.
   */
  private void doFeed(final Message<JsonObject> message) {
    // If the feed queue is full then reply immediately with an error.
    if (feedRequests.size() >= maxQueueSize) {
      sendError(message, "Vine queue full.");
      return;
    }

    final JsonMessage jsonMessage = new JsonMessage().setBody(getMandatoryObject("data", message));

    final long id = nextCorrelationID();
    jsonMessage.setIdentifier(id);

    feedRequests.put(id, message);
    inProcess.put(id, jsonMessage);

    // Create a future that will be invoked with the vine result.
    final Future<JsonObject> future = new DefaultFutureResult<JsonObject>();
    future.setHandler(new Handler<AsyncResult<JsonObject>>() {
      @Override
      public void handle(AsyncResult<JsonObject> result) {
        if (result.succeeded()) {
          message.reply(result.result());
          feedRequests.remove(id);
          inProcess.remove(id);
        }
        else {
          dispatch(jsonMessage, future);
        }
      }
    });
    dispatch(jsonMessage, future);
  }

  /**
   * Dispatches a message to all initial seeds.
   *
   * @param message
   *   The JSON message to dispatch.
   * @param future
   *   A future result to be triggered once processing is complete.
   */
  private void dispatch(JsonMessage message, final Future<JsonObject> future) {
    vertx.setTimer(messageExpiration, new Handler<Long>() {
      @Override
      public void handle(Long event) {
        future.setFailure(new TimeoutException("Message processing timed out."));
      }
    });
    futureResults.put(message.getIdentifier(), future);
  }

  /**
   * Receives a processed message.
   */
  private void doReceive(final Message<JsonObject> message) {
    final JsonMessage jsonMessage = new JsonMessage(message.body());
    long id = jsonMessage.getIdentifier();
    if (id == 0) {
      sendError(message, "Invalid message correlation identifier.");
    }

    // Get the message future. If a future does not exist then the
    // message may have been played through the vine more than once,
    // and an earlier version of the message eventually completed
    // processing. In that case, the message has already completed.
    Future<JsonObject> futureResult = futureResults.get(id);
    if (futureResult != null) {
      // Ensure that the message made it through all the necessary
      // steps in the vine. If not, trigger the future with a failure.
      if (!checkResult(jsonMessage)) {
        futureResult.setFailure(new TimeoutException("Message failed to complete all steps."));
      }
      else {
        futureResult.setResult(jsonMessage.body());
        futureResults.remove(id);
      }
    }
  }

  /**
   * Checks to ensure that a message has been tagged with all the
   * appropriate seeds.
   *
   * @param message
   *   The message to check.
   * @return
   *   Indicates whether the message result is valid.
   */
  private boolean checkResult(JsonMessage message) {
    return Arrays.equals(message.getTags(), tagNames);
  }

  /**
   * Returns the next message correlation ID.
   *
   * @return
   *   A correlation ID.
   */
  private long nextCorrelationID() {
    return ++currentID;
  }

}
