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
package net.kuujo.vertigo.input.impl;

import java.util.UUID;

import net.kuujo.vertigo.context.Context;
import net.kuujo.vertigo.context.InputContext;
import net.kuujo.vertigo.network.Input;
import net.kuujo.vertigo.input.Listener;
import net.kuujo.vertigo.message.JsonMessage;
import net.kuujo.vertigo.message.impl.DefaultJsonMessage;
import net.kuujo.vertigo.serializer.Serializer;
import net.kuujo.vertigo.serializer.SerializerFactory;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Future;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.impl.DefaultFutureResult;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;

/**
 * A default listener implementation.
 *
 * @author Jordan Halterman
 */
public class DefaultListener implements Listener {
  private final Serializer serializer = SerializerFactory.getSerializer(Context.class);
  private final String address;
  private final String statusAddress;
  private final InputContext context;
  private final Vertx vertx;
  private final EventBus eventBus;
  private boolean autoAck = true;
  private Handler<JsonMessage> messageHandler;
  private Future<Void> startFuture;
  private long pollTimer;
  private static final long POLL_INTERVAL = 1000;

  public DefaultListener(String address, Vertx vertx) {
    this.address = UUID.randomUUID().toString();
    this.statusAddress = UUID.randomUUID().toString();
    this.context = inputToContext(new Input(address));
    this.vertx = vertx;
    this.eventBus = vertx.eventBus();
  }

  public DefaultListener(InputContext context, Vertx vertx) {
    this.address = UUID.randomUUID().toString();
    this.statusAddress = UUID.randomUUID().toString();
    this.context = context;
    this.vertx = vertx;
    this.eventBus = vertx.eventBus();
  }

  public DefaultListener(Input input, Vertx vertx) {
    this.address = UUID.randomUUID().toString();
    this.statusAddress = UUID.randomUUID().toString();
    this.context = inputToContext(input);
    this.vertx = vertx;
    this.eventBus = vertx.eventBus();
  }

  @Deprecated
  public DefaultListener(String address, Vertx vertx, EventBus eventBus) {
    this.address = UUID.randomUUID().toString();
    this.statusAddress = UUID.randomUUID().toString();
    this.context = inputToContext(new Input(address));
    this.vertx = vertx;
    this.eventBus = eventBus;
  }

  @Deprecated
  public DefaultListener(String address, Vertx vertx, Logger logger) {
    this.address = UUID.randomUUID().toString();
    this.statusAddress = UUID.randomUUID().toString();
    this.context = inputToContext(new net.kuujo.vertigo.network.Input(address));
    this.vertx = vertx;
    this.eventBus = vertx.eventBus();
  }

  @Deprecated
  public DefaultListener(String address, Vertx vertx, EventBus eventBus, Logger logger) {
    this.address = UUID.randomUUID().toString();
    this.statusAddress = UUID.randomUUID().toString();
    this.context = inputToContext(new net.kuujo.vertigo.network.Input(address));
    this.vertx = vertx;
    this.eventBus = eventBus;
  }

  @Deprecated
  public DefaultListener(net.kuujo.vertigo.input.Input input, Vertx vertx) {
    this.address = UUID.randomUUID().toString();
    this.statusAddress = UUID.randomUUID().toString();
    this.context = inputToContext(input);
    this.vertx = vertx;
    this.eventBus = vertx.eventBus();
  }

  @Deprecated
  public DefaultListener(net.kuujo.vertigo.input.Input input, Vertx vertx, Logger logger) {
    this.address = UUID.randomUUID().toString();
    this.statusAddress = UUID.randomUUID().toString();
    this.context = inputToContext(input);
    this.vertx = vertx;
    this.eventBus = vertx.eventBus();
  }

  @Deprecated
  public DefaultListener(net.kuujo.vertigo.input.Input input, Vertx vertx, EventBus eventBus) {
    this.address = UUID.randomUUID().toString();
    this.statusAddress = UUID.randomUUID().toString();
    this.context = inputToContext(input);
    this.vertx = vertx;
    this.eventBus = eventBus;
  }

  @Deprecated
  public DefaultListener(net.kuujo.vertigo.input.Input input, Vertx vertx, EventBus eventBus, Logger logger) {
    this.address = UUID.randomUUID().toString();
    this.statusAddress = UUID.randomUUID().toString();
    this.context = inputToContext(input);
    this.vertx = vertx;
    this.eventBus = eventBus;
  }

  @SuppressWarnings("deprecation")
  private InputContext inputToContext(net.kuujo.vertigo.input.Input input) {
    Serializer serializer = SerializerFactory.getSerializer(Context.class);
    return serializer.deserialize(serializer.serialize(input), InputContext.class);
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

  private Handler<Message<JsonObject>> statusHandler = new Handler<Message<JsonObject>>() {
    @Override
    public void handle(Message<JsonObject> message) {
      JsonObject body = message.body();
      if (body != null) {
        String id = body.getString("id");
        if (id != null) {
          completeStart(id);
        }
      }
    }
  };

  /**
   * Receives message data.
   */
  private void doReceive(JsonObject messageData) {
    JsonMessage message = DefaultJsonMessage.fromJson(messageData);

    // Call the message handler.
    if (messageHandler != null) {
      messageHandler.handle(message);
    }

    // If auto acking is enabled then ack the message.
    if (autoAck) {
      ack(message);
    }
  }

  @Override
  public Listener setAutoAck(boolean autoAck) {
    this.autoAck = autoAck;
    return this;
  }

  @Override
  public boolean isAutoAck() {
    return autoAck;
  }

  @Override
  public Listener messageHandler(Handler<JsonMessage> handler) {
    messageHandler = handler;
    return this;
  }

  @Override
  public Listener ack(JsonMessage message) {
    String auditor = message.messageId().auditor();
    if (auditor != null) {
      eventBus.send(auditor, new JsonObject().putString("action", "ack").putObject("id", message.messageId().toJson()));
    }
    return this;
  }

  @Override
  public Listener fail(JsonMessage message) {
    String auditor = message.messageId().auditor();
    if (auditor != null) {
      eventBus.send(auditor, new JsonObject().putString("action", "fail").putObject("id", message.messageId().toJson()));
    }
    return this;
  }

  @Override
  public Listener start() {
    eventBus.registerHandler(address, handler, new Handler<AsyncResult<Void>>() {
      @Override
      public void handle(AsyncResult<Void> result) {
        if (result.succeeded()) {
          eventBus.registerHandler(statusAddress, statusHandler, new Handler<AsyncResult<Void>>() {
            @Override
            public void handle(AsyncResult<Void> result) {
              if (result.succeeded()) {
                startListen();
              }
              else {
                eventBus.unregisterHandler(address, handler);
              }
            }
          });
        }
      }
    });
    return this;
  }

  @Override
  public Listener start(Handler<AsyncResult<Void>> doneHandler) {
    startFuture = new DefaultFutureResult<Void>().setHandler(doneHandler);
    eventBus.registerHandler(address, handler, new Handler<AsyncResult<Void>>() {
      @Override
      public void handle(AsyncResult<Void> result) {
        if (result.failed()) {
          startFuture.setFailure(result.cause());
        }
        else {
          eventBus.registerHandler(statusAddress, statusHandler, new Handler<AsyncResult<Void>>() {
            @Override
            public void handle(AsyncResult<Void> result) {
              if (result.succeeded()) {
                startListen();
              }
              else {
                eventBus.unregisterHandler(address, handler);
                startFuture.setFailure(result.cause());
              }
            }
          });
        }
      }
    });
    return this;
  }

  /**
   * Sends periodic listen messages to the source.
   */
  private void startListen() {
    pollTimer = vertx.setPeriodic(POLL_INTERVAL, new Handler<Long>() {
      @Override
      public void handle(Long timerId) {
        eventBus.publish(context.address(), new JsonObject().putString("action", "listen")
            .putString("address", address).putString("status", statusAddress)
            .putObject("input", serializer.serialize(context)));
      }
    });
  }

  /**
   * Finishes starting the input.
   */
  private void completeStart(String id) {
    if (startFuture != null) {
      startFuture.setResult(null);
      startFuture = null;
    }
  }

  @Override
  public void stop() {
    if (pollTimer > 0) {
      vertx.cancelTimer(pollTimer);
      pollTimer = 0;
    }
  }

  @Override
  public void stop(Handler<AsyncResult<Void>> doneHandler) {
    if (pollTimer > 0) {
      vertx.cancelTimer(pollTimer);
      pollTimer = 0;
    }
    new DefaultFutureResult<Void>().setHandler(doneHandler).setResult(null);
  }

}
