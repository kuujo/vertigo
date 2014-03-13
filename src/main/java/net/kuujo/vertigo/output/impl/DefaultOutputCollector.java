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

import net.kuujo.vertigo.auditor.Acker;
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
import net.kuujo.vertigo.util.RoundRobin;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.impl.DefaultFutureResult;
import org.vertx.java.core.json.JsonObject;

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
  private final Map<String, List<OutputStream>> streams = new HashMap<>();
  private final Acker acker;
  private final Iterator<String> auditors;
  private final Random random = new Random();
  private final String componentAddress;
  private final String instanceAddress;

  public DefaultOutputCollector(Vertx vertx, OutputContext context, Acker acker) {
    this.vertx = vertx;
    this.context = context;
    this.acker = acker;
    List<String> auditors = new ArrayList<>();
    for (String auditor : context.instance().component().network().auditors()) {
      auditors.add(auditor);
    }
    this.auditors = new RoundRobin<>(auditors).iterator();
    this.componentAddress = context.instance().component().address();
    this.instanceAddress = context.instance().address();
  }

  @Override
  public OutputContext context() {
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
  private void hookAcked(final String messageId) {
    for (OutputHook hook : hooks) {
      hook.handleAcked(messageId);
    }
  }

  /**
   * Calls failed hooks.
   */
  private void hookFailed(final String messageId) {
    for (OutputHook hook : hooks) {
      hook.handleFailed(messageId);
    }
  }

  /**
   * Calls timed-out hooks.
   */
  private void hookTimeout(final String messageId) {
    for (OutputHook hook : hooks) {
      hook.handleTimeout(messageId);
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

  /**
   * Calls stop hooks.
   */
  private void hookStop() {
    for (OutputHook hook : hooks) {
      hook.handleStop(this);
    }
  }

  @Override
  public OutputCollector ackHandler(Handler<String> handler) {
    acker.ackHandler(createAckHandler(handler));
    return this;
  }

  private Handler<String> createAckHandler(final Handler<String> handler) {
    return new Handler<String>() {
      @Override
      public void handle(String messageId) {
        handler.handle(messageId);
        hookAcked(messageId);
      }
    };
  }

  @Override
  public OutputCollector failHandler(Handler<String> handler) {
    acker.failHandler(createFailHandler(handler));
    return this;
  }

  private Handler<String> createFailHandler(final Handler<String> handler) {
    return new Handler<String>() {
      @Override
      public void handle(String messageId) {
        handler.handle(messageId);
        hookFailed(messageId);
      }
    };
  }

  @Override
  public OutputCollector timeoutHandler(Handler<String> handler) {
    acker.timeoutHandler(createTimeoutHandler(handler));
    return this;
  }

  private Handler<String> createTimeoutHandler(final Handler<String> handler) {
    return new Handler<String>() {
      @Override
      public void handle(String messageId) {
        handler.handle(messageId);
        hookTimeout(messageId);
      }
    };
  }

  @Override
  public String emit(JsonObject body) {
    return doEmitNew(DEFAULT_STREAM, body);
  }

  @Override
  public String emit(JsonObject body, JsonMessage parent) {
    return doEmitChild(DEFAULT_STREAM, body, parent);
  }

  @Override
  public String emit(JsonMessage message) {
    return doEmitChild(DEFAULT_STREAM, message.body(), message);
  }

  @Override
  public String emitTo(String stream, JsonObject body) {
    return doEmitNew(stream, body);
  }

  @Override
  public String emitTo(String stream, JsonObject body, JsonMessage parent) {
    return doEmitChild(stream, body, parent);
  }

  @Override
  public String emitTo(String stream, JsonMessage message) {
    return doEmitChild(stream, message.body(), message);
  }

  /**
   * Emits a new message to an output stream.
   * New messages are tracked by calling fork() and create() on the local acker.
   * This will cause the acker to send a single message indicating the total
   * ack count for all emitted messages.
   */
  private String doEmitNew(String stream, JsonObject body) {
    final List<OutputStream> outputs = streams.get(stream);
    if (outputs != null) {
      final JsonMessage message = createNewMessage(stream, body);
      final MessageId messageId = message.messageId();
      acker.create(messageId, new Handler<AsyncResult<Void>>() {
        @Override
        public void handle(AsyncResult<Void> result) {
          if (result.succeeded()) {
            for (OutputStream output : outputs) {
              acker.fork(messageId, output.emit(message));
            }
            acker.commit(messageId);
            hookEmit(messageId.correlationId());
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
  private String doEmitChild(String stream, JsonObject body, JsonMessage parent) {
    List<OutputStream> outputs = streams.get(stream);
    if (outputs != null) {
      JsonMessage message = createChildMessage(stream, body, parent);
      MessageId messageId = message.messageId();
      for (OutputStream output : outputs) {
        acker.fork(messageId, output.emit(message));
      }
      hookEmit(messageId.correlationId());
      return messageId.correlationId();
    }
    return null;
  }

  /**
   * Creates a new message.
   */
  private JsonMessage createNewMessage(String stream, JsonObject body) {
    JsonMessage message = DefaultJsonMessage.Builder.newBuilder()
        .setMessageId(DefaultMessageId.Builder.newBuilder()
            .setCorrelationId(new StringBuilder()
                .append(instanceAddress)
                .append(":")
                .append(OutputCounter.incrementAndGet())
                .toString())
            .setAuditor(auditors.next())
            .setCode(random.nextInt())
            .build())
        .setBody(body)
        .setSource(componentAddress)
        .setStream(stream)
        .build();
    return message;
  }

  /**
   * Creates a child message.
   */
  private JsonMessage createChildMessage(String stream, JsonObject body, JsonMessage parent) {
    MessageId parentId = parent.messageId();
    JsonMessage message = DefaultJsonMessage.Builder.newBuilder()
        .setMessageId(DefaultMessageId.Builder.newBuilder()
            .setCorrelationId(new StringBuilder()
                .append(instanceAddress)
                .append(":")
                .append(OutputCounter.incrementAndGet())
                .toString())
            .setAuditor(parentId.auditor())
            .setCode(random.nextInt())
            .setTree(parentId.tree())
            .build())
        .setBody(body)
        .setSource(componentAddress)
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
        OutputStream output = new DefaultOutputStream(vertx, stream, context.instance().address()).start(new Handler<AsyncResult<Void>>() {
          @Override
          public void handle(AsyncResult<Void> result) {
            if (result.failed()) {
              startCounter.fail(result.cause());
            }
            else {
              startCounter.succeed();
            }
          }
        });

        if (!streams.containsKey(stream.stream())) {
          streams.put(stream.stream(), new ArrayList<OutputStream>());
        }
        streams.get(stream.stream()).add(output);
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
    int size = 0;
    for (List<OutputStream> outputs : streams.values()) {
      size += outputs.size();
    }

    final CountingCompletionHandler<Void> stopCounter = new CountingCompletionHandler<Void>(size);
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

    for (List<OutputStream> outputs : streams.values()) {
      for (OutputStream output : outputs) {
        output.stop(new Handler<AsyncResult<Void>>() {
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

}
