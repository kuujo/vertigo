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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.kuujo.vertigo.io.InputCollector;
import net.kuujo.vertigo.io.InputContext;
import net.kuujo.vertigo.io.port.InputPort;
import net.kuujo.vertigo.io.port.InputPortContext;
import net.kuujo.vertigo.io.port.impl.DefaultInputPort;
import net.kuujo.vertigo.io.port.impl.DefaultInputPortContext;
import net.kuujo.vertigo.util.CountingCompletionHandler;
import net.kuujo.vertigo.util.Observer;
import net.kuujo.vertigo.util.Task;
import net.kuujo.vertigo.util.TaskRunner;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.impl.DefaultFutureResult;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.core.logging.impl.LoggerFactory;

/**
 * Default input collector implementation.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class DefaultInputCollector implements InputCollector, Observer<InputContext> {
  private static final Logger log = LoggerFactory.getLogger(DefaultInputCollector.class);
  private final Vertx vertx;
  private InputContext context;
  private final Map<String, InputPort> ports = new HashMap<>();
  private final TaskRunner tasks = new TaskRunner();
  private boolean started;

  public DefaultInputCollector(Vertx vertx) {
    this.vertx = vertx;
  }

  public DefaultInputCollector(Vertx vertx, InputContext context) {
    this.vertx = vertx;
    this.context = context;
    context.registerObserver(this);
  }

  @Override
  public Collection<InputPort> ports() {
    return ports.values();
  }

  @Override
  public InputPort port(String name) {
    // Attempt to search for the port in the existing context. If the
    // port isn't an explicitly configured port then lazily create
    // and open the port. The lazy port will be empty.
    InputPort port = ports.get(name);
    if (port == null) {
      InputPortContext portContext = null;
      for (InputPortContext input : context.ports()) {
        if (input.name().equals(name)) {
          portContext = input;
          break;
        }
      }
      if (portContext == null) {
        portContext = DefaultInputPortContext.Builder.newBuilder()
            .setAddress(UUID.randomUUID().toString())
            .setName(name)
            .build();
        DefaultInputContext.Builder.newBuilder((DefaultInputContext) context).addPort((DefaultInputPortContext) portContext);
      }
      port = new DefaultInputPort(vertx, portContext);
      ports.put(name, port.open());
    }
    return port;
  }

  @Override
  public void update(final InputContext update) {
    // All updates are run sequentially to prevent race conditions
    // during configuration changes. Without essentially locking the
    // object, it could be possible that connections are simultaneously
    // added and removed or opened and closed on the object.
    tasks.runTask(new Handler<Task>() {
      @Override
      public void handle(final Task task) {
        final List<InputPort> newPorts = new ArrayList<>();
        for (InputPortContext input : update.ports()) {
          boolean exists = false;
          for (InputPort port : ports.values()) {
            if (port.name().equals(input.name())) {
              exists = true;
              break;
            }
          }
          if (!exists) {
            newPorts.add(new DefaultInputPort(vertx, input));
          }
        }

        // If the input has already been started, add each input port
        // only once the port has been started. This ensures that the
        // input cannot receive messages until the port is open.
        if (started) {
          final CountingCompletionHandler<Void> counter = new CountingCompletionHandler<Void>(newPorts.size());
          counter.setHandler(new Handler<AsyncResult<Void>>() {
            @Override
            public void handle(AsyncResult<Void> result) {
              task.complete();
            }
          });

          // Iterate through each new input port and open and add the port.
          for (final InputPort port : newPorts) {
            port.open(new Handler<AsyncResult<Void>>() {
              @Override
              public void handle(AsyncResult<Void> result) {
                if (result.failed()) {
                  log.error("Failed to open input port " + port.name());
                } else {
                  ports.put(port.name(), port);
                }
                counter.succeed();
              }
            });
          }
        } else {
          // If the input is not already started, simply open and add the ports.
          // The ports will be open once the input is started.
          for (InputPort port : newPorts) {
            ports.put(port.name(), port);
          }
          task.complete();
        }
      }
    });
  }

  @Override
  public InputCollector open() {
    return open(null);
  }

  @Override
  public InputCollector open(final Handler<AsyncResult<Void>> doneHandler) {
    // Prevent the object from being opened and closed simultaneously
    // by queueing open/close operations as tasks.
    tasks.runTask(new Handler<Task>() {
      @Override
      public void handle(final Task task) {
        // If the input hasn't already been started, start the input
        // by adding and opening any necessary ports.
        if (!started) {
          final CountingCompletionHandler<Void> startCounter = new CountingCompletionHandler<Void>(context.ports().size());
          startCounter.setHandler(new Handler<AsyncResult<Void>>() {
            @Override
            public void handle(AsyncResult<Void> result) {
              if (result.failed()) {
                new DefaultFutureResult<Void>(result.cause()).setHandler(doneHandler);
              } else {
                new DefaultFutureResult<Void>((Void) null).setHandler(doneHandler);
              }
              task.complete();
            }
          });

          for (InputPortContext port : context.ports()) {
            if (ports.containsKey(port.name())) {
              ((DefaultInputPort) ports.get(port.name())).open(new Handler<AsyncResult<Void>>() {
                @Override
                public void handle(AsyncResult<Void> result) {
                  if (result.failed()) {
                    startCounter.fail(result.cause());
                  } else {
                    startCounter.succeed();
                  }
                }
              });
            } else {
              ports.put(port.name(), new DefaultInputPort(vertx, port).open(new Handler<AsyncResult<Void>>() {
                @Override
                public void handle(AsyncResult<Void> result) {
                  if (result.failed()) {
                    startCounter.fail(result.cause());
                  } else {
                    startCounter.succeed();
                  }
                }
              }));
            }
          }
          started = true;
        } else {
          new DefaultFutureResult<Void>((Void) null).setHandler(doneHandler);
          task.complete();
        }
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
    tasks.runTask(new Handler<Task>() {
      @Override
      public void handle(final Task task) {
        if (started) {
          final CountingCompletionHandler<Void> stopCounter = new CountingCompletionHandler<Void>(ports.size());
          stopCounter.setHandler(new Handler<AsyncResult<Void>>() {
            @Override
            public void handle(AsyncResult<Void> result) {
              if (result.failed()) {
                new DefaultFutureResult<Void>(result.cause()).setHandler(doneHandler);
              } else {
                ports.clear();
                started = false;
                new DefaultFutureResult<Void>((Void) null).setHandler(doneHandler);
              }
              task.complete();
            }
          });
      
          for (InputPort port : ports.values()) {
            port.close(new Handler<AsyncResult<Void>>() {
              @Override
              public void handle(AsyncResult<Void> result) {
                if (result.failed()) {
                  stopCounter.fail(result.cause());
                } else {
                  stopCounter.succeed();
                }
              }
            });
          }
        } else {
          new DefaultFutureResult<Void>((Void) null).setHandler(doneHandler);
          task.complete();
        }
      }
    });
  }

}
