/*
 * Copyright 2013-2014 the original author or authors.
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
package net.kuujo.vertigo.component.impl;

import java.util.ArrayList;
import java.util.List;

import net.kuujo.vertigo.cluster.VertigoCluster;
import net.kuujo.vertigo.component.Component;
import net.kuujo.vertigo.component.ComponentCoordinator;
import net.kuujo.vertigo.context.ComponentContext;
import net.kuujo.vertigo.context.InstanceContext;
import net.kuujo.vertigo.hooks.ComponentHook;
import net.kuujo.vertigo.hooks.InputHook;
import net.kuujo.vertigo.hooks.OutputHook;
import net.kuujo.vertigo.input.InputCollector;
import net.kuujo.vertigo.input.impl.DefaultInputCollector;
import net.kuujo.vertigo.logging.PortLogger;
import net.kuujo.vertigo.logging.PortLoggerFactory;
import net.kuujo.vertigo.output.OutputCollector;
import net.kuujo.vertigo.output.impl.DefaultOutputCollector;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Future;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.impl.DefaultFutureResult;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.core.logging.impl.LoggerFactory;
import org.vertx.java.platform.Container;

/**
 * A default component implementation.
 *
 * @author Jordan Halterman
 */
public class DefaultComponent implements Component {
  protected final Vertx vertx;
  protected final Container container;
  protected final VertigoCluster cluster;
  private final ComponentCoordinator coordinator;
  protected Logger logger;
  private final String address;
  protected InstanceContext context;
  protected InputCollector input;
  protected OutputCollector output;
  private List<ComponentHook> hooks = new ArrayList<>();
  private boolean started;

  private final InputHook inputHook = new InputHook() {
    @Override
    public void handleReceive(String port, String messageId) {
      for (ComponentHook hook : hooks) {
        hook.handleReceive(port, messageId);
      }
    }
  };

  private final OutputHook outputHook = new OutputHook() {
    @Override
    public void handleSend(String port, String messageId) {
      for (ComponentHook hook : hooks) {
        hook.handleSend(port, messageId);
      }
    }
  };

  protected DefaultComponent(String network, String address, Vertx vertx, Container container, VertigoCluster cluster) {
    this.address = address;
    this.vertx = vertx;
    this.container = container;
    this.cluster = cluster;
    this.coordinator = new DefaultComponentCoordinator(network, address, cluster);
    // Until the component is started, create a Vert.x logger. Once the component has
    // been started a port-based logger will be created.
    this.logger = LoggerFactory.getLogger(String.format("%s-%s", getClass().getCanonicalName(), address));
  }

  @Override
  public Vertx vertx() {
    return vertx;
  }

  @Override
  public Container container() {
    return container;
  }

  @Override
  public InputCollector input() {
    return input;
  }

  @Override
  public OutputCollector output() {
    return output;
  }

  @Override
  public InstanceContext context() {
    return context;
  }

  @Override
  public VertigoCluster cluster() {
    return cluster;
  }

  @Override
  public Logger logger() {
    return logger;
  }

  @Override
  public Component addHook(ComponentHook hook) {
    if (hooks.isEmpty()) {
      input.addHook(inputHook);
      output.addHook(outputHook);
    }
    hooks.add(hook);
    return  this;
  }

  /**
   * Calls start hooks.
   */
  private void hookStart() {
    for (ComponentHook hook : hooks) {
      hook.handleStart(this);
    }
  }

  /**
   * Calls stop hooks
   */
  @SuppressWarnings("unused")
  private void hookStop() {
    for (ComponentHook hook : hooks) {
      hook.handleStop(this);
    }
  }

  /**
   * Sets up the component.
   */
  private void setup(final Handler<AsyncResult<Void>> doneHandler) {
    coordinator.start(new Handler<AsyncResult<InstanceContext>>() {
      @Override
      public void handle(AsyncResult<InstanceContext> result) {
        if (result.failed()) {
          new DefaultFutureResult<Void>(result.cause()).setHandler(doneHandler);
        } else {
          context = result.result();

          // Set up input and output collectors and add hooks for each.
          input = new DefaultInputCollector(vertx, context.input(), cluster);
          output = new DefaultOutputCollector(vertx, context.output(), cluster);
          for (ComponentHook hook : context.<ComponentContext<?>>component().hooks()) {
            addHook(hook);
          }

          // Set up a port-based logger.
          logger = PortLoggerFactory.getLogger(String.format("%s-%s", getClass().getCanonicalName(), address), output.port(PortLogger.LOGGER_PORT));

          output.open(new Handler<AsyncResult<Void>>() {
            @Override
            public void handle(AsyncResult<Void> result) {
              if (result.failed()) {
                new DefaultFutureResult<Void>(result.cause()).setHandler(doneHandler);
              } else {
                input.open(new Handler<AsyncResult<Void>>() {
                  @Override
                  public void handle(AsyncResult<Void> result) {
                    if (result.failed()) {
                      new DefaultFutureResult<Void>(result.cause()).setHandler(doneHandler);
                    } else {
                      coordinator.resume();
                    }
                  }
                });
              }
            }
          });
        }
      }
    });
    coordinator.resumeHandler(new Handler<Void>() {
      @Override
      public void handle(Void _) {
        started = true;
        new DefaultFutureResult<Void>((Void) null).setHandler(doneHandler);
      }
    });
  }

  @Override
  public Component start() {
    return start(null);
  }

  @Override
  public Component start(Handler<AsyncResult<Component>> doneHandler) {
    final Future<Component> future = new DefaultFutureResult<Component>().setHandler(doneHandler);
    if (!started) {
      setup(new Handler<AsyncResult<Void>>() {
        @Override
        public void handle(AsyncResult<Void> result) {
          if (result.failed()) {
            future.setFailure(result.cause());
          } else {
            hookStart();
            future.setResult(DefaultComponent.this);
          }
        }
      });
    }
    else {
      vertx.runOnContext(new Handler<Void>() {
        @Override
        public void handle(Void _) {
          future.setResult(DefaultComponent.this);
        }
      });
    }
    return this;
  }

}
