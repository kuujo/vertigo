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
import org.vertx.java.core.json.JsonObject;

/**
 * Mongo-based state persistor.
 *
 * @author Jordan Halterman
 */
@LocalType
@ClusterType
public class MongoStatePersistor implements StatePersistor {

  /**
   * Configuration option that indicates the Mongo module address.
   */
  public static final String ADDRESS = "address";

  /**
   * Configuration option that indicates the Mongo collection to use.
   */
  public static final String COLLECTION = "collection";

  private final String address;
  private final String collection;
  private final Vertx vertx;

  @Factory
  public static MongoStatePersistor factory(JsonObject config, Vertx vertx) {
    if (!config.containsField("address")) throw new IllegalArgumentException("No Mongo address specified.");
    if (!config.containsField("collection")) throw new IllegalArgumentException("No Mongo collection specified.");
    return new MongoStatePersistor(config.getString("address"), config.getString("collection"), vertx);
  }

  public MongoStatePersistor(String address, String collection, Vertx vertx) {
    this.address = address;
    this.collection = collection;
    this.vertx = vertx;
  }

  @Override
  public StatePersistor loadState(final Handler<AsyncResult<JsonObject>> resultHandler) {
    JsonObject message = new JsonObject()
        .putString("action", "findone")
        .putString("collection", collection)
        .putObject("matcher", new JsonObject().putString("instance", ""));
    vertx.eventBus().sendWithTimeout(address, message, 30000, new Handler<AsyncResult<Message<JsonObject>>>() {
      @Override
      public void handle(AsyncResult<Message<JsonObject>> result) {
        if (result.failed()) {
          new DefaultFutureResult<JsonObject>(result.cause()).setHandler(resultHandler);
        } else if (result.result().body().getString("status", "ok").equals("error")) {
          new DefaultFutureResult<JsonObject>(new VertigoException(result.result().body().getString("message"))).setHandler(resultHandler);
        } else {
          new DefaultFutureResult<JsonObject>(result.result().body().getObject("state", new JsonObject())).setHandler(resultHandler);
        }
      }
    });
    return this;
  }

  @Override
  public StatePersistor storeState(JsonObject state, final Handler<AsyncResult<Void>> doneHandler) {
    JsonObject message = new JsonObject()
        .putString("action", "save")
        .putString("collection", collection)
        .putObject("document", new JsonObject().putString("instance", "").putObject("state", state));
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
