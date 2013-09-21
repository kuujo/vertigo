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
package net.kuujo.vine.feeder;

import net.kuujo.vine.eventbus.ReliableEventBus;
import net.kuujo.vine.eventbus.WrappedReliableEventBus;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;

/**
 * A remote vine feeder implementation.
 *
 * @author Jordan Halterman
 */
public class RemoteFeeder implements Feeder, Handler<Message<JsonObject>> {

  protected String address;

  protected ReliableEventBus eventBus;

  public RemoteFeeder(String address, Vertx vertx) {
    this(address, vertx.eventBus(), vertx);
  }

  public RemoteFeeder(String address, EventBus eventBus) {
    this.address = address;
    setEventBus(eventBus);
  }

  public RemoteFeeder(String address, EventBus eventBus, Vertx vertx) {
    this.address = address;
    setEventBus(eventBus).setVertx(vertx);
  }

  private ReliableEventBus setEventBus(EventBus eventBus) {
    if (eventBus instanceof ReliableEventBus) {
      this.eventBus = (ReliableEventBus) eventBus;
    }
    else {
      this.eventBus = new WrappedReliableEventBus(eventBus);
    }
    eventBus.registerHandler(String.format("%s.status", address), this);
    return this.eventBus;
  }

  @Override
  public void handle(Message<JsonObject> message) {
    
  }

  @Override
  public Feeder setFeedQueueMaxSize(long maxSize) {
    return this;
  }

  @Override
  public boolean feedQueueFull() {
    return false;
  }

  @Override
  public Feeder feed(JsonObject data) {
    return this;
  }

  @Override
  public Feeder feed(JsonObject data, Handler<AsyncResult<Void>> doneHandler) {
    return this;
  }

  @Override
  public Feeder feed(JsonObject data, long timeout, Handler<AsyncResult<Void>> doneHandler) {
    return this;
  }

  @Override
  public Feeder drainHandler(Handler<Void> handler) {
    return this;
  }

}
