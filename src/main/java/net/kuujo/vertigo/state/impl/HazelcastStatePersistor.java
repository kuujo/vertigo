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

import net.kuujo.vertigo.annotations.ClusterType;
import net.kuujo.vertigo.annotations.Factory;
import net.kuujo.vertigo.data.impl.XyncMap;
import net.kuujo.vertigo.state.StatePersistor;
import net.kuujo.xync.data.impl.XyncAsyncMap;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.impl.DefaultFutureResult;
import org.vertx.java.core.json.JsonObject;

/**
 * Hazelcast-based state persistor.
 *
 * @author Jordan Halterman
 */
@ClusterType
public class HazelcastStatePersistor implements StatePersistor {
  private final XyncMap<String, String> map;

  @Factory
  public static HazelcastStatePersistor factory(JsonObject config, Vertx vertx) {
    return new HazelcastStatePersistor(new XyncMap<String, String>(new XyncAsyncMap<String, String>(config.getString("name"), vertx.eventBus())));
  }

  public HazelcastStatePersistor(XyncMap<String, String> map) {
    this.map = map;
  }

  @Override
  public StatePersistor loadState(final Handler<AsyncResult<JsonObject>> resultHandler) {
    map.get("value", new Handler<AsyncResult<String>>() {
      @Override
      public void handle(AsyncResult<String> result) {
        if (result.failed()) {
          new DefaultFutureResult<JsonObject>(result.cause()).setHandler(resultHandler);
        } else if (result.result() == null) {
          new DefaultFutureResult<JsonObject>(new JsonObject()).setHandler(resultHandler);
        } else {
          new DefaultFutureResult<JsonObject>(new JsonObject(result.result())).setHandler(resultHandler);
        }
      }
    });
    return this;
  }

  @Override
  public StatePersistor storeState(JsonObject state, final Handler<AsyncResult<Void>> doneHandler) {
    map.put("value", state.encode(), new Handler<AsyncResult<String>>() {
      @Override
      public void handle(AsyncResult<String> result) {
        if (result.failed()) {
          new DefaultFutureResult<Void>(result.cause()).setHandler(doneHandler);
        } else {
          new DefaultFutureResult<Void>((Void) null).setHandler(doneHandler);
        }
      }
    });
    return this;
  }

}
