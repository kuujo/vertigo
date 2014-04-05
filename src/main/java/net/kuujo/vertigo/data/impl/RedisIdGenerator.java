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

import net.kuujo.vertigo.VertigoException;
import net.kuujo.vertigo.annotations.ClusterType;
import net.kuujo.vertigo.annotations.Factory;
import net.kuujo.vertigo.annotations.LocalType;
import net.kuujo.vertigo.data.AsyncIdGenerator;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.impl.DefaultFutureResult;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

/**
 * Redis-based unique ID generator.
 *
 * @author Jordan Halterman
 */
@LocalType
@ClusterType
public class RedisIdGenerator implements AsyncIdGenerator {
  private final String name;
  private final String address;
  private final Vertx vertx;

  @Factory
  public static RedisIdGenerator factory(String name, String address, Vertx vertx) {
    return new RedisIdGenerator(name, address, vertx);
  }

  private RedisIdGenerator(String name, String address, Vertx vertx) {
    this.name = name;
    this.address = address;
    this.vertx = vertx;
  }

  @Override
  public String name() {
    return name;
  }

  @Override
  public void nextId(final Handler<AsyncResult<Long>> resultHandler) {
    JsonObject message = new JsonObject()
        .putString("command", "incr")
        .putArray("args", new JsonArray().add(name));
    vertx.eventBus().sendWithTimeout(address, message, 30000, new Handler<AsyncResult<Message<JsonObject>>>() {
      @Override
      public void handle(AsyncResult<Message<JsonObject>> result) {
        if (result.failed()) {
          new DefaultFutureResult<Long>(result.cause()).setHandler(resultHandler);
        } else if (result.result().body().getString("status", "ok").equals("error")) {
          new DefaultFutureResult<Long>(new VertigoException(result.result().body().getString("message"))).setHandler(resultHandler);
        } else {
          new DefaultFutureResult<Long>(result.result().body().getLong("value")).setHandler(resultHandler);
        }
      }
    });
  }

}
