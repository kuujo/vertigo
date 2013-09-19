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
package net.kuujo.vine.eventbus.vine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.kuujo.vine.context.ConnectionContext;
import net.kuujo.vine.context.VineContext;
import net.kuujo.vine.eventbus.Actions;
import net.kuujo.vine.eventbus.ReliableEventBus;
import net.kuujo.vine.eventbus.WrappedReliableEventBus;
import net.kuujo.vine.messaging.ConnectionPool;
import net.kuujo.vine.messaging.Dispatcher;
import net.kuujo.vine.messaging.EventBusConnection;
import net.kuujo.vine.messaging.EventBusConnectionPool;
import net.kuujo.vine.messaging.EventBusStream;
import net.kuujo.vine.messaging.JsonMessage;
import net.kuujo.vine.messaging.Stream;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Verticle;

/**
 * A vine controller verticle.
 *
 * @author Jordan Halterman
 */
public class VineVerticle extends Verticle {

  private ReliableEventBus eventBus;

  private String address;

  private String statusAddress;

  private VineContext context;

  private OutputCollector output;

  private Map<Long, JsonMessage> messageMap = new HashMap<Long, JsonMessage>();

  private Map<Long, List<JsonObject>> responses = new HashMap<Long, List<JsonObject>>();

  private long currentId;

  private int writeQueueSize;

  private int inProcess;

  private boolean active;

  private static final long DEFAULT_MESSAGE_TIMEOUT = 15000;

  @Override
  public void start() {
    this.eventBus = new WrappedReliableEventBus(vertx.eventBus(), vertx);
    this.context = new VineContext(container.config());
    this.address = context.getAddress();
    this.statusAddress = String.format("%s.status", address);
    this.writeQueueSize = context.getDefinition().getMaxQueueSize();
    setupOutputs();
    setupInputs();
  }

  /**
   * Sets up seed outputs.
   */
  private void setupOutputs() {
    output = new OutputCollector();

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
    eventBus.registerHandler(address, new Handler<Message<JsonObject>>() {
      @Override
      public void handle(Message<JsonObject> message) {
        String action = message.body().getString("action");
        if (action != null) {
          switch (action) {
            case "feed":
              doFeed(message);
              break;
            case "exec":
              doExec(message);
              break;
            case "receive":
              doReceive(message);
              break;
            case "shutdown":
              doShutdown(message);
              break;
          }
        }
      }
    });
  }

  private void doFeed(Message<JsonObject> message) {
    JsonObject body = message.body();
    if (body != null) {
      doEmitMessage(new JsonMessage(body.getObject("feed"), message));
    }
  }

  private void doEmitMessage(final JsonMessage message) {
    inProcess++;
    output.emit(message, new Handler<Boolean>() {
      @Override
      public void handle(Boolean succeeded) {
        inProcess--;
        if (!active && inProcess <= writeQueueSize / 2) {
          eventBus.publish(statusAddress, Actions.create("empty"));
          active = true;
        }

        if (succeeded) {
          message.message().reply();
        }
        else {
          doEmitMessage(message);
        }
      }
    });

    if (active && inProcess >= writeQueueSize) {
      eventBus.publish(statusAddress, Actions.create("full"));
    }
  }

  private long nextId() {
    return ++currentId;
  }

  private void doExec(Message<JsonObject> message) {
    JsonObject body = message.body();
    if (body == null) {
      return;
    }

    long id = nextId();
    final JsonMessage jsonMessage = new JsonMessage(id, body.getObject("exec"), message);
    messageMap.put(id, jsonMessage);
    doExecMessage(jsonMessage);
  }

  private void doExecMessage(final JsonMessage message) {
    inProcess++;
    final long id = message.message().getIdentifier();
    output.emit(message, new Handler<Boolean>() {
      @Override
      public void handle(Boolean succeeded) {
        inProcess--;
        if (!active && inProcess <= writeQueueSize / 2) {
          eventBus.publish(statusAddress, Actions.create("empty"));
          active = true;
        }

        if (succeeded) {
          checkResponses(id);
        }
        else {
          doExecMessage(message);
        }
      }
    });

    if (active && inProcess >= writeQueueSize) {
      eventBus.publish(statusAddress, Actions.create("full"));
    }
  }

  private void checkResponses(long id) {
    List<JsonObject> responseList = responses.get(id);
    if (responseList != null) {
      if (messageMap.containsKey(id)) {
        JsonMessage originalMessage = messageMap.get(id);
        for (JsonObject response : responseList) {
          originalMessage.message().reply(response);
        }
        messageMap.remove(id);
      }
      responses.remove(id);
    }
  }

  private void doReceive(Message<JsonObject> message) {
    // TODO: Handle malformed messages here.
    JsonObject body = message.body();
    if (body == null) {
      return;
    }

    JsonObject receive = body.getObject("receive");
    if (receive != null) {
      JsonObject jsonBody = receive.getObject("body");
      if (jsonBody != null) {
        JsonMessage jsonMessage;
        if (receive.getFieldNames().contains("id")) {
          long id = receive.getLong("id");
          jsonMessage = new JsonMessage(id, jsonBody, message);
          if (messageMap.containsKey(id)) {
            List<JsonObject> responseList = responses.get(id);
            if (responseList == null) {
              responseList = new ArrayList<JsonObject>();
              responses.put(id, responseList);
            }
            responseList.add(jsonBody);
          }
        }
        else {
          jsonMessage = new JsonMessage(jsonBody, message);
        }

        jsonMessage.message().ready();
        jsonMessage.message().ack();
      }
    }
  }

  private void doShutdown(Message<JsonObject> message) {
    container.exit();
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

}
