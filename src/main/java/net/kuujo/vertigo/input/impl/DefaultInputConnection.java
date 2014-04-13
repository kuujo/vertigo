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

import java.util.HashMap;
import java.util.Map;

import net.kuujo.vertigo.context.InputConnectionContext;
import net.kuujo.vertigo.input.InputConnection;
import net.kuujo.vertigo.input.InputGroup;
import net.kuujo.vertigo.util.CountingCompletionHandler;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;

/**
 * Default input connection implementation.
 *
 * @author Jordan Halterman
 */
public class DefaultInputConnection implements InputConnection {
  private final Vertx vertx;
  private final InputConnectionContext context;
  private final Map<String, Handler<InputGroup>> groupHandlers = new HashMap<>();
  private final Map<String, DefaultInputGroup> groups = new HashMap<>();
  @SuppressWarnings("rawtypes")
  private Handler messageHandler;

  private final Handler<Message<JsonObject>> internalStartHandler = new Handler<Message<JsonObject>>() {
    @Override
    public void handle(Message<JsonObject> message) {
      String id = message.body().getString("id");
      String name = message.body().getString("group");
      DefaultInputGroup group = new DefaultInputGroup(name);
      groups.put(id, group);
      Handler<InputGroup> handler = groupHandlers.get(name);
      if (handler != null) {
        handler.handle(group);
      }
      group.handleStart();
      message.reply();
    }
  };

  private final Handler<Message<JsonObject>> internalGroupHandler = new Handler<Message<JsonObject>>() {
    @Override
    public void handle(Message<JsonObject> message) {
      String id = message.body().getString("id");
      DefaultInputGroup group = groups.get(id);
      if (group != null) {
        group.handleMessage(message.body().getValue("message"));
      }
      message.reply();
    }
  };

  private final Handler<Message<JsonObject>> internalEndHandler = new Handler<Message<JsonObject>>() {
    @Override
    public void handle(Message<JsonObject> message) {
      String id = message.body().getString("id");
      DefaultInputGroup group = groups.remove(id);
      if (group != null) {
        group.handleEnd();
      }
      message.reply();
    }
  };

  private final Handler<Message<Object>> internalMessageHandler = new Handler<Message<Object>>() {
    @Override
    @SuppressWarnings("unchecked")
    public void handle(Message<Object> message) {
      if (messageHandler != null) {
        messageHandler.handle(message.body());
      }
      message.reply();
    }
  };

  public DefaultInputConnection(Vertx vertx, InputConnectionContext context) {
    this.vertx = vertx;
    this.context = context;
  }

  @Override
  public InputConnectionContext context() {
    return context;
  }

  @Override
  public InputConnection open() {
    return open(null);
  }

  @Override
  public InputConnection open(Handler<AsyncResult<Void>> doneHandler) {
    final CountingCompletionHandler<Void> counter = new CountingCompletionHandler<Void>(4).setHandler(doneHandler);
    vertx.eventBus().registerHandler(String.format("%s.start", context.address()), internalStartHandler, counter);
    vertx.eventBus().registerHandler(String.format("%s.group", context.address()), internalGroupHandler, counter);
    vertx.eventBus().registerHandler(String.format("%s.end", context.address()), internalEndHandler, counter);
    vertx.eventBus().registerHandler(context.address(), internalMessageHandler, counter);
    return this;
  }

  @Override
  public InputConnection pause() {
    return this;
  }

  @Override
  public InputConnection resume() {
    return this;
  }

  @Override
  @SuppressWarnings("rawtypes")
  public InputConnection messageHandler(Handler handler) {
    this.messageHandler = handler;
    return this;
  }

  @Override
  public InputConnection groupHandler(String group, Handler<InputGroup> handler) {
    groupHandlers.put(group, handler);
    return this;
  }

  @Override
  public void close() {
    close(null);
  }

  @Override
  public void close(Handler<AsyncResult<Void>> doneHandler) {
    final CountingCompletionHandler<Void> counter = new CountingCompletionHandler<Void>(4).setHandler(doneHandler);
    vertx.eventBus().unregisterHandler(String.format("%s.start", context.address()), internalStartHandler, counter);
    vertx.eventBus().unregisterHandler(String.format("%s.group", context.address()), internalGroupHandler, counter);
    vertx.eventBus().unregisterHandler(String.format("%s.end", context.address()), internalEndHandler, counter);
    vertx.eventBus().unregisterHandler(context.address(), internalMessageHandler, counter);
  }

}
