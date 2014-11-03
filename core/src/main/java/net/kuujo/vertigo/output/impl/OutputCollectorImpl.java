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

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.impl.LoggerFactory;
import net.kuujo.vertigo.output.OutputCollector;
import net.kuujo.vertigo.output.OutputContext;
import net.kuujo.vertigo.output.port.OutputPort;
import net.kuujo.vertigo.output.port.OutputPortContext;
import net.kuujo.vertigo.output.port.impl.OutputPortImpl;
import net.kuujo.vertigo.util.CountingCompletionHandler;
import net.kuujo.vertigo.util.TaskRunner;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Output collector implementation.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class OutputCollectorImpl implements OutputCollector {
  private final Logger log;
  private final Vertx vertx;
  private OutputContext context;
  private final Map<String, OutputPort> ports = new HashMap<>();
  private final TaskRunner tasks = new TaskRunner();
  private boolean open;

  public OutputCollectorImpl(Vertx vertx, OutputContext context) {
    this.vertx = vertx;
    this.context = context;
    this.log = LoggerFactory.getLogger(String.format("%s-%s-%d", OutputCollectorImpl.class.getName(), context.partition().component().name(), context.partition().number()));
  }

  @Override
  public Collection<OutputPort> ports() {
    return ports.values();
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> OutputPort<T> port(String name) {
    OutputPort<T> port = ports.get(name);
    if (port == null) {
      if (open) {
        throw new IllegalStateException("cannot declare port on locked output collector");
      }
      port = new OutputPortImpl<>(vertx, OutputPortContext.builder().setName(name).build());
      ports.put(name, port);
    }
    return port;
  }

  @Override
  public OutputCollector open() {
    return open(null);
  }

  @Override
  public OutputCollector open(final Handler<AsyncResult<Void>> doneHandler) {
    // Prevent the object from being opened and closed simultaneously
    // by queueing open/close operations as tasks.
    tasks.runTask((task) -> {
      if (!open) {
        final CountingCompletionHandler<Void> startCounter = new CountingCompletionHandler<Void>(context.ports().size());
        startCounter.setHandler((result) -> {
          doneHandler.handle(result);
          task.complete();
        });

        for (OutputPortContext output : context.ports()) {
          if (ports.containsKey(output.name())) {
            ports.get(output.name()).open(startCounter);
          } else {
            final OutputPort port = new OutputPortImpl(vertx, output);
            log.debug(String.format("%s - Opening out port: %s", OutputCollectorImpl.this, output));
            port.open((result) -> {
              if (result.failed()) {
                log.error(String.format("%s - Failed to open out port: %s", OutputCollectorImpl.this, port));
                startCounter.fail(result.cause());
              } else {
                log.info(String.format("%s - Opened out port: %s", OutputCollectorImpl.this, port));
                ports.put(port.name(), port);
                startCounter.succeed();
              }
            });
          }
        }
        open = true;
      } else {
        doneHandler.handle(Future.completedFuture());
        task.complete();
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
      if (open) {
        final CountingCompletionHandler<Void> stopCounter = new CountingCompletionHandler<Void>(ports.size());
        stopCounter.setHandler((result) -> {
          if (result.succeeded()) {
            ports.clear();
            open = false;
          }
          doneHandler.handle(result);
          task.complete();
        });

        for (final OutputPort output : ports.values()) {
          log.debug(String.format("%s - Closing out port: %s", OutputCollectorImpl.this, output));
          output.close((result) -> {
            if (result.failed()) {
              log.warn(String.format("%s - Failed to close out port: %s", OutputCollectorImpl.this, output));
              stopCounter.fail(result.cause());
            } else {
              log.info(String.format("%s - Closed out port: %s", OutputCollectorImpl.this, output));
              stopCounter.succeed();
            }
          });
        }
      } else {
        doneHandler.handle(Future.completedFuture());
        task.complete();
      }
    });
  }

  @Override
  public String toString() {
    return context.toString();
  }

}
