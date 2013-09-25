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
package net.kuujo.vitis.node;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.kuujo.vitis.context.ShootContext;
import net.kuujo.vitis.context.WorkerContext;
import net.kuujo.vitis.eventbus.Actions;
import net.kuujo.vitis.eventbus.ReliableEventBus;
import net.kuujo.vitis.eventbus.WrappedReliableEventBus;
import net.kuujo.vitis.heartbeat.DefaultHeartBeatEmitter;
import net.kuujo.vitis.heartbeat.HeartBeatEmitter;
import net.kuujo.vitis.messaging.ConnectionPool;
import net.kuujo.vitis.messaging.CoordinatingOutputCollector;
import net.kuujo.vitis.messaging.DefaultJsonMessage;
import net.kuujo.vitis.messaging.Dispatcher;
import net.kuujo.vitis.messaging.EventBusConnection;
import net.kuujo.vitis.messaging.EventBusConnectionPool;
import net.kuujo.vitis.messaging.EventBusShoot;
import net.kuujo.vitis.messaging.JsonMessage;
import net.kuujo.vitis.messaging.OutputCollector;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.platform.Container;

/**
 * A basic seed implementation.
 *
 * @author Jordan Halterman
 */
public class DefaultNode implements Node {

  protected Vertx vertx;

  protected Container container;

  protected Logger logger;

  protected ReliableEventBus eventBus;

  protected String address;

  protected String vineAddress;

  protected WorkerContext context;

  protected HeartBeatEmitter heartbeat;

  protected OutputCollector output;

  protected Handler<JsonMessage> dataHandler;

  protected Map<JsonMessage, InternalMessage> messageMap = new HashMap<JsonMessage, InternalMessage>();

  protected static final long DEFAULT_MESSAGE_TIMEOUT = 15000;

  @Override
  public Node setVertx(Vertx vertx) {
    this.vertx = vertx;
    this.eventBus = new WrappedReliableEventBus(vertx.eventBus(), vertx);
    return this;
  }

  @Override
  public Node setContainer(Container container) {
    this.container = container;
    this.logger = container.logger();
    return this;
  }

  @Override
  public Node setContext(WorkerContext context) {
    this.context = context;
    this.address = context.getAddress();
    this.vineAddress = context.getContext().getContext().getAddress();
    return this;
  }

  @Override
  public void start() {
    setupHeartbeat();
    setupOutputs();
    setupInputs();
  }

  /**
   * Sets up the seed verticle heartbeat.
   */
  private void setupHeartbeat() {
    heartbeat = new DefaultHeartBeatEmitter(vertx, vertx.eventBus());
    eventBus.send(context.getStem(), Actions.create("register", context.getAddress()), 10000, new AsyncResultHandler<Message<JsonObject>>() {
      @Override
      public void handle(AsyncResult<Message<JsonObject>> result) {
        if (result.succeeded()) {
          Message<JsonObject> message = result.result();
          JsonObject body = message.body();
          String error = body.getString("error");
          if (error != null) {
            logger.error(error);
          }
          else {
            // Set the heartbeat address. This is returned by the "register" action.
            String address = body.getString("address");
            if (address != null) {
              heartbeat.setAddress(address);
            }

            // Set the heartbeat interval. This setting is derived from the
            // seed definition's heartbeat interval option.
            heartbeat.setInterval(context.getContext().getDefinition().getHeartbeatInterval());
            heartbeat.start();
          }
        }
        else {
          logger.error(String.format("Failed to fetch heartbeat address from stem at %s.", context.getStem()));
        }
      }
    });
  }

  /**
   * Sets up seed outputs.
   */
  private void setupOutputs() {
    output = new CoordinatingOutputCollector();

    Collection<ShootContext> connections = context.getContext().getConnectionContexts();
    Iterator<ShootContext> iter = connections.iterator();
    while (iter.hasNext()) {
      ShootContext connectionContext = iter.next();
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
        output.addStream(connectionContext.getSeedName(), new EventBusShoot(dispatcher));
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
  public Node dataHandler(Handler<JsonMessage> handler) {
    dataHandler = handler;
    return this;
  }

  @Override
  public void emit(JsonObject data) {
    output.emit(DefaultJsonMessage.create(data));
  }

  @Override
  public void emit(JsonObject data, String tag) {
    output.emit(DefaultJsonMessage.create(data, new JsonArray().add(tag)));
  }

  @Override
  public void emit(JsonObject data, String[] tags) {
    JsonArray tagsArray = new JsonArray();
    for (String tag : tags) {
      tagsArray.add(tag);
    }
    output.emit(DefaultJsonMessage.create(data, tagsArray));
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
      messageMap.get(parent).emit(data, new String[]{tag});
    }
  }

  @Override
  public void emit(JsonObject data, String[] tags, JsonMessage parent) {
    if (messageMap.containsKey(parent)) {
      messageMap.get(parent).emit(data, tags);
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
     * @param tags
     *   The child tags.
     */
    public void emit(JsonObject data, String[] tags) {
      JsonArray jsonTags = new JsonArray();
      for (String tag : tags) {
        jsonTags.add(tag);
      }
      checkEmit(json.createChild(data, jsonTags));
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
