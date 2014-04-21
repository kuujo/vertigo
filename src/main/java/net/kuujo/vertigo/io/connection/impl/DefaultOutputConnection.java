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
import java.util.TreeMap;
import java.util.UUID;

import net.kuujo.vertigo.context.OutputConnectionContext;
import net.kuujo.vertigo.hooks.OutputHook;
import net.kuujo.vertigo.io.OutputSerializer;
import net.kuujo.vertigo.io.connection.OutputConnection;
import net.kuujo.vertigo.io.group.OutputGroup;
import net.kuujo.vertigo.io.group.impl.ConnectionOutputGroup;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.eventbus.ReplyException;
import org.vertx.java.core.eventbus.ReplyFailure;
import org.vertx.java.core.impl.DefaultFutureResult;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

/**
 * Default output connection implementation.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class DefaultOutputConnection implements OutputConnection {
  private static final int DEFAULT_MAX_QUEUE_SIZE = 1000;
  private final Vertx vertx;
  private final EventBus eventBus;
  private final OutputConnectionContext context;
  private final String address;
  private final String feedbackAddress = UUID.randomUUID().toString();
  private final OutputSerializer serializer = new OutputSerializer();
  private List<OutputHook> hooks = new ArrayList<>();
  private int maxQueueSize = DEFAULT_MAX_QUEUE_SIZE;
  private Handler<Void> drainHandler;
  private long currentMessage = 1;
  private final TreeMap<Long, JsonObject> messages = new TreeMap<>();
  private final Map<String, ConnectionOutputGroup> groups = new HashMap<>();
  private boolean open;
  private boolean full;
  private boolean paused;

  private final Handler<Message<JsonObject>> internalFeedbackHandler = new Handler<Message<JsonObject>>() {
    @Override
    public void handle(Message<JsonObject> message) {
      String action = message.body().getString("action");
      long id = message.body().getLong("id");
      if (action != null) {
        switch (action) {
          case "ack":
            doAck(id);
            break;
          case "fail":
            doFail(id);
            break;
          case "pause":
            doPause(id);
            break;
          case "resume":
            doResume(id);
            break;
        }
      }
    }
  };

  public DefaultOutputConnection(Vertx vertx, OutputConnectionContext context) {
    this.vertx = vertx;
    this.eventBus = vertx.eventBus();
    this.context = context;
    this.hooks = context.hooks();
    this.address = context.address();
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
  public OutputConnectionContext context() {
    return context;
  }

  @Override
  public OutputConnection open() {
    return open(null);
  }

  @Override
  public OutputConnection open(final Handler<AsyncResult<Void>> doneHandler) {
    eventBus.registerHandler(feedbackAddress, internalFeedbackHandler, new Handler<AsyncResult<Void>>() {
      @Override
      public void handle(AsyncResult<Void> result) {
        if (result.failed()) {
          new DefaultFutureResult<Void>(result.cause()).setHandler(doneHandler);
        } else {
          doOpen(doneHandler);
        }
      }
    });
    return this;
  }

  /**
   * Connects to the other side of the connection.
   */
  private void doOpen(final Handler<AsyncResult<Void>> doneHandler) {
    eventBus.sendWithTimeout(String.format("%s.open", address), feedbackAddress, 5000, new Handler<AsyncResult<Message<Boolean>>>() {
      @Override
      public void handle(AsyncResult<Message<Boolean>> result) {
        if (result.failed()) {
          ReplyException failure = (ReplyException) result.cause();
          if (failure.failureType().equals(ReplyFailure.RECIPIENT_FAILURE)) {
            new DefaultFutureResult<Void>(failure).setHandler(doneHandler);
          } else {
            doOpen(doneHandler);
          }
        } else if (result.result().body()) {
          open = true;
          new DefaultFutureResult<Void>((Void) null).setHandler(doneHandler);
        } else {
          doOpen(doneHandler);
        }
      }
    });
  }

  @Override
  public OutputConnection setSendQueueMaxSize(int maxSize) {
    this.maxQueueSize = maxSize;
    return this;
  }

  @Override
  public int getSendQueueMaxSize() {
    return maxQueueSize;
  }

  @Override
  public int size() {
    return messages.size();
  }

  @Override
  public boolean sendQueueFull() {
    return paused || messages.size() >= maxQueueSize;
  }

  @Override
  public OutputConnection drainHandler(Handler<Void> handler) {
    this.drainHandler = handler;
    return this;
  }

  @Override
  public OutputConnection group(final String name, final Handler<OutputGroup> handler) {
    final ConnectionOutputGroup group = new ConnectionOutputGroup(UUID.randomUUID().toString(), name, vertx, this, serializer);

    // Set an end handler on the group that will remove it from the groups
    // list if the group is completed before any new groups with the same
    // name are created.
    group.endHandler(new Handler<Void>() {
      @Override
      public void handle(Void _) {
        groups.remove(name);
      }
    });

    // Get the last group that was created for this group name.
    final ConnectionOutputGroup lastGroup = groups.get(name);
    if (lastGroup != null) {
      // Override the default end handler for the group to start the following
      // group once the last group as completed. This guarantees ordering of
      // groups with the same name.
      lastGroup.endHandler(new Handler<Void>() {
        @Override
        public void handle(Void _) {
          groups.put(name, group);
          group.start(handler);
        }
      });
    } else {
      // If there is no previous group then start the group immediately.
      groups.put(name, group);
      group.start(handler);
    }
    return this;
  }

  @Override
  public void close() {
    close(null);
  }

  @Override
  public void close(final Handler<AsyncResult<Void>> doneHandler) {
    eventBus.unregisterHandler(feedbackAddress, internalFeedbackHandler, new Handler<AsyncResult<Void>>() {
      @Override
      public void handle(AsyncResult<Void> result) {
        if (result.failed()) {
          new DefaultFutureResult<Void>(result.cause()).setHandler(doneHandler);
        } else {
          doClose(doneHandler);
        }
      }
    });
  }

  /**
   * Disconnects from the other side of the connection.
   */
  private void doClose(final Handler<AsyncResult<Void>> doneHandler) {
    eventBus.sendWithTimeout(String.format("%s.close"), feedbackAddress, 5000, new Handler<AsyncResult<Message<Boolean>>>() {
      @Override
      public void handle(AsyncResult<Message<Boolean>> result) {
        if (result.failed()) {
          ReplyException failure = (ReplyException) result.cause();
          if (failure.failureType().equals(ReplyFailure.RECIPIENT_FAILURE)) {
            new DefaultFutureResult<Void>(failure).setHandler(doneHandler);
          } else {
            doClose(doneHandler);
          }
        } else if (result.result().body()) {
          open = false;
          new DefaultFutureResult<Void>((Void) null).setHandler(doneHandler);
        } else {
          doClose(doneHandler);
        }
      }
    });
  }

  /**
   * Checks whether the connection is open.
   */
  private void checkOpen() {
    if (!open) throw new IllegalStateException("Connection to " + address + " not open.");
  }

  /**
   * Checks whether the connection is full.
   */
  private void checkFull() {
    if (!full && messages.size() >= maxQueueSize) {
      full = true;
    }
  }

  /**
   * Checks whether the connection has been drained.
   */
  private void checkDrain() {
    if (full && !paused && messages.size() < maxQueueSize / 2) {
      full = false;
      if (drainHandler != null) {
        drainHandler.handle((Void) null);
      }
    }
  }

  /**
   * Handles a batch ack.
   */
  private void doAck(long id) {
    if (messages.containsKey(id+1)) {
      messages.tailMap(id+1);
    } else {
      messages.clear();
    }
    checkDrain();
  }

  /**
   * Handles a batch fail.
   */
  private void doFail(long id) {
    if (messages.containsKey(id+1)) {
      for (long i = id+1; i <= messages.lastKey(); i++) {
        eventBus.send(address, messages.get(i));
      }
    }
  }

  /**
   * Handles a connection pause.
   */
  private void doPause(long id) {
    paused = true;
  }

  /**
   * Handles a connection resume.
   */
  private void doResume(long id) {
    paused = false;
    checkDrain();
  }

  /**
   * Sends a message.
   */
  private OutputConnection doSend(final Object value) {
    doSend(address, serializer.serialize(value));
    for (OutputHook hook : hooks) {
      hook.handleSend(value);
    }
    return this;
  }

  public void doSend(String address, JsonObject message) {
    checkOpen();
    long id = currentMessage++;
    message.putNumber("id", id);
    messages.put(id, message);
    // Don't bother sending the message if the connection is paused.
    // The input connection will simply ignore the message anyways.
    // Just queue the message and wait for the connection to be resumed.
    if (!paused) {
      eventBus.send(address, message);
    }
    checkFull();
  }

  @Override
  public OutputConnection send(final Object message) {
    return doSend(message);
  }

  @Override
  public OutputConnection send(String message) {
    return doSend(message);
  }

  @Override
  public OutputConnection send(Boolean message) {
    return doSend(message);
  }

  @Override
  public OutputConnection send(Character message) {
    return doSend(message);
  }

  @Override
  public OutputConnection send(Short message) {
    return doSend(message);
  }

  @Override
  public OutputConnection send(Integer message) {
    return doSend(message);
  }

  @Override
  public OutputConnection send(Long message) {
    return doSend(message);
  }

  @Override
  public OutputConnection send(Double message) {
    return doSend(message);
  }

  @Override
  public OutputConnection send(Float message) {
    return doSend(message);
  }

  @Override
  public OutputConnection send(Buffer message) {
    return doSend(message);
  }

  @Override
  public OutputConnection send(JsonObject message) {
    return doSend(message);
  }

  @Override
  public OutputConnection send(JsonArray message) {
    return doSend(message);
  }

  @Override
  public OutputConnection send(Byte message) {
    return doSend(message);
  }

  @Override
  public OutputConnection send(byte[] message) {
    return doSend(message);
  }

}
