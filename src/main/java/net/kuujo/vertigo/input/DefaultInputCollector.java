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
package net.kuujo.vertigo.input;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.kuujo.vertigo.context.InstanceContext;
import net.kuujo.vertigo.messaging.JsonMessage;
import net.kuujo.vertigo.network.MalformedNetworkException;
import net.kuujo.vertigo.output.Output;
import net.kuujo.vertigo.serializer.SerializationException;
import net.kuujo.vertigo.serializer.Serializer;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Future;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.impl.DefaultFutureResult;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.platform.Container;

/**
 * A default input collector implementation.
 *
 * @author Jordan Halterman
 */
public class DefaultInputCollector implements InputCollector {
  private final String address;
  private final Vertx vertx;
  private final Logger logger;
  private final EventBus eventBus;
  private final InstanceContext context;
  private Handler<JsonMessage> messageHandler;
  private Map<UUID, Long> listenTimers = new HashMap<>();
  private static final long LISTEN_PERIOD = 5000;

  public DefaultInputCollector(Vertx vertx, Container container, InstanceContext context) {
    this(vertx, container, vertx.eventBus(), context);
  }

  public DefaultInputCollector(Vertx vertx, Container container, EventBus eventBus, InstanceContext context) {
    this.address = context.id();
    this.vertx = vertx;
    this.logger = container.logger();
    this.eventBus = eventBus;
    this.context = context;
  }

  private Handler<Message<JsonObject>> handler = new Handler<Message<JsonObject>>() {
    @Override
    public void handle(Message<JsonObject> message) {
      JsonObject body = message.body();
      if (body != null) {
        doReceive(body);
      }
    }
  };

  /**
   * Receives message data.
   */
  private void doReceive(JsonObject messageData) {
    try {
      JsonMessage message = (JsonMessage) Serializer.deserialize(messageData);
      if (messageHandler != null) {
        messageHandler.handle(message);
      }
    }
    catch (SerializationException e) {
      logger.warn(e);
    }
  }

  @Override
  public InputCollector messageHandler(Handler<JsonMessage> handler) {
    this.messageHandler = handler;
    return this;
  }

  @Override
  public InputCollector start() {
    eventBus.registerHandler(address, handler, new Handler<AsyncResult<Void>>() {
      @Override
      public void handle(AsyncResult<Void> result) {
        if (result.succeeded()) {
          for (Input input : context.getComponent().getInputs()) {
            input.getGrouping().setCount(context.getComponent().getNumInstances());
            try {
              periodicListen(UUID.randomUUID(), input, Output.fromInput(input));
            }
            catch (MalformedNetworkException e) {
              stopListeners();
              return;
            }
          }
        }
      }
    });
    return this;
  }

  @Override
  public InputCollector start(Handler<AsyncResult<Void>> doneHandler) {
    final Future<Void> future = new DefaultFutureResult<Void>().setHandler(doneHandler);
    eventBus.registerHandler(address, handler, new Handler<AsyncResult<Void>>() {
      @Override
      public void handle(AsyncResult<Void> result) {
        if (result.succeeded()) {
          for (Input input : context.getComponent().getInputs()) {
            input.getGrouping().setCount(context.getComponent().getNumInstances());
            try {
              startPeriodicListen(UUID.randomUUID(), input, Output.fromInput(input));
            }
            catch (MalformedNetworkException e) {
              stopListeners();
              future.setFailure(e);
              return;
            }
          }
          future.setResult(null);
        }
        else {
          future.setFailure(result.cause());
        }
      }
    });
    return this;
  }

  /**
   * Starts periodically listening for messages from a source.
   */
  private void startPeriodicListen(final UUID id, final Input input, final Output output) {
    eventBus.publish(input.getAddress(), Serializer.serialize(output).putString("action", "listen").putString("address", address));
    periodicListen(id, input, output);
  }

  /**
   * Periodically sends listen messages to the listen source to
   * let it know we're still interested in receiving messages.
   */
  private void periodicListen(final UUID id, final Input input, final Output output) {
    listenTimers.put(id, vertx.setTimer(LISTEN_PERIOD, new Handler<Long>() {
      @Override
      public void handle(Long timerID) {
        eventBus.publish(input.getAddress(), Serializer.serialize(output).putString("action", "listen").putString("address", address));
        periodicListen(id, input, output);
      }
    }));
  }

  /**
   * Stops all component listeners.
   */
  private void stopListeners() {
    for (UUID id : listenTimers.keySet()) {
      vertx.cancelTimer(listenTimers.remove(id));
    }
  }

  @Override
  public void stop() {
    stopListeners();
    eventBus.unregisterHandler(address, handler);
  }

  @Override
  public void stop(Handler<AsyncResult<Void>> doneHandler) {
    stopListeners();
    eventBus.unregisterHandler(address, handler, doneHandler);
  }

  @Override
  public InputCollector ack(JsonMessage message) {
    String auditor = message.auditor();
    if (auditor != null) {
      eventBus.send(auditor, createAckAction(message.id()));
    }
    return this;
  }

  /**
   * Creates an ack message action.
   */
  private static final JsonObject createAckAction(String id) {
    return new JsonObject().putString("action", "ack").putString("id", id);
  }

  @Override
  public InputCollector fail(JsonMessage message) {
    String auditor = message.auditor();
    if (auditor != null) {
      eventBus.send(auditor, createFailAction(message.id()));
    }
    return this;
  }

  /**
   * Creates a fail message action.
   */
  private static final JsonObject createFailAction(String id) {
    return new JsonObject().putString("action", "fail").putString("id", id);
  }

}
