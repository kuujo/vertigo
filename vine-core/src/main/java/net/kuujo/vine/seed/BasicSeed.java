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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.kuujo.vine.context.ConnectionContext;
import net.kuujo.vine.context.WorkerContext;
import net.kuujo.vine.eventbus.Actions;
import net.kuujo.vine.eventbus.ReliableEventBus;
import net.kuujo.vine.eventbus.WrappedReliableEventBus;
import net.kuujo.vine.heartbeat.DefaultHeartBeatEmitter;
import net.kuujo.vine.heartbeat.HeartBeatEmitter;
import net.kuujo.vine.messaging.ConnectionPool;
import net.kuujo.vine.messaging.Dispatcher;
import net.kuujo.vine.messaging.EventBusConnection;
import net.kuujo.vine.messaging.EventBusConnectionPool;
import net.kuujo.vine.messaging.EventBusStream;
import net.kuujo.vine.messaging.JsonMessage;
import net.kuujo.vine.messaging.Stream;

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
public class BasicSeed implements Seed {

  protected Vertx vertx;

  protected Container container;

  protected Logger logger;

  protected ReliableEventBus eventBus;

  protected String address;

  protected WorkerContext context;

  protected HeartBeatEmitter heartbeat;

  protected OutputCollector output;

  protected Handler<JsonObject> dataHandler;

  protected JsonMessage currentMessage;

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
    output = new OutputCollector();

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
          handleMessage(new JsonMessage(receive.getLong("id"), receive.getObject("body"), message));
        }
        else {
          handleMessage(new JsonMessage(receive.getObject("body"), message));
        }
      }
    }
  }

  protected void handleMessage(JsonMessage message) {
    currentMessage = message;
    dataHandler.handle(message);
  }

  @Override
  public Seed dataHandler(Handler<JsonObject> handler) {
    dataHandler = handler;
    return this;
  }

  @Override
  public void emit(final JsonObject data) {
    if (currentMessage != null) {
      output.emit(currentMessage.createChild(data), new Handler<Boolean>() {
        @Override
        public void handle(Boolean succeeded) {
          if (succeeded) {
            currentMessage.message().ready();
          }
          else {
            emit(data);
          }
        }
      });
    }
    else {
      output.emit(JsonMessage.create(data), new Handler<Boolean>() {
        @Override
        public void handle(Boolean succeeded) {
          if (succeeded) {
            currentMessage.message().ready();
          }
          else {
            emit(data);
          }
        }
      });
    }
  }

  @Override
  public void emit(JsonObject... data) {
    for (JsonObject item : data) {
      emit(item);
    }
  }

  @Override
  public void emitTo(final String seedName, final JsonObject data) {
    output.emitTo(seedName, new JsonMessage(data), new Handler<Boolean>() {
      @Override
      public void handle(Boolean succeeded) {
        if (succeeded) {
          currentMessage.message().ready();
        }
        else {
          emitTo(seedName, data);
        }
      }
    });
  }

  @Override
  public void emitTo(String seedName, JsonObject... data) {
    for (JsonObject item : data) {
      emitTo(seedName, item);
    }
  }

  /**
   * Publishes seed output to the appropriate streams.
   */
  protected static class OutputCollector {

    private List<Stream<?>> streamList = new ArrayList<>();
    private Map<String, Stream<?>> streams = new HashMap<>();

    /**
     * Adds a stream to the output.
     */
    public OutputCollector addStream(String name, Stream<?> stream) {
      if (streams.containsKey(name)) {
        Stream<?> oldStream = streams.get(name);
        if (streamList.contains(oldStream)) {
          streamList.remove(oldStream);
        }
      }
      streams.put(name, stream);
      streamList.add(stream);
      return this;
    }

    /**
     * Removes a stream from the output.
     */
    public OutputCollector removeStream(String name) {
      if (streams.containsKey(name)) {
        streams.remove(name);
      }
      return this;
    }

    /**
     * Returns a set of stream names.
     */
    public Set<String> getStreamNames() {
      return streams.keySet();
    }

    /**
     * Returns a stream by name.
     */
    public Stream<?> getStream(String name) {
      return streams.get(name);
    }

    /**
     * Returns the number of streams in the output.
     */
    public int size() {
      return streams.size();
    }

    /**
     * Emits a message to all streams.
     */
    public void emit(JsonMessage message) {
      for (Stream<?> stream : streamList) {
        stream.emit(message);
      }
    }

    /**
     * Emits a message to all streams.
     */
    public void emit(JsonMessage message, final Handler<Boolean> ackHandler) {
      for (Stream<?> stream : streamList) {
        stream.emit(message, DEFAULT_MESSAGE_TIMEOUT, new Handler<AsyncResult<Message<Boolean>>>() {
          @Override
          public void handle(AsyncResult<Message<Boolean>> result) {
            if (result.succeeded()) {
              ackHandler.handle(result.result().body());
            }
            else {
              ackHandler.handle(false);
            }
          }
        });
      }
    }

    /**
     * Emits a message to a specific stream.
     */
    public void emitTo(String name, JsonMessage message) {
      if (streams.containsKey(name)) {
        streams.get(name).emit(message);
      }
    }

    /**
     * Emits a message to a specific stream.
     */
    public void emitTo(String name, JsonMessage message, final Handler<Boolean> ackHandler) {
      if (streams.containsKey(name)) {
        streams.get(name).emit(message, DEFAULT_MESSAGE_TIMEOUT, new Handler<AsyncResult<Message<Boolean>>>() {
          @Override
          public void handle(AsyncResult<Message<Boolean>> result) {
            if (result.succeeded()) {
              ackHandler.handle(result.result().body());
            }
            else {
              ackHandler.handle(false);
            }
          }
        });
      }
    }
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

}
