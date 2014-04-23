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
package net.kuujo.vertigo.io.group.impl;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

import net.kuujo.vertigo.io.group.InputGroup;

import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;

/**
 * Default input group implementation.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class DefaultInputGroup implements InputGroup {
  private final String id;
  private final String name;
  private final Vertx vertx;
  private Handler<Void> startHandler;
  @SuppressWarnings("rawtypes")
  private Handler messageHandler;
  private Handler<Void> endHandler;
  private Map<String, Handler<InputGroup>> groupHandlers = new HashMap<>();
  private Queue<Object> queue = new ArrayDeque<>();
  private boolean paused;
  private boolean ended;

  public DefaultInputGroup(String id, String name, Vertx vertx) {
    this.id = id;
    this.name = name;
    this.vertx = vertx;
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
    return vertx;
  }

  @Override
  public InputGroup pause() {
    paused = true;
    return this;
  }

  @Override
  @SuppressWarnings("unchecked")
  public InputGroup resume() {
    paused = false;
    if (messageHandler != null) {
      for (Object message : queue) {
        messageHandler.handle(message);
      }
    }
    queue.clear();
    if (ended && endHandler != null) {
      endHandler.handle((Void) null);
    }
    return this;
  }

  @Override
  public InputGroup startHandler(Handler<Void> handler) {
    this.startHandler = handler;
    return this;
  }

  public void handleStart() {
    if (startHandler != null) {
      startHandler.handle((Void) null);
    }
  }

  @Override
  public InputGroup endHandler(Handler<Void> handler) {
    this.endHandler = handler;
    return this;
  }

  public void handleEnd() {
    if (paused) {
      ended = true;
    }
    else if (endHandler != null) {
      endHandler.handle((Void) null);
    }
  }

  @Override
  @SuppressWarnings("rawtypes")
  public InputGroup messageHandler(Handler handler) {
    this.messageHandler = handler;
    return this;
  }

  @SuppressWarnings("unchecked")
  public void handleMessage(Object message) {
    if (paused) {
      queue.add(message);
    }
    else if (messageHandler != null) {
      messageHandler.handle(message);
    }
  }

  @Override
  public InputGroup groupHandler(String group, Handler<InputGroup> handler) {
    this.groupHandlers.put(group, handler);
    return this;
  }

  public void handleGroup(InputGroup group) {
    Handler<InputGroup> handler = groupHandlers.get(group.name());
    if (handler != null) {
      handler.handle(group);
    }
  }

}
