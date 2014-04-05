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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.kuujo.vertigo.VertigoException;
import net.kuujo.vertigo.annotations.ClusterType;
import net.kuujo.vertigo.annotations.Factory;
import net.kuujo.vertigo.annotations.LocalType;
import net.kuujo.vertigo.data.AsyncMap;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.impl.DefaultFutureResult;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

/**
 * Redis-based map implementation.
 *
 * @author Jordan Halterman
 *
 * @param <K> The map key type.
 * @param <V> The map value type.
 */
@LocalType
@ClusterType
public class RedisMap<K, V> implements AsyncMap<K, V> {
  private final String name;
  private final String address;
  private final Vertx vertx;

  @Factory
  public static <K, V> RedisMap<K, V> factory(String name, String address, Vertx vertx) {
    return new RedisMap<K, V>(name, address, vertx);
  }

  private RedisMap(String name, String address, Vertx vertx) {
    this.name = name;
    this.address = address;
    this.vertx = vertx;
  }

  @Override
  public String name() {
    return name;
  }

  @Override
  public void put(K key, V value) {
    put(key, value, null);
  }

  @Override
  public void put(K key, V value, final Handler<AsyncResult<V>> doneHandler) {
    JsonObject message = new JsonObject()
       .putString("command", "hset")
       .putArray("args", new JsonArray().add(name).add(key).add(value));
    vertx.eventBus().sendWithTimeout(address, message, 30000, new Handler<AsyncResult<Message<JsonObject>>>() {
      @Override
      public void handle(AsyncResult<Message<JsonObject>> result) {
        if (result.failed()) {
          new DefaultFutureResult<V>(result.cause()).setHandler(doneHandler);
        } else if (result.result().body().getString("status", "ok").equals("error")) {
          new DefaultFutureResult<V>(new VertigoException(result.result().body().getString("message"))).setHandler(doneHandler);
        } else {
          new DefaultFutureResult<V>((V) null).setHandler(doneHandler);
        }
      }
    });
  }

  @Override
  public void get(K key, final Handler<AsyncResult<V>> resultHandler) {
    JsonObject message = new JsonObject()
        .putString("command", "hget")
        .putArray("args", new JsonArray().add(name).add(key));
    vertx.eventBus().sendWithTimeout(address, message, 30000, new Handler<AsyncResult<Message<JsonObject>>>() {
      @Override
      public void handle(AsyncResult<Message<JsonObject>> result) {
        if (result.failed()) {
          new DefaultFutureResult<V>(result.cause()).setHandler(resultHandler);
        } else if (result.result().body().getString("status", "ok").equals("error")) {
          new DefaultFutureResult<V>(new VertigoException(result.result().body().getString("message"))).setHandler(resultHandler);
        } else {
          new DefaultFutureResult<V>(result.result().body().<V>getValue("value")).setHandler(resultHandler);
        }
      }
    });
  }

  @Override
  public void remove(K key) {
    remove(key, null);
  }

  @Override
  public void remove(K key, final Handler<AsyncResult<V>> resultHandler) {
    JsonObject message = new JsonObject()
        .putString("command", "hdel")
        .putArray("args", new JsonArray().add(name).add(key));
    vertx.eventBus().sendWithTimeout(address, message, 30000, new Handler<AsyncResult<Message<JsonObject>>>() {
      @Override
      public void handle(AsyncResult<Message<JsonObject>> result) {
        if (result.failed()) {
          new DefaultFutureResult<V>(result.cause()).setHandler(resultHandler);
        } else if (result.result().body().getString("status", "ok").equals("error")) {
          new DefaultFutureResult<V>(new VertigoException(result.result().body().getString("message"))).setHandler(resultHandler);
        } else {
          new DefaultFutureResult<V>(result.result().body().<V>getValue("value")).setHandler(resultHandler);
        }
      }
    });
  }

  @Override
  public void containsKey(K key, final Handler<AsyncResult<Boolean>> resultHandler) {
    JsonObject message = new JsonObject()
        .putString("command", "hexists")
        .putArray("args", new JsonArray().add(name).add(key));
    vertx.eventBus().sendWithTimeout(address, message, 30000, new Handler<AsyncResult<Message<JsonObject>>>() {
      @Override
      public void handle(AsyncResult<Message<JsonObject>> result) {
        if (result.failed()) {
          new DefaultFutureResult<Boolean>(result.cause()).setHandler(resultHandler);
        } else if (result.result().body().getString("status", "ok").equals("error")) {
          new DefaultFutureResult<Boolean>(new VertigoException(result.result().body().getString("message"))).setHandler(resultHandler);
        } else {
          new DefaultFutureResult<Boolean>(result.result().body().getBoolean("value")).setHandler(resultHandler);
        }
      }
    });
  }

  @Override
  public void keySet(final Handler<AsyncResult<Set<K>>> resultHandler) {
    JsonObject message = new JsonObject()
        .putString("command", "hkeys")
        .putArray("args", new JsonArray().add(name));
    vertx.eventBus().sendWithTimeout(address, message, 30000, new Handler<AsyncResult<Message<JsonObject>>>() {
      @Override
      @SuppressWarnings("unchecked")
      public void handle(AsyncResult<Message<JsonObject>> result) {
        if (result.failed()) {
          new DefaultFutureResult<Set<K>>(result.cause()).setHandler(resultHandler);
        } else if (result.result().body().getString("status", "ok").equals("error")) {
          new DefaultFutureResult<Set<K>>(new VertigoException(result.result().body().getString("message"))).setHandler(resultHandler);
        } else {
          Set<K> keys = new HashSet<>();
          JsonArray jsonKeys = result.result().body().getArray("value");
          if (jsonKeys != null) {
            for (Object key : jsonKeys) {
              keys.add((K) key);
            }
          }
          new DefaultFutureResult<Set<K>>(keys).setHandler(resultHandler);
        }
      }
    });
  }

  @Override
  public void values(final Handler<AsyncResult<Collection<V>>> resultHandler) {
    JsonObject message = new JsonObject()
        .putString("command", "hvals")
        .putArray("args", new JsonArray().add(name));
    vertx.eventBus().sendWithTimeout(address, message, 30000, new Handler<AsyncResult<Message<JsonObject>>>() {
      @Override
      @SuppressWarnings("unchecked")
      public void handle(AsyncResult<Message<JsonObject>> result) {
        if (result.failed()) {
          new DefaultFutureResult<Collection<V>>(result.cause()).setHandler(resultHandler);
        } else if (result.result().body().getString("status", "ok").equals("error")) {
          new DefaultFutureResult<Collection<V>>(new VertigoException(result.result().body().getString("message"))).setHandler(resultHandler);
        } else {
          List<V> values = new ArrayList<>();
          JsonArray jsonValues = result.result().body().getArray("value");
          if (jsonValues != null) {
            for (Object value : jsonValues) {
              values.add((V) value);
            }
          }
          new DefaultFutureResult<Collection<V>>(values).setHandler(resultHandler);
        }
      }
    });
  }

  @Override
  public void size(final Handler<AsyncResult<Integer>> resultHandler) {
    JsonObject message = new JsonObject()
        .putString("command", "hlen")
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
        .putString("command", "hlen")
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

}
