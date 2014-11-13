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

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.impl.LoggerFactory;
import net.kuujo.vertigo.io.OutputCollector;
import net.kuujo.vertigo.io.OutputContext;
import net.kuujo.vertigo.io.port.OutputPort;
import net.kuujo.vertigo.io.port.OutputPortContext;
import net.kuujo.vertigo.io.port.impl.OutputPortImpl;

import java.util.*;

/**
 * Output collector implementation.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class OutputCollectorImpl implements OutputCollector, Handler<Message<Object>> {
  private final Logger log;
  private final Vertx vertx;
  private OutputContext context;
  private final Map<String, OutputPortImpl> ports = new HashMap<>();

  public OutputCollectorImpl(Vertx vertx, OutputContext context) {
    this.vertx = vertx;
    this.context = context;
    this.log = LoggerFactory.getLogger(String.format("%s-%s", OutputCollectorImpl.class.getName(), context.component().name()));
    init();
  }

  /**
   * Initializes the output.
   */
  private void init() {
    for (OutputPortContext output : context.ports()) {
      if (!ports.containsKey(output.name())) {
        ports.put(output.name(), new OutputPortImpl(vertx, output));
      }
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public void handle(Message<Object> message) {
    String portName = message.headers().get("port");
    if (portName != null) {
      OutputPortImpl port = ports.get(portName);
      if (port != null) {
        port.handle(message);
      }
    }
  }

  @Override
  public Collection<OutputPort> ports() {
    List<OutputPort> ports = new ArrayList<>(this.ports.size());
    for (OutputPortImpl port : this.ports.values()) {
      ports.add(port);
    }
    return ports;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> OutputPort<T> port(String name) {
    return ports.get(name);
  }

  @Override
  public String toString() {
    return context.toString();
  }

}
