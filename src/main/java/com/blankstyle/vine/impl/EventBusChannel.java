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
package com.blankstyle.vine.impl;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import com.blankstyle.vine.Channel;

/**
 * A Vert.x EventBus channel implementation.
 *
 * @author Jordan Halterman
 */
public class EventBusChannel implements Channel {

  private String address;

  private EventBus eventBus;

  public EventBusChannel(String address, EventBus eventBus) {
    this.address = address;
    this.eventBus = eventBus;
  }

  @Override
  public void send(Object message) {
    eventBus.send(address, message);
  }

  @Override
  public void send(Object message, @SuppressWarnings("rawtypes") Handler<Message> replyHandler) {
    eventBus.send(address, message, replyHandler);
  }

  @Override
  public void send(JsonObject message) {
    eventBus.send(address, message);
  }

  @Override
  public <T> void send(JsonObject message, Handler<Message<T>> replyHandler) {
    eventBus.send(address, message, replyHandler);
  }

  @Override
  public void send(JsonArray message) {
    eventBus.send(address, message);
  }

  @Override
  public <T> void send(JsonArray message, Handler<Message<T>> replyHandler) {
    eventBus.send(address, message, replyHandler);
  }

  @Override
  public void send(Buffer message) {
    eventBus.send(address, message);
  }

  @Override
  public <T> void send(Buffer message, Handler<Message<T>> replyHandler) {
    eventBus.send(address, message, replyHandler);
  }

  @Override
  public void send(byte[] message) {
    eventBus.send(address, message);
  }

  @Override
  public <T> void send(byte[] message, Handler<Message<T>> replyHandler) {
    eventBus.send(address, message, replyHandler);
  }

  @Override
  public void send(String message) {
    eventBus.send(address, message);
  }

  @Override
  public <T> void send(String message, Handler<Message<T>> replyHandler) {
    eventBus.send(address, message, replyHandler);
  }

  @Override
  public void send(Integer message) {
    eventBus.send(address, message);
  }

  @Override
  public <T> void send(Integer message, Handler<Message<T>> replyHandler) {
    eventBus.send(address, message, replyHandler);
  }

  @Override
  public void send(Long message) {
    eventBus.send(address, message);
  }

  @Override
  public <T> void send(Long message, Handler<Message<T>> replyHandler) {
    eventBus.send(address, message, replyHandler);
  }

  @Override
  public void send(Float message) {
    eventBus.send(address, message);
  }

  @Override
  public <T> void send(Float message, Handler<Message<T>> replyHandler) {
    eventBus.send(address, message, replyHandler);
  }

  @Override
  public void send(Double message) {
    eventBus.send(address, message);
  }

  @Override
  public <T> void send(Double message, Handler<Message<T>> replyHandler) {
    eventBus.send(address, message, replyHandler);
  }

  @Override
  public void send(Boolean message) {
    eventBus.send(address, message);
  }

  @Override
  public <T> void send(Boolean message, Handler<Message<T>> replyHandler) {
    eventBus.send(address, message, replyHandler);
  }

  @Override
  public void send(Short message) {
    eventBus.send(address, message);
  }

  @Override
  public <T> void send(Short message, Handler<Message<T>> replyHandler) {
    eventBus.send(address, message, replyHandler);
  }

  @Override
  public void send(Character message) {
    eventBus.send(address, message);
  }

  @Override
  public <T> void send(Character message, Handler<Message<T>> replyHandler) {
    eventBus.send(address, message, replyHandler);
  }

  @Override
  public void send(Byte message) {
    eventBus.send(address, message);
  }

  @Override
  public <T> void send(Byte message, Handler<Message<T>> replyHandler) {
    eventBus.send(address, message, replyHandler);
  }

  @Override
  public void registerHandler(Handler<? extends Message<?>> handler) {
    eventBus.registerHandler(address, handler);
  }

  @Override
  public void registerHandler(Handler<? extends Message<?>> handler, Handler<AsyncResult<Void>> resultHandler) {
    eventBus.registerHandler(address, handler, resultHandler);
  }

  @Override
  public void registerLocalHandler(Handler<? extends Message<?>> handler) {
    eventBus.registerLocalHandler(address, handler);
  }

  @Override
  public void unregisterHandler(Handler<? extends Message<?>> handler) {
    eventBus.unregisterHandler(address, handler);
  }

  @Override
  public void unregisterHandler(Handler<? extends Message<?>> handler, Handler<AsyncResult<Void>> resultHandler) {
    eventBus.unregisterHandler(address, handler, resultHandler);
  }

}
