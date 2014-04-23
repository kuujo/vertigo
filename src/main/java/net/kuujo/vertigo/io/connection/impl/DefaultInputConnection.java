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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.kuujo.vertigo.context.InputConnectionContext;
import net.kuujo.vertigo.hooks.InputHook;
import net.kuujo.vertigo.io.InputDeserializer;
import net.kuujo.vertigo.io.connection.InputConnection;
import net.kuujo.vertigo.io.group.InputGroup;
import net.kuujo.vertigo.io.group.impl.DefaultInputGroup;
import net.kuujo.vertigo.util.CountingCompletionHandler;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.impl.DefaultFutureResult;
import org.vertx.java.core.json.JsonObject;

/**
 * Default input connection implementation.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class DefaultInputConnection implements InputConnection {
  private static final long BATCH_SIZE = 1000;
  private final Vertx vertx;
  private final EventBus eventBus;
  private final InputConnectionContext context;
  private List<InputHook> hooks = new ArrayList<>();
  private final Map<String, Handler<InputGroup>> groupHandlers = new HashMap<>();
  private final Map<String, DefaultInputGroup> groups = new HashMap<>();
  private final InputDeserializer deserializer = new InputDeserializer();
  @SuppressWarnings("rawtypes")
  private Handler messageHandler;
  private String feedbackAddress;
  private long lastReceived;
  private boolean open;
  private boolean paused;

  private final Handler<Message<String>> internalOpenHandler = new Handler<Message<String>>() {
    @Override
    public void handle(Message<String> message) {
      if (open) {
        feedbackAddress = message.body();
        groups.clear();
        message.reply(true);
      }
    }
  };

  private final Handler<Message<JsonObject>> internalStartHandler = new Handler<Message<JsonObject>>() {
    @Override
    public void handle(Message<JsonObject> message) {
      if (!paused) {
        long id = message.body().getLong("id");
        if (checkID(id)) {
          JsonObject body = message.body();
          String groupID = body.getString("group");
          String name = body.getString("name");
          String parentId = body.getString("parent");
          DefaultInputGroup group = new DefaultInputGroup(groupID, name, vertx);
          groups.put(groupID, group);
          if (parentId != null) {
            DefaultInputGroup parent = groups.get(parentId);
            if (parent != null) {
              parent.handleGroup(group);
            }
          } else {
            Handler<InputGroup> handler = groupHandlers.get(name);
            if (handler != null) {
              handler.handle(group);
            }
          }
          group.handleStart();
        }
      }
    }
  };

  private final Handler<Message<JsonObject>> internalGroupHandler = new Handler<Message<JsonObject>>() {
    @Override
    public void handle(Message<JsonObject> message) {
      if (!paused) {
        long id = message.body().getLong("id");
        if (checkID(id)) {
          String groupID = message.body().getString("group");
          DefaultInputGroup group = groups.get(groupID);
          if (group != null) {
            Object value = deserializer.deserialize(message.body());
            if (value != null) {
              group.handleMessage(value);
            }
          }
        }
      }
    }
  };

  private final Handler<Message<JsonObject>> internalEndHandler = new Handler<Message<JsonObject>>() {
    @Override
    public void handle(Message<JsonObject> message) {
      if (!paused) {
        long id = message.body().getLong("id");
        if (checkID(id)) {
          String groupID = message.body().getString("group");
          DefaultInputGroup group = groups.remove(groupID);
          if (group != null) {
            group.handleEnd();
          }
        }
      }
    }
  };

  private final Handler<Message<JsonObject>> internalMessageHandler = new Handler<Message<JsonObject>>() {
    @Override
    @SuppressWarnings("unchecked")
    public void handle(Message<JsonObject> message) {
      if (!paused) {
        long id = message.body().getLong("id");
        if (checkID(id)) {
          Object value = deserializer.deserialize(message.body());
          if (value != null && messageHandler != null) {
            messageHandler.handle(value);
          }
          for (InputHook hook : hooks) {
            hook.handleReceive(value);
          }
        }
      }
    }
  };

  private final Handler<Message<Boolean>> internalCloseHandler = new Handler<Message<Boolean>>() {
    @Override
    public void handle(Message<Boolean> message) {
      if (open) {
        groups.clear();
        message.reply(true);
      }
    }
  };

  public DefaultInputConnection(Vertx vertx, InputConnectionContext context) {
    this.vertx = vertx;
    this.eventBus = vertx.eventBus();
    this.context = context;
    this.hooks = context.hooks();
  }

  @Override
  public String address() {
    return context.address();
  }

  @Override
  public Vertx vertx() {
    return vertx;
  }

  @Override
  public InputConnectionContext context() {
    return context;
  }

  @Override
  public int size() {
    return 0;
  }

  @Override
  public InputConnection open() {
    return open(null);
  }

  @Override
  public InputConnection open(final Handler<AsyncResult<Void>> doneHandler) {
    final CountingCompletionHandler<Void> counter = new CountingCompletionHandler<Void>(4).setHandler(new Handler<AsyncResult<Void>>() {
      @Override
      public void handle(AsyncResult<Void> result) {
        if (result.failed()) {
          new DefaultFutureResult<Void>(result.cause()).setHandler(doneHandler);
        } else {
          open = true;
          new DefaultFutureResult<Void>((Void) null).setHandler(doneHandler);
        }
      }
    });
    vertx.eventBus().registerHandler(String.format("%s.open", context.address()), internalOpenHandler, counter);
    vertx.eventBus().registerHandler(String.format("%s.start", context.address()), internalStartHandler, counter);
    vertx.eventBus().registerHandler(String.format("%s.group", context.address()), internalGroupHandler, counter);
    vertx.eventBus().registerHandler(String.format("%s.end", context.address()), internalEndHandler, counter);
    vertx.eventBus().registerHandler(String.format("%s.close", context.address()), internalCloseHandler, counter);
    vertx.eventBus().registerHandler(context.address(), internalMessageHandler, counter);
    return this;
  }

  /**
   * Checks that the given ID is valid.
   */
  private boolean checkID(long id) {
    // Ensure that the given ID is a monotonically increasing ID.
    // If the ID is less than the last received ID then reset the
    // last received ID since the connection must have been reset.
    if (lastReceived == 0 || id == lastReceived + 1 || id < lastReceived) {
      lastReceived = id;
      // If the ID reaches the end of the current batch then tell the data
      // source that it's okay to remove all previous messages.
      if (lastReceived % BATCH_SIZE == 0 && open && feedbackAddress != null) {
        eventBus.send(feedbackAddress, new JsonObject().putString("action", "ack").putNumber("id", lastReceived));
      }
      return true;
    } else if (open && feedbackAddress != null) {
      eventBus.send(feedbackAddress, new JsonObject().putString("action", "fail").putNumber("id", lastReceived));
    }
    return false;
  }

  @Override
  public InputConnection pause() {
    paused = true;
    if (open && feedbackAddress != null) {
      eventBus.send(feedbackAddress, new JsonObject().putString("action", "pause").putNumber("id", lastReceived));
    }
    return this;
  }

  @Override
  public InputConnection resume() {
    paused = false;
    if (open && feedbackAddress != null) {
      eventBus.send(feedbackAddress, new JsonObject().putString("action", "resume").putNumber("id", lastReceived));
    }
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
  public void close(final Handler<AsyncResult<Void>> doneHandler) {
    final CountingCompletionHandler<Void> counter = new CountingCompletionHandler<Void>(4).setHandler(new Handler<AsyncResult<Void>>() {
      @Override
      public void handle(AsyncResult<Void> result) {
        open = false;
        doneHandler.handle(result);
      }
    });
    vertx.eventBus().unregisterHandler(String.format("%s.open", context.address()), internalOpenHandler, counter);
    vertx.eventBus().unregisterHandler(String.format("%s.start", context.address()), internalStartHandler, counter);
    vertx.eventBus().unregisterHandler(String.format("%s.group", context.address()), internalGroupHandler, counter);
    vertx.eventBus().unregisterHandler(String.format("%s.end", context.address()), internalEndHandler, counter);
    vertx.eventBus().unregisterHandler(String.format("%s.close", context.address()), internalCloseHandler, counter);
    vertx.eventBus().unregisterHandler(context.address(), internalMessageHandler, counter);
  }

}
