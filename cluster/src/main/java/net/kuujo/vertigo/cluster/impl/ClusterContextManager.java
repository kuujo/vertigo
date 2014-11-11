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
package net.kuujo.vertigo.cluster.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import net.kuujo.vertigo.ContextManager;
import net.kuujo.vertigo.network.NetworkContext;

/**
 * Cluster based Vertigo context manager.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class ClusterContextManager implements ContextManager {
  private final Vertx vertx;
  private final String address;

  ClusterContextManager(Vertx vertx, String address) {
    this.vertx = vertx;
    this.address = address;
  }

  @Override
  public ContextManager getNetwork(String id, Handler<AsyncResult<NetworkContext>> doneHandler) {
    return this;
  }

  @Override
  public ContextManager deployNetwork(NetworkContext network, Handler<AsyncResult<Void>> doneHandler) {
    return this;
  }

  @Override
  public ContextManager undeployNetwork(NetworkContext network, Handler<AsyncResult<Void>> doneHandler) {
    return this;
  }

}
