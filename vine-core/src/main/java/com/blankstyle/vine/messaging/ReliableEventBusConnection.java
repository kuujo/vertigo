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
package com.blankstyle.vine.messaging;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.Future;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.impl.DefaultFutureResult;
import org.vertx.java.core.json.JsonObject;

import com.blankstyle.vine.eventbus.ReliableEventBus;
import com.blankstyle.vine.util.Messaging;

/**
 * A reliable eventbus connection.
 *
 * @author Jordan Halterman
 */
public class ReliableEventBusConnection extends EventBusConnection implements ReliableConnection {

  private ReliableEventBus eventBus;

  public ReliableEventBusConnection(String address, ReliableEventBus eventBus) {
    super(address);
    this.eventBus = eventBus;
  }

  @Override
  public Connection send(JsonMessage message, final AsyncResultHandler<Void> doneHandler) {
    final Future<Void> future = new DefaultFutureResult<Void>();
    future.setHandler(doneHandler);
    eventBus.send(address, createJsonAction(message), new AsyncResultHandler<Message<JsonObject>>() {
      @Override
      public void handle(AsyncResult<Message<JsonObject>> result) {
        Messaging.checkResponse(result, doneHandler);
      }
    });
    return this;
  }

  @Override
  public Connection send(JsonMessage message, long timeout, final AsyncResultHandler<Void> doneHandler) {
    final Future<Void> future = new DefaultFutureResult<Void>();
    future.setHandler(doneHandler);
    eventBus.send(address, createJsonAction(message), timeout, new AsyncResultHandler<Message<JsonObject>>() {
      @Override
      public void handle(AsyncResult<Message<JsonObject>> result) {
        Messaging.checkResponse(result, doneHandler);
      }
    });
    return this;
  }

  @Override
  public Connection send(JsonMessage message, long timeout, boolean retry, final AsyncResultHandler<Void> doneHandler) {
    final Future<Void> future = new DefaultFutureResult<Void>();
    future.setHandler(doneHandler);
    eventBus.send(address, createJsonAction(message), timeout, retry, new AsyncResultHandler<Message<JsonObject>>() {
      @Override
      public void handle(AsyncResult<Message<JsonObject>> result) {
        Messaging.checkResponse(result, doneHandler);
      }
    });
    return this;
  }

  @Override
  public Connection send(JsonMessage message, long timeout, boolean retry, int attempts,
      final AsyncResultHandler<Void> doneHandler) {
    final Future<Void> future = new DefaultFutureResult<Void>();
    future.setHandler(doneHandler);
    eventBus.send(address, createJsonAction(message), timeout, retry, attempts, new AsyncResultHandler<Message<JsonObject>>() {
      @Override
      public void handle(AsyncResult<Message<JsonObject>> result) {
        Messaging.checkResponse(result, doneHandler);
      }
    });
    return this;
  }

}
