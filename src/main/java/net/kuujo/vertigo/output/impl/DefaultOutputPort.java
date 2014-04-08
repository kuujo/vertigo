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
import net.kuujo.vertigo.context.OutputConnectionContext;
import net.kuujo.vertigo.context.OutputPortContext;
import net.kuujo.vertigo.hooks.OutputPortHook;
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
  private final List<OutputPortHook> hooks = new ArrayList<>();
  private final List<OutputConnection> connections = new ArrayList<>();

  public DefaultOutputPort(Vertx vertx, OutputPortContext context, VertigoCluster cluster) {
    this.vertx = vertx;
    this.context = context;
    this.cluster = cluster;
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
  public OutputPort addHook(OutputPortHook hook) {
    hooks.add(hook);
    return this;
  }

  @Override
  public String send(JsonObject body) {
    final DefaultJsonMessage message = createNewMessage(body);
    for (OutputConnection connection : connections) {
      connection.send(createChildMessage(body, message));
    }
    for (OutputPortHook hook : hooks) {
      hook.handleSend(message.id());
    }
    return message.id();
  }

  /**
   * Creates a new message.
   */
  protected DefaultJsonMessage createNewMessage(JsonObject body) {
    return DefaultJsonMessage.Builder.newBuilder()
        .setId(createUniqueId())
        .setBody(body)
        .build();
  }

  /**
   * Creates a child message.
   */
  protected DefaultJsonMessage createChildMessage(JsonObject body, DefaultJsonMessage parent) {
    DefaultJsonMessage message = DefaultJsonMessage.Builder.newBuilder()
        .setId(createUniqueId())
        .setBody(body)
        .build();
    return message;
  }

  /**
   * Creates a copy of a message.
   */
  protected DefaultJsonMessage createCopy(DefaultJsonMessage message) {
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
        OutputConnection connection = BasicOutputConnection.factory(vertx, output, cluster);
        connections.add(connection.open());
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
        OutputConnection connection = BasicOutputConnection.factory(vertx, connectionContext, cluster);
        connections.add(connection.open(startCounter));
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
      connection.close(stopCounter);
    }
  }

}
