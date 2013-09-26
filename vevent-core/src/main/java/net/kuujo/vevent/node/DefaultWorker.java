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
package net.kuujo.vevent.node;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.kuujo.vevent.context.ConnectionContext;
import net.kuujo.vevent.context.WorkerContext;
import net.kuujo.vevent.eventbus.ReliableEventBus;
import net.kuujo.vevent.eventbus.WrappedReliableEventBus;
import net.kuujo.vevent.messaging.ConnectionPool;
import net.kuujo.vevent.messaging.CoordinatingOutputCollector;
import net.kuujo.vevent.messaging.DefaultJsonMessage;
import net.kuujo.vevent.messaging.Dispatcher;
import net.kuujo.vevent.messaging.EventBusConnection;
import net.kuujo.vevent.messaging.EventBusConnectionPool;
import net.kuujo.vevent.messaging.EventBusChannel;
import net.kuujo.vevent.messaging.JsonMessage;
import net.kuujo.vevent.messaging.OutputCollector;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.platform.Container;

/**
 * A basic seed implementation.
 *
 * @author Jordan Halterman
 */
public class DefaultWorker implements Worker {

  protected Vertx vertx;

  protected Container container;

  protected Logger logger;

  protected ReliableEventBus eventBus;

  protected String address;

  protected String vineAddress;

  protected WorkerContext context;

  protected OutputCollector output;

  protected Handler<JsonMessage> dataHandler;

  protected Map<JsonMessage, InternalMessage> messageMap = new HashMap<JsonMessage, InternalMessage>();

  protected static final long DEFAULT_MESSAGE_TIMEOUT = 15000;

  @Override
  public Worker setVertx(Vertx vertx) {
    this.vertx = vertx;
    this.eventBus = new WrappedReliableEventBus(vertx.eventBus(), vertx);
    return this;
  }

  @Override
  public Worker setContainer(Container container) {
    this.container = container;
    this.logger = container.logger();
    return this;
  }

  @Override
  public Worker setContext(WorkerContext context) {
    this.context = context;
    this.address = context.getAddress();
    this.vineAddress = context.getContext().getContext().getAddress();
    return this;
  }

  @Override
  public void start() {
    setupOutputs();
    setupInputs();
  }

  /**
   * Sets up worker outputs.
   */
  private void setupOutputs() {
    output = new CoordinatingOutputCollector();

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
        ConnectionPool<EventBusConnection> connectionPool = new EventBusConnectionPool();
        String[] addresses = connectionContext.getAddresses();
        for (String address : addresses) {
          connectionPool.add(new EventBusConnection(address, eventBus));
        }

        // Initialize the dispatcher and add a channel to the channels list.
        dispatcher.init(connectionPool);
        output.addStream(connectionContext.getSeedName(), new EventBusChannel(dispatcher));
      }
      catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
        container.logger().error("Failed to find grouping handler.");
      }
    }
  }

  /**
   * Sets up input handlers.
   */
  private void setupInputs() {
    if (dataHandler != null) {
      eventBus.registerHandler(address, new Handler<Message<JsonObject>>() {
        @Override
        public void handle(Message<JsonObject> message) {
          String action = message.body().getString("action");
          if (action != null) {
            switch (action) {
              case "receive":
                doReceive(message);
                break;
            }
          }
        }
      });
    }
  }

  private void doReceive(Message<JsonObject> message) {
    JsonObject body = message.body();
    if (body != null) {
      JsonObject receive = body.getObject("receive");
      if (receive != null && dataHandler != null) {
        JsonMessage jsonMessage = new DefaultJsonMessage(receive);
        InternalMessage internal = new InternalMessage(message, jsonMessage);
        messageMap.put(jsonMessage, internal);
        dataHandler.handle(jsonMessage);
      }
    }
  }

  @Override
  public Worker dataHandler(Handler<JsonMessage> handler) {
    dataHandler = handler;
    return this;
  }

  @Override
  public void emit(JsonObject data) {
    output.emit(DefaultJsonMessage.create(data));
  }

  @Override
  public void emit(JsonObject data, String tag) {
    output.emit(DefaultJsonMessage.create(data, tag));
  }

  @Override
  public void emit(JsonObject data, JsonMessage parent) {
    if (messageMap.containsKey(parent)) {
      messageMap.get(parent).emit(data);
    }
  }

  @Override
  public void emit(JsonObject data, String tag, JsonMessage parent) {
    if (messageMap.containsKey(parent)) {
      messageMap.get(parent).emit(data, tag);
    }
  }

  @Override
  public void ack(JsonMessage message) {
    if (messageMap.containsKey(message)) {
      messageMap.get(message).ack();
      messageMap.remove(message);
    }
  }

  @Override
  public void ack(JsonMessage... messages) {
    for (JsonMessage message : messages) {
      ack(message);
    }
  }

  @Override
  public void fail(JsonMessage message) {
    if (messageMap.containsKey(message)) {
      messageMap.get(message).fail();
      messageMap.remove(message);
    }
  }

  @Override
  public void fail(JsonMessage... messages) {
    for (JsonMessage message : messages) {
      fail(message);
    }
  }

  /**
   * An internal wrapped message.
   */
  private class InternalMessage {
    private Message<JsonObject> message;
    private JsonMessage json;
    private int childCount;
    private int completeCount;
    private boolean ready;
    private boolean acked;
    private boolean ack;
    private boolean failed;
    private boolean locked;

    public InternalMessage(Message<JsonObject> message, JsonMessage json) {
      this.message = message;
      this.json = json;
    }

    /**
     * Emits data as a child of the message.
     *
     * @param data
     *   The child data.
     */
    public void emit(JsonObject data) {
      checkEmit(json.createChild(data));
    }

    /**
     * Emits data as a child of the message.
     *
     * @param data
     *   The child data.
     * @param tag
     *   The child tag.
     */
    public void emit(JsonObject data, String tag) {
      checkEmit(json.createChild(data, tag));
    }

    /**
     * Emits a child message to the appropriate stream.
     */
    private void checkEmit(JsonMessage child) {
      if (!failed()) {
        if (output.size() == 0) {
          ready();
          eventBus.publish(vineAddress, child.serialize());
        }
        else {
          childCount++;
          doEmit(child);
        }
      }
    }

    /**
     * Emits a child via the output collector.
     *
     * @param child
     *   A child message.
     */
    private void doEmit(final JsonMessage child) {
      output.emit(child, DEFAULT_MESSAGE_TIMEOUT, new Handler<AsyncResult<Boolean>>() {
        @Override
        public void handle(AsyncResult<Boolean> result) {
          if (!failed) {
            // If a timeout occurred then try resending the message.
            if (result.failed()) {
              doEmit(child);
            }
            // If the child was failed, fail the parent.
            else if (!result.result()) {
              fail();
            }
            // Otherwise, check if all message children have completed.
            else {
              completeCount++;
              if (acked && completeCount == childCount) {
                ready();
              }
            }
          }
        }
      });
    }

    /**
     * Indicates that the message is ready for sending an ack reply.
     */
    public void ready() {
      ready = true;
      checkAck();
    }

    /**
     * Indicates that the message has been fully processed.
     */
    public void ack() {
      if (!acked) {
        acked = true; ack = true;
      }
      checkAck();
    }

    /**
     * Indicates that message processing failed.
     */
    public void fail() {
      acked = true; ack = false;
      checkAck();
    }

    /**
     * Returns a boolean indicating whether the message has failed.
     */
    public boolean failed() {
      return acked && !ack;
    }

    /**
     * Checks and sends an ack reply if necessary.
     */
    private void checkAck() {
      if (!locked && acked) {
        if (ready) {
          message.reply(ack);
          locked = true;
        }
        else if (!ack) {
          message.reply(false);
          locked = true;
        }
      }
    }
    
  }

}
