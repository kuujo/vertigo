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
package net.kuujo.vertigo.output.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.kuujo.vertigo.context.OutputConnectionContext;
import net.kuujo.vertigo.eventbus.AdaptiveEventBus;
import net.kuujo.vertigo.eventbus.impl.WrappedAdaptiveEventBus;
import net.kuujo.vertigo.output.OutputConnection;
import net.kuujo.vertigo.output.OutputGroup;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.eventbus.ReplyException;
import org.vertx.java.core.eventbus.ReplyFailure;
import org.vertx.java.core.impl.DefaultFutureResult;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

/**
 * Default output connection implementation.
 *
 * @author Jordan Halterman
 */
public class DefaultOutputConnection implements OutputConnection {
  private static final int DEFAULT_MAX_QUEUE_SIZE = 1000;
  private final Vertx vertx;
  private final AdaptiveEventBus eventBus;
  private final OutputConnectionContext context;
  private final String address;
  final OutputSerializer serializer = new OutputSerializer();
  private int currentQueueSize;
  private int maxQueueSize = DEFAULT_MAX_QUEUE_SIZE;
  private Handler<Void> drainHandler;
  private final Map<String, ConnectionOutputGroup> groups = new HashMap<>();
  private boolean open;
  private boolean paused;

  public DefaultOutputConnection(Vertx vertx, OutputConnectionContext context) {
    this.vertx = vertx;
    this.eventBus = new WrappedAdaptiveEventBus(vertx);
    eventBus.setDefaultAdaptiveTimeout(5.0f);
    this.context = context;
    this.address = context.address();
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
    eventBus.sendWithAdaptiveTimeout(String.format("%s.open", address), true, 5, new Handler<AsyncResult<Message<Boolean>>>() {
      @Override
      public void handle(AsyncResult<Message<Boolean>> result) {
        if (result.failed()) {
          ReplyException failure = (ReplyException) result.cause();
          if (failure.failureType().equals(ReplyFailure.RECIPIENT_FAILURE)) {
            new DefaultFutureResult<Void>(failure).setHandler(doneHandler);
          } else {
            open(doneHandler);
          }
        } else {
          open = true;
          new DefaultFutureResult<Void>((Void) null).setHandler(doneHandler);
        }
      }
    });
    return this;
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
  public int getSendQueueSize() {
    return currentQueueSize;
  }

  @Override
  public boolean sendQueueFull() {
    return currentQueueSize >= maxQueueSize;
  }

  @Override
  public OutputConnection drainHandler(Handler<Void> handler) {
    this.drainHandler = handler;
    return this;
  }

  @Override
  public OutputConnection group(final String name, final Handler<OutputGroup> handler) {
    final ConnectionOutputGroup group = new ConnectionOutputGroup(UUID.randomUUID().toString(), name, vertx, this);

    // Get the last group that was created for this group name.
    ConnectionOutputGroup lastGroup = groups.get(name);
    if (lastGroup != null) {
      // Override the default end handler for the group to start the following
      // group once the last group as completed. This guarantees ordering of
      // groups with the same name.
      lastGroup.endHandler(new Handler<Void>() {
        @Override
        public void handle(Void _) {
          group.start(handler);
        }
      });
    } else {
      // If there is no previous group then start the group immediately.
      group.start(handler);
    }

    // Set an end handler on the group that will remove it from the groups
    // list if the group is completed before any new groups with the same
    // name are created.
    group.endHandler(new Handler<Void>() {
      @Override
      public void handle(Void _) {
        groups.remove(name);
      }      
    });

    // Add the group as the last created group.
    groups.put(name, group);
    return this;
  }

  @Override
  public void close() {
    close(null);
  }

  @Override
  public void close(final Handler<AsyncResult<Void>> doneHandler) {
    eventBus.sendWithAdaptiveTimeout(String.format("%s.close"), true, 5, new Handler<AsyncResult<Message<Boolean>>>() {
      @Override
      public void handle(AsyncResult<Message<Boolean>> result) {
        if (result.failed()) {
          ReplyException failure = (ReplyException) result.cause();
          if (failure.failureType().equals(ReplyFailure.RECIPIENT_FAILURE)) {
            new DefaultFutureResult<Void>(failure).setHandler(doneHandler);
          } else {
            close(doneHandler);
          }
        } else {
          open = false;
          new DefaultFutureResult<Void>((Void) null).setHandler(doneHandler);
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
   * Checks whether to pause the connection.
   */
  private void checkPause() {
    if (!paused && currentQueueSize >= maxQueueSize) {
      paused = true;
    }
  }

  /**
   * Checks whether the connection has been drained.
   */
  private void checkDrain() {
    if (paused && currentQueueSize < maxQueueSize / 2) {
      paused = false;
      if (drainHandler != null) {
        drainHandler.handle((Void) null);
      }
    }
  }

  /**
   * Sends a message.
   */
  private OutputConnection doSend(final Object value) {
    checkOpen();
    currentQueueSize++;
    final JsonObject message = serializer.serialize(value);
    eventBus.sendWithAdaptiveTimeout(address, message, 5, new Handler<AsyncResult<Message<Void>>>() {
      @Override
      public void handle(AsyncResult<Message<Void>> result) {
        currentQueueSize--;
        if (result.failed() && (!((ReplyException) result.cause()).failureType().equals(ReplyFailure.RECIPIENT_FAILURE))) {
          send(message);
        } else {
          checkDrain();
        }
      }
    });
    checkPause();
    return this;
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
