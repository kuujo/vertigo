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

import java.util.List;

import net.kuujo.vertigo.cluster.Cluster;
import net.kuujo.vertigo.component.Component;
import net.kuujo.vertigo.component.ComponentCoordinator;
import net.kuujo.vertigo.component.InstanceContext;
import net.kuujo.vertigo.hook.ComponentHook;
import net.kuujo.vertigo.io.InputCollector;
import net.kuujo.vertigo.io.OutputCollector;
import net.kuujo.vertigo.io.impl.DefaultInputCollector;
import net.kuujo.vertigo.io.impl.DefaultOutputCollector;
import net.kuujo.vertigo.io.logging.PortLoggerFactory;
import net.kuujo.vertigo.util.CountingCompletionHandler;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Future;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.impl.DefaultFutureResult;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.platform.Container;

/**
 * A default component implementation.<p>
 *
 * This component implementation simply handles startup of component
 * inputs and outputs and provides an interface to access messaging APIs.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class DefaultComponent implements Component {
  protected final Vertx vertx;
  protected final Container container;
  protected final Cluster cluster;
  protected final Logger logger;
  private final ComponentCoordinator coordinator;
  private final String address;
  protected InstanceContext context;
  protected final DefaultInputCollector input;
  protected final DefaultOutputCollector output;
  private boolean started;

  protected DefaultComponent(InstanceContext context, Vertx vertx, Container container, Cluster cluster) {
    this.address = context.address();
    this.vertx = vertx;
    this.container = container;
    this.context = context;
    this.cluster = cluster;
    this.coordinator = new DefaultComponentCoordinator(context, vertx, cluster);
    this.input = new DefaultInputCollector(vertx, context.input());
    this.output = new DefaultOutputCollector(vertx, context.output());
    this.logger = PortLoggerFactory.getLogger(String.format("%s-%s", getClass().getCanonicalName(), address), output);
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
  public Cluster cluster() {
    return cluster;
  }

  @Override
  public Logger logger() {
    return logger;
  }

  /**
   * Sets up the component.
   */
  private void setup(final Handler<AsyncResult<Void>> doneHandler) {
    // Retrieve the component context from the coordinator (the current cluster).
    // If the context has changed due to a network configuration change, the
    // internal context and input/output connections will be automatically updated.
    coordinator.start(new Handler<AsyncResult<InstanceContext>>() {
      @Override
      public void handle(AsyncResult<InstanceContext> result) {
        if (result.failed()) {
          new DefaultFutureResult<Void>(result.cause()).setHandler(doneHandler);
        } else {
          context = result.result();

          // We have to make sure the input and output collectors are started
          // simultaneously in order to support circular connections. If both
          // input and output aren't started at the same time then circular
          // connections will never open.
          final CountingCompletionHandler<Void> ioHandler = new CountingCompletionHandler<Void>(2);
          ioHandler.setHandler(new Handler<AsyncResult<Void>>() {
            @Override
            public void handle(AsyncResult<Void> result) {
              if (result.failed()) {
                new DefaultFutureResult<Void>(result.cause()).setHandler(doneHandler);
              } else {
                // Tell the coordinator we're ready for the network to start.
                coordinator.resume();
              }
            }
          });

          output.open(ioHandler);
          input.open(ioHandler);
        }
      }
    });

    // The resume handler will be called by the coordinator once the
    // network's manager has indicated that all the components in the
    // network have finished setting up their connections.
    coordinator.resumeHandler(new Handler<Void>() {
      @Override
      @SuppressWarnings("unchecked")
      public void handle(Void _) {
        if (!started) {
          started = true;
          List<ComponentHook> hooks = context.component().hooks();
          for (ComponentHook hook : hooks) {
            hook.handleStart(DefaultComponent.this);
          }
          new DefaultFutureResult<Void>((Void) null).setHandler(doneHandler);
        }
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
            future.setResult(DefaultComponent.this);
          }
        }
      });
    }
    else {
      future.setResult(DefaultComponent.this);
    }
    return this;
  }

}
