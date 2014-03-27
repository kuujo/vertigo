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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.kuujo.vertigo.cluster.VertigoCluster;
import net.kuujo.vertigo.context.OutputContext;
import net.kuujo.vertigo.context.OutputPortContext;
import net.kuujo.vertigo.context.impl.DefaultOutputPortContext;
import net.kuujo.vertigo.hooks.OutputHook;
import net.kuujo.vertigo.network.auditor.Acker;
import net.kuujo.vertigo.output.OutputCollector;
import net.kuujo.vertigo.output.OutputPort;
import net.kuujo.vertigo.util.CountingCompletionHandler;
import net.kuujo.vertigo.util.Observer;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.impl.DefaultFutureResult;

/**
 * Default output collector implementation.
 *
 * @author Jordan Halterman
 */
public class DefaultOutputCollector implements OutputCollector, Observer<OutputContext> {
  private final Vertx vertx;
  private final OutputContext context;
  private final VertigoCluster cluster;
  private final Acker acker;
  private final List<OutputHook> hooks = new ArrayList<>();
  private final Map<String, OutputPort> ports = new HashMap<>();
  private boolean started;

  public DefaultOutputCollector(Vertx vertx, OutputContext context, VertigoCluster cluster, Acker acker) {
    this.vertx = vertx;
    this.context = context;
    this.cluster = cluster;
    this.acker = acker;
  }

  @Override
  public OutputContext context() {
    return context;
  }

  @Override
  public OutputCollector addHook(OutputHook hook) {
    hooks.add(hook);
    for (OutputPort port : ports.values()) {
      port.addHook(hook);
    }
    return this;
  }

  @Override
  public Collection<OutputPort> ports() {
    return ports.values();
  }

  @Override
  public OutputPort port(String name) {
    OutputPort port = ports.get(name);
    if (port == null) {
      OutputPortContext context = DefaultOutputPortContext.Builder.newBuilder()
          .setAddress(UUID.randomUUID().toString())
          .setName(name)
          .build();
      port = new DefaultOutputPort(vertx, context, cluster, acker);
      ports.put(name, port);
    }
    return port;
  }

  @Override
  public void update(OutputContext update) {
    Iterator<OutputPort> iter = ports.values().iterator();
    while (iter.hasNext()) {
      OutputPort port = iter.next();
      boolean exists = false;
      for (OutputPortContext output : update.ports()) {
        if (output.equals(port.context())) {
          exists = true;
          break;
        }
      }
      if (!exists) {
        port.close();
        iter.remove();
      }
    }

    for (OutputPortContext output : update.ports()) {
      boolean exists = false;
      for (OutputPort port : ports.values()) {
        if (port.context().equals(output)) {
          exists = true;
          break;
        }
      }
      if (!exists) {
        ports.put(output.name(), new DefaultOutputPort(vertx, output, cluster, acker).open());
      }
    }
  }

  @Override
  public OutputCollector open() {
    return open(null);
  }

  @Override
  public OutputCollector open(final Handler<AsyncResult<Void>> doneHandler) {
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
        }
      });

      for (OutputPortContext port : context.ports()) {
        if (ports.containsKey(port.name())) {
          ((DefaultOutputPort) ports.get(port.name())).setContext(port).open(new Handler<AsyncResult<Void>>() {
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
          ports.put(port.name(), new DefaultOutputPort(vertx, port, cluster, acker).open(new Handler<AsyncResult<Void>>() {
            @Override
            public void handle(AsyncResult<Void> result) {
              if (result.failed()) {
                startCounter.fail(result.cause());
              } else {
                startCounter.succeed();
              }
            }
          }));
          for (OutputHook hook : hooks) {
            ports.get(port.name()).addHook(hook);
          }
        }
      }
      started = true;
    } else {
      new DefaultFutureResult<Void>((Void) null).setHandler(doneHandler);
    }
    return this;
  }

  @Override
  public void close() {
    close(null);
  }

  @Override
  public void close(final Handler<AsyncResult<Void>> doneHandler) {
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
        }
      });
  
      for (OutputPort output : ports.values()) {
        output.close(new Handler<AsyncResult<Void>>() {
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
    }
  }

}
