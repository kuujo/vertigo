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
package com.blankstyle.vine;

import java.util.Collection;
import java.util.Iterator;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;

import com.blankstyle.vine.context.ConnectionContext;
import com.blankstyle.vine.context.WorkerContext;
import com.blankstyle.vine.eventbus.ReliableBusVerticle;
import com.blankstyle.vine.eventbus.ReliableEventBus;
import com.blankstyle.vine.heartbeat.DefaultHeartBeatEmitter;
import com.blankstyle.vine.heartbeat.HeartBeatEmitter;
import com.blankstyle.vine.messaging.ChannelPublisher;
import com.blankstyle.vine.messaging.ConnectionPool;
import com.blankstyle.vine.messaging.DefaultChannel;
import com.blankstyle.vine.messaging.DefaultConnectionPool;
import com.blankstyle.vine.messaging.Dispatcher;
import com.blankstyle.vine.messaging.JsonMessage;
import com.blankstyle.vine.messaging.RecursiveChannelPublisher;
import com.blankstyle.vine.messaging.ReliableChannel;
import com.blankstyle.vine.messaging.ReliableEventBusConnection;

/**
 * A core seed verticle.
 *
 * @author Jordan Halterman
 */
public abstract class SeedVerticle extends ReliableBusVerticle implements Handler<Message<JsonObject>> {

  protected ReliableEventBus eventBus;

  protected WorkerContext context;

  protected ChannelPublisher<ReliableChannel> publisher;

  private JsonMessage currentMessage;

  private String seedAddress;

  private HeartBeatEmitter heartbeat;

  @Override
  protected void start(ReliableEventBus eventBus) {
    this.eventBus = eventBus;
    setupContext();
    setupHeartbeat();
    setupChannels();
    eventBus.registerHandler(getMandatoryStringConfig("address"), this);
  }

  /**
   * Sets up the seed context.
   */
  private void setupContext() {
    this.context = new WorkerContext(config);
    this.seedAddress = context.getContext().getAddress();
  }

  /**
   * Sets up the seed verticle heartbeat.
   */
  private void setupHeartbeat() {
    heartbeat = new DefaultHeartBeatEmitter(vertx, vertx.eventBus());
    final String stemAddress = getMandatoryStringConfig("stem");
    eventBus.send(stemAddress, new JsonObject().putString("action", "register").putString("address", context.getAddress()), 10000, new AsyncResultHandler<Message<JsonObject>>() {
      @Override
      public void handle(AsyncResult<Message<JsonObject>> result) {
        if (result.succeeded()) {
          Message<JsonObject> message = result.result();
          JsonObject body = message.body();
          String error = body.getString("error");
          if (error != null) {
            container.logger().error(error);
          }
          else {
            heartbeat.setAddress(getMandatoryString("address", message));
            heartbeat.setInterval(getOptionalIntConfig("heartbeat", 1000));
            heartbeat.start();
          }
        }
        else {
          logger.error(String.format("Failed to fetch heartbeat address from stem at %s.", stemAddress));
        }
      }
    });
  }

  /**
   * Sets up seed channels.
   */
  private void setupChannels() {
    publisher = new RecursiveChannelPublisher();
    Collection<ConnectionContext> connections = context.getContext().getConnectionContexts();
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
        publisher.addChannel(new DefaultChannel(dispatcher));
      }
      catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
        container.logger().error("Failed to find grouping handler.");
      }
    }
  }

  @Override
  public void handle(Message<JsonObject> message) {
    String action = getMandatoryString("action", message);
    if (action == null) {
      sendError(message, "No action specified.");
    }

    switch (action) {
      case "receive":
        doReceive(message);
        break;
      default:
        sendError(message, String.format("Invalid action %s.", action));
    }
  }

  /**
   * Handles receiving a message for processing.
   */
  private void doReceive(Message<JsonObject> message) {
    message.reply();
    currentMessage = new JsonMessage(message.body());
    JsonObject body = getMandatoryObject("body", message);
    process(body.copy());
  }

  /**
   * Processes a JSON message.
   *
   * @param data
   *   The data to process.
   */
  protected abstract void process(JsonObject data);

  /**
   * Emits a JSON message.
   *
   * @param data
   *   The data to emit.
   */
  protected void emit(JsonObject data) {
    JsonMessage newMessage = currentMessage.copy();
    newMessage.setBody(data).tag(seedAddress);
    publisher.publish(newMessage);
  }

  /**
   * Emits a JSON message.
   *
   * @param data
   *   The data to emit.
   * @param doneHandler
   *   A handler to be invoked once complete.
   */
  protected void emit(JsonObject data, Handler<AsyncResult<Void>> doneHandler) {
    JsonMessage newMessage = currentMessage.copy();
    newMessage.setBody(data).tag(seedAddress);
    publisher.publish(newMessage, doneHandler);
  }

}
