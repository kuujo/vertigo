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
import io.vertx.core.json.JsonObject;
import net.kuujo.vertigo.network.NetworkDefinition;
import net.kuujo.vertigo.network.NetworkContext;
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
   * Creates a new uniquely named network.
   *
   * @return A new network instance.
   * @see Vertigo#createNetwork(String)
   */
  NetworkDefinition createNetwork();

  /**
   * Creates a new network.
   * 
   * @param name The network name.
   * @return A new network instance.
   * @see Vertigo#createNetwork()
   */
  NetworkDefinition createNetwork(String name);

  /**
   * Creates a network configuration from json.
   *
   * @param json A json network configuration.
   * @return A network configuration.
   * @see Vertigo#createNetwork(String)
   */
  NetworkDefinition createNetwork(JsonObject json);

  /**
   * Deploys a bare network to an anonymous local-only cluster.<p>
   *
   * The network will be deployed with no components and no connections.
   *
   * @param name The name of the network to deploy.
   * @return The Vertigo instance.
   */
  @Fluent
  Vertigo deployNetwork(String name);

  /**
   * Deploys a bare network to an anonymous local-only cluster.<p>
   *
   * The network will be deployed with no components and no connections.
   *
   * @param name The name of the network to deploy.
   * @param doneHandler An asynchronous handler to be called once the network has
   *        completed deployment.
   * @return The Vertigo instance.
   */
  @Fluent
  Vertigo deployNetwork(final String name, final Handler<AsyncResult<NetworkContext>> doneHandler);

  /**
   * Deploys a json network to an anonymous local-only cluster.<p>
   *
   * The JSON network configuration will be converted to a {@link net.kuujo.vertigo.network.NetworkDefinition} before
   * being deployed to the cluster. The conversion is done synchronously, so if the
   * configuration is invalid then this method may throw an exception.
   *
   * @param network The JSON network configuration. For the configuration format see
   *        the project documentation.
   * @return The Vertigo instance.
   */
  @Fluent
  Vertigo deployNetwork(JsonObject network);

  /**
   * Deploys a json network to an anonymous local-only cluster.<p>
   *
   * The JSON network configuration will be converted to a {@link net.kuujo.vertigo.network.NetworkDefinition} before
   * being deployed to the cluster. The conversion is done synchronously, so if the
   * configuration is invalid then this method may throw an exception.
   *
   * @param network The JSON network configuration. For the configuration format see
   *        the project documentation.
   * @param doneHandler An asynchronous handler to be called once the network has
   *        completed deployment.
   * @return The Vertigo instance.
   */
  @Fluent
  Vertigo deployNetwork(final JsonObject network, final Handler<AsyncResult<NetworkContext>> doneHandler);

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
  Vertigo deployNetwork(NetworkDefinition network);

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
  Vertigo deployNetwork(final NetworkDefinition network, final Handler<AsyncResult<NetworkContext>> doneHandler);

  /**
   * Deploys a bare network to a specific cluster.<p>
   *
   * The network will be deployed with no components and no connections.
   *
   * @param cluster The cluster to which to deploy the network.
   * @param name The name of the network to deploy.
   * @return The Vertigo instance.
   */
  @Fluent
  Vertigo deployNetwork(String cluster, String name);

  /**
   * Deploys a bare network to a specific cluster.<p>
   *
   * The network will be deployed with no components and no connections.
   *
   * @param cluster The cluster to which to deploy the network.
   * @param name The name of the network to deploy.
   * @param doneHandler An asynchronous handler to be called once the network has
   *        completed deployment.
   * @return The Vertigo instance.
   */
  @Fluent
  Vertigo deployNetwork(String cluster, final String name, final Handler<AsyncResult<NetworkContext>> doneHandler);

  /**
   * Deploys a json network to a specific cluster.<p>
   *
   * The JSON network configuration will be converted to a {@link net.kuujo.vertigo.network.NetworkDefinition} before
   * being deployed to the cluster. The conversion is done synchronously, so if the
   * configuration is invalid then this method may throw an exception.
   *
   * @param cluster The cluster to which to deploy the network.
   * @param network The JSON network configuration. For the configuration format see
   *        the project documentation.
   * @return The Vertigo instance.
   */
  @Fluent
  Vertigo deployNetwork(String cluster, JsonObject network);

  /**
   * Deploys a json network to a specific cluster.<p>
   *
   * The JSON network configuration will be converted to a {@link net.kuujo.vertigo.network.NetworkDefinition} before
   * being deployed to the cluster. The conversion is done synchronously, so if the
   * configuration is invalid then this method may throw an exception.
   *
   * @param cluster The cluster to which to deploy the network.
   * @param network The JSON network configuration. For the configuration format see
   *        the project documentation.
   * @param doneHandler An asynchronous handler to be called once the network has
   *        completed deployment.
   * @return The Vertigo instance.
   */
  @Fluent
  Vertigo deployNetwork(String cluster, final JsonObject network, final Handler<AsyncResult<NetworkContext>> doneHandler);

  /**
   * Deploys a network to a specific cluster.<p>
   *
   * If the given network configuration's name matches the name of a network
   * that is already running in the cluster then the given configuration will
   * be <b>merged</b> with the running network's configuration. This allows networks
   * to be dynamically updated with partial configurations. If the configuration
   * matches the already running configuration then no changes will occur, so it's
   * not necessary to check whether a network is already running if the configuration
   * has not been altered.
   *
   * @param cluster The cluster to which to deploy the network.
   * @param network The configuration of the network to deploy.
   * @return The Vertigo instance.
   */
  @Fluent
  Vertigo deployNetwork(String cluster, NetworkDefinition network);

  /**
   * Deploys a network to a specific cluster.<p>
   *
   * If the given network configuration's name matches the name of a network
   * that is already running in the cluster then the given configuration will
   * be <b>merged</b> with the running network's configuration. This allows networks
   * to be dynamically updated with partial configurations. If the configuration
   * matches the already running configuration then no changes will occur, so it's
   * not necessary to check whether a network is already running if the configuration
   * has not been altered.
   *
   * @param cluster The cluster to which to deploy the network.
   * @param network The configuration of the network to deploy.
   * @param doneHandler An asynchronous handler to be called once the network has
   *        completed deployment.
   * @return The Vertigo instance.
   */
  @Fluent
  Vertigo deployNetwork(String cluster, final NetworkDefinition network, final Handler<AsyncResult<NetworkContext>> doneHandler);

  /**
   * Undeploys a complete network from the given cluster.<p>
   *
   * This method does not require a network configuration for undeployment. Vertigo
   * will load the configuration from the fault-tolerant data store and undeploy
   * components internally. This allows networks to be undeployed without the network
   * configuration.
   *
   * @param cluster The cluster from which to undeploy the network.
   * @param name The name of the network to undeploy.
   * @return The Vertigo instance.
   */
  @Fluent
  Vertigo undeployNetwork(String cluster, String name);

  /**
   * Undeploys a complete network from the given cluster.<p>
   *
   * This method does not require a network configuration for undeployment. Vertigo
   * will load the configuration from the fault-tolerant data store and undeploy
   * components internally. This allows networks to be undeployed without the network
   * configuration.
   *
   * @param cluster The cluster from which to undeploy the network.
   * @param name The name of the network to undeploy.
   * @param doneHandler An asynchronous handler to be called once the network is undeployed.
   * @return The Vertigo instance.
   */
  @Fluent
  Vertigo undeployNetwork(String cluster, final String name, final Handler<AsyncResult<Void>> doneHandler);

  /**
   * Undeploys a network from the given cluster from a json configuration.<p>
   *
   * The JSON configuration will immediately be converted to a {@link net.kuujo.vertigo.network.NetworkDefinition} prior
   * to undeploying the network. In order to undeploy the entire network, the configuration
   * should be the same as the deployed configuration. Vertigo will use configuration
   * components to determine whether two configurations are identical. If the given
   * configuration is not identical to the running configuration, any components or
   * connections in the json configuration that are present in the running network
   * will be closed and removed from the running network.
   *
   * @param cluster The cluster from which to undeploy the network.
   * @param network The JSON configuration to undeploy. For the configuration format see
   *        the project documentation.
   * @return The Vertigo instance.
   */
  @Fluent
  Vertigo undeployNetwork(String cluster, JsonObject network);

  /**
   * Undeploys a network from the given cluster from a json configuration.<p>
   *
   * The JSON configuration will immediately be converted to a {@link net.kuujo.vertigo.network.NetworkDefinition} prior
   * to undeploying the network. In order to undeploy the entire network, the configuration
   * should be the same as the deployed configuration. Vertigo will use configuration
   * components to determine whether two configurations are identical. If the given
   * configuration is not identical to the running configuration, any components or
   * connections in the json configuration that are present in the running network
   * will be closed and removed from the running network.
   *
   * @param cluster The cluster from which to undeploy the network.
   * @param network The JSON configuration to undeploy. For the configuration format see
   *        the project documentation.
   * @param doneHandler An asynchronous handler to be called once the configuration is undeployed.
   * @return The Vertigo instance.
   */
  @Fluent
  Vertigo undeployNetwork(String cluster, final JsonObject network, final Handler<AsyncResult<Void>> doneHandler);

  /**
   * Undeploys a network from the given cluster.<p>
   *
   * This method supports both partial and complete undeployment of networks. When
   * undeploying networks by specifying a {@link net.kuujo.vertigo.network.NetworkDefinition}, the network configuration
   * should contain all components and connections that are being undeployed. If the
   * configuration's components and connections match all deployed components and
   * connections then the entire network will be undeployed.
   *
   * @param cluster The cluster from which to undeploy the network.
   * @param network The network configuration to undeploy.
   * @return The Vertigo instance.
   */
  @Fluent
  Vertigo undeployNetwork(String cluster, NetworkDefinition network);

  /**
   * Undeploys a network from the given cluster.<p>
   *
   * This method supports both partial and complete undeployment of networks. When
   * undeploying networks by specifying a {@link net.kuujo.vertigo.network.NetworkDefinition}, the network configuration
   * should contain all components and connections that are being undeployed. If the
   * configuration's components and connections match all deployed components and
   * connections then the entire network will be undeployed.
   *
   * @param cluster The cluster from which to undeploy the network.
   * @param network The network configuration to undeploy.
   * @param doneHandler An asynchronous handler to be called once the configuration is undeployed.
   * @return The Vertigo instance.
   */
  @Fluent
  Vertigo undeployNetwork(String cluster, final NetworkDefinition network, final Handler<AsyncResult<Void>> doneHandler);

  static final VertigoFactory factory = ServiceHelper.loadFactory(VertigoFactory.class);

}
