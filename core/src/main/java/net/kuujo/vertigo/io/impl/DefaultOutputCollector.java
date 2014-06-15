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

import net.kuujo.vertigo.io.OutputCollector;
import net.kuujo.vertigo.io.OutputContext;
import net.kuujo.vertigo.io.port.OutputPort;
import net.kuujo.vertigo.io.port.OutputPortContext;
import net.kuujo.vertigo.io.port.impl.DefaultOutputPort;
import net.kuujo.vertigo.io.port.impl.DefaultOutputPortContext;
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
 * Default output collector implementation.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class DefaultOutputCollector implements OutputCollector, Observer<OutputContext> {
  private final Logger log;
  private final Vertx vertx;
  private OutputContext context;
  private final Map<String, OutputPort> ports = new HashMap<>();
  private final TaskRunner tasks = new TaskRunner();
  private boolean started;

  public DefaultOutputCollector(Vertx vertx) {
    this.vertx = vertx;
    this.log = LoggerFactory.getLogger(DefaultOutputCollector.class);
  }

  public DefaultOutputCollector(Vertx vertx, OutputContext context) {
    this.vertx = vertx;
    this.context = context;
    if (!context.ports().isEmpty()) {
      System.out.println(context.ports().iterator().next().streams());
    }
    this.log = LoggerFactory.getLogger(String.format("%s-%s-%d", DefaultOutputCollector.class.getName(), context.instance().component().name(), context.instance().number()));
    context.registerObserver(this);
  }

  @Override
  public Collection<OutputPort> ports() {
    return ports.values();
  }

  @Override
  public OutputPort port(String name) {
    OutputPort port = ports.get(name);
    if (port == null) {
      log.debug(String.format("%s - Lazily creating out port: %s", this, name));

      // Attempt to search for the port in the existing context. If the
      // port isn't an explicitly configured port then lazily create
      // and open the port. The lazy port will be empty.
      OutputPortContext portContext = context.port(name);
      if (portContext == null) {
        portContext = DefaultOutputPortContext.Builder.newBuilder()
            .setAddress(UUID.randomUUID().toString())
            .setName(name)
            .build();
        DefaultOutputContext.Builder.newBuilder((DefaultOutputContext) context).addPort(portContext);
      }
      port = new DefaultOutputPort(vertx, context.port(name));
      ports.put(name, port.open());
    }
    return port;
  }

  @Override
  public void update(final OutputContext update) {
    log.debug(String.format("%s - Output context changed, updating ports", this));

    // All updates are run sequentially to prevent race conditions
    // during configuration changes. Without essentially locking the
    // object, it could be possible that connections are simultaneously
    // added and removed or opened and closed on the object.
    tasks.runTask(new Handler<Task>() {
      @Override
      public void handle(final Task task) {
        final List<OutputPort> newPorts = new ArrayList<>();
        for (OutputPortContext output : update.ports()) {
          boolean exists = false;
          for (OutputPort port : ports.values()) {
            if (port.name().equals(output.name())) {
              exists = true;
              break;
            }
          }
          if (!exists) {
            log.debug(String.format("%s - Adding out port: %s", DefaultOutputCollector.this, output));
            newPorts.add(new DefaultOutputPort(vertx, output));
          }
        }

        // If the output has already been started, add each output port
        // only once the port has been started. This ensures that messages
        // cannot be sent on the output port until connections have opened.
        if (started) {
          final CountingCompletionHandler<Void> counter = new CountingCompletionHandler<Void>(newPorts.size());
          counter.setHandler(new Handler<AsyncResult<Void>>() {
            @Override
            public void handle(AsyncResult<Void> result) {
              task.complete();
            }
          });

          // Iterate through each new output port and open and add the port.
          for (final OutputPort port : newPorts) {
            log.debug(String.format("%s - Opening out port: %s", DefaultOutputCollector.this, port));
            port.open(new Handler<AsyncResult<Void>>() {
              @Override
              public void handle(AsyncResult<Void> result) {
                if (result.failed()) {
                  log.error(String.format("%s - Failed to open out port: %s", DefaultOutputCollector.this, port));
                } else {
                  log.info(String.format("%s - Opened out port: %s", DefaultOutputCollector.this, port));
                  ports.put(port.name(), port);
                }
                counter.succeed();
              }
            });
          }
        } else {
          // If the output is not already started, simply open and add the ports.
          // The ports will be open once the output is started.
          for (OutputPort port : newPorts) {
            ports.put(port.name(), port);
          }
          task.complete();
        }
      }
    });
  }

  @Override
  public OutputCollector open() {
    return open(null);
  }

  @Override
  public OutputCollector open(final Handler<AsyncResult<Void>> doneHandler) {
    // Prevent the object from being opened and closed simultaneously
    // by queueing open/close operations as tasks.
    tasks.runTask(new Handler<Task>() {
      @Override
      public void handle(final Task task) {
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

          for (OutputPortContext output : context.ports()) {
            if (ports.containsKey(output.name())) {
              ((DefaultOutputPort) ports.get(output.name())).open(startCounter);
            } else {
              final OutputPort port = new DefaultOutputPort(vertx, output);
              log.debug(String.format("%s - Opening out port: %s", DefaultOutputCollector.this, output));
              port.open(new Handler<AsyncResult<Void>>() {
                @Override
                public void handle(AsyncResult<Void> result) {
                  if (result.failed()) {
                    log.error(String.format("%s - Failed to open out port: %s", DefaultOutputCollector.this, port));
                    startCounter.fail(result.cause());
                  } else {
                    log.info(String.format("%s - Opened out port: %s", DefaultOutputCollector.this, port));
                    ports.put(port.name(), port);
                    startCounter.succeed();
                  }
                }
              });
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
      
          for (final OutputPort output : ports.values()) {
            log.debug(String.format("%s - Closing out port: %s", DefaultOutputCollector.this, output));
            output.close(new Handler<AsyncResult<Void>>() {
              @Override
              public void handle(AsyncResult<Void> result) {
                if (result.failed()) {
                  log.warn(String.format("%s - Failed to close out port: %s", DefaultOutputCollector.this, output));
                  stopCounter.fail(result.cause());
                } else {
                  log.info(String.format("%s - Closed out port: %s", DefaultOutputCollector.this, output));
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

  @Override
  public String toString() {
    return context.toString();
  }

}
