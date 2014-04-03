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
import java.util.Iterator;
import java.util.List;

import net.kuujo.vertigo.cluster.VertigoCluster;
import net.kuujo.vertigo.context.ConnectionContext;
import net.kuujo.vertigo.context.OutputConnectionContext;
import net.kuujo.vertigo.context.OutputPortContext;
import net.kuujo.vertigo.message.JsonMessage;
import net.kuujo.vertigo.message.MessageAcker;
import net.kuujo.vertigo.message.impl.DefaultJsonMessage;
import net.kuujo.vertigo.output.OutputConnection;
import net.kuujo.vertigo.output.OutputPort;
import net.kuujo.vertigo.util.CountingCompletionHandler;
import net.kuujo.vertigo.util.Observer;

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
public class DefaultOutputPort implements OutputPort, Observer<OutputPortContext> {
  private final Vertx vertx;
  private OutputPortContext context;
  private final VertigoCluster cluster;
  private final MessageAcker acker;
  private int maxQueueSize = 10000;
  private boolean full;
  private Handler<Void> drainHandler;
  private final List<OutputConnection> connections = new ArrayList<>();

  private final Handler<Void> connectionFullHandler = new Handler<Void>() {
    @Override
    public void handle(Void _) {
      full = true;
    }
  };

  private final Handler<Void> connectionDrainHandler = new Handler<Void>() {
    @Override
    public void handle(Void _) {
      full = false;
      if (drainHandler != null) {
        drainHandler.handle((Void) null);
      }
    }
  };

  public DefaultOutputPort(Vertx vertx, OutputPortContext context, VertigoCluster cluster, MessageAcker acker) {
    this.vertx = vertx;
    this.context = context;
    this.cluster = cluster;
    this.acker = acker;
  }

  @Override
  public String name() {
    return context.name();
  }

  @Override
  public OutputPortContext context() {
    return context;
  }

  DefaultOutputPort setContext(OutputPortContext context) {
    this.context = context;
    return this;
  }

  @Override
  public OutputPort setSendQueueMaxSize(int maxSize) {
    this.maxQueueSize = maxSize;
    for (OutputConnection connection : connections) {
      connection.setSendQueueMaxSize(maxSize);
    }
    return this;
  }

  @Override
  public boolean sendQueueFull() {
    return full;
  }

  @Override
  public OutputPort drainHandler(Handler<Void> handler) {
    this.drainHandler = handler;
    return this;
  }

  @Override
  public String emit(JsonObject body) {
    final JsonMessage message = createNewMessage(body);
    for (OutputConnection connection : connections) {
      connection.send(createChildMessage(body, message));
    }
    return message.id();
  }

  @Override
  public String emit(JsonObject body, Handler<AsyncResult<Void>> doneHandler) {
    final JsonMessage message = createNewMessage(body);
    final CountingCompletionHandler<Void> counter = new CountingCompletionHandler<Void>(connections.size());
    counter.setHandler(doneHandler);
    for (OutputConnection connection : connections) {
      connection.send(createChildMessage(body, message), new Handler<AsyncResult<Void>>() {
        @Override
        public void handle(AsyncResult<Void> result) {
          if (result.failed()) {
            counter.fail(result.cause());
          } else {
            counter.succeed();
          }
        }
      });
    }
    return message.id();
  }

  @Override
  public String emit(JsonObject body, JsonMessage parent) {
    final JsonMessage message = createChildMessage(body, parent);
    for (OutputConnection connection : connections) {
      connection.send(createCopy(message), parent);
    }
    return message.id();
  }

  @Override
  public String emit(JsonObject body, JsonMessage parent, Handler<AsyncResult<Void>> doneHandler) {
    final JsonMessage message = createChildMessage(body, parent);
    final CountingCompletionHandler<Void> counter = new CountingCompletionHandler<Void>(connections.size());
    counter.setHandler(doneHandler);
    for (OutputConnection connection : connections) {
      connection.send(createCopy(message), parent, new Handler<AsyncResult<Void>>() {
        @Override
        public void handle(AsyncResult<Void> result) {
          if (result.failed()) {
            counter.fail(result.cause());
          } else {
            counter.succeed();
          }
        }
      });
    }
    return message.id();
  }

  @Override
  public String emit(JsonMessage message) {
    return emit(message.body(), message);
  }

  @Override
  public String emit(JsonMessage message, Handler<AsyncResult<Void>> doneHandler) {
    return emit(message.body(), message, doneHandler);
  }

  /**
   * Creates a new message.
   */
  protected JsonMessage createNewMessage(JsonObject body) {
    JsonMessage message = DefaultJsonMessage.Builder.newBuilder()
        .setId(createUniqueId())
        .setBody(body)
        .build();
    return message;
  }

  /**
   * Creates a child message.
   */
  protected JsonMessage createChildMessage(JsonObject body, JsonMessage parent) {
    JsonMessage message = DefaultJsonMessage.Builder.newBuilder()
        .setId(createUniqueId())
        .setBody(body)
        .setParent(parent.id())
        .setRoot(parent.root() != null ? parent.root() : parent.id())
        .build();
    return message;
  }

  /**
   * Creates a copy of a message.
   */
  protected JsonMessage createCopy(JsonMessage message) {
    return message.copy(createUniqueId());
  }

  /**
   * Creates a unique message ID.
   */
  private String createUniqueId() {
    return new StringBuilder()
      .append(context.address())
      .append(":")
      .append(OutputCounter.incrementAndGet())
      .toString();
  }

  @Override
  public void update(OutputPortContext update) {
    Iterator<OutputConnection> iter = connections.iterator();
    while (iter.hasNext()) {
      OutputConnection connection = iter.next();
      boolean exists = false;
      for (OutputConnectionContext output : update.connections()) {
        if (output.equals(connection.context())) {
          exists = true;
          break;
        }
      }
      if (!exists) {
        connection.close();
        iter.remove();
      }
    }

    for (OutputConnectionContext output : update.connections()) {
      boolean exists = false;
      for (OutputConnection connection : connections) {
        if (connection.context().equals(output)) {
          exists = true;
          break;
        }
      }

      if (!exists) {
        OutputConnection connection = null;

        // Basic at-most-once delivery.
        if (output.delivery().equals(ConnectionContext.Delivery.AT_MOST_ONCE)) {
          if (output.order().equals(ConnectionContext.Order.NO_ORDER)) {
            connection = new BasicOutputConnection(vertx, output, cluster, acker);
          } else if (output.order().equals(ConnectionContext.Order.STRONG_ORDER)) {
            connection = new OrderedOutputConnection(vertx, output, cluster, acker);
          }
        // Required at-least-once delivery.
        } else if (output.delivery().equals(ConnectionContext.Delivery.AT_LEAST_ONCE)) {
          if (output.order().equals(ConnectionContext.Order.NO_ORDER)) {
            connection = new AtLeastOnceOutputConnection(vertx, output, cluster, acker);
          } else if (output.order().equals(ConnectionContext.Order.STRONG_ORDER)) {
            connection = new OrderedAtLeastOnceOutputConnection(vertx, output, cluster, acker);
          }
        // Required exactly-once delivery.
        } else if (output.delivery().equals(ConnectionContext.Delivery.EXACTLY_ONCE)) {
          if (output.order().equals(ConnectionContext.Order.NO_ORDER)) {
            connection = new ExactlyOnceOutputConnection(vertx, output, cluster, acker);
          } else if (output.order().equals(ConnectionContext.Order.STRONG_ORDER)) {
            connection = new OrderedExactlyOnceOutputConnection(vertx, output, cluster, acker);
          }
        }

        if (connection != null) {
          connection.setSendQueueMaxSize(maxQueueSize);
          connection.fullHandler(connectionFullHandler);
          connection.drainHandler(connectionDrainHandler);

          connections.add(connection.open());
        }
      }
    }
  }

  @Override
  public OutputPort open() {
    return open(null);
  }

  @Override
  public OutputPort open(final Handler<AsyncResult<Void>> doneHandler) {
    if (connections.isEmpty()) {
      final CountingCompletionHandler<Void> startCounter = new CountingCompletionHandler<Void>(context.connections().size());
      startCounter.setHandler(doneHandler);

      for (OutputConnectionContext connectionContext : context.connections()) {
        OutputConnection connection = null;

        // Basic at-most-once delivery.
        if (connectionContext.delivery().equals(ConnectionContext.Delivery.AT_MOST_ONCE)) {
          if (connectionContext.order().equals(ConnectionContext.Order.NO_ORDER)) {
            connection = new BasicOutputConnection(vertx, connectionContext, cluster, acker);
          } else if (connectionContext.order().equals(ConnectionContext.Order.STRONG_ORDER)) {
            connection = new OrderedOutputConnection(vertx, connectionContext, cluster, acker);
          }
        // Required at-least-once delivery.
        } else if (connectionContext.delivery().equals(ConnectionContext.Delivery.AT_LEAST_ONCE)) {
          if (connectionContext.order().equals(ConnectionContext.Order.NO_ORDER)) {
            connection = new AtLeastOnceOutputConnection(vertx, connectionContext, cluster, acker);
          } else if (connectionContext.order().equals(ConnectionContext.Order.STRONG_ORDER)) {
            connection = new OrderedAtLeastOnceOutputConnection(vertx, connectionContext, cluster, acker);
          }
        // Required exactly-once delivery.
        } else if (connectionContext.delivery().equals(ConnectionContext.Delivery.EXACTLY_ONCE)) {
          if (connectionContext.order().equals(ConnectionContext.Order.NO_ORDER)) {
            connection = new ExactlyOnceOutputConnection(vertx, connectionContext, cluster, acker);
          } else if (connectionContext.order().equals(ConnectionContext.Order.STRONG_ORDER)) {
            connection = new OrderedExactlyOnceOutputConnection(vertx, connectionContext, cluster, acker);
          }
        }

        if (connection != null) {
          connection.setSendQueueMaxSize(maxQueueSize);
          connection.fullHandler(connectionFullHandler);
          connection.drainHandler(connectionDrainHandler);

          connections.add(connection.open(new Handler<AsyncResult<Void>>() {
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
