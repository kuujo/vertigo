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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.kuujo.vertigo.cluster.VertigoCluster;
import net.kuujo.vertigo.context.ConnectionContext;
import net.kuujo.vertigo.context.InputConnectionContext;
import net.kuujo.vertigo.context.InputPortContext;
import net.kuujo.vertigo.hooks.InputPortHook;
import net.kuujo.vertigo.input.InputConnection;
import net.kuujo.vertigo.input.InputPort;
import net.kuujo.vertigo.message.JsonMessage;
import net.kuujo.vertigo.message.impl.ReliableJsonMessage;
import net.kuujo.vertigo.util.CountingCompletionHandler;
import net.kuujo.vertigo.util.Observer;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.impl.DefaultFutureResult;

/**
 * Default input port implementation.
 *
 * @author Jordan Halterman
 */
public class DefaultInputPort implements InputPort, Observer<InputPortContext> {
  private final Vertx vertx;
  private InputPortContext context;
  private final VertigoCluster cluster;
  private final List<InputPortHook> hooks = new ArrayList<>();
  private final Map<String, InputConnection> connections = new HashMap<>();
  private Handler<ReliableJsonMessage> messageHandler;

  public DefaultInputPort(Vertx vertx, InputPortContext context, VertigoCluster cluster) {
    this.vertx = vertx;
    this.context = context;
    this.cluster = cluster;
  }

  DefaultInputPort setContext(InputPortContext context) {
    this.context = context;
    return this;
  }

  @Override
  public String name() {
    return context.name();
  }

  @Override
  public InputPortContext context() {
    return context;
  }

  @Override
  public InputPort addHook(InputPortHook hook) {
    hooks.add(hook);
    return this;
  }

  @Override
  public void update(InputPortContext update) {
    Iterator<Map.Entry<String, InputConnection>> iter = connections.entrySet().iterator();
    while (iter.hasNext()) {
      InputConnection connection = iter.next().getValue();
      boolean exists = false;
      for (InputConnectionContext input : update.connections()) {
        if (input.equals(connection.context())) {
          exists = true;
          break;
        }
      }
      if (!exists) {
        connection.close();
        iter.remove();
      }
    }

    for (InputConnectionContext input : update.connections()) {
      boolean exists = false;
      for (InputConnection connection : connections.values()) {
        if (connection.context().equals(input)) {
          exists = true;
          break;
        }
      }

      if (!exists) {
        InputConnection connection = null;

        // Basic at-most-once delivery.
        if (input.delivery().equals(ConnectionContext.Delivery.AT_MOST_ONCE)) {
          if (input.order().equals(ConnectionContext.Order.NO_ORDER)) {
            connection = new BasicInputConnection(vertx, input, cluster);
          } else if (input.order().equals(ConnectionContext.Order.STRONG_ORDER)) {
            connection = new OrderedInputConnection(vertx, input, cluster);
          }
        // Required at-least-once delivery.
        } else if (input.delivery().equals(ConnectionContext.Delivery.AT_LEAST_ONCE)) {
          if (input.order().equals(ConnectionContext.Order.NO_ORDER)) {
            connection = new AtLeastOnceInputConnection(vertx, input, cluster);
          } else if (input.order().equals(ConnectionContext.Order.STRONG_ORDER)) {
            connection = new OrderedAtLeastOnceInputConnection(vertx, input, cluster);
          }
        // Required exactly-once delivery.
        } else if (input.delivery().equals(ConnectionContext.Delivery.EXACTLY_ONCE)) {
          if (input.order().equals(ConnectionContext.Order.NO_ORDER)) {
            connection = new ExactlyOnceInputConnection(vertx, input, cluster);
          } else if (input.order().equals(ConnectionContext.Order.STRONG_ORDER)) {
            connection = new OrderedExactlyOnceInputConnection(vertx, input, cluster);
          }
        }

        if (connection != null) {
          connections.put(input.address(), connection.open());
        }
      }
    }
  }

  @Override
  public InputPort messageHandler(final Handler<JsonMessage> handler) {
    this.messageHandler = new Handler<ReliableJsonMessage>() {
      @Override
      public void handle(ReliableJsonMessage message) {
        handler.handle(message);
        for (InputPortHook hook : hooks) {
          hook.handleReceive(message.id());
        }
      }
    };
    for (InputConnection connection : connections.values()) {
      connection.messageHandler(messageHandler);
    }
    return this;
  }

  @Override
  public InputPort ack(JsonMessage message) {
    ((ReliableJsonMessage) message).ack();
    return this;
  }

  @Override
  public InputPort open() {
    return open(null);
  }

  @Override
  public InputPort open(Handler<AsyncResult<Void>> doneHandler) {
    if (connections.isEmpty()) {
      final CountingCompletionHandler<Void> startCounter = new CountingCompletionHandler<Void>(context.connections().size());
      startCounter.setHandler(doneHandler);

      for (InputConnectionContext connectionContext : context.connections()) {
        InputConnection connection = null;

        // Basic at-most-once delivery.
        if (connectionContext.delivery().equals(ConnectionContext.Delivery.AT_MOST_ONCE)) {
          if (connectionContext.order().equals(ConnectionContext.Order.NO_ORDER)) {
            connection = new BasicInputConnection(vertx, connectionContext, cluster);
          } else if (connectionContext.order().equals(ConnectionContext.Order.STRONG_ORDER)) {
            connection = new OrderedInputConnection(vertx, connectionContext, cluster);
          }
        // Required at-least-once delivery.
        } else if (connectionContext.delivery().equals(ConnectionContext.Delivery.AT_LEAST_ONCE)) {
          if (connectionContext.order().equals(ConnectionContext.Order.NO_ORDER)) {
            connection = new AtLeastOnceInputConnection(vertx, connectionContext, cluster);
          } else if (connectionContext.order().equals(ConnectionContext.Order.STRONG_ORDER)) {
            connection = new OrderedAtLeastOnceInputConnection(vertx, connectionContext, cluster);
          }
        // Required exactly-once delivery.
        } else if (connectionContext.delivery().equals(ConnectionContext.Delivery.EXACTLY_ONCE)) {
          if (connectionContext.order().equals(ConnectionContext.Order.NO_ORDER)) {
            connection = new ExactlyOnceInputConnection(vertx, connectionContext, cluster);
          } else if (connectionContext.order().equals(ConnectionContext.Order.STRONG_ORDER)) {
            connection = new OrderedExactlyOnceInputConnection(vertx, connectionContext, cluster);
          }
        }

        if (connection != null) {
          connection.messageHandler(messageHandler);
          connections.put(connectionContext.address(), connection.open(new Handler<AsyncResult<Void>>() {
            @Override
            public void handle(AsyncResult<Void> result) {
              if (result.failed()) {
                startCounter.fail(result.cause());
              } else {
                startCounter.succeed();
              }
            }
          }));
        } else {
          startCounter.succeed();
        }
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

    for (InputConnection connection : connections.values()) {
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
