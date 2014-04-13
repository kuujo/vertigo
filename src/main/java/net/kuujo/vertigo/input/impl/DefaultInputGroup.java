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
package net.kuujo.vertigo.input.impl;

import java.util.ArrayDeque;
import java.util.Queue;

import net.kuujo.vertigo.input.InputGroup;

import org.vertx.java.core.Handler;

/**
 * Default input group implementation.
 *
 * @author Jordan Halterman
 */
public class DefaultInputGroup implements InputGroup {
  private final String name;
  private Handler<Void> startHandler;
  @SuppressWarnings("rawtypes")
  private Handler messageHandler;
  private Handler<Void> endHandler;
  private Handler<InputGroup> groupHandler;
  private Queue<Object> queue = new ArrayDeque<>();
  private boolean paused;
  private boolean ended;

  public DefaultInputGroup(String name) {
    this.name = name;
  }

  @Override
  public String name() {
    return name;
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

  void handleStart() {
    if (startHandler != null) {
      startHandler.handle((Void) null);
    }
  }

  @Override
  public InputGroup endHandler(Handler<Void> handler) {
    this.endHandler = handler;
    return this;
  }

  void handleEnd() {
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
  void handleMessage(Object message) {
    if (paused) {
      queue.add(message);
    }
    else if (messageHandler != null) {
      messageHandler.handle(message);
    }
  }

  @Override
  public InputGroup groupHandler(String group, Handler<InputGroup> handler) {
    this.groupHandler = handler;
    return this;
  }

  void handleGroup(InputGroup group) {
    if (groupHandler != null) {
      groupHandler.handle(group);
    }
  }

}
