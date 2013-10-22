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

import net.kuujo.vertigo.messaging.JsonMessage;
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

/**
 * A default listener implementation.
 *
 * @author Jordan Halterman
 */
public class DefaultListener implements Listener {
  private String address;
  private Input input;
  private Vertx vertx;
  private EventBus eventBus;
  private Handler<JsonMessage> messageHandler;
  private static final long LISTEN_PERIOD = 5000;
  private long listenTimerID;

  public DefaultListener(String address, Vertx vertx) {
    this.address = UUID.randomUUID().toString();
    this.input = new Input(address);
    this.vertx = vertx;
    this.eventBus = vertx.eventBus();
  }

  public DefaultListener(String address, Vertx vertx, EventBus eventBus) {
    this.address = UUID.randomUUID().toString();
    this.input = new Input(address);
    this.vertx = vertx;
    this.eventBus = eventBus;
  }

  public DefaultListener(Input input, Vertx vertx) {
    this.address = UUID.randomUUID().toString();
    this.input = input;
    this.vertx = vertx;
    this.eventBus = vertx.eventBus();
  }

  public DefaultListener(Input input, Vertx vertx, EventBus eventBus) {
    this.address = UUID.randomUUID().toString();
    this.input = input;
    this.vertx = vertx;
    this.eventBus = eventBus;
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
      String auditor = message.auditor();
 
      // Call the message handler.
      if (messageHandler != null) {
        messageHandler.handle(message);
      }

      // Always attempt to ack the message once the handler has been called.
      if (auditor != null) {
        eventBus.send(auditor, new JsonObject().putString("action", "ack").putString("id", message.id()));
      }
    }
    catch (SerializationException e) {
      // Do nothing.
    }
  }

  @Override
  public Listener messageHandler(Handler<JsonMessage> handler) {
    messageHandler = handler;
    return this;
  }

  @Override
  public Listener start() {
    eventBus.registerHandler(address, handler, new Handler<AsyncResult<Void>>() {
      @Override
      public void handle(AsyncResult<Void> result) {
        if (result.succeeded()) {
          periodicListen();
        }
      }
    });
    return this;
  }

  @Override
  public Listener start(Handler<AsyncResult<Void>> doneHandler) {
    final Future<Void> future = new DefaultFutureResult<Void>().setHandler(doneHandler);
    eventBus.registerHandler(address, handler, new Handler<AsyncResult<Void>>() {
      @Override
      public void handle(AsyncResult<Void> result) {
        if (result.succeeded()) {
          periodicListen();
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
   * Periodically sends listen messages to the listen source to
   * let it know we're still interested in receiving messages.
   */
  private void periodicListen() {
    listenTimerID = vertx.setTimer(LISTEN_PERIOD, new Handler<Long>() {
      @Override
      public void handle(Long timerID) {
        eventBus.publish(input.getAddress(), Serializer.serialize(input).putString("action", "listen").putString("address", address));
        periodicListen();
      }
    });
  }

  @Override
  public void stop() {
    if (listenTimerID > 0) {
      vertx.cancelTimer(listenTimerID);
    }
    eventBus.unregisterHandler(address, handler);
  }

  @Override
  public void stop(Handler<AsyncResult<Void>> doneHandler) {
    if (listenTimerID > 0) {
      vertx.cancelTimer(listenTimerID);
    }
    eventBus.unregisterHandler(address, handler, doneHandler);
  }

}
