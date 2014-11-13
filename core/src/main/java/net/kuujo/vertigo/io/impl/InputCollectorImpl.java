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

package net.kuujo.vertigo.io.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.impl.LoggerFactory;
import net.kuujo.vertigo.io.InputCollector;
import net.kuujo.vertigo.io.InputContext;
import net.kuujo.vertigo.io.port.InputPort;
import net.kuujo.vertigo.io.port.impl.InputPortImpl;
import net.kuujo.vertigo.util.Closeable;
import net.kuujo.vertigo.util.Openable;
import net.kuujo.vertigo.util.TaskRunner;

import java.util.*;

/**
 * Input collector implementation.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class InputCollectorImpl implements InputCollector, Openable<InputCollector>, Closeable<InputCollector>, Handler<Message<Object>> {
  private final Logger log;
  private final Vertx vertx;
  private InputContext context;
  private final Map<String, InputPortImpl> ports = new HashMap<>();
  private final TaskRunner tasks = new TaskRunner();
  private MessageConsumer<Object> consumer;

  public InputCollectorImpl(Vertx vertx, InputContext context) {
    this.vertx = vertx;
    this.context = context;
    this.log = LoggerFactory.getLogger(String.format("%s-%s", InputCollectorImpl.class.getName(), context.component().name()));
  }

  /**
   * Handles an input message.
   *
   * @param message The message to handle.
   */
  @Override
  @SuppressWarnings("unchecked")
  public void handle(Message<Object> message) {
    String portName = message.headers().get("port");
    if (portName != null) {
      InputPortImpl port = ports.get(portName);
      if (port != null) {
        port.handle(message);
      }
    }
  }

  @Override
  public Collection<InputPort> ports() {
    List<InputPort> ports = new ArrayList<>(this.ports.size());
    for (InputPortImpl port : this.ports.values()) {
      ports.add(port);
    }
    return ports;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> InputPort<T> port(String name) {
    return ports.get(name);
  }

  @Override
  public InputCollector open() {
    return open(null);
  }

  @Override
  public InputCollector open(final Handler<AsyncResult<Void>> doneHandler) {
    // Prevent the object from being opened and closed simultaneously
    // by queueing open/close operations as tasks.
    tasks.runTask((task) -> {
      if (consumer == null) {
        consumer = vertx.eventBus().consumer(context.component().address());
        consumer.handler(this);
        consumer.completionHandler(result -> {
          if (doneHandler != null) {
            doneHandler.handle(result);
          }
          task.complete();
        });
      } else {
        Future.<Void>completedFuture().setHandler(doneHandler);
      }
    });
    return this;
  }

  @Override
  public void close() {
    close(null);
  }

  @Override
  public void close(final Handler<AsyncResult<Void>> doneHandler) {
    // Prevent the object from being opened and closed simultaneously
    // by queueing open/close operations as tasks.
    tasks.runTask((task) -> {
      if (consumer != null) {
        consumer.unregister(result -> {
          consumer = null;
          if (doneHandler != null) {
            doneHandler.handle(result);
          }
          task.complete();
        });
      } else {
        Future.<Void>completedFuture().setHandler(doneHandler);
      }
    });
  }

  @Override
  public String toString() {
    return context.toString();
  }

}
