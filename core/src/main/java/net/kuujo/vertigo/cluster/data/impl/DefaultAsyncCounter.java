/*
 * Copyright 2014 the original author or authors.
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
package net.kuujo.vertigo.cluster.data.impl;

import net.kuujo.vertigo.cluster.data.AsyncCounter;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.impl.DefaultFutureResult;
import org.vertx.java.core.json.JsonObject;

/**
 * Event bus based counter.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class DefaultAsyncCounter implements AsyncCounter {
  private final String address;
  private final String name;
  private final EventBus eventBus;

  public DefaultAsyncCounter(String address, String name, Vertx vertx) {
    this.address = address;
    this.name = name;
    this.eventBus = vertx.eventBus();
  }

  @Override
  public String name() {
    return name;
  }

  @Override
  public void get(final Handler<AsyncResult<Long>> doneHandler) {
    JsonObject message = new JsonObject()
        .putString("action", "get")
        .putString("type", "counter")
        .putString("name", name);
    eventBus.sendWithTimeout(address, message, 30000, new Handler<AsyncResult<Message<JsonObject>>>() {
      @Override
      public void handle(AsyncResult<Message<JsonObject>> result) {
        if (result.failed()) {
          new DefaultFutureResult<Long>(result.cause()).setHandler(doneHandler);
        } else {
          new DefaultFutureResult<Long>(result.result().body().getLong("result", 0)).setHandler(doneHandler);
        }
      }
    });
  }

  @Override
  public void increment() {
    increment(null);
  }

  @Override
  public void increment(final Handler<AsyncResult<Void>> doneHandler) {
    JsonObject message = new JsonObject()
        .putString("action", "increment")
        .putString("type", "counter")
        .putString("name", name);
    eventBus.sendWithTimeout(address, message, 30000, new Handler<AsyncResult<Message<JsonObject>>>() {
      @Override
      public void handle(AsyncResult<Message<JsonObject>> result) {
        if (result.failed()) {
          new DefaultFutureResult<Void>(result.cause()).setHandler(doneHandler);
        } else {
          new DefaultFutureResult<Void>((Void) null).setHandler(doneHandler);
        }
      }
    });
  }

  @Override
  public void incrementAndGet(final Handler<AsyncResult<Long>> doneHandler) {
    JsonObject message = new JsonObject()
        .putString("action", "increment")
        .putString("type", "counter")
        .putString("name", name);
    eventBus.sendWithTimeout(address, message, 30000, new Handler<AsyncResult<Message<JsonObject>>>() {
      @Override
      public void handle(AsyncResult<Message<JsonObject>> result) {
        if (result.failed()) {
          new DefaultFutureResult<Long>(result.cause()).setHandler(doneHandler);
        } else {
          new DefaultFutureResult<Long>(result.result().body().getLong("result", 0)).setHandler(doneHandler);
        }
      }
    });
  }

  @Override
  public void decrement() {
    decrement(null);
  }

  @Override
  public void decrement(final Handler<AsyncResult<Void>> doneHandler) {
    JsonObject message = new JsonObject()
        .putString("action", "decrement")
        .putString("type", "counter")
        .putString("name", name);
    eventBus.sendWithTimeout(address, message, 30000, new Handler<AsyncResult<Message<JsonObject>>>() {
      @Override
      public void handle(AsyncResult<Message<JsonObject>> result) {
        if (result.failed()) {
          new DefaultFutureResult<Void>(result.cause()).setHandler(doneHandler);
        } else {
          new DefaultFutureResult<Void>((Void) null).setHandler(doneHandler);
        }
      }
    });
  }

  @Override
  public void decrementAndGet(final Handler<AsyncResult<Long>> doneHandler) {
    JsonObject message = new JsonObject()
        .putString("action", "decrement")
        .putString("type", "counter")
        .putString("name", name);
    eventBus.sendWithTimeout(address, message, 30000, new Handler<AsyncResult<Message<JsonObject>>>() {
      @Override
      public void handle(AsyncResult<Message<JsonObject>> result) {
        if (result.failed()) {
          new DefaultFutureResult<Long>(result.cause()).setHandler(doneHandler);
        } else {
          new DefaultFutureResult<Long>(result.result().body().getLong("result", 0)).setHandler(doneHandler);
        }
      }
    });
  }

}
