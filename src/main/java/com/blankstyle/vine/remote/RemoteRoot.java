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
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.impl.DefaultFutureResult;
import org.vertx.java.core.json.JsonObject;

import com.blankstyle.vine.AbstractRoot;
import com.blankstyle.vine.BasicFeeder;
import com.blankstyle.vine.Feeder;
import com.blankstyle.vine.context.JsonVineContext;
import com.blankstyle.vine.definition.VineDefinition;
import com.blankstyle.vine.eventbus.ReliableEventBus;
import com.blankstyle.vine.eventbus.WrappedReliableEventBus;

/**
 * A remote reference to a root verticle.
 *
 * @author Jordan Halterman
 */
public class RemoteRoot extends AbstractRoot {

  protected String address;

  protected ReliableEventBus eventBus;

  public RemoteRoot(String address, EventBus eventBus) {
    setAddress(address);
    setEventBus(eventBus);
  }

  /**
   * Sets the remote root address.
   *
   * @param address
   *   The remote root address.
   * @return
   *   The called root instance.
   */
  public RemoteRoot setAddress(String address) {
    this.address = address;
    return this;
  }

  /**
   * Gets the remote root address.
   *
   * @return
   *   The remote root address.
   */
  public String getAddress() {
    return address;
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

  /**
   * Loads a remote vine context.
   *
   * @param address
   *   The vine address.
   * @return
   *   A remote vine context. The context will be updated once a response is
   *   received from the remote root.
   */
  public JsonVineContext loadContext(String address) {
    final JsonVineContext context = new JsonVineContext(address);
    context.register(eventBus);
    eventBus.send(address, new JsonObject().putString("action", "load").putString("context", address), new Handler<Message<JsonObject>>() {
      @Override
      public void handle(Message<JsonObject> message) {
        context.update(message.body());
      }
    });
    return context;
  }

  @Override
  public void deploy(VineDefinition component) {
    eventBus.send(address, new JsonObject().putString("action", "deploy").putObject("definition", component.serialize()));
  }

  @Override
  public void deploy(final VineDefinition component, final Handler<AsyncResult<Feeder>> doneHandler) {
    final Future<Feeder> future = new DefaultFutureResult<Feeder>();
    eventBus.send(address, new JsonObject().putString("action", "deploy").putObject("definition", component.serialize()), new Handler<Message<JsonObject>>() {
      @Override
      public void handle(Message<JsonObject> message) {
        Feeder feeder = new BasicFeeder(component.getAddress(), eventBus);
        future.setResult(feeder);
        doneHandler.handle(future);
      }
    });
  }

}
