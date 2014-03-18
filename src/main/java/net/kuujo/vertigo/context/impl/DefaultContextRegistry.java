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
package net.kuujo.vertigo.context.impl;

import net.kuujo.vertigo.cluster.ClusterClient;
import net.kuujo.vertigo.cluster.ClusterEvent;
import net.kuujo.vertigo.context.ComponentContext;
import net.kuujo.vertigo.context.Context;
import net.kuujo.vertigo.context.ContextRegistry;
import net.kuujo.vertigo.context.InstanceContext;
import net.kuujo.vertigo.context.NetworkContext;
import net.kuujo.vertigo.util.CountingCompletionHandler;
import net.kuujo.vertigo.util.serializer.Serializer;
import net.kuujo.vertigo.util.serializer.SerializerFactory;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.impl.DefaultFutureResult;

/**
 * Default context registry.
 *
 * @author Jordan Halterman
 */
public class DefaultContextRegistry implements ContextRegistry {
  private final Serializer serializer = SerializerFactory.getSerializer(Context.class);
  private final ClusterClient cluster;

  public DefaultContextRegistry(ClusterClient cluster) {
    this.cluster = cluster;
  }

  @Override
  public ContextRegistry registerContext(final NetworkContext network, final Handler<AsyncResult<NetworkContext>> doneHandler) {
    cluster.watch(network.address(), new Handler<ClusterEvent>() {
      @Override
      public void handle(ClusterEvent event) {
        network.notify(serializer.deserializeString(event.<String>value(), NetworkContext.class));
      }
    }, new Handler<AsyncResult<Void>>() {
      @Override
      @SuppressWarnings({"unchecked", "rawtypes"})
      public void handle(AsyncResult<Void> result) {
        if (result.failed()) {
          new DefaultFutureResult<NetworkContext>(result.cause()).setHandler(doneHandler);
        }
        else {
          final CountingCompletionHandler<Void> counter = new CountingCompletionHandler<>(network.components().size());
          counter.setHandler(new Handler<AsyncResult<Void>>() {
            @Override
            public void handle(AsyncResult<Void> result) {
              if (result.failed()) {
                new DefaultFutureResult<NetworkContext>(result.cause()).setHandler(doneHandler);
              }
              else {
                new DefaultFutureResult<NetworkContext>(network).setHandler(doneHandler);
              }
            }
          });

          for (ComponentContext component : network.components()) {
            registerContext(component, new Handler<AsyncResult<ComponentContext>>() {
              @Override
              public void handle(AsyncResult<ComponentContext> result) {
                if (result.failed()) {
                  counter.fail(result.cause());
                }
                else {
                  counter.succeed();
                }
              }
            });
          }
        }
      }
    });
    return this;
  }

  @Override
  public <T extends ComponentContext<T>> ContextRegistry registerContext(final T component, final Handler<AsyncResult<T>> doneHandler) {
    cluster.watch(component.address(), new Handler<ClusterEvent>() {
      @Override
      @SuppressWarnings("unchecked")
      public void handle(ClusterEvent event) {
        component.notify((T) serializer.deserializeString(event.<String>value(), ComponentContext.class));
      }
    }, new Handler<AsyncResult<Void>>() {
      @Override
      public void handle(AsyncResult<Void> result) {
        if (result.failed()) {
          new DefaultFutureResult<T>(result.cause()).setHandler(doneHandler);
        }
        else {
          final CountingCompletionHandler<Void> counter = new CountingCompletionHandler<>(component.instances().size());
          counter.setHandler(new Handler<AsyncResult<Void>>() {
            @Override
            public void handle(AsyncResult<Void> result) {
              if (result.failed()) {
                new DefaultFutureResult<T>(result.cause()).setHandler(doneHandler);
              }
              else {
                new DefaultFutureResult<T>(component).setHandler(doneHandler);
              }
            }
          });

          for (InstanceContext instance : component.instances()) {
            registerContext(instance, new Handler<AsyncResult<InstanceContext>>() {
              @Override
              public void handle(AsyncResult<InstanceContext> result) {
                if (result.failed()) {
                  counter.fail(result.cause());
                }
                else {
                  counter.succeed();
                }
              }
            });
          }
        }
      }
    });
    return this;
  }

  @Override
  public ContextRegistry registerContext(final InstanceContext instance, final Handler<AsyncResult<InstanceContext>> doneHandler) {
    cluster.watch(instance.address(), new Handler<ClusterEvent>() {
      @Override
      public void handle(ClusterEvent event) {
        
      }
    }, new Handler<AsyncResult<Void>>() {
      @Override
      public void handle(AsyncResult<Void> result) {
        if (result.failed()) {
          new DefaultFutureResult<InstanceContext>(result.cause()).setHandler(doneHandler);
        }
        else {
          new DefaultFutureResult<InstanceContext>(instance).setHandler(doneHandler);
        }
      }
    });
    return this;
  }

}
