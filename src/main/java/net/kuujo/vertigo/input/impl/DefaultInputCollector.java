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
package net.kuujo.vertigo.input.impl;

import java.util.ArrayList;
import java.util.List;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.impl.DefaultFutureResult;

import net.kuujo.vertigo.acker.Acker;
import net.kuujo.vertigo.context.InputContext;
import net.kuujo.vertigo.context.InputStreamContext;
import net.kuujo.vertigo.hooks.InputHook;
import net.kuujo.vertigo.input.InputCollector;
import net.kuujo.vertigo.input.InputStream;
import net.kuujo.vertigo.message.JsonMessage;
import net.kuujo.vertigo.message.MessageId;
import net.kuujo.vertigo.util.CountingCompletionHandler;

/**
 * Default input collector implementation.
 *
 * @author Jordan Halterman
 */
public class DefaultInputCollector implements InputCollector {
  private final Vertx vertx;
  private final InputContext context;
  private final Acker acker;
  private final List<InputHook> hooks = new ArrayList<InputHook>();
  private final List<InputStream> streams = new ArrayList<>();
  private Handler<JsonMessage> messageHandler;

  public DefaultInputCollector(Vertx vertx, InputContext context, Acker acker) {
    this.vertx = vertx;
    this.context = context;
    this.acker = acker;
  }

  @Override
  public InputContext context() {
    return context;
  }

  @Override
  public InputCollector addHook(InputHook hook) {
    hooks.add(hook);
    return this;
  }

  @Override
  public InputCollector messageHandler(Handler<JsonMessage> handler) {
    messageHandler = handler != null ? wrapMessageHandler(handler) : null;
    for (InputStream stream : streams) {
      stream.messageHandler(messageHandler);
    }
    return this;
  }

  private Handler<JsonMessage> wrapMessageHandler(final Handler<JsonMessage> handler) {
    return new Handler<JsonMessage>() {
      @Override
      public void handle(JsonMessage message) {
        handler.handle(message);
        hookReceived(message.messageId());
      }
    };
  }

  @Override
  public InputCollector ack(JsonMessage message) {
    acker.ack(message.messageId());
    hookAck(message.messageId());
    return this;
  }

  @Override
  public InputCollector fail(JsonMessage message) {
    acker.fail(message.messageId());
    hookFail(message.messageId());
    return this;
  }

  /**
   * Calls start hooks.
   */
  private void hookStart() {
    for (InputHook hook : hooks) {
      hook.handleStart(this);
    }
  }

  /**
   * Calls receive hooks.
   */
  private void hookReceived(final MessageId messageId) {
    for (InputHook hook : hooks) {
      hook.handleReceive(messageId);
    }
  }

  /**
   * Calls ack hooks.
   */
  private void hookAck(final MessageId messageId) {
    for (InputHook hook : hooks) {
      hook.handleAck(messageId);
    }
  }

  /**
   * Calls fail hooks.
   */
  private void hookFail(final MessageId messageId) {
    for (InputHook hook : hooks) {
      hook.handleFail(messageId);
    }
  }

  /**
   * Calls stop hooks.
   */
  private void hookStop() {
    for (InputHook hook : hooks) {
      hook.handleStart(this);
    }
  }

  @Override
  public InputCollector start() {
    return start(null);
  }

  @Override
  public InputCollector start(final Handler<AsyncResult<Void>> doneHandler) {
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

      for (InputStreamContext stream : context.streams()) {
        streams.add(new DefaultInputStream(vertx, stream).start(new Handler<AsyncResult<Void>>() {
          @Override
          public void handle(AsyncResult<Void> result) {
            if (result.failed()) {
              startCounter.fail(result.cause());
            }
            else {
              startCounter.succeed();
            }
          }
        }).messageHandler(messageHandler));
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

    for (InputStream stream : streams) {
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
