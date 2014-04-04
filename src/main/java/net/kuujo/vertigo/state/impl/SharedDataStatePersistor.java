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

import net.kuujo.vertigo.annotations.Factory;
import net.kuujo.vertigo.annotations.LocalType;
import net.kuujo.vertigo.state.StatePersistor;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.impl.DefaultFutureResult;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.shareddata.ConcurrentSharedMap;

/**
 * Shared-data-based state persistor.
 *
 * @author Jordan Halterman
 */
@LocalType
public class SharedDataStatePersistor implements StatePersistor {
  private final ConcurrentSharedMap<String, String> map;

  @Factory
  public static SharedDataStatePersistor factory(JsonObject config, Vertx vertx) {
    return new SharedDataStatePersistor(vertx.sharedData().<String, String>getMap(config.getString("name")));
  }

  public SharedDataStatePersistor(ConcurrentSharedMap<String, String> map) {
    this.map = map;
  }

  @Override
  public StatePersistor loadState(final Handler<AsyncResult<JsonObject>> resultHandler) {
    String state = map.get("value");
    new DefaultFutureResult<JsonObject>(state != null ? new JsonObject(state) : new JsonObject()).setHandler(resultHandler);
    return this;
  }

  @Override
  public StatePersistor storeState(JsonObject state, final Handler<AsyncResult<Void>> doneHandler) {
    map.put("value", state.encode());
    new DefaultFutureResult<Void>((Void) null).setHandler(doneHandler);
    return this;
  }

}
