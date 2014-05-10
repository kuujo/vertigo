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
package net.kuujo.vertigo.io.stream.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.kuujo.vertigo.io.batch.OutputBatch;
import net.kuujo.vertigo.io.connection.ConnectionOutputBatch;
import net.kuujo.vertigo.io.connection.OutputConnection;
import net.kuujo.vertigo.io.connection.OutputConnectionContext;
import net.kuujo.vertigo.io.connection.impl.DefaultOutputConnection;
import net.kuujo.vertigo.io.group.OutputGroup;
import net.kuujo.vertigo.io.group.impl.BaseOutputGroup;
import net.kuujo.vertigo.io.selector.Selector;
import net.kuujo.vertigo.io.stream.OutputStream;
import net.kuujo.vertigo.io.stream.OutputStreamContext;
import net.kuujo.vertigo.util.CountingCompletionHandler;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

/**
 * Default output stream implementation.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class DefaultOutputStream implements OutputStream {
  private final Vertx vertx;
  private final OutputStreamContext context;
  final List<OutputConnection> connections = new ArrayList<>();
  private int maxQueueSize;
  Selector selector;

  public DefaultOutputStream(Vertx vertx, OutputStreamContext context) {
    this.vertx = vertx;
    this.context = context;
    for (OutputConnectionContext connection : context.connections()) {
      connections.add(new DefaultOutputConnection(vertx, connection));
    }
    this.selector = context.selector();
  }

  @Override
  public Vertx vertx() {
    return vertx;
  }

  @Override
  public String address() {
    return context.address();
  }

  @Override
  public OutputStream open() {
    return open(null);
  }

  @Override
  public OutputStream open(Handler<AsyncResult<Void>> doneHandler) {
    final CountingCompletionHandler<Void> counter = new CountingCompletionHandler<Void>(connections.size()).setHandler(doneHandler);
    for (OutputConnection connection : connections) {
      connection.open(counter);
    }
    return this;
  }

  @Override
  public OutputStream setSendQueueMaxSize(int maxSize) {
    this.maxQueueSize = maxSize;
    for (OutputConnection connection : connections) {
      connection.setSendQueueMaxSize(Math.round(maxQueueSize / connections.size()));
    }
    return this;
  }

  @Override
  public int getSendQueueMaxSize() {
    return maxQueueSize;
  }

  @Override
  public int size() {
    int highest = 0;
    for (OutputConnection connection : connections) {
      highest = Math.max(highest, connection.size());
    }
    return highest;
  }

  @Override
  public boolean sendQueueFull() {
    for (OutputConnection connection : connections) {
      if (connection.sendQueueFull()) {
        return true;
      }
    }
    return false;
  }

  @Override
  public OutputStream drainHandler(Handler<Void> handler) {
    for (OutputConnection connection : connections) {
      connection.drainHandler(handler);
    }
    return this;
  }

  @Override
  public OutputStream batch(final Handler<OutputBatch> handler) {
    return batch(UUID.randomUUID().toString(), handler);
  }

  @Override
  public OutputStream batch(final String id, final Handler<OutputBatch> handler) {
    final List<ConnectionOutputBatch> batches = new ArrayList<>();
    final int connectionsSize = connections.size();
    if (connectionsSize == 0) {
      handler.handle(new StreamOutputBatch(id, this, batches));
    } else {
      for (OutputConnection connection : connections) {
        connection.batch(id, new Handler<ConnectionOutputBatch>() {
          @Override
          public void handle(ConnectionOutputBatch batch) {
            batches.add(batch);
            if (batches.size() == connectionsSize) {
              handler.handle(new StreamOutputBatch(id, DefaultOutputStream.this, batches));
            }
          }
        });
      }
    }
    return this;
  }

  @Override
  public OutputStream group(Handler<OutputGroup> handler) {
    return group(UUID.randomUUID().toString(), handler);
  }

  @Override
  public OutputStream group(final String name, final Handler<OutputGroup> handler) {
    final List<OutputGroup> groups = new ArrayList<>();
    List<OutputConnection> connections = selector.select(name, this.connections);
    final int connectionsSize = connections.size();
    if (connectionsSize == 0) {
      handler.handle(new BaseOutputGroup(name, vertx, groups));
    } else {
      for (OutputConnection connection : connections) {
        connection.group(name, new Handler<OutputGroup>() {
          @Override
          public void handle(OutputGroup group) {
            groups.add(group);
            if (groups.size() == connectionsSize) {
              handler.handle(new BaseOutputGroup(name, vertx, groups));
            }
          }
        });
      }
    }
    return this;
  }

  @Override
  public OutputStream send(Object message) {
    for (OutputConnection connection : selector.select(message, connections)) {
      connection.send(message);
    }
    return this;
  }

  @Override
  public OutputStream send(String message) {
    for (OutputConnection connection : selector.select(message, connections)) {
      connection.send(message);
    }
    return this;
  }

  @Override
  public OutputStream send(Boolean message) {
    for (OutputConnection connection : selector.select(message, connections)) {
      connection.send(message);
    }
    return this;
  }

  @Override
  public OutputStream send(Character message) {
    for (OutputConnection connection : selector.select(message, connections)) {
      connection.send(message);
    }
    return this;
  }

  @Override
  public OutputStream send(Short message) {
    for (OutputConnection connection : selector.select(message, connections)) {
      connection.send(message);
    }
    return this;
  }

  @Override
  public OutputStream send(Integer message) {
    for (OutputConnection connection : selector.select(message, connections)) {
      connection.send(message);
    }
    return this;
  }

  @Override
  public OutputStream send(Long message) {
    for (OutputConnection connection : selector.select(message, connections)) {
      connection.send(message);
    }
    return this;
  }

  @Override
  public OutputStream send(Double message) {
    for (OutputConnection connection : selector.select(message, connections)) {
      connection.send(message);
    }
    return this;
  }

  @Override
  public OutputStream send(Float message) {
    for (OutputConnection connection : selector.select(message, connections)) {
      connection.send(message);
    }
    return this;
  }

  @Override
  public OutputStream send(Buffer message) {
    for (OutputConnection connection : selector.select(message, connections)) {
      connection.send(message);
    }
    return this;
  }

  @Override
  public OutputStream send(JsonObject message) {
    for (OutputConnection connection : selector.select(message, connections)) {
      connection.send(message);
    }
    return this;
  }

  @Override
  public OutputStream send(JsonArray message) {
    for (OutputConnection connection : selector.select(message, connections)) {
      connection.send(message);
    }
    return this;
  }

  @Override
  public OutputStream send(Byte message) {
    for (OutputConnection connection : selector.select(message, connections)) {
      connection.send(message);
    }
    return this;
  }

  @Override
  public OutputStream send(byte[] message) {
    for (OutputConnection connection : selector.select(message, connections)) {
      connection.send(message);
    }
    return this;
  }

  @Override
  public void close() {
    close(null);
  }

  @Override
  public void close(Handler<AsyncResult<Void>> doneHandler) {
    final CountingCompletionHandler<Void> counter = new CountingCompletionHandler<Void>(connections.size()).setHandler(doneHandler);
    for (OutputConnection connection : connections) {
      connection.close(counter);
    }
  }

}
