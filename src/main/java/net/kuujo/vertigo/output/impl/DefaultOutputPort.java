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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import net.kuujo.vertigo.context.OutputConnectionContext;
import net.kuujo.vertigo.context.OutputPortContext;
import net.kuujo.vertigo.hooks.OutputHook;
import net.kuujo.vertigo.message.JsonMessage;
import net.kuujo.vertigo.message.MessageId;
import net.kuujo.vertigo.message.impl.DefaultJsonMessage;
import net.kuujo.vertigo.message.impl.DefaultMessageId;
import net.kuujo.vertigo.network.auditor.Acker;
import net.kuujo.vertigo.output.OutputConnection;
import net.kuujo.vertigo.output.OutputPort;
import net.kuujo.vertigo.util.CountingCompletionHandler;
import net.kuujo.vertigo.util.RoundRobin;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.impl.DefaultFutureResult;
import org.vertx.java.core.json.JsonObject;

/**
 * Default output port implementation.
 *
 * @author Jordan Halterman
 */
public class DefaultOutputPort implements OutputPort {
  private final Vertx vertx;
  private final String address;
  private final OutputPortContext context;
  private final Acker acker;
  private final List<OutputConnection> connections = new ArrayList<>();
  private final Iterator<String> auditors;
  private final List<OutputHook> hooks = new ArrayList<>();
  private final Random random = new Random();
  private final Map<String, JsonMessage> messages = new HashMap<>();

  private final Handler<String> ackHandler = new Handler<String>() {
    @Override
    public void handle(String messageId) {
      hookAcked(messageId);
    }
  };

  private final Handler<String> failHandler = new Handler<String>() {
    @Override
    public void handle(String messageId) {
      hookFailed(messageId);
    }
  };

  private final Handler<String> timeoutHandler = new Handler<String>() {
    @Override
    public void handle(String messageId) {
      hookTimeout(messageId);
    }
  };

  public DefaultOutputPort(Vertx vertx, OutputPortContext context, Acker acker) {
    this.vertx = vertx;
    this.address = context.address();
    this.context = context;
    this.acker = acker;
    List<String> auditors = new ArrayList<>();
    for (String auditor : acker.auditors()) {
      auditors.add(auditor);
    }
    this.auditors = new RoundRobin<>(auditors).iterator();
    acker.ackHandler(ackHandler);
    acker.failHandler(failHandler);
    acker.timeoutHandler(timeoutHandler);
  }

  @Override
  public String name() {
    return context.name();
  }

  @Override
  public OutputPortContext context() {
    return context;
  }

  @Override
  public OutputPort addHook(OutputHook hook) {
    hooks.add(hook);
    return this;
  }

  /**
   * Calls acked hooks.
   */
  private void hookAcked(final String messageId) {
    JsonMessage message = messages.remove(messageId);
    if (message != null) {
      for (OutputHook hook : hooks) {
        hook.handleAcked(messageId);
      }
    }
  }

  /**
   * Calls failed hooks.
   */
  private void hookFailed(final String messageId) {
    JsonMessage message = messages.remove(messageId);
    if (message != null) {
      for (OutputHook hook : hooks) {
        hook.handleFailed(messageId);
      }
    }
  }

  /**
   * Calls timed-out hooks.
   */
  private void hookTimeout(final String messageId) {
    JsonMessage message = messages.remove(messageId);
    if (message != null) {
      for (OutputHook hook : hooks) {
        hook.handleTimeout(messageId);
      }
      doEmitNew(message.body());
    }
  }

  /**
   * Calls emit hooks.
   */
  private void hookEmit(final String messageId) {
    for (OutputHook hook : hooks) {
      hook.handleEmit(messageId);
    }
  }

  @Override
  public String emit(JsonObject body) {
    return doEmitNew(body);
  }

  @Override
  public String emit(JsonObject body, JsonMessage parent) {
    return doEmitChild(body, parent);
  }

  @Override
  public String emit(JsonMessage message) {
    return doEmitChild(message.body(), message);
  }

  /**
   * Emits a new message to an output stream.
   * New messages are tracked by calling fork() and create() on the local acker.
   * This will cause the acker to send a single message indicating the total
   * ack count for all emitted messages.
   */
  private String doEmitNew(JsonObject body) {
    if (!connections.isEmpty()) {
      final JsonMessage message = createNewMessage(body);
      final MessageId messageId = message.messageId();
      messages.put(messageId.correlationId(), message);
      acker.create(messageId, new Handler<AsyncResult<Void>>() {
        @Override
        public void handle(AsyncResult<Void> result) {
          if (result.succeeded()) {
            for (OutputConnection connection : connections) {
              acker.fork(messageId, connection.send(message));
            }
            acker.commit(messageId);
            hookEmit(messageId.correlationId());
          }
          else {
            hookTimeout(messageId.correlationId());
          }
        }
      });
      return messageId.correlationId();
    }
    return null;
  }

  /**
   * Emits a child message to an output stream.
   * Child messages are tracked by only calling fork() on the local acker.
   * Once the input collector calls ack() on the acker, the acker will notify
   * the auditor of the update on the ack count for the message tree.
   */
  private String doEmitChild(JsonObject body, JsonMessage parent) {
    if (!connections.isEmpty()) {
      JsonMessage message = createChildMessage(body, parent);
      MessageId messageId = message.messageId();
      for (OutputConnection connection : connections) {
        acker.fork(messageId, connection.send(message));
      }
      hookEmit(messageId.correlationId());
      return messageId.correlationId();
    }
    return null;
  }

  /**
   * Creates a new message.
   */
  private JsonMessage createNewMessage(JsonObject body) {
    JsonMessage message = DefaultJsonMessage.Builder.newBuilder()
        .setMessageId(DefaultMessageId.Builder.newBuilder()
            .setCorrelationId(new StringBuilder()
                .append(address)
                .append(":")
                .append(OutputCounter.incrementAndGet())
                .toString())
            .setAuditor(auditors.next())
            .setCode(random.nextInt())
            .build())
        .setBody(body)
        .build();
    return message;
  }

  /**
   * Creates a child message.
   */
  private JsonMessage createChildMessage(JsonObject body, JsonMessage parent) {
    MessageId parentId = parent.messageId();
    JsonMessage message = DefaultJsonMessage.Builder.newBuilder()
        .setMessageId(DefaultMessageId.Builder.newBuilder()
            .setCorrelationId(new StringBuilder()
                .append(address)
                .append(":")
                .append(OutputCounter.incrementAndGet())
                .toString())
            .setAuditor(parentId.auditor())
            .setCode(random.nextInt())
            .setTree(parentId.tree())
            .build())
        .setBody(body)
        .build();
    return message;
  }

  @Override
  public OutputPort open() {
    return open(null);
  }

  @Override
  public OutputPort open(final Handler<AsyncResult<Void>> doneHandler) {
    if (connections.isEmpty()) {
      final CountingCompletionHandler<Void> startCounter = new CountingCompletionHandler<Void>(context.connections().size());
      startCounter.setHandler(new Handler<AsyncResult<Void>>() {
        @Override
        public void handle(AsyncResult<Void> result) {
          if (result.failed()) {
            new DefaultFutureResult<Void>(result.cause()).setHandler(doneHandler);
          } else {
            new DefaultFutureResult<Void>((Void) null).setHandler(doneHandler);
          }
        }
      });

      for (OutputConnectionContext connection : context.connections()) {
        connections.add(new DefaultOutputConnection(vertx, connection).open(new Handler<AsyncResult<Void>>() {
          @Override
          public void handle(AsyncResult<Void> result) {
            if (result.failed()) {
              startCounter.fail(result.cause());
            } else {
              startCounter.succeed();
            }
          }
        }));
      }
    }
    return this;
  }

  @Override
  public void close() {
    close(null);
  }

  @Override
  public void close(final Handler<AsyncResult<Void>> doneHandler) {
    final CountingCompletionHandler<Void> stopCounter = new CountingCompletionHandler<Void>(connections.size());
    stopCounter.setHandler(new Handler<AsyncResult<Void>>() {
      @Override
      public void handle(AsyncResult<Void> result) {
        if (result.failed()) {
          new DefaultFutureResult<Void>(result.cause()).setHandler(doneHandler);
        } else {
          connections.clear();
          new DefaultFutureResult<Void>((Void) null).setHandler(doneHandler);
        }
      }
    });

    for (OutputConnection connection : connections) {
      connection.close(new Handler<AsyncResult<Void>>() {
        @Override
        public void handle(AsyncResult<Void> result) {
          if (result.failed()) {
            stopCounter.fail(result.cause());
          } else {
            stopCounter.succeed();
          }
        }
      });
    }
  }

}
