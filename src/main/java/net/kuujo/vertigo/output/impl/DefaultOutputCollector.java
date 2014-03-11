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
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.impl.DefaultFutureResult;
import org.vertx.java.core.json.JsonObject;

import net.kuujo.vertigo.acker.Acker;
import net.kuujo.vertigo.context.OutputContext;
import net.kuujo.vertigo.context.OutputStreamContext;
import net.kuujo.vertigo.hooks.OutputHook;
import net.kuujo.vertigo.message.JsonMessage;
import net.kuujo.vertigo.message.MessageId;
import net.kuujo.vertigo.message.impl.DefaultJsonMessage;
import net.kuujo.vertigo.message.impl.DefaultMessageId;
import net.kuujo.vertigo.output.OutputCollector;
import net.kuujo.vertigo.output.OutputStream;
import net.kuujo.vertigo.util.CountingCompletionHandler;

/**
 * Default output collector implementation.
 *
 * @author Jordan Halterman
 */
public class DefaultOutputCollector implements OutputCollector {
  private static final String DEFAULT_STREAM = "default";
  private final Vertx vertx;
  private final OutputContext context;
  private final List<OutputHook> hooks = new ArrayList<>();
  private final List<OutputStream> streams = new ArrayList<>();
  private final Acker acker;
  private final Random random = new Random();
  private List<String> auditors;

  public DefaultOutputCollector(Vertx vertx, OutputContext context, Acker acker) {
    this.vertx = vertx;
    this.context = context;
    this.acker = acker;
    auditors = new ArrayList<>();
    for (String auditor : context.instance().component().network().auditors()) {
      auditors.add(auditor);
    }
  }

  @Override
  public OutputContext output() {
    return context;
  }

  @Override
  public OutputCollector addHook(OutputHook hook) {
    hooks.add(hook);
    return this;
  }

  /**
   * Calls start hooks.
   */
  private void hookStart() {
    for (OutputHook hook : hooks) {
      hook.handleStart(this);
    }
  }

  /**
   * Calls acked hooks.
   */
  private void hookAcked(final MessageId messageId) {
    for (OutputHook hook : hooks) {
      hook.handleAcked(messageId);
    }
  }

  /**
   * Calls failed hooks.
   */
  private void hookFailed(final MessageId messageId) {
    for (OutputHook hook : hooks) {
      hook.handleFailed(messageId);
    }
  }

  /**
   * Calls timed-out hooks.
   */
  private void hookTimeout(final MessageId messageId) {
    for (OutputHook hook : hooks) {
      hook.handleTimeout(messageId);
    }
  }

  /**
   * Calls emit hooks.
   */
  private void hookEmit(final MessageId messageId) {
    for (OutputHook hook : hooks) {
      hook.handleEmit(messageId);
    }
  }

  /**
   * Calls stop hooks.
   */
  private void hookStop() {
    for (OutputHook hook : hooks) {
      hook.handleStop(this);
    }
  }

  @Override
  public OutputCollector ackHandler(Handler<MessageId> handler) {
    acker.ackHandler(createAckHandler(handler));
    return this;
  }

  private Handler<MessageId> createAckHandler(final Handler<MessageId> handler) {
    return new Handler<MessageId>() {
      @Override
      public void handle(MessageId messageId) {
        handler.handle(messageId);
        hookAcked(messageId);
      }
    };
  }

  @Override
  public OutputCollector failHandler(Handler<MessageId> handler) {
    acker.failHandler(createFailHandler(handler));
    return this;
  }

  private Handler<MessageId> createFailHandler(final Handler<MessageId> handler) {
    return new Handler<MessageId>() {
      @Override
      public void handle(MessageId messageId) {
        handler.handle(messageId);
        hookFailed(messageId);
      }
    };
  }

  @Override
  public OutputCollector timeoutHandler(Handler<MessageId> handler) {
    acker.timeoutHandler(createTimeoutHandler(handler));
    return this;
  }

  private Handler<MessageId> createTimeoutHandler(final Handler<MessageId> handler) {
    return new Handler<MessageId>() {
      @Override
      public void handle(MessageId messageId) {
        handler.handle(messageId);
        hookTimeout(messageId);
      }
    };
  }

  @Override
  public MessageId emit(JsonObject body) {
    return emitTo(DEFAULT_STREAM, body);
  }

  @Override
  public MessageId emit(JsonObject body, JsonMessage parent) {
    return emitTo(DEFAULT_STREAM, body, parent);
  }

  @Override
  public MessageId emit(JsonMessage message) {
    return emitTo(DEFAULT_STREAM, message);
  }

  @Override
  public MessageId emitTo(String stream, JsonObject body) {
    return doEmitTo(stream, createNewMessage(stream, body));
  }

  @Override
  public MessageId emitTo(String stream, JsonObject body, JsonMessage parent) {
    return doEmitTo(stream, createChildMessage(stream, body, parent));
  }

  @Override
  public MessageId emitTo(String stream, JsonMessage message) {
    return doEmitTo(stream, createChildMessage(stream, message.body(), message));
  }

  private MessageId doEmitTo(String stream, JsonMessage message) {
    JsonMessage child = createChildMessage(stream, message.body(), message);
    for (OutputStream output : streams) {
      if (output.context().stream().equals(stream)) {
        acker.fork(message.messageId(), output.emit(child));
      }
    }
    acker.create(message.messageId());
    hookEmit(message.messageId());
    return message.messageId();
  }

  /**
   * Creates a new message.
   */
  private JsonMessage createNewMessage(String stream, JsonObject body) {
    String auditor = auditors.get(random.nextInt(auditors.size()));
    MessageId messageId = DefaultMessageId.Builder.newBuilder()
        .setCorrelationId(UUID.randomUUID().toString())
        .setAuditor(auditor)
        .setCode(random.nextInt())
        .setOwner(context.instance().address())
        .build();
    JsonMessage message = DefaultJsonMessage.Builder.newBuilder()
        .setMessageId(messageId)
        .setBody(body)
        .setSource(context.instance().component().address())
        .setStream(stream)
        .build();
    return message;
  }

  /**
   * Creates a child message.
   */
  private JsonMessage createChildMessage(String stream, JsonObject body, JsonMessage parent) {
    MessageId messageId = DefaultMessageId.Builder.newBuilder()
        .setCorrelationId(UUID.randomUUID().toString())
        .setAuditor(parent.messageId().auditor())
        .setCode(random.nextInt())
        .setOwner(parent.messageId().owner())
        .setParent(parent.messageId().correlationId())
        .setRoot(parent.messageId().hasRoot() ? parent.messageId().root() : parent.messageId().correlationId())
        .build();
    JsonMessage message = DefaultJsonMessage.Builder.newBuilder()
        .setMessageId(messageId)
        .setBody(body)
        .setSource(context.instance().component().address())
        .setStream(stream)
        .build();
    return message;
  }

  @Override
  public OutputCollector start() {
    return start(null);
  }

  @Override
  public OutputCollector start(final Handler<AsyncResult<Void>> doneHandler) {
    if (streams.isEmpty()) {
      final CountingCompletionHandler<Void> startCounter = new CountingCompletionHandler<Void>(context.streams().size());
      startCounter.setHandler(new Handler<AsyncResult<Void>>() {
        @Override
        public void handle(AsyncResult<Void> result) {
          if (result.failed()) {
            new DefaultFutureResult<Void>(result.cause()).setHandler(doneHandler);
          }
          else {
            hookStart();
            new DefaultFutureResult<Void>((Void) null).setHandler(doneHandler);
          }
        }
      });

      for (OutputStreamContext stream : context.streams()) {
        streams.add(new DefaultOutputStream(vertx, stream).start(new Handler<AsyncResult<Void>>() {
          @Override
          public void handle(AsyncResult<Void> result) {
            if (result.failed()) {
              startCounter.fail(result.cause());
            }
            else {
              startCounter.succeed();
            }
          }
        }));
      }
    }
    return this;
  }

  @Override
  public void stop() {
    stop(null);
  }

  @Override
  public void stop(final Handler<AsyncResult<Void>> doneHandler) {
    final CountingCompletionHandler<Void> stopCounter = new CountingCompletionHandler<Void>(streams.size());
    stopCounter.setHandler(new Handler<AsyncResult<Void>>() {
      @Override
      public void handle(AsyncResult<Void> result) {
        if (result.failed()) {
          new DefaultFutureResult<Void>(result.cause()).setHandler(doneHandler);
        }
        else {
          hookStop();
          streams.clear();
          new DefaultFutureResult<Void>((Void) null).setHandler(doneHandler);
        }
      }
    });

    for (OutputStream stream : streams) {
      stream.stop(new Handler<AsyncResult<Void>>() {
        @Override
        public void handle(AsyncResult<Void> result) {
          if (result.failed()) {
            stopCounter.fail(result.cause());
          }
          else {
            stopCounter.succeed();
          }
        }
      });
    }
  }

}
