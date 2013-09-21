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
package net.kuujo.vine.seed;

import java.util.Collection;
import java.util.Iterator;

import net.kuujo.vine.context.ConnectionContext;
import net.kuujo.vine.context.WorkerContext;
import net.kuujo.vine.eventbus.Actions;
import net.kuujo.vine.eventbus.ReliableEventBus;
import net.kuujo.vine.eventbus.WrappedReliableEventBus;
import net.kuujo.vine.heartbeat.DefaultHeartBeatEmitter;
import net.kuujo.vine.heartbeat.HeartBeatEmitter;
import net.kuujo.vine.messaging.ConnectionPool;
import net.kuujo.vine.messaging.CoordinatingOutputCollector;
import net.kuujo.vine.messaging.Dispatcher;
import net.kuujo.vine.messaging.EventBusConnection;
import net.kuujo.vine.messaging.EventBusConnectionPool;
import net.kuujo.vine.messaging.EventBusStream;
import net.kuujo.vine.messaging.JsonMessage;
import net.kuujo.vine.messaging.OutputCollector;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.AsyncResultHandler;
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
public class DefaultSeed implements Seed {

  protected Vertx vertx;

  protected Container container;

  protected Logger logger;

  protected ReliableEventBus eventBus;

  protected String address;

  protected String vineAddress;

  protected WorkerContext context;

  protected HeartBeatEmitter heartbeat;

  protected OutputCollector output;

  protected Handler<JsonObject> dataHandler;

  protected InternalMessage currentMessage;

  protected static final long DEFAULT_MESSAGE_TIMEOUT = 15000;

  @Override
  public Seed setVertx(Vertx vertx) {
    this.vertx = vertx;
    this.eventBus = new WrappedReliableEventBus(vertx.eventBus(), vertx);
    return this;
  }

  @Override
  public Seed setContainer(Container container) {
    this.container = container;
    this.logger = container.logger();
    return this;
  }

  @Override
  public Seed setContext(WorkerContext context) {
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
        output.addStream(connectionContext.getSeedName(), new EventBusStream(dispatcher));
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
      if (receive != null) {
        if (receive.getFieldNames().contains("id")) {
          handleMessage(new JsonMessage(receive.getValue("id"), receive.getObject("body"), message));
        }
        else {
          handleMessage(new JsonMessage(receive.getObject("body"), message));
        }
      }
    }
  }

  private void handleMessage(JsonMessage message) {
    currentMessage = new InternalMessage(message);
    if (dataHandler != null) {
      dataHandler.handle(message);
    }
    currentMessage.complete();
  }

  @Override
  public Seed dataHandler(Handler<JsonObject> handler) {
    dataHandler = handler;
    return this;
  }

  @Override
  public void emit(JsonObject data) {
    currentMessage.emit(data);
  }

  @Override
  public void emit(JsonObject... data) {
    currentMessage.emit(data);
  }

  @Override
  public void emitTo(String seedName, JsonObject data) {
    currentMessage.emitTo(seedName, data);
  }

  @Override
  public void emitTo(String seedName, JsonObject... data) {
    currentMessage.emitTo(seedName, data);
  }

  @Override
  public void ack(JsonMessage message) {
    message.message().ack();
  }

  @Override
  public void ack(JsonMessage... messages) {
    for (JsonMessage message : messages) {
      ack(message);
    }
  }

  @Override
  public void fail(JsonMessage message) {
    message.message().fail();
  }

  @Override
  public void fail(JsonMessage... messages) {
    for (JsonMessage message : messages) {
      fail(message);
    }
  }

  /**
   * An internal message.
   */
  private class InternalMessage {
    private JsonMessage message;
    private int childCount;
    private int completeCount;
    private boolean completed;
    private boolean failed;
    private static final long EMIT_TIMEOUT = 30000;

    public InternalMessage(JsonMessage message) {
      this.message = message;
    }

    /**
     * Emits child data of the message.
     *
     * @param data
     *   The data to emit.
     */
    public void emit(JsonObject data) {
      if (!failed) {
        // If the seed has no outputs, publish the data back to
        // the vine address.
        if (output.size() == 0) {
          message.message().ready();
          eventBus.publish(vineAddress, createJsonObject(message.createChild(data)));
        }
        // Otherwise, increment message children. The child count will
        // be used to determine when the message children have been acked.
        else {
          childCount++;
          doEmit(message.createChild(data));
        }
      }
    }

    /**
     * Emits child data of the message.
     *
     * @param data
     *   The data to emit.
     */
    public void emit(JsonObject... data) {
      for (JsonObject item : data) {
        emit(item);
      }
    }

    /**
     * Emits a single child of the message.
     *
     * @param child
     *   The child message to emit.
     */
    private void doEmit(final JsonMessage child) {
      output.emit(child, EMIT_TIMEOUT, new Handler<AsyncResult<Boolean>>() {
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
              if (completed && completeCount == childCount) {
                message.message().ready();
              }
            }
          }
        }
      });
    }

    /**
     * Emits child data to a specific seed.
     *
     * @param seedName
     *   The seed name.
     * @param data
     *   The data to emit.
     */
    public void emitTo(String seedName, JsonObject data) {
      if (!failed) {
        if (output.size() == 0) {
          message.message().ready();
          eventBus.publish(vineAddress, createJsonObject(message.createChild(data)));
        }
        else {
          childCount++;
          doEmitTo(seedName, message.createChild(data));
        }
      }
    }

    /**
     * Emits child data to a specific seed.
     *
     * @param seedName
     *   The seed name.
     * @param data
     *   The data to emit.
     */
    public void emitTo(String seedName, JsonObject... data) {
      for (JsonObject item : data) {
        emitTo(seedName, item);
      }
    }

    /**
     * Emits a single child message to a specific seed.
     *
     * @param seedName
     *   The seed name.
     * @param child
     *   The child message to emit.
     */
    private void doEmitTo(final String seedName, final JsonMessage child) {
      output.emitTo(seedName, child, EMIT_TIMEOUT, new Handler<AsyncResult<Boolean>>() {
        @Override
        public void handle(AsyncResult<Boolean> result) {
          if (!failed) {
            // If a timeout occurred then try resending the message.
            if (result.failed()) {
              doEmitTo(seedName, child);
            }
            // If the child was failed, fail the parent.
            else if (!result.result()) {
              fail();
            }
            // Otherwise, check if all message children have completed.
            else {
              completeCount++;
              if (completed && completeCount == childCount) {
                message.message().ready();
              }
            }
          }
        }
      });
    }

    /**
     * Fails processing of all message children and thus the parent
     * message as well.
     *
     * Once a message has been failed, child messages can no longer
     * be emitted and will be ignored.
     */
    public void fail() {
      if (!failed) {
        failed = true;
        message.message().fail();
        message.message().ready();
      }
    }

    /**
     * Indicates that all children have been emitted from the seed.
     */
    public void complete() {
      completed = true;
    }

    private JsonObject createJsonObject(JsonMessage message) {
      JsonObject data = new JsonObject();
      Object id = message.message().getIdentifier();
      if (id != null) {
        data.putValue("id", id);
      }
      data.putObject("body", (JsonObject) message);
      return data;
    }

  }

}
