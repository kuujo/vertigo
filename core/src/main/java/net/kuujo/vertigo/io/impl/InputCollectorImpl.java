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
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.impl.LoggerFactory;
import net.kuujo.vertigo.io.InputCollector;
import net.kuujo.vertigo.io.InputContext;
import net.kuujo.vertigo.io.port.InputPort;
import net.kuujo.vertigo.io.port.InputPortContext;
import net.kuujo.vertigo.io.port.impl.InputPortImpl;
import net.kuujo.vertigo.util.Closeable;
import net.kuujo.vertigo.util.CountingCompletionHandler;
import net.kuujo.vertigo.util.Openable;
import net.kuujo.vertigo.util.TaskRunner;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Input collector implementation.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class InputCollectorImpl implements InputCollector, Openable<InputCollector>, Closeable<InputCollector> {
  private final Logger log;
  private final Vertx vertx;
  private InputContext context;
  private final Map<String, InputPort> ports = new HashMap<>();
  private final TaskRunner tasks = new TaskRunner();
  private boolean open;

  public InputCollectorImpl(Vertx vertx, InputContext context) {
    this.vertx = vertx;
    this.context = context;
    this.log = LoggerFactory.getLogger(String.format("%s-%s-%d", InputCollectorImpl.class.getName(), context.partition().component().name(), context.partition().number()));
  }

  @Override
  public Collection<InputPort> ports() {
    return ports.values();
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> InputPort<T> port(String name) {
    InputPort<T> port = ports.get(name);
    if (port == null) {
      if (open) {
        throw new IllegalStateException("cannot declare port on locked input collector");
      }
      port = new InputPortImpl<>(vertx, InputPortContext.builder().setId(
        String.format(
          "%s:%s:%d:%s",
          context.partition().component().network().name(),
          context.partition().component().name(),
          context.partition().number(),
          name
        )).setName(name).build());
      ports.put(name, port);
    }
    return port;
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
      if (!open) {
        final CountingCompletionHandler<Void> startCounter = new CountingCompletionHandler<Void>(context.ports().size());
        startCounter.setHandler((result) -> {
          doneHandler.handle(result);
          task.complete();
        });

        for (final InputPortContext port : context.ports()) {
          log.debug(String.format("%s - Opening in port: %s", InputCollectorImpl.this, port));
          if (ports.containsKey(port.name())) {
            ((Openable) ports.get(port.name())).open((Handler<AsyncResult<Void>>)(result) -> {
              if (result.failed()) {
                log.error(String.format("%s - Failed to open in port: %s", InputCollectorImpl.this, port));
                startCounter.fail(result.cause());
              } else {
                log.info(String.format("%s - Opened in port: %s", InputCollectorImpl.this, port));
                startCounter.succeed();
              }
            });
          } else {
            ports.put(port.name(), new InputPortImpl(vertx, port).open((Handler<AsyncResult<Void>>)(result) -> {
              if (result.failed()) {
                log.error(String.format("%s - Failed to open in port: %s", InputCollectorImpl.this, port));
                startCounter.fail(result.cause());
              } else {
                log.info(String.format("%s - Opened in port: %s", InputCollectorImpl.this, port));
                startCounter.succeed();
              }
            }));
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

        for (final InputPort port : ports.values()) {
          log.debug(String.format("%s - Closing in port: %s", InputCollectorImpl.this, port));
          ((Closeable) port).close((Handler<AsyncResult<Void>>)(result) -> {
            if (result.failed()) {
              log.warn(String.format("%s - Failed to close in port: %s", InputCollectorImpl.this, port));
              stopCounter.fail(result.cause());
            } else {
              log.info(String.format("%s - Closed in port: %s", InputCollectorImpl.this, port));
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
