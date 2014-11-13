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

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.impl.LoggerFactory;
import net.kuujo.vertigo.io.ControllableInput;
import net.kuujo.vertigo.io.VertigoMessage;
import net.kuujo.vertigo.io.connection.InputConnection;
import net.kuujo.vertigo.io.connection.InputConnectionContext;
import net.kuujo.vertigo.io.connection.impl.InputConnectionImpl;
import net.kuujo.vertigo.io.port.InputPort;
import net.kuujo.vertigo.io.port.InputPortContext;
import net.kuujo.vertigo.util.TaskRunner;

import java.util.HashMap;
import java.util.Map;

/**
 * Input port implementation.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class InputPortImpl<T> implements InputPort<T>, ControllableInput<InputPort<T>, T>, Handler<Message<T>> {
  private static final Logger log = LoggerFactory.getLogger(InputPortImpl.class);
  private final Vertx vertx;
  private InputPortContext context;
  private final Map<String, InputConnectionImpl<T>> connections = new HashMap<>();
  private final TaskRunner tasks = new TaskRunner();
  @SuppressWarnings("rawtypes")
  private Handler<VertigoMessage<T>> messageHandler;
  private boolean open;
  private boolean paused;

  public InputPortImpl(Vertx vertx, InputPortContext context) {
    this.vertx = vertx;
    this.context = context;
    init();
  }

  /**
   * Initializes the output connections.
   */
  private void init() {
    for (InputConnectionContext connection : context.connections()) {
      connections.put(connection.target().address(), new InputConnectionImpl<T>(vertx, connection));
    }
  }

  @Override
  public String name() {
    return context.name();
  }

  @Override
  public void handle(Message<T> message) {
    String source = message.headers().get("source");
    if (source != null) {
      InputConnectionImpl<T> connection = connections.get(source);
      if (connection != null) {
        connection.handle(message);
      }
    }
  }

  @Override
  public InputPort<T> pause() {
    paused = true;
    for (InputConnection connection : connections.values()) {
      connection.pause();
    }
    return this;
  }

  @Override
  public InputPort<T> resume() {
    paused = false;
    for (InputConnection connection : connections.values()) {
      connection.resume();
    }
    return this;
  }

  @Override
  public InputPort<T> handler(final Handler<VertigoMessage<T>> handler) {
    if (open && handler == null) {
      throw new IllegalStateException("cannot unset handler on locked port");
    }
    this.messageHandler = handler;
    for (InputConnection<T> connection : connections.values()) {
      connection.handler(messageHandler);
    }
    return this;
  }

  @Override
  public String toString() {
    return context.toString();
  }

}
