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

import java.util.UUID;

import net.kuujo.vertigo.context.OutputConnectionContext;
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
import org.vertx.java.core.impl.DefaultFutureResult;
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
  final String id;
  final String name;
  private final Vertx vertx;
  private final OutputConnectionContext context;
  private final String address;
  private final AdaptiveEventBus eventBus;
  private Handler<Void> endHandler;
  private ConnectionOutputGroup lastGroup;
  private int sentCount;
  private int ackedCount;
  private boolean childrenComplete = true;
  private boolean ended;

  public ConnectionOutputGroup(String id, String name, Vertx vertx, OutputConnectionContext context) {
    this.id = id;
    this.name = name;
    this.vertx = vertx;
    this.context = context;
    this.address = context.address();
    this.eventBus = new WrappedAdaptiveEventBus(vertx);
    eventBus.setDefaultAdaptiveTimeout(5.0f);
  }

  /**
   * Checks whether the group is complete.
   */
  private void checkEnd() {
    if (ended && ackedCount == sentCount && childrenComplete) {
      if (endHandler != null) {
        endHandler.handle((Void) null);
      }
    }
  }

  ConnectionOutputGroup endHandler(Handler<Void> handler) {
    this.endHandler = handler;
    return this;
  }

  /**
   * Returns the unique connection group ID.
   *
   * @return The unique group ID.
   */
  public String id() {
    return id;
  }

  @Override
  public String name() {
    return name;
  }

  @Override
  public OutputGroup group(String name, final Handler<AsyncResult<OutputGroup>> handler) {
    final ConnectionOutputGroup group = new ConnectionOutputGroup(UUID.randomUUID().toString(), name, vertx, context);
    childrenComplete = false;
    group.endHandler(new Handler<Void>() {
      @Override
      public void handle(Void _) {
        childrenComplete = true;
      }
    });
    if (lastGroup != null) {
      lastGroup.endHandler(new Handler<Void>() {
        @Override
        public void handle(Void _) {
          doStartGroup(group, handler);
        }
      });
    } else {
      doStartGroup(group, handler);
    }
    lastGroup = group;
    return this;
  }

  /**
   * Starts a new output group.
   */
  private void doStartGroup(final ConnectionOutputGroup group, final Handler<AsyncResult<OutputGroup>> handler) {
    eventBus.sendWithTimeout(String.format("%s.start", address), new JsonObject().putString("id", group.id).putString("name", group.name).putString("parent", id), 30000, new Handler<AsyncResult<Message<Void>>>() {
      @Override
      public void handle(AsyncResult<Message<Void>> result) {
        if (result.failed()) {
          doStartGroup(group, handler);
        } else {
          new DefaultFutureResult<OutputGroup>(group).setHandler(handler);
        }
      }
    });
  }

  /**
   * Sends a message.
   */
  private OutputGroup doSend(final Object message) {
    sentCount++;
    eventBus.sendWithTimeout(String.format("%s.group", address), new JsonObject().putString("id", id).putValue("message", message), 30000, new Handler<AsyncResult<Message<Void>>>() {
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
    eventBus.sendWithTimeout(String.format("%s.end", address), new JsonObject().putString("id", id), 30000, new Handler<AsyncResult<Message<Void>>>() {
      @Override
      public void handle(AsyncResult<Message<Void>> result) {
        if (result.failed()) {
          end();
        } else {
          ended = true;
          checkEnd();
        }
      }
    });
    return this;
  }

}
