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
package net.kuujo.vertigo.state.impl;

import net.kuujo.vertigo.VertigoException;
import net.kuujo.vertigo.annotations.ClusterType;
import net.kuujo.vertigo.annotations.Factory;
import net.kuujo.vertigo.annotations.LocalType;
import net.kuujo.vertigo.state.StatePersistor;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.impl.DefaultFutureResult;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

/**
 * Redis-based state persistor.
 *
 * @author Jordan Halterman
 */
@LocalType
@ClusterType
public class RedisStatePersistor implements StatePersistor {
  private final String address;
  private final String key;
  private final Vertx vertx;

  @Factory
  public static RedisStatePersistor factory(JsonObject config, Vertx vertx) {
    if (!config.containsField("address")) throw new IllegalArgumentException("No Redis address specified.");
    if (!config.containsField("key")) throw new IllegalArgumentException("No Redis key specified.");
    return new RedisStatePersistor(config.getString("address"), config.getString("key"), vertx);
  }

  public RedisStatePersistor(String address, String key, Vertx vertx) {
    this.address = address;
    this.key = key;
    this.vertx = vertx;
  }

  @Override
  public StatePersistor loadState(final Handler<AsyncResult<JsonObject>> resultHandler) {
    JsonObject message = new JsonObject()
        .putString("command", "get")
        .putArray("args", new JsonArray().add(key));
    vertx.eventBus().sendWithTimeout(address, message, 30000, new Handler<AsyncResult<Message<JsonObject>>>() {
      @Override
      public void handle(AsyncResult<Message<JsonObject>> result) {
        if (result.failed()) {
          new DefaultFutureResult<JsonObject>(result.cause()).setHandler(resultHandler);
        } else if (result.result().body().getString("status", "ok").equals("error")) {
          new DefaultFutureResult<JsonObject>(new VertigoException(result.result().body().getString("message"))).setHandler(resultHandler);
        } else {
          String state = result.result().body().getString("value");
          if (state == null) {
            new DefaultFutureResult<JsonObject>(new JsonObject()).setHandler(resultHandler);
          } else {
            new DefaultFutureResult<JsonObject>(new JsonObject(state)).setHandler(resultHandler);
          }
        }
      }
    });
    return this;
  }

  @Override
  public StatePersistor storeState(JsonObject state, final Handler<AsyncResult<Void>> doneHandler) {
    JsonObject message = new JsonObject()
        .putString("command", "set")
        .putArray("args", new JsonArray().add(key).add(state.encode()));
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
    return this;
  }

}
