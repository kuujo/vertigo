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

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.ServiceHelper;
import io.vertx.core.Vertx;
import net.kuujo.vertigo.network.Network;
import net.kuujo.vertigo.network.NetworkReference;
import net.kuujo.vertigo.spi.VertigoFactory;

/**
 * The primary Vertigo API.<p>
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
@VertxGen
public interface Vertigo {

  /**
   * Creates a new Vertigo instance.
   *
   * @return The new Vertigo instance.
   */
  static Vertigo vertigo() {
    return factory.vertigo();
  }

  /**
   * Creates a new Vertigo instance.
   *
   * @param options The Vertigo options.
   * @return The Vertigo instance.
   */
  static Vertigo vertigo(VertigoOptions options) {
    return factory.vertigo(options);
  }

  /**
   * Creates a new Vertigo instance.
   *
   * @param vertx The underlying Vert.x instance.
   * @return The Vertigo instance.
   */
  static Vertigo vertigo(Vertx vertx) {
    return factory.vertigo(vertx);
  }

  /**
   * Asynchronously creates a new Vertigo instance.
   *
   * @param options The Vertigo options.
   * @param resultHandler An asynchronous handler to be called once complete.
   */
  static void vertigoAsync(VertigoOptions options, Handler<AsyncResult<Vertigo>> resultHandler) {
    factory.vertigoAsync(options, resultHandler);
  }

  /**
   * Returns a network reference for the given network.
   *
   * @param id The unique ID of the network for which to return a reference.
   * @return The network reference.
   */
  NetworkReference network(String id);

  /**
   * Deploys a network to an anonymous local-only cluster.<p>
   *
   * If the given network configuration's name matches the name of a network
   * that is already running in the cluster then the given configuration will
   * be <b>merged</b> with the running network's configuration. This allows networks
   * to be dynamically updated with partial configurations. If the configuration
   * matches the already running configuration then no changes will occur, so it's
   * not necessary to check whether a network is already running if the configuration
   * has not been altered.
   *
   * @param network The configuration of the network to deploy.
   * @return The Vertigo instance.
   */
  @Fluent
  Vertigo deployNetwork(Network network);

  /**
   * Deploys a network to an anonymous local-only cluster.<p>
   *
   * If the given network configuration's name matches the name of a network
   * that is already running in the cluster then the given configuration will
   * be <b>merged</b> with the running network's configuration. This allows networks
   * to be dynamically updated with partial configurations. If the configuration
   * matches the already running configuration then no changes will occur, so it's
   * not necessary to check whether a network is already running if the configuration
   * has not been altered.
   *
   * @param network The configuration of the network to deploy.
   * @param doneHandler An asynchronous handler to be called once the network has
   *        completed deployment.
   * @return The Vertigo instance.
   */
  @Fluent
  Vertigo deployNetwork(Network network, Handler<AsyncResult<NetworkReference>> doneHandler);

  /**
   * Undeploys a complete network.<p>
   *
   * This method does not require a network configuration for undeployment. Vertigo
   * will load the configuration from the fault-tolerant data store and undeploy
   * components internally. This allows networks to be undeployed without the network
   * configuration.
   *
   * @param id The ID of the network to undeploy.
   * @return The Vertigo instance.
   */
  @Fluent
  Vertigo undeployNetwork(String id);

  /**
   * Undeploys a complete network.<p>
   *
   * This method does not require a network configuration for undeployment. Vertigo
   * will load the configuration from the fault-tolerant data store and undeploy
   * components internally. This allows networks to be undeployed without the network
   * configuration.
   *
   * @param id The ID of the network to undeploy.
   * @param doneHandler An asynchronous handler to be called once the network is undeployed.
   * @return The Vertigo instance.
   */
  @Fluent
  Vertigo undeployNetwork(String id, Handler<AsyncResult<Void>> doneHandler);

  static final VertigoFactory factory = ServiceHelper.loadFactory(VertigoFactory.class);

}
