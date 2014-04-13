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

import net.kuujo.vertigo.context.InputConnectionContext;
import net.kuujo.vertigo.context.InputPortContext;
import net.kuujo.vertigo.input.InputConnection;
import net.kuujo.vertigo.input.InputGroup;
import net.kuujo.vertigo.input.InputPort;
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
  private final List<InputConnection> connections = new ArrayList<>();
  @SuppressWarnings("rawtypes")
  private Handler messageHandler;
  private final Map<String, Handler<InputGroup>> groupHandlers = new HashMap<>();
  private boolean paused;

  public DefaultInputPort(Vertx vertx, InputPortContext context) {
    this.vertx = vertx;
    this.context = context;
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
  public void update(InputPortContext update) {
    Iterator<InputConnection> iter = connections.iterator();
    while (iter.hasNext()) {
      InputConnection connection = iter.next();
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
      for (InputConnection connection : connections) {
        if (connection.context().equals(input)) {
          exists = true;
          break;
        }
      }

      if (!exists) {
        InputConnection newConnection = new DefaultInputConnection(vertx, input).open();
        newConnection.messageHandler(messageHandler);
        for (Map.Entry<String, Handler<InputGroup>> entry : groupHandlers.entrySet()) {
          newConnection.groupHandler(entry.getKey(), entry.getValue());
        }
        if (paused) {
          newConnection.pause();
        }
        connections.add(newConnection);
      }
    }
  }

  @Override
  public InputPort pause() {
    paused = true;
    for (InputConnection connection : connections) {
      connection.pause();
    }
    return this;
  }

  @Override
  public InputPort resume() {
    paused = false;
    for (InputConnection connection : connections) {
      connection.resume();
    }
    return this;
  }

  @Override
  @SuppressWarnings("rawtypes")
  public InputPort messageHandler(Handler handler) {
    this.messageHandler = handler;
    for (InputConnection connection : connections) {
      connection.messageHandler(messageHandler);
    }
    return this;
  }

  @Override
  public InputPort groupHandler(String group, Handler<InputGroup> handler) {
    this.groupHandlers.put(group, handler);
    for (InputConnection connection : connections) {
      connection.groupHandler(group, handler);
    }
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
        InputConnection connection = new DefaultInputConnection(vertx, connectionContext);
        connection.messageHandler(messageHandler);
        for (Map.Entry<String, Handler<InputGroup>> entry : groupHandlers.entrySet()) {
          connection.groupHandler(entry.getKey(), entry.getValue());
        }
        if (paused) {
          connection.pause();
        }
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

    for (InputConnection connection : connections) {
      connection.close(stopCounter);
    }
  }

}
