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
package net.kuujo.vine.feeder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;

import net.kuujo.vine.context.ConnectionContext;
import net.kuujo.vine.context.VineContext;
import net.kuujo.vine.eventbus.ReliableEventBus;
import net.kuujo.vine.eventbus.WrappedReliableEventBus;
import net.kuujo.vine.messaging.ConnectionPool;
import net.kuujo.vine.messaging.Dispatcher;
import net.kuujo.vine.messaging.EventBusConnection;
import net.kuujo.vine.messaging.EventBusConnectionPool;
import net.kuujo.vine.messaging.EventBusStream;
import net.kuujo.vine.messaging.JsonMessage;
import net.kuujo.vine.messaging.Stream;

/**
 * An abstract feeder implementation. This class handles setup of vine
 * input and output streams.
 *
 * @author Jordan Halterman
 */
public abstract class AbstractFeeder {

  protected VineContext context;

  protected ReliableEventBus eventBus;

  protected OutputCollector output;

  public AbstractFeeder(VineContext context, Vertx vertx) {
    this(context, vertx, vertx.eventBus());
  }

  public AbstractFeeder(VineContext context, EventBus eventBus) {
    this.context = context;
    setEventBus(eventBus);
  }

  public AbstractFeeder(VineContext context, Vertx vertx, EventBus eventBus) {
    this.context = context;
    setEventBus(eventBus).setVertx(vertx);
  }

  private ReliableEventBus setEventBus(EventBus eventBus) {
    if (eventBus instanceof ReliableEventBus) {
      this.eventBus = (ReliableEventBus) eventBus;
    }
    else {
      this.eventBus = new WrappedReliableEventBus(eventBus);
    }
    return this.eventBus;
  }

  /**
   * Initializes the feeder, setting up input and output streams.
   */
  protected void init() {
    setupOutputs();
  }

  /**
   * Sets up seed outputs.
   */
  protected void setupOutputs() {
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
      }
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
        stream.emit(message, new Handler<AsyncResult<Message<Boolean>>>() {
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
     * Emits a message to all streams.
     */
    public void emit(JsonMessage message, long timeout, final Handler<Boolean> ackHandler) {
      for (Stream<?> stream : streamList) {
        stream.emit(message, timeout, new Handler<AsyncResult<Message<Boolean>>>() {
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
