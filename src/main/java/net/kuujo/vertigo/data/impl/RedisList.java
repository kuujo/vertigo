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
package net.kuujo.vertigo.data.impl;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.impl.DefaultFutureResult;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import net.kuujo.vertigo.VertigoException;
import net.kuujo.vertigo.annotations.ClusterType;
import net.kuujo.vertigo.annotations.Factory;
import net.kuujo.vertigo.annotations.LocalType;
import net.kuujo.vertigo.data.AsyncList;

/**
 * Redis-based list implementation.
 *
 * @author Jordan Halterman
 */
@LocalType
@ClusterType
public class RedisList<T> implements AsyncList<T> {
  private final String name;
  private final String address;
  private final Vertx vertx;

  @Factory
  public static <T> RedisList<T> factory(String name, String address, Vertx vertx) {
    return new RedisList<T>(name, address, vertx);
  }

  private RedisList(String name, String address, Vertx vertx) {
    this.name = name;
    this.address = address;
    this.vertx = vertx;
  }

  @Override
  public String name() {
    return name;
  }

  @Override
  public void add(T value) {
    add(value, null);
  }

  @Override
  public void add(T value, final Handler<AsyncResult<Boolean>> doneHandler) {
    JsonObject message = new JsonObject()
        .putString("command", "rpush")
        .putArray("args", new JsonArray().add(name).add(value));
    vertx.eventBus().sendWithTimeout(address, message, 30000, new Handler<AsyncResult<Message<JsonObject>>>() {
      @Override
      public void handle(AsyncResult<Message<JsonObject>> result) {
        if (result.failed()) {
          new DefaultFutureResult<Boolean>(result.cause()).setHandler(doneHandler);
        } else if (result.result().body().getString("status", "ok").equals("error")) {
          new DefaultFutureResult<Boolean>(new VertigoException(result.result().body().getString("message"))).setHandler(doneHandler);
        } else {
          new DefaultFutureResult<Boolean>(result.result().body().getInteger("value") == 1).setHandler(doneHandler);
        }
      }
    });
  }

  @Override
  public void remove(T value) {
    remove(value, null);
  }

  @Override
  public void remove(T value, final Handler<AsyncResult<Boolean>> doneHandler) {
    JsonObject message = new JsonObject()
        .putString("command", "lrem")
        .putArray("args", new JsonArray().add(name).add(1).add(value));
    vertx.eventBus().sendWithTimeout(address, message, 30000, new Handler<AsyncResult<Message<JsonObject>>>() {
      @Override
      public void handle(AsyncResult<Message<JsonObject>> result) {
        if (result.failed()) {
          new DefaultFutureResult<Boolean>(result.cause()).setHandler(doneHandler);
        } else if (result.result().body().getString("status", "ok").equals("error")) {
          new DefaultFutureResult<Boolean>(new VertigoException(result.result().body().getString("message"))).setHandler(doneHandler);
        } else {
          new DefaultFutureResult<Boolean>(result.result().body().getInteger("value") == 1).setHandler(doneHandler);
        }
      }
    });
  }

  @Override
  public void contains(Object value, Handler<AsyncResult<Boolean>> resultHandler) {
    throw new UnsupportedOperationException("Redis list contains operation not supported.");
  }

  @Override
  public void size(final Handler<AsyncResult<Integer>> resultHandler) {
    JsonObject message = new JsonObject()
        .putString("command", "scard")
        .putArray("args", new JsonArray().add(name));
    vertx.eventBus().sendWithTimeout(address, message, 30000, new Handler<AsyncResult<Message<JsonObject>>>() {
      @Override
      public void handle(AsyncResult<Message<JsonObject>> result) {
        if (result.failed()) {
          new DefaultFutureResult<Integer>(result.cause()).setHandler(resultHandler);
        } else if (result.result().body().getString("status", "ok").equals("error")) {
          new DefaultFutureResult<Integer>(new VertigoException(result.result().body().getString("message"))).setHandler(resultHandler);
        } else {
          new DefaultFutureResult<Integer>(result.result().body().getInteger("value")).setHandler(resultHandler);
        }
      }
    });
  }

  @Override
  public void isEmpty(final Handler<AsyncResult<Boolean>> resultHandler) {
    JsonObject message = new JsonObject()
        .putString("command", "llen")
        .putArray("args", new JsonArray().add(name));
    vertx.eventBus().sendWithTimeout(address, message, 30000, new Handler<AsyncResult<Message<JsonObject>>>() {
      @Override
      public void handle(AsyncResult<Message<JsonObject>> result) {
        if (result.failed()) {
          new DefaultFutureResult<Boolean>(result.cause()).setHandler(resultHandler);
        } else if (result.result().body().getString("status", "ok").equals("error")) {
          new DefaultFutureResult<Boolean>(new VertigoException(result.result().body().getString("message"))).setHandler(resultHandler);
        } else {
          new DefaultFutureResult<Boolean>(result.result().body().getInteger("value") == 0).setHandler(resultHandler);
        }
      }
    });
  }

  @Override
  public void clear() {
    clear(null);
  }

  @Override
  public void clear(final Handler<AsyncResult<Void>> doneHandler) {
    JsonObject message = new JsonObject()
        .putString("command", "del")
        .putArray("args", new JsonArray().add(name));
    vertx.eventBus().sendWithTimeout(address, message, 30000, new Handler<AsyncResult<Message<JsonObject>>>() {
      @Override
      public void handle(AsyncResult<Message<JsonObject>> result) {
        if (result.failed()) {
          new DefaultFutureResult<Void>(result.cause()).setHandler(doneHandler);
        } else if (result.result().body().getString("status", "ok").equals("error")) {
          new DefaultFutureResult<Void>(new VertigoException(result.result().body().getString("message"))).setHandler(doneHandler);
        } else {
          new DefaultFutureResult<Void>((Void) null).setHandler(doneHandler);
        }
      }
    });
  }

  @Override
  public void get(int index, final Handler<AsyncResult<T>> resultHandler) {
    JsonObject message = new JsonObject()
        .putString("command", "lindex")
        .putArray("args", new JsonArray().add(name).add(0));
    vertx.eventBus().sendWithTimeout(address, message, 30000, new Handler<AsyncResult<Message<JsonObject>>>() {
      @Override
      public void handle(AsyncResult<Message<JsonObject>> result) {
        if (result.failed()) {
          new DefaultFutureResult<T>(result.cause()).setHandler(resultHandler);
        } else if (result.result().body().getString("status", "ok").equals("error")) {
          new DefaultFutureResult<T>(new VertigoException(result.result().body().getString("message"))).setHandler(resultHandler);
        } else {
          new DefaultFutureResult<T>(result.result().body().<T>getValue("value")).setHandler(resultHandler);
        }
      }
    });
  }

  @Override
  public void set(int index, T value) {
    set(index, value, null);
  }

  @Override
  public void set(int index, T value, final Handler<AsyncResult<Void>> doneHandler) {
    JsonObject message = new JsonObject()
        .putString("command", "linsert")
        .putArray("args", new JsonArray().add(name).add(index).add(value));
    vertx.eventBus().sendWithTimeout(address, message, 30000, new Handler<AsyncResult<Message<JsonObject>>>() {
      @Override
      public void handle(AsyncResult<Message<JsonObject>> result) {
        if (result.failed()) {
          new DefaultFutureResult<Void>(result.cause()).setHandler(doneHandler);
        } else if (result.result().body().getString("status", "ok").equals("error")) {
          new DefaultFutureResult<Void>(new VertigoException(result.result().body().getString("message"))).setHandler(doneHandler);
        } else {
          new DefaultFutureResult<Void>((Void) null).setHandler(doneHandler);
        }
      }
    });
  }

  @Override
  public void remove(int index) {
    remove(index, null);
  }

  @Override
  public void remove(int index, Handler<AsyncResult<T>> doneHandler) {
    throw new UnsupportedOperationException("Redis list remove by index operation not supported.");
  }

}
