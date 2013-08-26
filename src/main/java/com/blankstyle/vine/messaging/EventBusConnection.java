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

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonArray;
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

  @Override
  public Connection send(Object message) {
    eventBus.send(address, message);
    return this;
  }

  @Override
  public Connection send(Object message, @SuppressWarnings("rawtypes") Handler<Message> replyHandler) {
    eventBus.send(address, message, replyHandler);
    return this;
  }

  @Override
  public Connection send(JsonObject message) {
    eventBus.send(address, message);
    return this;
  }

  @Override
  public <T> Connection send(JsonObject message, Handler<Message<T>> replyHandler) {
    eventBus.send(address, message, replyHandler);
    return this;
  }

  @Override
  public Connection send(JsonArray message) {
    eventBus.send(address, message);
    return this;
  }

  @Override
  public <T> Connection send(JsonArray message, Handler<Message<T>> replyHandler) {
    eventBus.send(address, message, replyHandler);
    return this;
  }

  @Override
  public Connection send(Buffer message) {
    eventBus.send(address, message);
    return this;
  }

  @Override
  public <T> Connection send(Buffer message, Handler<Message<T>> replyHandler) {
    eventBus.send(address, message, replyHandler);
    return this;
  }

  @Override
  public Connection send(byte[] message) {
    eventBus.send(address, message);
    return this;
  }

  @Override
  public <T> Connection send(byte[] message, Handler<Message<T>> replyHandler) {
    eventBus.send(address, message, replyHandler);
    return this;
  }

  @Override
  public Connection send(String message) {
    eventBus.send(address, message);
    return this;
  }

  @Override
  public <T> Connection send(String message, Handler<Message<T>> replyHandler) {
    eventBus.send(address, message, replyHandler);
    return this;
  }

  @Override
  public Connection send(Integer message) {
    eventBus.send(address, message);
    return this;
  }

  @Override
  public <T> Connection send(Integer message, Handler<Message<T>> replyHandler) {
    eventBus.send(address, message, replyHandler);
    return this;
  }

  @Override
  public Connection send(Long message) {
    eventBus.send(address, message);
    return this;
  }

  @Override
  public <T> Connection send(Long message, Handler<Message<T>> replyHandler) {
    eventBus.send(address, message, replyHandler);
    return this;
  }

  @Override
  public Connection send(Float message) {
    eventBus.send(address, message);
    return this;
  }

  @Override
  public <T> Connection send(Float message, Handler<Message<T>> replyHandler) {
    eventBus.send(address, message, replyHandler);
    return this;
  }

  @Override
  public Connection send(Double message) {
    eventBus.send(address, message);
    return this;
  }

  @Override
  public <T> Connection send(Double message, Handler<Message<T>> replyHandler) {
    eventBus.send(address, message, replyHandler);
    return this;
  }

  @Override
  public Connection send(Boolean message) {
    eventBus.send(address, message);
    return this;
  }

  @Override
  public <T> Connection send(Boolean message, Handler<Message<T>> replyHandler) {
    eventBus.send(address, message, replyHandler);
    return this;
  }

  @Override
  public Connection send(Short message) {
    eventBus.send(address, message);
    return this;
  }

  @Override
  public <T> Connection send(Short message, Handler<Message<T>> replyHandler) {
    eventBus.send(address, message, replyHandler);
    return this;
  }

  @Override
  public Connection send(Character message) {
    eventBus.send(address, message);
    return this;
  }

  @Override
  public <T> Connection send(Character message, Handler<Message<T>> replyHandler) {
    eventBus.send(address, message, replyHandler);
    return this;
  }

  @Override
  public Connection send(Byte message) {
    eventBus.send(address, message);
    return this;
  }

  @Override
  public <T> Connection send(Byte message, Handler<Message<T>> replyHandler) {
    eventBus.send(address, message, replyHandler);
    return this;
  }

  @Override
  public Connection registerHandler(Handler<? extends Message<?>> handler) {
    eventBus.registerHandler(address, handler);
    return this;
  }

  @Override
  public Connection registerHandler(Handler<? extends Message<?>> handler, Handler<AsyncResult<Void>> resultHandler) {
    eventBus.registerHandler(address, handler, resultHandler);
    return this;
  }

  @Override
  public Connection registerLocalHandler(Handler<? extends Message<?>> handler) {
    eventBus.registerLocalHandler(address, handler);
    return this;
  }

  @Override
  public Connection unregisterHandler(Handler<? extends Message<?>> handler) {
    eventBus.unregisterHandler(address, handler);
    return this;
  }

  @Override
  public Connection unregisterHandler(Handler<? extends Message<?>> handler, Handler<AsyncResult<Void>> resultHandler) {
    eventBus.unregisterHandler(address, handler, resultHandler);
    return this;
  }

}
