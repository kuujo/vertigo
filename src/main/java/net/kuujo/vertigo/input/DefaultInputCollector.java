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

import net.kuujo.vertigo.context.InstanceContext;
import net.kuujo.vertigo.messaging.JsonMessage;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Future;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.impl.DefaultFutureResult;
import org.vertx.java.core.json.JsonObject;
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
  private final EventBus eventBus;
  private final InstanceContext context;
  private Handler<JsonMessage> messageHandler;
  private List<Listener> listeners;

  public DefaultInputCollector(Vertx vertx, Container container, InstanceContext context) {
    this(vertx, container, vertx.eventBus(), context);
  }

  public DefaultInputCollector(Vertx vertx, Container container, EventBus eventBus, InstanceContext context) {
    this.vertx = vertx;
    this.logger = container.logger();
    this.eventBus = eventBus;
    this.context = context;
  }

  @Override
  public InputCollector messageHandler(Handler<JsonMessage> handler) {
    this.messageHandler = handler;
    if (listeners != null) {
      for (Listener listener : listeners) {
        listener.messageHandler(messageHandler);
      }
    }
    return this;
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
    }
  }

  @Override
  public InputCollector ack(JsonMessage message) {
    String auditor = message.auditor();
    if (auditor != null) {
      eventBus.send(auditor, new JsonObject().putString("action", "ack").putString("id", message.id()));
    }
    return this;
  }

  @Override
  public InputCollector fail(JsonMessage message) {
    String auditor = message.auditor();
    if (auditor != null) {
      eventBus.send(auditor, new JsonObject().putString("action", "fail").putString("id", message.id()));
    }
    return this;
  }

}
