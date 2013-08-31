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
package com.blankstyle.vine.messaging;

import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;

/**
 * An eventbus-based connection.
 *
 * @author Jordan Halterman
 */
public class EventBusConnection implements Connection {

  protected String address;

  protected EventBus eventBus;

  public EventBusConnection(String address) {
    this.address = address;
  }

  public EventBusConnection(String address, EventBus eventBus) {
    this.address = address;
    this.eventBus = eventBus;
  }

  public void setEventBus(EventBus eventBus) {
    this.eventBus = eventBus;
  }

  public EventBus getEventBus() {
    return eventBus;
  }

  @Override
  public String getAddress() {
    return address;
  }

  protected JsonObject createJsonAction(JsonMessage message) {
    return new JsonObject().putString("action", "receive").putObject("message", message.serialize());
  }

  @Override
  public Connection send(JsonMessage message) {
    eventBus.send(address, createJsonAction(message));
    return this;
  }

  @Override
  public Connection send(JsonMessage message, Handler<Message<Void>> replyHandler) {
    eventBus.send(address, createJsonAction(message), replyHandler);
    return this;
  }

}
