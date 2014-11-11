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
package net.kuujo.vertigo.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import net.kuujo.vertigo.ContextManager;
import net.kuujo.vertigo.Vertigo;
import net.kuujo.vertigo.VertigoOptions;
import net.kuujo.vertigo.network.Network;
import net.kuujo.vertigo.network.NetworkContext;
import net.kuujo.vertigo.network.NetworkReference;
import net.kuujo.vertigo.network.impl.NetworkReferenceImpl;

/**
 * Vertigo implementation.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class VertigoImpl implements Vertigo {
  private final Vertx vertx;
  private final VertigoOptions options;
  private final ContextManager manager;

  public VertigoImpl(Vertx vertx, VertigoOptions options) {
    this.vertx = vertx;
    this.options = options;
    this.manager = ContextManager.manager(vertx, options);
  }

  @Override
  public Vertigo network(String id, Handler<AsyncResult<NetworkReference>> resultHandler) {
    manager.getNetwork(id, result -> {
      if (result.failed()) {
        Future.<NetworkReference>completedFuture(result.cause()).setHandler(resultHandler);
      } else {
        Future.<NetworkReference>completedFuture(new NetworkReferenceImpl(vertx, result.result())).setHandler(resultHandler);
      }
    });
    return this;
  }

  @Override
  public Vertigo deployNetwork(Network network) {
    return deployNetwork(network, null);
  }

  @Override
  public Vertigo deployNetwork(Network network, Handler<AsyncResult<NetworkReference>> doneHandler) {
    NetworkContext context = ContextBuilder.buildContext(network);
    manager.deployNetwork(context, result -> {
      if (result.failed()) {
        Future.<NetworkReference>completedFuture(result.cause()).setHandler(doneHandler);
      } else {
        Future.<NetworkReference>completedFuture(new NetworkReferenceImpl(vertx, context)).setHandler(doneHandler);
      }
    });
    return this;
  }

  @Override
  public Vertigo undeployNetwork(String id) {
    return undeployNetwork(id, null);
  }

  @Override
  public Vertigo undeployNetwork(String id, Handler<AsyncResult<Void>> doneHandler) {
    NetworkContext context = ContextBuilder.buildContext(Network.network(id));
    manager.undeployNetwork(context, doneHandler);
    return this;
  }

}
