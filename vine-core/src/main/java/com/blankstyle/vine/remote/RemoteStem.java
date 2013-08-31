/*
* Copyright 2013 the original author or authors.
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
package com.blankstyle.vine.remote;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Container;

import com.blankstyle.vine.Stem;
import com.blankstyle.vine.context.WorkerContext;
import com.blankstyle.vine.eventbus.ReliableEventBus;
import com.blankstyle.vine.eventbus.WrappedReliableEventBus;

import com.blankstyle.vine.util.Messaging;

/**
 * A remote reference to a stem verticle.
 *
 * @author Jordan Halterman
 */
public class RemoteStem implements Stem {

  protected String address;

  protected Vertx vertx;

  protected Container container;

  protected ReliableEventBus eventBus;

  public RemoteStem(String address) {
    this.address = address;
  }

  public RemoteStem(String address, Vertx vertx) {
    this.address = address;
    this.vertx = vertx;
    setEventBus(vertx.eventBus());
  }

  public RemoteStem(String address, Vertx vertx, Container container) {
    this.address = address;
    this.vertx = vertx;
    this.container = container;
    setEventBus(vertx.eventBus());
  }

  public RemoteStem(String address, Vertx vertx, Container container, EventBus eventBus) {
    this.address = address;
    this.vertx = vertx;
    this.container = container;
    setEventBus(eventBus);
  }

  public RemoteStem setAddress(String address) {
    this.address = address;
    return this;
  }

  @Override
  public String getAddress() {
    return address;
  }

  @Override
  public Stem setVertx(Vertx vertx) {
    this.vertx = vertx;
    if (eventBus != null) {
      eventBus.setVertx(vertx);
    }
    return this;
  }

  @Override
  public Vertx getVertx() {
    return vertx;
  }

  @Override
  public Stem setContainer(Container container) {
    this.container = container;
    return this;
  }

  @Override
  public Container getContainer() {
    return container;
  }

  /**
   * Sets the stem eventbus.
   *
   * @param eventBus
   *   The event bus.
   * @return
   *   The called stem instance.
   */
  public RemoteStem setEventBus(EventBus eventBus) {
    if (!(eventBus instanceof ReliableEventBus)) {
      eventBus = new WrappedReliableEventBus(eventBus, vertx);
    }
    this.eventBus = (ReliableEventBus) eventBus;
    return this;
  }

  /**
   * Gets the stem eventbus.
   *
   * @return
   *   The stem eventbus.
   */
  public EventBus getEventBus() {
    return eventBus;
  }

  @Override
  public void assign(WorkerContext context, final Handler<AsyncResult<Void>> doneHandler) {
    eventBus.send(address, new JsonObject().putString("action", "assign").putObject("context", context.serialize()), new Handler<Message<JsonObject>>() {
      @Override
      public void handle(Message<JsonObject> message) {
        Messaging.checkResponse(message, doneHandler);
      }
    });
  }

  @Override
  public void assign(WorkerContext context, long timeout, final Handler<AsyncResult<Void>> doneHandler) {
    eventBus.send(address, new JsonObject().putString("action", "assign").putObject("context", context.serialize()), timeout, new AsyncResultHandler<Message<JsonObject>>() {
      @Override
      public void handle(AsyncResult<Message<JsonObject>> result) {
        Messaging.checkResponse(result, doneHandler);
      }
    });
  }

  @Override
  public void release(WorkerContext context, final Handler<AsyncResult<Void>> doneHandler) {
    eventBus.send(address, new JsonObject().putString("action", "release").putObject("context", context.serialize()), new AsyncResultHandler<Message<JsonObject>>() {
      @Override
      public void handle(AsyncResult<Message<JsonObject>> result) {
        Messaging.checkResponse(result, doneHandler);
      }
    });
  }

  @Override
  public void release(WorkerContext context, long timeout, final Handler<AsyncResult<Void>> doneHandler) {
    eventBus.send(address, new JsonObject().putString("action", "release").putObject("context", context.serialize()), timeout, new AsyncResultHandler<Message<JsonObject>>() {
      @Override
      public void handle(AsyncResult<Message<JsonObject>> result) {
        Messaging.checkResponse(result, doneHandler);
      }
    });
  }

}
