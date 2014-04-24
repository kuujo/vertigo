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

import net.kuujo.vertigo.annotations.ClusterType;
import net.kuujo.vertigo.annotations.Factory;
import net.kuujo.vertigo.data.DataException;
import net.kuujo.vertigo.data.AsyncLock;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.eventbus.ReplyException;
import org.vertx.java.core.eventbus.ReplyFailure;
import org.vertx.java.core.impl.DefaultFutureResult;
import org.vertx.java.core.json.JsonObject;

/**
 * An event bus lock implementation.
 *
 * @author Jordan Halterman
 */
@ClusterType
public class XyncLock implements AsyncLock {
  private static final String CLUSTER_ADDRESS = "__CLUSTER__";
  private final String name;
  private final EventBus eventBus;

  @Factory
  public static XyncLock factory(String name, Vertx vertx) {
    return new XyncLock(name, vertx.eventBus());
  }

  public XyncLock(String name, EventBus eventBus) {
    this.name = name;
    this.eventBus = eventBus;
  }

  @Override
  public String name() {
    return name;
  }

  @Override
  public void lock(final Handler<AsyncResult<Void>> resultHandler) {
    JsonObject message = new JsonObject()
        .putString("action", "lock")
        .putString("type", "lock")
        .putString("name", name);
    eventBus.send(CLUSTER_ADDRESS, message, new Handler<Message<JsonObject>>() {
      @Override
      public void handle(Message<JsonObject> message) {
        if (message.body().getString("status").equals("error")) {
          new DefaultFutureResult<Void>(new DataException(message.body().getString("message"))).setHandler(resultHandler);
        } else {
          new DefaultFutureResult<Void>((Void) null).setHandler(resultHandler);
        }
      }
    });
  }

  @Override
  public void tryLock(final Handler<AsyncResult<Boolean>> resultHandler) {
    tryLock(30000, resultHandler);
  }

  @Override
  public void tryLock(long timeout, final Handler<AsyncResult<Boolean>> resultHandler) {
    JsonObject message = new JsonObject()
        .putString("action", "try")
        .putString("type", "lock")
        .putString("name", name);
    eventBus.sendWithTimeout(CLUSTER_ADDRESS, message, timeout, new Handler<AsyncResult<Message<JsonObject>>>() {
      @Override
      public void handle(AsyncResult<Message<JsonObject>> result) {
        if (result.failed()) {
          if (((ReplyException) result.cause()).failureType().equals(ReplyFailure.TIMEOUT)) {
            new DefaultFutureResult<Boolean>(false).setHandler(resultHandler);
          } else {
            new DefaultFutureResult<Boolean>(result.cause()).setHandler(resultHandler);
          }
        } else if (result.result().body().getString("status").equals("error")) {
          new DefaultFutureResult<Boolean>(new DataException(result.result().body().getString("message"))).setHandler(resultHandler);
        } else {
          new DefaultFutureResult<Boolean>(result.result().body().getBoolean("result")).setHandler(resultHandler);
        }
      }
    });
  }

  @Override
  public void unlock() {
    unlock(null);
  }

  @Override
  public void unlock(final Handler<AsyncResult<Void>> doneHandler) {
    JsonObject message = new JsonObject()
        .putString("action", "unlock")
        .putString("type", "lock")
        .putString("name", name);
    eventBus.sendWithTimeout(CLUSTER_ADDRESS, message, 30000, new Handler<AsyncResult<Message<JsonObject>>>() {
      @Override
      public void handle(AsyncResult<Message<JsonObject>> result) {
        if (result.failed()) {
          new DefaultFutureResult<Void>(result.cause()).setHandler(doneHandler);
        } else if (result.result().body().getString("status").equals("error")) {
          new DefaultFutureResult<Void>(new DataException(result.result().body().getString("message"))).setHandler(doneHandler);
        } else {
          new DefaultFutureResult<Void>((Void) null).setHandler(doneHandler);
        }
      }
    });
  }

}
