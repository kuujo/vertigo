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
package net.kuujo.vertigo.input.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.kuujo.vertigo.cluster.VertigoCluster;
import net.kuujo.vertigo.context.InputContext;
import net.kuujo.vertigo.context.InputPortContext;
import net.kuujo.vertigo.context.impl.DefaultInputPortContext;
import net.kuujo.vertigo.hooks.InputHook;
import net.kuujo.vertigo.input.InputCollector;
import net.kuujo.vertigo.input.InputPort;
import net.kuujo.vertigo.network.auditor.Acker;
import net.kuujo.vertigo.util.CountingCompletionHandler;
import net.kuujo.vertigo.util.Observer;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.impl.DefaultFutureResult;

/**
 * Default input collector implementation.
 *
 * @author Jordan Halterman
 */
public class DefaultInputCollector implements InputCollector, Observer<InputContext> {
  private final Vertx vertx;
  private final InputContext context;
  private final VertigoCluster cluster;
  private final Acker acker;
  private final List<InputHook> hooks = new ArrayList<>();
  private final Map<String, InputPort> ports = new HashMap<>();
  private boolean started;

  public DefaultInputCollector(Vertx vertx, InputContext context, VertigoCluster cluster, Acker acker) {
    this.vertx = vertx;
    this.context = context;
    this.cluster = cluster;
    this.acker = acker;
  }

  @Override
  public InputContext context() {
    return context;
  }

  @Override
  public InputCollector addHook(InputHook hook) {
    hooks.add(hook);
    for (InputPort port : ports.values()) {
      port.addHook(hook);
    }
    return this;
  }

  @Override
  public Collection<InputPort> ports() {
    return ports.values();
  }

  @Override
  public InputPort port(String name) {
    InputPort port = ports.get(name);
    if (port == null) {
      InputPortContext context = DefaultInputPortContext.Builder.newBuilder()
          .setAddress(UUID.randomUUID().toString())
          .setName(name)
          .build();
      port = new DefaultInputPort(vertx, context, cluster, acker);
      ports.put(name, port);
    }
    return port;
  }

  @Override
  public void update(InputContext update) {
    Iterator<InputPort> iter = ports.values().iterator();
    while (iter.hasNext()) {
      InputPort port = iter.next();
      boolean exists = false;
      for (InputPortContext input : update.ports()) {
        if (input.equals(port.context())) {
          exists = true;
          break;
        }
      }
      if (!exists) {
        port.close();
        iter.remove();
      }
    }

    for (InputPortContext input : update.ports()) {
      boolean exists = false;
      for (InputPort port : ports.values()) {
        if (port.context().equals(input)) {
          exists = true;
          break;
        }
      }
      if (!exists) {
        ports.put(input.name(), new DefaultInputPort(vertx, input, cluster, acker).open());
      }
    }
  }

  @Override
  public InputCollector open() {
    return open(null);
  }

  @Override
  public InputCollector open(final Handler<AsyncResult<Void>> doneHandler) {
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

      for (InputPortContext port : context.ports()) {
        if (ports.containsKey(port.name())) {
          ((DefaultInputPort) ports.get(port.name())).setContext(port).open(new Handler<AsyncResult<Void>>() {
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
          ports.put(port.name(), new DefaultInputPort(vertx, port, cluster, acker).open(new Handler<AsyncResult<Void>>() {
            @Override
            public void handle(AsyncResult<Void> result) {
              if (result.failed()) {
                startCounter.fail(result.cause());
              } else {
                startCounter.succeed();
              }
            }
          }));
          for (InputHook hook : hooks) {
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
    }
  }

}
