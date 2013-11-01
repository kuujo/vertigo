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

import java.util.UUID;

import net.kuujo.vertigo.VertigoException;
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

/**
 * A default listener implementation.
 *
 * @author Jordan Halterman
 */
public class DefaultListener implements Listener {
  private final String address;
  private final String statusAddress;
  private final Input input;
  private Output output;
  private final Vertx vertx;
  private final EventBus eventBus;
  private final Logger logger;
  private boolean autoAck = true;
  private Handler<JsonMessage> messageHandler;
  private Future<Void> startFuture;
  private long pollTimer;
  private static final long POLL_INTERVAL = 1000;
  private long timeoutTimer;
  private static final long START_TIMEOUT = 30000;

  public DefaultListener(String address, Vertx vertx) {
    this.address = UUID.randomUUID().toString();
    this.statusAddress = UUID.randomUUID().toString();
    this.input = new Input(address);
    this.vertx = vertx;
    this.eventBus = vertx.eventBus();
    this.logger = null;
  }

  public DefaultListener(String address, Vertx vertx, Logger logger) {
    this.address = UUID.randomUUID().toString();
    this.statusAddress = UUID.randomUUID().toString();
    this.input = new Input(address);
    this.vertx = vertx;
    this.eventBus = vertx.eventBus();
    this.logger = logger;
  }

  public DefaultListener(String address, Vertx vertx, EventBus eventBus) {
    this.address = UUID.randomUUID().toString();
    this.statusAddress = UUID.randomUUID().toString();
    this.input = new Input(address);
    this.vertx = vertx;
    this.eventBus = eventBus;
    this.logger = null;
  }

  public DefaultListener(String address, Vertx vertx, EventBus eventBus, Logger logger) {
    this.address = UUID.randomUUID().toString();
    this.statusAddress = UUID.randomUUID().toString();
    this.input = new Input(address);
    this.vertx = vertx;
    this.eventBus = eventBus;
    this.logger = logger;
  }

  public DefaultListener(Input input, Vertx vertx) {
    this.address = UUID.randomUUID().toString();
    this.statusAddress = UUID.randomUUID().toString();
    this.input = input;
    this.vertx = vertx;
    this.eventBus = vertx.eventBus();
    this.logger = null;
  }

  public DefaultListener(Input input, Vertx vertx, Logger logger) {
    this.address = UUID.randomUUID().toString();
    this.statusAddress = UUID.randomUUID().toString();
    this.input = input;
    this.vertx = vertx;
    this.eventBus = vertx.eventBus();
    this.logger = logger;
  }

  public DefaultListener(Input input, Vertx vertx, EventBus eventBus) {
    this.address = UUID.randomUUID().toString();
    this.statusAddress = UUID.randomUUID().toString();
    this.input = input;
    this.vertx = vertx;
    this.eventBus = eventBus;
    this.logger = null;
  }

  public DefaultListener(Input input, Vertx vertx, EventBus eventBus, Logger logger) {
    this.address = UUID.randomUUID().toString();
    this.statusAddress = UUID.randomUUID().toString();
    this.input = input;
    this.vertx = vertx;
    this.eventBus = eventBus;
    this.logger = logger;
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
    try {
      JsonMessage message = (JsonMessage) Serializer.deserialize(messageData);

      // Call the message handler.
      if (messageHandler != null) {
        messageHandler.handle(message);
      }

      // If auto acking is enabled then ack the message.
      if (autoAck) {
        ack(message);
      }
    }
    catch (SerializationException e) {
      if (logger != null) {
        logger.warn(e);
      }
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
    String auditor = message.auditor();
    if (auditor != null) {
      eventBus.send(auditor, new JsonObject().putString("action", "ack").putString("id", message.id()));
    }
    return this;
  }

  @Override
  public Listener fail(JsonMessage message) {
    String auditor = message.auditor();
    if (auditor != null) {
      eventBus.send(auditor, new JsonObject().putString("action", "fail").putString("id", message.id()));
    }
    return this;
  }

  @Override
  public Listener start() {
    try {
      output = Output.fromInput(input);
      timeoutTimer = vertx.setTimer(START_TIMEOUT, new Handler<Long>() {
        @Override
        public void handle(Long timerID) {
          stop();
        }
      });

      eventBus.registerHandler(address, handler, new Handler<AsyncResult<Void>>() {
        @Override
        public void handle(AsyncResult<Void> result) {
          if (result.succeeded()) {
            eventBus.registerHandler(statusAddress, statusHandler, new Handler<AsyncResult<Void>>() {
              @Override
              public void handle(AsyncResult<Void> result) {
                if (result.succeeded()) {
                  recursiveListen();
                }
                else {
                  eventBus.unregisterHandler(address, handler);
                }
              }
            });
          }
        }
      });
    }
    catch (MalformedNetworkException e) {
      stop();
    }
    return this;
  }

  @Override
  public Listener start(Handler<AsyncResult<Void>> doneHandler) {
    startFuture = new DefaultFutureResult<Void>().setHandler(doneHandler);
    try {
      output = Output.fromInput(input);
  
      // Set a start timeout timer that will be cancelled once the input is started.
      timeoutTimer = vertx.setTimer(START_TIMEOUT, new Handler<Long>() {
        @Override
        public void handle(Long timerID) {
          stop();
          startFuture.setFailure(new VertigoException("Failed to start listener."));
        }
      });

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
                  recursiveListen();
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
    }
    catch (MalformedNetworkException e) {
      startFuture.setFailure(e);
    }
    return this;
  }

  /**
   * Recursively sends listen messages to the source until a valid
   * response is received.
   */
  private void recursiveListen() {
    eventBus.publish(input.getAddress(), Serializer.serialize(output).putString("action", "listen").putString("address", address).putString("status", statusAddress));
    pollTimer = vertx.setTimer(POLL_INTERVAL, new Handler<Long>() {
      @Override
      public void handle(Long timerID) {
        recursiveListen();
      }
    });
  }

  /**
   * Finishes starting the input.
   */
  private void completeStart(String id) {
    if (timeoutTimer > 0) {
      vertx.cancelTimer(timeoutTimer);
      timeoutTimer = 0;
    }
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
