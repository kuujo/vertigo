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
package net.kuujo.vertigo.component.impl;

import net.kuujo.vertigo.component.Coordinator;
import net.kuujo.vertigo.context.ContextRegistry;
import net.kuujo.vertigo.context.InstanceContext;
import net.kuujo.vertigo.context.impl.DefaultContextRegistry;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.impl.DefaultFutureResult;

/**
 * Default coordinator implementation.
 *
 * @author Jordan Halterman
 */
public class DefaultCoordinator implements Coordinator {
  private final InstanceContext context;
  private final ContextRegistry registry;

  public DefaultCoordinator(InstanceContext context) {
    this.context = context;
    this.registry = new DefaultContextRegistry(context.cluster());
  }

  @Override
  public InstanceContext context() {
    return context;
  }

  @Override
  public Coordinator start(final Handler<AsyncResult<Void>> doneHandler) {
    registry.registerContext(context, new Handler<AsyncResult<InstanceContext>>() {
      @Override
      public void handle(AsyncResult<InstanceContext> result) {
        if (result.failed()) {
          new DefaultFutureResult<Void>(result.cause()).setHandler(doneHandler);
        }
        else {
          new DefaultFutureResult<Void>((Void) null).setHandler(doneHandler);
        }
      }
    });
    return this;
  }

  @Override
  public void stop(Handler<AsyncResult<Void>> doneHandler) {
    registry.unregisterContext(context, doneHandler);
  }

}
