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

import net.kuujo.vertigo.hook.InputHook;
import net.kuujo.vertigo.io.batch.InputBatch;
import net.kuujo.vertigo.io.connection.InputConnection;
import net.kuujo.vertigo.io.connection.InputConnectionContext;
import net.kuujo.vertigo.io.group.InputGroup;
import net.kuujo.vertigo.io.impl.InputDeserializer;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.core.logging.impl.LoggerFactory;

/**
 * Default input connection implementation.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class DefaultInputConnection implements InputConnection {
  private static final long BATCH_SIZE = 1000;
  private static final long MAX_BATCH_TIME = 100;
  private final Logger log;
  private final Vertx vertx;
  private final EventBus eventBus;
  private final InputConnectionContext context;
  private final String inAddress;
  private final String outAddress;
  private List<InputHook> hooks = new ArrayList<>();
  private Handler<InputGroup> groupHandler;
  private final Map<String, Handler<InputGroup>> groupHandlers = new HashMap<>();
  private final Map<String, DefaultConnectionInputGroup> groups = new HashMap<>();
  private final InputDeserializer deserializer = new InputDeserializer();
  @SuppressWarnings("rawtypes")
  private Handler messageHandler;
  private Handler<InputBatch> batchHandler;
  private DefaultConnectionInputBatch currentBatch;
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
        ack();
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
          case "startGroup":
            if (checkID(message.body().getLong("id"))) {
              doGroupStart(message.body());
            }
            break;
          case "group":
            if (checkID(message.body().getLong("id"))) {
              doGroupMessage(message.body());
            }
            break;
          case "endGroup":
            if (checkID(message.body().getLong("id"))) {
              doGroupEnd(message.body());
            }
            break;
          case "startBatch":
            if (checkID(message.body().getLong("id"))) {
              doBatchStart(message.body());
            }
            break;
          case "batch":
            if (checkID(message.body().getLong("id"))) {
              doBatchMessage(message.body());
            }
            break;
          case "endBatch":
            if (checkID(message.body().getLong("id"))) {
              doBatchEnd(message.body());
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

  public DefaultInputConnection(Vertx vertx, String address) {
    this(vertx, DefaultInputConnectionContext.Builder.newBuilder().setAddress(address).build());
  }

  public DefaultInputConnection(Vertx vertx, InputConnectionContext context) {
    this.vertx = vertx;
    this.eventBus = vertx.eventBus();
    this.context = context;
    this.inAddress = String.format("%s.in", context.address());
    this.outAddress = String.format("%s.out", context.address());
    this.log = LoggerFactory.getLogger(String.format("%s-%s", DefaultInputConnection.class.getName(), context.address()));
    this.hooks = context.hooks();
  }

  @Override
  public String address() {
    return context.address();
  }

  @Override
  public InputConnectionContext context() {
    return context;
  }

  @Override
  public Vertx vertx() {
    return vertx;
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
          log.info(String.format("%s - Opened connection to %s", DefaultInputConnection.this, context.source()));
          if (feedbackTimerID == 0) {
            log.debug(String.format("%s - Starting periodic ack timer with max batch interval: %d", DefaultInputConnection.this, MAX_BATCH_TIME));
            feedbackTimerID = vertx.setPeriodic(MAX_BATCH_TIME, internalTimer);
          }
          open = true;
        } else {
          log.warn(String.format("%s - Failed to open connection to %s", DefaultInputConnection.this, context.source()));
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
      if (lastReceived % BATCH_SIZE == 0) {
        ack();
      }
      return true;
    } else {
      fail();
    }
    return false;
  }

  /**
   * Sends an ack message for the current received count.
   */
  private void ack() {
    // Send a message to the other side of the connection indicating the
    // last message that we received in order. This will allow it to
    // purge messages we've already received from its queue.
    if (open && connected) {
      if (log.isDebugEnabled()) {
        log.debug(String.format("%s - Acking messages up to: %d", this, lastReceived));
      }
      eventBus.send(outAddress, new JsonObject().putString("action", "ack").putNumber("id", lastReceived));
      lastFeedbackTime = System.currentTimeMillis();
    }
  }

  /**
   * Sends a fail message for the current received count.
   */
  private void fail() {
    // Send a "fail" message indicating the last message we received in order.
    // This will cause the other side of the connection to resend messages
    // in order from that point on.
    if (open && connected) {
      if (log.isDebugEnabled()) {
        log.debug(String.format("%s - Received a message out of order: %d", this, lastReceived));
      }
      eventBus.send(outAddress, new JsonObject().putString("action", "fail").putNumber("id", lastReceived));
      lastFeedbackTime = System.currentTimeMillis();
    }
  }

  @Override
  public InputConnection pause() {
    if (!paused) {
      paused = true;
      if (open && connected) {
        log.debug(String.format("%s - Pausing connection: %s", this, context.source()));
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
        log.debug(String.format("%s - Resuming connection: %s", this, context.source()));
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
  public InputConnection batchHandler(Handler<InputBatch> handler) {
    batchHandler = handler;
    return this;
  }

  @Override
  public InputConnection groupHandler(Handler<InputGroup> handler) {
    groupHandler = handler;
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
      if (log.isDebugEnabled()) {
        log.debug(String.format("%s - Received: Message[id=%d, value=%s]", this, message.getLong("id"), value));
      }
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
    Object args = deserializer.deserialize(message);
    DefaultConnectionInputGroup group = new DefaultConnectionInputGroup(groupID, name, this);
    groups.put(groupID, group);
    if (parentId != null) {
      if (log.isDebugEnabled()) {
        log.debug(String.format("%s - Group started: Group[name=%s, group=%s, parent=%s, args=%s]", this, name, groupID, parentId, args));
      }
      if (currentBatch != null && parentId.equals(currentBatch.id())) {
        currentBatch.handleGroup(group);
      } else {
        DefaultConnectionInputGroup parent = groups.get(parentId);
        if (parent != null) {
          parent.handleGroup(group);
        }
      }
    } else {
      if (log.isDebugEnabled()) {
        log.debug(String.format("%s - Group started: Group[name=%s, group=%s, args=%s]", this, name, groupID, args));
      }
      // First check for a named group handler. If a named group handler isn't
      // registered then trigger the arbitrary group handler if one is registered.
      Handler<InputGroup> handler = groupHandlers.get(name);
      if (handler != null) {
        handler.handle(group);
      } else if (groupHandler != null) {
        groupHandler.handle(group);
      } else {
        groupReady(groupID);
      }
    }
    group.handleStart(args);
  }

  /**
   * Indicates that an input group is ready.
   */
  void groupReady(String group) {
    if (log.isDebugEnabled()) {
      log.debug(String.format("%s - Group ready: Group[group=%s]", this, group));
    }
    eventBus.send(outAddress, new JsonObject().putString("action", "group").putString("group", group));
  }

  /**
   * Handles a group message.
   */
  private void doGroupMessage(final JsonObject message) {
    String groupID = message.getString("group");
    DefaultConnectionInputGroup group = groups.get(groupID);
    if (group != null) {
      Object value = deserializer.deserialize(message);
      if (value != null) {
        if (log.isDebugEnabled()) {
          log.debug(String.format("%s - Group received: Group[group=%s, id=%d, message=%s", this, groupID, message.getLong("id"), value));
        }
        group.handleMessage(value);
      }
    }
  }

  /**
   * Handles a group end.
   */
  private void doGroupEnd(final JsonObject message) {
    String groupID = message.getString("group");
    DefaultConnectionInputGroup group = groups.remove(groupID);
    if (group != null) {
      Object args = deserializer.deserialize(message);
      if (log.isDebugEnabled()) {
        log.debug(String.format("%s - Group ended: Group[group=%s, args=%s]", this, group.id(), args));
      }
      group.handleEnd(args);
    }
  }

  /**
   * Handles a batch start.
   */
  private void doBatchStart(final JsonObject message) {
    if (currentBatch != null) {
      currentBatch.handleEnd(null);
    }
    String batchID = message.getString("batch");
    Object args = deserializer.deserialize(message);
    if (log.isDebugEnabled()) {
      log.debug(String.format("%s - Batch started: Batch[batch=%s, args=%s]", this, batchID, args));
    }
    currentBatch = new DefaultConnectionInputBatch(batchID, this);
    if (batchHandler != null) {
      batchHandler.handle(currentBatch);
    }
    currentBatch.handleStart(args);
  }

  /**
   * Indicates that an input batch is ready.
   */
  void batchReady(String batch) {
    if (log.isDebugEnabled()) {
      log.debug(String.format("%s - Batch ready: Batch[batch=%s]", this, batch));
    }
    eventBus.send(outAddress, new JsonObject().putString("action", "batch").putString("batch", batch));
  }

  /**
   * Handles a batch message.
   */
  private void doBatchMessage(final JsonObject message) {
    String batchID = message.getString("batch");
    if (currentBatch != null && currentBatch.id().equals(batchID)) {
      Object value = deserializer.deserialize(message);
      if (value != null) {
        if (log.isDebugEnabled()) {
          log.debug(String.format("%s - Batch received: Batch[batch=%s, id=%d, message=%s]", this, batchID, message.getLong("id"), value));
        }
        currentBatch.handleMessage(value);
      }
    }
  }

  /**
   * Handles a batch end.
   */
  private void doBatchEnd(final JsonObject message) {
    if (currentBatch != null) {
      Object args = deserializer.deserialize(message);
      if (log.isDebugEnabled()) {
        log.debug(String.format("%s - Batch ended: Batch[batch=%s, args=%s]", this, currentBatch.id(), args));
      }
      currentBatch.handleEnd(args);
      currentBatch = null;
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
      log.debug(String.format("%s - Accepted connect request from %s", this, context.source()));
    } else {
      message.reply(false);
      log.debug(String.format("%s - Rejected connect request from %s, connection not open", this, context.source()));
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
      log.debug(String.format("%s - Accepted disconnect request from %s", this, context.source()));
    } else {
      message.reply(false);
      log.debug(String.format("%s - Rejected connect request from %s, connection not open", this, context.source()));
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
          log.debug(String.format("%s - Stopping periodic ack timer ", DefaultInputConnection.this, feedbackTimerID));
          vertx.cancelTimer(feedbackTimerID);
          feedbackTimerID = 0;
        }
        open = false;
        log.info(String.format("%s - Closed connection from %s", DefaultInputConnection.this, context.source()));
        doneHandler.handle(result);
      }
    });
  }

  @Override
  public String toString() {
    return context.toString();
  }

}
