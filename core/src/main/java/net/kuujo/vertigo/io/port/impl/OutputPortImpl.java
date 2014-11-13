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
package net.kuujo.vertigo.io.port.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.impl.LoggerFactory;
import net.kuujo.vertigo.io.ControllableOutput;
import net.kuujo.vertigo.io.connection.impl.OutputConnectionImpl;
import net.kuujo.vertigo.io.port.OutputPort;
import net.kuujo.vertigo.io.port.OutputPortContext;
import net.kuujo.vertigo.util.Args;
import net.kuujo.vertigo.util.TaskRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Output port implementation.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class OutputPortImpl<T> implements OutputPort<T>, ControllableOutput<OutputPort<T>, T>, Handler<Message<T>> {
  private static final Logger log = LoggerFactory.getLogger(OutputPortImpl.class);
  private static final int DEFAULT_SEND_QUEUE_MAX_SIZE = 10000;
  private final Vertx vertx;
  private OutputPortContext info;
  private final Map<String, OutputConnectionImpl<T>> connections = new HashMap<>();
  private final TaskRunner tasks = new TaskRunner();
  private int maxQueueSize = DEFAULT_SEND_QUEUE_MAX_SIZE;
  private Handler<Void> drainHandler;
  private boolean open;

  public OutputPortImpl(Vertx vertx, OutputPortContext info) {
    this.vertx = vertx;
    this.info = info;
  }

  @Override
  public void handle(Message<T> message) {
    String source = message.headers().get("source");
    if (source != null) {
      OutputConnectionImpl<T> connection = connections.get(source);
      if (connection != null) {
        connection.handle(message);
      }
    }
  }

  @Override
  public OutputPort<T> send(T message, Handler<AsyncResult<Void>> ackHandler) {
    return null;
  }

  @Override
  public OutputPort<T> send(T message, MultiMap headers, Handler<AsyncResult<Void>> ackHandler) {
    return null;
  }

  @Override
  public String name() {
    return info.name();
  }

  @Override
  public OutputPort<T> setSendQueueMaxSize(int maxSize) {
    Args.checkPositive(maxSize, "max size must be a positive number");
    this.maxQueueSize = maxSize;
    for (OutputConnectionImpl connection : connections.values()) {
      connection.setSendQueueMaxSize(maxQueueSize);
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
    for (OutputConnectionImpl connection : connections.values()) {
      highest = Math.max(highest, connection.size());
    }
    return highest;
  }

  @Override
  public boolean sendQueueFull() {
    for (OutputConnectionImpl stream : connections.values()) {
      if (stream.sendQueueFull()) {
        return true;
      }
    }
    return false;
  }

  @Override
  public OutputPort<T> drainHandler(Handler<Void> handler) {
    this.drainHandler = handler;
    for (OutputConnectionImpl connection : connections.values()) {
      connection.drainHandler(handler);
    }
    return this;
  }

  @Override
  public OutputPort<T> send(T message) {
    for (OutputConnectionImpl<T> connection : connections.values()) {
      connection.send(message);
    }
    return this;
  }

  @Override
  public OutputPort<T> send(T message, MultiMap headers) {
    for (OutputConnectionImpl<T> connection : connections.values()) {
      connection.send(message, headers);
    }
    return this;
  }

  @Override
  public String toString() {
    return info.toString();
  }

}
