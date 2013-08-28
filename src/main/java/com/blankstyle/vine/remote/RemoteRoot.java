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
import org.vertx.java.core.Future;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.impl.DefaultFutureResult;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Container;

import com.blankstyle.vine.BasicFeeder;
import com.blankstyle.vine.Feeder;
import com.blankstyle.vine.Root;
import com.blankstyle.vine.definition.VineDefinition;
import com.blankstyle.vine.eventbus.ReliableEventBus;
import com.blankstyle.vine.eventbus.WrappedReliableEventBus;

/**
 * A remote reference to a root verticle.
 *
 * @author Jordan Halterman
 */
public class RemoteRoot implements Root {

  protected Vertx vertx;

  protected Container container;

  protected String address;

  protected ReliableEventBus eventBus;

  public RemoteRoot(String address, EventBus eventBus) {
    setAddress(address);
    setEventBus(eventBus);
  }

  @Override
  public RemoteRoot setAddress(String address) {
    this.address = address;
    return this;
  }

  @Override
  public String getAddress() {
    return address;
  }

  @Override
  public Root setVertx(Vertx vertx) {
    this.vertx = vertx;
    return this;
  }

  @Override
  public Vertx getVertx() {
    return vertx;
  }

  @Override
  public Root setContainer(Container container) {
    this.container = container;
    return this;
  }

  @Override
  public Container getContainer() {
    return container;
  }

  /**
   * Sets the root eventbus.
   *
   * @param eventBus
   *   The event bus.
   * @return
   *   The called root instance.
   */
  public RemoteRoot setEventBus(EventBus eventBus) {
    if (!(eventBus instanceof ReliableEventBus)) {
      eventBus = new WrappedReliableEventBus(eventBus);
    }
    this.eventBus = (ReliableEventBus) eventBus;
    return this;
  }

  /**
   * Gets the root eventbus.
   *
   * @return
   *   The root eventbus.
   */
  public EventBus getEventBus() {
    return eventBus;
  }

  @Override
  public void deploy(VineDefinition vine) {
    eventBus.send(address, new JsonObject().putString("action", "deploy").putObject("definition", vine.serialize()));
  }

  @Override
  public void deploy(final VineDefinition vine, final Handler<AsyncResult<Feeder>> doneHandler) {
    final Future<Feeder> future = new DefaultFutureResult<Feeder>();
    eventBus.send(address, new JsonObject().putString("action", "deploy").putObject("definition", vine.serialize()), new Handler<Message<JsonObject>>() {
      @Override
      public void handle(Message<JsonObject> message) {
        Feeder feeder = new BasicFeeder(vine.getAddress(), eventBus);
        future.setResult(feeder);
        doneHandler.handle(future);
      }
    });
  }

}
