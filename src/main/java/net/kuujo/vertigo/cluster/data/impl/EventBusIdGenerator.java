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

import net.kuujo.vertigo.cluster.data.DataException;
import net.kuujo.vertigo.cluster.data.AsyncIdGenerator;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.impl.DefaultFutureResult;
import org.vertx.java.core.json.JsonObject;

/**
 * An event bus ID generator implementation.
 *
 * @author Jordan Halterman
 */
public class EventBusIdGenerator implements AsyncIdGenerator {
  private static final String CLUSTER_ADDRESS = "__CLUSTER__";
  private final String name;
  private final EventBus eventBus;

  public EventBusIdGenerator(String name, EventBus eventBus) {
    this.name = name;
    this.eventBus = eventBus;
  }

  @Override
  public String name() {
    return name;
  }

  @Override
  public void nextId(final Handler<AsyncResult<Long>> resultHandler) {
    JsonObject message = new JsonObject()
        .putString("action", "next")
        .putString("type", "id")
        .putString("name", name);
    eventBus.sendWithTimeout(CLUSTER_ADDRESS, message, 30000, new Handler<AsyncResult<Message<JsonObject>>>() {
      @Override
      public void handle(AsyncResult<Message<JsonObject>> result) {
        if (result.failed()) {
          new DefaultFutureResult<Long>(result.cause()).setHandler(resultHandler);
        }
        else if (result.result().body().getString("status").equals("error")) {
          new DefaultFutureResult<Long>(new DataException(result.result().body().getString("message"))).setHandler(resultHandler);
        }
        else {
          new DefaultFutureResult<Long>(result.result().body().getLong("result")).setHandler(resultHandler);
        }
      }
    });
  }

}
