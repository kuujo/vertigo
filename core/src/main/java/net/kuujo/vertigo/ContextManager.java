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
package net.kuujo.vertigo;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.ServiceHelper;
import io.vertx.core.Vertx;
import net.kuujo.vertigo.network.NetworkContext;
import net.kuujo.vertigo.spi.ContextManagerFactory;

/**
 * Vertigo context manager.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public interface ContextManager {

  /**
   * Loads a context manager.
   *
   * @param vertx The current Vert.x instance.
   * @param options The Vertigo options.
   * @return A new context manager.
   */
  static ContextManager manager(Vertx vertx, VertigoOptions options) {
    return factory.createContextManager(vertx, options);
  }

  /**
   * Loads a network context.
   *
   * @param id The network context ID.
   * @param doneHandler An asynchronous handler to be called once the network context has been loaded.
   * @return The context manager.
   */
  ContextManager getNetwork(String id, Handler<AsyncResult<NetworkContext>> doneHandler);

  /**
   * Deploys a network.
   *
   * @param network The network to deploy.
   * @param doneHandler An asynchronous handler to be called once the network has been deployed.
   * @return The context manager.
   */
  ContextManager deployNetwork(NetworkContext network, Handler<AsyncResult<Void>> doneHandler);

  /**
   * Undeploys a network.
   *
   * @param network The network to undeploy.
   * @param doneHandler An asynchronous handler to be called once complete.
   * @return The context manager.
   */
  ContextManager undeployNetwork(NetworkContext network, Handler<AsyncResult<Void>> doneHandler);

  static ContextManagerFactory factory = ServiceHelper.loadFactory(ContextManagerFactory.class);

}
