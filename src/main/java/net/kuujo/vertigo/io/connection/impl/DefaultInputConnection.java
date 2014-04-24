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

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
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
  private final String inAddress;
  private final String outAddress;
  private List<InputHook> hooks = new ArrayList<>();
  private final Map<String, Handler<InputGroup>> groupHandlers = new HashMap<>();
  private final Map<String, ConnectionInputGroup> groups = new HashMap<>();
  private final InputDeserializer deserializer = new InputDeserializer();
  @SuppressWarnings("rawtypes")
  private Handler messageHandler;
  private long lastReceived;
  private long lastFeedbackTime;
  private long feedbackTimerID;
  private boolean open;
  private boolean connected;
  private boolean paused;

  private final Handler<Long> internalTimer = new Handler<Long>() {
    @Override
    public void handle(Long timerID) {
      // Ensure that feedback messages are sent at least every second or so.
      // This will ensure that feedback is still provided when output connections
      // are full, otherwise the feedback will never be triggered.
      long currentTime = System.currentTimeMillis();
      if (currentTime - lastFeedbackTime > 1000) {
        eventBus.send(outAddress, new JsonObject().putString("action", "ack").putNumber("id", lastReceived));
        lastFeedbackTime = currentTime;
      }
    }
  };

  private final Handler<Message<JsonObject>> internalMessageHandler = new Handler<Message<JsonObject>>() {
    @Override
    public void handle(Message<JsonObject> message) {
      if (open && !paused) {
        String action = message.body().getString("action");
        switch (action) {
          case "message":
            if (checkID(message.body().getLong("id"))) {
              doMessage(message.body());
            }
            break;
          case "start":
            if (checkID(message.body().getLong("id"))) {
              doGroupStart(message.body());
            }
            break;
          case "group":
            if (checkID(message.body().getLong("id"))) {
              doGroupMessage(message.body());
            }
            break;
          case "end":
            if (checkID(message.body().getLong("id"))) {
              doGroupEnd(message.body());
            }
            break;
          case "connect":
            doConnect(message);
            break;
          case "disconnect":
            doDisconnect(message);
            break;
        }
      }
    }
  };

  public DefaultInputConnection(Vertx vertx, InputConnectionContext context) {
    this.vertx = vertx;
    this.eventBus = vertx.eventBus();
    this.context = context;
    this.inAddress = String.format("%s.in", context.address());
    this.outAddress = String.format("%s.out", context.address());
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
    eventBus.registerHandler(inAddress, internalMessageHandler, new Handler<AsyncResult<Void>>() {
      @Override
      public void handle(AsyncResult<Void> result) {
        if (result.succeeded()) {
          if (feedbackTimerID == 0) {
            feedbackTimerID = vertx.setPeriodic(1000, internalTimer);
          }
          open = true;
        }
        doneHandler.handle(result);
      }
    });
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
      if (lastReceived % BATCH_SIZE == 0 && open && connected) {
        eventBus.send(outAddress, new JsonObject().putString("action", "ack").putNumber("id", lastReceived));
        lastFeedbackTime = System.currentTimeMillis();
      }
      return true;
    } else if (open && connected) {
      eventBus.send(outAddress, new JsonObject().putString("action", "fail").putNumber("id", lastReceived));
      lastFeedbackTime = System.currentTimeMillis();
    }
    return false;
  }

  @Override
  public InputConnection pause() {
    if (!paused) {
      paused = true;
      if (open && connected) {
        eventBus.send(outAddress, new JsonObject().putString("action", "pause").putNumber("id", lastReceived));
      }
    }
    return this;
  }

  @Override
  public InputConnection resume() {
    if (paused) {
      paused = false;
      if (open && connected) {
        eventBus.send(outAddress, new JsonObject().putString("action", "resume").putNumber("id", lastReceived));
      }
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

  /**
   * Handles receiving a message.
   */
  @SuppressWarnings("unchecked")
  private void doMessage(final JsonObject message) {
    Object value = deserializer.deserialize(message);
    if (value != null && messageHandler != null) {
      messageHandler.handle(value);
    }
    for (InputHook hook : hooks) {
      hook.handleReceive(value);
    }
  }

  /**
   * Handles a group start.
   */
  private void doGroupStart(final JsonObject message) {
    String groupID = message.getString("group");
    String name = message.getString("name");
    String parentId = message.getString("parent");
    ConnectionInputGroup group = new ConnectionInputGroup(groupID, name, this);
    groups.put(groupID, group);
    if (parentId != null) {
      ConnectionInputGroup parent = groups.get(parentId);
      if (parent != null) {
        parent.handleGroup(group);
      }
    } else {
      Handler<InputGroup> handler = groupHandlers.get(name);
      if (handler != null) {
        handler.handle(group);
      } else {
        groupReady(groupID);
      }
    }
    group.handleStart();
  }

  /**
   * Indicates that an input group is ready.
   */
  void groupReady(String group) {
    eventBus.send(outAddress, new JsonObject().putString("action", "start").putString("group", group));
  }

  /**
   * Handles a group message.
   */
  private void doGroupMessage(final JsonObject message) {
    String groupID = message.getString("group");
    ConnectionInputGroup group = groups.get(groupID);
    if (group != null) {
      Object value = deserializer.deserialize(message);
      if (value != null) {
        group.handleMessage(value);
      }
    }
  }

  /**
   * Handles a group end.
   */
  private void doGroupEnd(final JsonObject message) {
    String groupID = message.getString("group");
    ConnectionInputGroup group = groups.remove(groupID);
    if (group != null) {
      group.handleEnd();
    }
  }

  /**
   * Handles connect.
   */
  private void doConnect(final Message<JsonObject> message) {
    if (open) {
      if (!connected) {
        groups.clear();
        connected = true;
      }
      message.reply(true);
    } else {
      message.reply(false);
    }
  }

  /**
   * Handles disconnect.
   */
  private void doDisconnect(final Message<JsonObject> message) {
    if (open) {
      if (connected) {
        groups.clear();
        connected = false;
      }
      message.reply(true);
    } else {
      message.reply(false);
    }
  }

  @Override
  public void close() {
    close(null);
  }

  @Override
  public void close(final Handler<AsyncResult<Void>> doneHandler) {
    eventBus.unregisterHandler(inAddress, internalMessageHandler, new Handler<AsyncResult<Void>>() {
      @Override
      public void handle(AsyncResult<Void> result) {
        if (feedbackTimerID > 0) {
          vertx.cancelTimer(feedbackTimerID);
          feedbackTimerID = 0;
        }
        open = false;
        doneHandler.handle(result);
      }
    });
  }

}
