/*
 * Copyright 2013 the original author or authors.
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
package net.kuujo.vertigo.input;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.kuujo.vertigo.acker.Acker;
import net.kuujo.vertigo.acker.DefaultAcker;
import net.kuujo.vertigo.context.InstanceContext;
import net.kuujo.vertigo.hooks.InputHook;
import net.kuujo.vertigo.message.JsonMessage;
import net.kuujo.vertigo.message.MessageId;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Future;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.impl.DefaultFutureResult;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.platform.Container;

/**
 * A default input collector implementation.
 *
 * @author Jordan Halterman
 */
public class DefaultInputCollector implements InputCollector {
  private final Vertx vertx;
  private final Logger logger;
  private final InstanceContext context;
  private final List<InputHook> hooks = new ArrayList<InputHook>();
  private final Acker acker;
  private Handler<JsonMessage> messageHandler;
  private List<Listener> listeners;

  public DefaultInputCollector(Vertx vertx, Container container, InstanceContext context) {
    this.vertx = vertx;
    this.logger = container.logger();
    this.context = context;
    this.acker = new DefaultAcker(context.id(), vertx.eventBus());
  }

  public DefaultInputCollector(Vertx vertx, Container container, InstanceContext context, Acker acker) {
    this.vertx = vertx;
    this.logger = container.logger();
    this.context = context;
    this.acker = acker;
  }

  @Override
  public InputCollector addHook(InputHook hook) {
    hooks.add(hook);
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
  public InputCollector messageHandler(Handler<JsonMessage> handler) {
    this.messageHandler = wrapMessageHandler(handler);
    if (listeners != null) {
      for (Listener listener : listeners) {
        listener.messageHandler(messageHandler);
      }
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
  public InputCollector start() {
    final Future<Void> future = new DefaultFutureResult<Void>();
    future.setHandler(new Handler<AsyncResult<Void>>() {
      @Override
      public void handle(AsyncResult<Void> result) {
        if (result.failed()) {
          logger.error(result.cause());
        }
      }
    });

    stop(new Handler<AsyncResult<Void>>() {
      @Override
      public void handle(AsyncResult<Void> result) {
        listeners = new ArrayList<Listener>();
        recursiveStart(context.getComponent().getInputs().iterator(), future);
      }
    });
    return this;
  }

  @Override
  public InputCollector start(Handler<AsyncResult<Void>> doneHandler) {
    final Future<Void> future = new DefaultFutureResult<Void>().setHandler(doneHandler);
    stop(new Handler<AsyncResult<Void>>() {
      @Override
      public void handle(AsyncResult<Void> result) {
        listeners = new ArrayList<Listener>();
        recursiveStart(context.getComponent().getInputs().iterator(), future);
      }
    });
    return this;
  }

  /**
   * Recursively starts inputs.
   */
  private void recursiveStart(final Iterator<Input> inputs, final Future<Void> future) {
    if (inputs.hasNext()) {
      Input input = inputs.next();
      Listener listener = new DefaultListener(input, vertx).setAutoAck(false).messageHandler(messageHandler);
      listeners.add(listener);
      listener.start(new Handler<AsyncResult<Void>>() {
        @Override
        public void handle(AsyncResult<Void> result) {
          if (result.failed()) {
            future.setFailure(result.cause());
          }
          else {
            recursiveStart(inputs, future);
          }
        }
      });
    }
    else {
      future.setResult(null);
      hookStart();
    }
  }

  @Override
  public void stop() {
    if (listeners != null) {
      final Future<Void> future = new DefaultFutureResult<Void>();
      future.setHandler(new Handler<AsyncResult<Void>>() {
        @Override
        public void handle(AsyncResult<Void> result) {
          if (result.failed()) {
            logger.error(result.cause());
          }
          listeners = null;
          for (InputHook hook : hooks) {
            hook.handleStop(DefaultInputCollector.this);
          }
        }
      });
      recursiveStop(listeners.iterator(), future);
    }
  }

  @Override
  public void stop(final Handler<AsyncResult<Void>> doneHandler) {
    final Future<Void> future = new DefaultFutureResult<Void>();
    future.setHandler(new Handler<AsyncResult<Void>>() {
      @Override
      public void handle(AsyncResult<Void> result) {
        listeners = null;
        if (result.failed()) {
          new DefaultFutureResult<Void>().setHandler(doneHandler).setFailure(result.cause());
        }
        else {
          new DefaultFutureResult<Void>().setHandler(doneHandler).setResult(null);
        }
      }
    });
    if (listeners != null) {
      recursiveStop(listeners.iterator(), future);
    }
    else {
      future.setResult(null);
    }
  }

  /**
   * Recursively stops listeners.
   */
  private void recursiveStop(final Iterator<Listener> listeners, final Future<Void> future) {
    if (listeners.hasNext()) {
      Listener listener = listeners.next();
      listener.stop(new Handler<AsyncResult<Void>>() {
        @Override
        public void handle(AsyncResult<Void> result) {
          if (result.failed()) {
            future.setFailure(result.cause());
          }
          else {
            recursiveStop(listeners, future);
          }
        }
      });
    }
    else {
      future.setResult(null);
      hookStop();
    }
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

}
