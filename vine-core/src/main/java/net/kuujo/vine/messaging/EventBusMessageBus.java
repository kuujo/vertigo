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
package net.kuujo.vine.messaging;

import net.kuujo.vine.eventbus.ReliableEventBus;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;

/**
 * An eventbus-based message bus.
 *
 * @author Jordan Halterman
 */
public class EventBusMessageBus implements MessageBus {

  private ReliableEventBus eventBus;

  private static final long DEFAULT_TIMEOUT = 30000;

  public EventBusMessageBus(ReliableEventBus eventBus) {
    this.eventBus = eventBus;
  }

  @Override
  public MessageBus send(String address, JsonMessage message) {
    eventBus.send(address, message);
    return this;
  }

  @Override
  public MessageBus send(String address, JsonMessage message, final Handler<Boolean> ackHandler) {
    return send(address, message, DEFAULT_TIMEOUT, false, 0, ackHandler);
  }

  @Override
  public MessageBus send(String address, JsonMessage message, long timeout, final Handler<Boolean> ackHandler) {
    return send(address, message, timeout, false, 0, ackHandler);
  }

  @Override
  public MessageBus send(String address, JsonMessage message, long timeout, boolean retry, Handler<Boolean> ackHandler) {
    return send(address, message, timeout, retry, 1, ackHandler);
  }

  @Override
  public MessageBus send(final String address, final JsonMessage message, final long timeout, final boolean retry, final int attempts,
      final Handler<Boolean> ackHandler) {
    eventBus.send(address, message, timeout, new AsyncResultHandler<Message<Boolean>>() {
      @Override
      public void handle(AsyncResult<Message<Boolean>> result) {
        if (result.failed()) {
          if (retry && attempts > 0) {
            send(address, message, timeout, retry, attempts-1, ackHandler);
          }
          else {
            ackHandler.handle(false);
          }
        }
        else {
          ackHandler.handle(result.result().body());
        }
      }
    });
    return this;
  }

  @Override
  public MessageBus registerHandler(String address, final Handler<JsonMessage> handler) {
    eventBus.registerHandler(address, new Handler<Message<JsonObject>>() {
      @Override
      public void handle(Message<JsonObject> message) {
        handler.handle(new JsonMessage(message));
      }
    });
    return this;
  }

  @Override
  public MessageBus registerHandler(String address, final Handler<JsonMessage> handler, Handler<AsyncResult<Void>> doneHandler) {
    eventBus.registerHandler(address, new Handler<Message<JsonObject>>() {
      @Override
      public void handle(Message<JsonObject> message) {
        handler.handle(new JsonMessage(message));
      }
    }, doneHandler);
    return this;
  }

  @Override
  public MessageBus registerLocalHandler(String address, final Handler<JsonMessage> handler) {
    eventBus.registerLocalHandler(address, new Handler<Message<JsonObject>>() {
      @Override
      public void handle(Message<JsonObject> message) {
        handler.handle(new JsonMessage(message));
      }
    });
    return this;
  }

}
