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
package net.kuujo.vertigo.io.connection.impl;

import java.util.HashMap;
import java.util.Map;

import net.kuujo.vertigo.io.connection.ConnectionInputGroup;
import net.kuujo.vertigo.io.group.InputGroup;

import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;

/**
 * Connection level input group.<p>
 *
 * This input group is created directly by a {@link DefaultInputConnection} when
 * the connection receives a new group message.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class DefaultConnectionInputGroup implements ConnectionInputGroup {
  private final String id;
  private final String name;
  private final DefaultInputConnection connection;
  @SuppressWarnings("rawtypes")
  private Handler messageHandler;
  private Handler<Void> startHandler;
  private Handler<Void> endHandler;
  private Handler<InputGroup> groupHandler;
  private final Map<String, Handler<InputGroup>> groupHandlers = new HashMap<>();
  private boolean started;

  public DefaultConnectionInputGroup(String id, String name, DefaultInputConnection connection) {
    this.id = id;
    this.name = name;
    this.connection = connection;
  }

  @Override
  public String id() {
    return id;
  }

  @Override
  public String name() {
    return name;
  }

  @Override
  public Vertx vertx() {
    return connection.vertx();
  }

  @Override
  public InputGroup pause() {
    connection.pause();
    return this;
  }

  @Override
  public InputGroup resume() {
    connection.resume();
    return this;
  }

  @SuppressWarnings("unchecked")
  void handleMessage(Object message) {
    if (messageHandler != null) {
      messageHandler.handle(message);
    }
  }

  @Override
  @SuppressWarnings("rawtypes")
  public InputGroup messageHandler(Handler handler) {
    // When a message handler is registered on the group, notify the other
    // side of the connection that the group is ready to receive messages.
    // This allows the input group to perform asynchronous setup operations
    // prior to receiving messages.
    this.messageHandler = handler;
    if (!started && handler != null) {
      connection.groupReady(id);
      started = true;
    }
    return this;
  }

  void handleGroup(InputGroup group) {
    // First check for a named group handler. If a named group handler isn't
    // registered then trigger the arbitrary group handler if one is registered.
    Handler<InputGroup> handler = groupHandlers.get(group.name());
    if (handler != null) {
      handler.handle(group);
    } else if (groupHandler != null) {
      groupHandler.handle(group);
    } else {
      // If there is no group handler for this input group then immediately
      // indicate that the group is ready, otherwise no message handler will
      // ever be registered and the group will never be ready.
      connection.groupReady(group.id());
    }
  }

  @Override
  public InputGroup groupHandler(Handler<InputGroup> handler) {
    // When a group handler is registered on the group, if the group hasn't
    // already been started then send a start message to the source.
    this.groupHandler = handler;
    if (handler != null && !started) {
      connection.groupReady(id);
      started = true;
    }
    return this;
  }

  @Override
  public InputGroup groupHandler(String group, Handler<InputGroup> handler) {
    // When a group handler is registered on the group, if the group hasn't
    // already been started then send a start message to the source.
    if (handler != null) {
      this.groupHandlers.put(group, handler);
      if (!started) {
        connection.groupReady(id);
        started = true;
      }
    } else {
      this.groupHandlers.remove(group);
    }
    return this;
  }

  void handleStart() {
    if (startHandler != null) {
      startHandler.handle(null);
    }
  }

  @Override
  public InputGroup startHandler(Handler<Void> handler) {
    this.startHandler = handler;
    return this;
  }

  void handleEnd() {
    if (endHandler != null) {
      endHandler.handle(null);
    }
  }

  @Override
  public InputGroup endHandler(Handler<Void> handler) {
    this.endHandler = handler;
    return this;
  }

}
