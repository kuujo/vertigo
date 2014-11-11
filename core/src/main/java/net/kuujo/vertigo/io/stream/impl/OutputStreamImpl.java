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

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.http.CaseInsensitiveHeaders;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.impl.LoggerFactory;
import net.kuujo.vertigo.io.connection.OutputConnection;
import net.kuujo.vertigo.io.connection.OutputConnectionContext;
import net.kuujo.vertigo.io.connection.impl.OutputConnectionImpl;
import net.kuujo.vertigo.io.partition.Partitioner;
import net.kuujo.vertigo.io.stream.OutputStream;
import net.kuujo.vertigo.io.stream.OutputStreamContext;
import net.kuujo.vertigo.util.Closeable;
import net.kuujo.vertigo.util.CountingCompletionHandler;
import net.kuujo.vertigo.util.Openable;

import java.util.HashMap;
import java.util.Map;

/**
 * Output stream implementation.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class OutputStreamImpl<T> implements OutputStream<T>, Openable<OutputStream<T>>, Closeable<OutputStream<T>> {
  private static final MultiMap EMPTY_HEADERS = new CaseInsensitiveHeaders();
  private final Logger log;
  private final OutputStreamContext context;
  private final Map<Integer, OutputConnectionImpl> connections = new HashMap<>();
  private int maxQueueSize;
  private Partitioner partitioner;

  public OutputStreamImpl(Vertx vertx, OutputStreamContext context) {
    this.context = context;
    this.log = LoggerFactory.getLogger(String.format("%s-%s", OutputStreamImpl.class.getName(), context.port().toString()));
    context.connections().forEach((connection) -> {
      connections.put(connection.target().partition(), new OutputConnectionImpl<>(vertx, connection));
    });
    this.partitioner = context.partitioner();
  }

  @Override
  public OutputStream<T> open() {
    return open(null);
  }

  @Override
  public OutputStream<T> open(Handler<AsyncResult<Void>> doneHandler) {
    final CountingCompletionHandler<Void> counter = new CountingCompletionHandler<Void>(connections.size()).setHandler(doneHandler);
    for (final OutputConnectionImpl connection : connections.values()) {
      log.debug(String.format("%s - Opening connection to: %s", this, connection.context().target()));
      connection.open((Handler<AsyncResult<Void>>)(result) -> {
        if (result.failed()) {
          log.error(String.format("%s - Failed to open connection to: %s", OutputStreamImpl.this, connection.context().target()));
          counter.fail(result.cause());
        } else {
          log.info(String.format("%s - Opened connection to: %s", OutputStreamImpl.this, connection.context().target()));
          counter.succeed();
        }
      });
    }
    return this;
  }

  @Override
  public OutputStream<T> setSendQueueMaxSize(int maxSize) {
    this.maxQueueSize = maxSize;
    for (OutputConnection connection : connections.values()) {
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
    for (OutputConnection connection : connections.values()) {
      highest = Math.max(highest, connection.size());
    }
    return highest;
  }

  @Override
  public boolean sendQueueFull() {
    for (OutputConnection connection : connections.values()) {
      if (connection.sendQueueFull()) {
        return true;
      }
    }
    return false;
  }

  @Override
  public OutputStream<T> drainHandler(Handler<Void> handler) {
    for (OutputConnection connection : connections.values()) {
      connection.drainHandler(handler);
    }
    return this;
  }

  @Override
  @SuppressWarnings("unchecked")
  public OutputStream<T> send(T message) {
    for (OutputConnectionContext connectionContext : partitioner.partition(EMPTY_HEADERS, context.connections())) {
      OutputConnection connection = connections.get(connectionContext.target().partition());
      if (connection != null) {
        connection.send(message);
      }
    }
    return this;
  }

  @Override
  @SuppressWarnings("unchecked")
  public OutputStream<T> send(T message, MultiMap headers) {
    for (OutputConnectionContext connectionContext : partitioner.partition(headers, context.connections())) {
      OutputConnection connection = connections.get(connectionContext.target().partition());
      if (connection != null) {
        connection.send(message, headers);
      }
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
    for (final OutputConnectionImpl connection : connections.values()) {
      log.debug(String.format("%s - Closing connection to: %s", this, connection.context().target()));
      connection.close((Handler<AsyncResult<Void>>)(result) -> {
        if (result.failed()) {
          log.warn(String.format("%s - Failed to close connection to: %s", OutputStreamImpl.this, connection.context().target()));
          counter.fail(result.cause());
        } else {
          log.info(String.format("%s - Closed connection to: %s", OutputStreamImpl.this, connection.context().target()));
          counter.succeed();
        }
      });
    }
  }

  @Override
  public String toString() {
    return context.toString();
  }

}
