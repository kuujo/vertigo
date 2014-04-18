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

import net.kuujo.vertigo.eventbus.AdaptiveEventBus;
import net.kuujo.vertigo.eventbus.impl.WrappedAdaptiveEventBus;
import net.kuujo.vertigo.output.OutputGroup;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.eventbus.ReplyException;
import org.vertx.java.core.eventbus.ReplyFailure;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

/**
 * Connection output group implementation.<p>
 *
 * This is the output group that handles actually sending grouped messages on the
 * connection.
 *
 * @author Jordan Halterman
 */
public class ConnectionOutputGroup implements OutputGroup {
  private final String id;
  private final String parent;
  private final String name;
  private final Vertx vertx;
  private final DefaultOutputConnection connection;
  private final String address;
  private final AdaptiveEventBus eventBus;
  private Handler<Void> endHandler;
  private final Map<String, ConnectionOutputGroup> groups = new HashMap<>();
  private int sentCount;
  private int ackedCount;
  private int groupCount;
  private int completeCount;
  private boolean ended;

  public ConnectionOutputGroup(String id, String name, Vertx vertx, DefaultOutputConnection connection) {
    this.id = id;
    this.name = name;
    this.parent = null;
    this.vertx = vertx;
    this.connection = connection;
    this.address = connection.context().address();
    this.eventBus = new WrappedAdaptiveEventBus(vertx);
    eventBus.setDefaultAdaptiveTimeout(5.0f);
  }

  public ConnectionOutputGroup(String id, String name, String parent, Vertx vertx, DefaultOutputConnection connection) {
    this.id = id;
    this.name = name;
    this.parent = parent;
    this.vertx = vertx;
    this.connection = connection;
    this.address = connection.context().address();
    this.eventBus = new WrappedAdaptiveEventBus(vertx);
    eventBus.setDefaultAdaptiveTimeout(5.0f);
  }

  /**
   * Checks whether the group is complete.
   */
  private void checkEnd() {
    if (ended && ackedCount == sentCount && groupCount == completeCount) {
      eventBus.sendWithAdaptiveTimeout(String.format("%s.end", address), new JsonObject().putString("id", id), 5, new Handler<AsyncResult<Message<Void>>>() {
        @Override
        public void handle(AsyncResult<Message<Void>> result) {
          if (result.failed()) {
            checkEnd();
          } else if (endHandler != null) {
            endHandler.handle((Void) null);
          }
        }
      });
    }
  }

  ConnectionOutputGroup endHandler(Handler<Void> handler) {
    this.endHandler = handler;
    return this;
  }

  @Override
  public String name() {
    return name;
  }

  @Override
  public Vertx vertx() {
    return vertx;
  }

  /**
   * Starts the output group.
   */
  public OutputGroup start(final Handler<OutputGroup> doneHandler) {
    eventBus.sendWithAdaptiveTimeout(String.format("%s.start", address), new JsonObject().putString("id", id).putString("name", name).putString("parent", parent), 5, new Handler<AsyncResult<Message<Void>>>() {
      @Override
      public void handle(AsyncResult<Message<Void>> result) {
        if (result.failed()) {
          start(doneHandler);
        } else {
          doneHandler.handle(ConnectionOutputGroup.this);
        }
      }
    });
    return this;
  }

  @Override
  public OutputGroup setSendQueueMaxSize(int maxSize) {
    connection.setSendQueueMaxSize(maxSize);
    return this;
  }

  @Override
  public int getSendQueueMaxSize() {
    return connection.getSendQueueMaxSize();
  }

  @Override
  public int getSendQueueSize() {
    return connection.getSendQueueSize();
  }

  @Override
  public boolean sendQueueFull() {
    return (sentCount - ackedCount) + connection.getSendQueueSize() >= connection.getSendQueueMaxSize();
  }

  @Override
  public OutputGroup drainHandler(Handler<Void> handler) {
    connection.drainHandler(handler);
    return this;
  }

  @Override
  public OutputGroup group(final String name, final Handler<OutputGroup> handler) {
    final ConnectionOutputGroup group = new ConnectionOutputGroup(UUID.randomUUID().toString(), name, this.id, vertx, connection);
    groupCount++;

    // Get the last group for the given group name.
    ConnectionOutputGroup lastGroup = groups.get(name);
    if (lastGroup != null) {
      // Override the default group end handler with a handler that started the
      // next group once the previous group has completed. This guarantees ordering
      // between groups with the same name.
      lastGroup.endHandler(new Handler<Void>() {
        @Override
        public void handle(Void _) {
          completeCount++;
          group.start(handler);
        }
      });
    } else {
      // If there is no previous group then start the group immediately.
      group.start(handler);
    }

    // Set an end handler on the group that removes the group from the groups map.
    // If this group is not the last group in the groups set for the given group,
    // the end handler will be overridden. If this group is the last group, however,
    // once it's complete we need to check whether the parent group has completed.
    group.endHandler(new Handler<Void>() {
      @Override
      public void handle(Void _) {
        completeCount++;
        groups.remove(name);
        checkEnd();
      }      
    });

    // Set this as the last group in for the given group name.
    groups.put(name, group);
    return this;
  }

  /**
   * Sends a message.
   */
  private OutputGroup doSend(final Object value) {
    sentCount++;
    final JsonObject message = connection.serializer.serialize(value).putString("id", id);
    eventBus.sendWithAdaptiveTimeout(String.format("%s.group", address), message, 5, new Handler<AsyncResult<Message<Void>>>() {
      @Override
      public void handle(AsyncResult<Message<Void>> result) {
        if (result.failed() && (!((ReplyException) result.cause()).failureType().equals(ReplyFailure.RECIPIENT_FAILURE))) {
          send(message);
        } else {
          ackedCount++;
          checkEnd();
        }
      }
    });
    return this;
  }

  @Override
  public OutputGroup send(final Object message) {
    return doSend(message);
  }

  @Override
  public OutputGroup send(String message) {
    return doSend(message);
  }

  @Override
  public OutputGroup send(Boolean message) {
    return doSend(message);
  }

  @Override
  public OutputGroup send(Character message) {
    return doSend(message);
  }

  @Override
  public OutputGroup send(Short message) {
    return doSend(message);
  }

  @Override
  public OutputGroup send(Integer message) {
    return doSend(message);
  }

  @Override
  public OutputGroup send(Long message) {
    return doSend(message);
  }

  @Override
  public OutputGroup send(Double message) {
    return doSend(message);
  }

  @Override
  public OutputGroup send(Float message) {
    return doSend(message);
  }

  @Override
  public OutputGroup send(Buffer message) {
    return doSend(message);
  }

  @Override
  public OutputGroup send(JsonObject message) {
    return doSend(message);
  }

  @Override
  public OutputGroup send(JsonArray message) {
    return doSend(message);
  }

  @Override
  public OutputGroup send(Byte message) {
    return doSend(message);
  }

  @Override
  public OutputGroup send(byte[] message) {
    return doSend(message);
  }

  @Override
  public OutputGroup end() {
    ended = true;
    checkEnd();
    return this;
  }

}
