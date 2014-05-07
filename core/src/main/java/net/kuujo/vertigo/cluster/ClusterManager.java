/*
w * Copyright 2014 the original author or authors.
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
package net.kuujo.vertigo.cluster;

import java.util.Collection;

import net.kuujo.vertigo.network.ActiveNetwork;
import net.kuujo.vertigo.network.NetworkConfig;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonObject;

/**
 * The cluster manager handles deployment and undeployment of complete
 * or partial Vertigo networks and provides an interface for accessing existing
 * networks in the Vertigo cluster.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public interface ClusterManager {

  /**
   * Returns the cluster address.
   *
   * @return The cluster address.
   */
  String address();

  /**
   * Gets a list of networks deployed in the cluster.
   *
   * @param resultHandler An asynchronous handler to be called with a collection of
   *        {@link ActiveNetwork} instances, each representing a network running in
   *        the cluster.
   * @return The cluster manager.
   */
  ClusterManager getNetworks(Handler<AsyncResult<Collection<ActiveNetwork>>> resultHandler);

  /**
   * Gets a network reference for a deployed network.<p>
   *
   * If the network is running in the cluster then an {@link ActiveNetwork}
   * instance will be returned to the asynchronous handler. The <code>ActiveNetwork</code>
   * can be used to arbitrarily and asynchronously alter the structure of the
   * running network.
   *
   * @param name The name of the network to get.
   * @param resultHandler An asynchronous handler to be called with the result. If the
   *        network exists then an {@link ActiveNetwork} instance which references the
   *        running network will be provided, otherwise, the result will be <code>null</code>
   * @return The cluster manager.
   */
  ClusterManager getNetwork(String name, Handler<AsyncResult<ActiveNetwork>> resultHandler);

  /**
   * Checks whether a network is deployed in the cluster.<p>
   *
   * In order to check whether a network is deployed, the cluster manager attempts
   * to load the network's configuration from the internal Vertigo coordination
   * cluster. If the configuration exists then the cluster manager then checks
   * to see if the network is actually running.
   *
   * @param name The name of the network to check.
   * @param resultHandler An asynchronous handler to be called with the result.
   * @return The cluster manager.
   */
  ClusterManager isDeployed(String name, Handler<AsyncResult<Boolean>> resultHandler);

  /**
   * Deploys a bare network to the cluster.<p>
   *
   * The network will be deployed with no components and no connections. You
   * can add components and connections to the network with an {@link ActiveNetwork}
   * instance.
   *
   * @param name The name of the network to deploy.
   * @return The cluster manager.
   */
  ClusterManager deployNetwork(String name);

  /**
   * Deploys a bare network to the cluster.<p>
   *
   * The network will be deployed with no components and no connections. You
   * can add components and connections to the network with an {@link ActiveNetwork}
   * instance.
   *
   * @param name The name of the network to deploy.
   * @param doneHandler An asynchronous handler to be called once the network has
   *        completed deployment. The handler will be called with an {@link ActiveNetwork}
   *        instance which can be used to add or remove components and connections from
   *        the network.
   * @return The cluster manager.
   */
  ClusterManager deployNetwork(String name, Handler<AsyncResult<ActiveNetwork>> doneHandler);

  /**
   * Deploys a json network to the cluster.<p>
   *
   * The JSON network configuration will be converted to a {@link NetworkConfig} before
   * being deployed to the cluster. The conversion is done synchronously, so if the
   * configuration is invalid then this method may throw an exception.
   *
   * @param network The JSON network configuration. For the configuration format see
   *        the project documentation.
   * @return The cluster manager.
   */
  ClusterManager deployNetwork(JsonObject network);

  /**
   * Deploys a json network to the cluster.<p>
   *
   * The JSON network configuration will be converted to a {@link NetworkConfig} before
   * being deployed to the cluster. The conversion is done synchronously, so if the
   * configuration is invalid then this method may throw an exception. Once the network
   * has been deployed an {@link ActiveNetwork} will be provided which can be used to
   * add and remove components and connections from the network.
   *
   * @param network The JSON network configuration. For the configuration format see
   *        the project documentation.
   * @param doneHandler An asynchronous handler to be called once the network has
   *        completed deployment. The handler will be called with an {@link ActiveNetwork}
   *        instance which can be used to add or remove components and connections from
   *        the network.
   * @return The cluster manager.
   */
  ClusterManager deployNetwork(JsonObject network, Handler<AsyncResult<ActiveNetwork>> doneHandler);

  /**
   * Deploys a network to the cluster.<p>
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
   * @return The cluster manager.
   */
  ClusterManager deployNetwork(NetworkConfig network);

  /**
   * Deploys a network to the cluster.<p>
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
   * @param doneHandler An asynchronous handler to be called once the deployment is complete.
   *        The handler will be called with an {@link ActiveNetwork} instance that references
   *        the running network. The <code>ActiveNetwork</code> can be used to arbitrarily
   *        and asynchronously update the live network configuration.
   * @return The cluster manager.
   */
  ClusterManager deployNetwork(NetworkConfig network, Handler<AsyncResult<ActiveNetwork>> doneHandler);

  /**
   * Undeploys a complete network from the cluster.<p>
   *
   * This method does not require a network configuration for undeployment. Vertigo
   * will load the configuration from the fault-tolerant data store and undeploy
   * components internally. This allows networks to be undeployed without the network
   * configuration.
   *
   * @param name The name of the network to undeploy.
   * @return The cluster manager.
   */
  ClusterManager undeployNetwork(String name);

  /**
   * Undeploys a complete network from the cluster.<p>
   *
   * This method does not require a network configuration for undeployment. Vertigo
   * will load the configuration from the fault-tolerant data store and undeploy
   * components internally. This allows networks to be undeployed without the network
   * configuration.
   *
   * @param name The name of the network to undeploy.
   * @param doneHandler An asynchronous handler to be called once the network is undeployed.
   * @return The cluster manager.
   */
  ClusterManager undeployNetwork(String name, Handler<AsyncResult<Void>> doneHandler);

  /**
   * Undeploys a network from json.<p>
   *
   * The JSON configuration will immediately be converted to a {@link NetworkConfig} prior
   * to undeploying the network. In order to undeploy the entire network, the configuration
   * should be the same as the deployed configuration. Vertigo will use configuration
   * components to determine whether two configurations are identical. If the given
   * configuration is not identical to the running configuration, any components or
   * connections in the json configuration that are present in the running network
   * will be closed and removed from the running network.
   *
   * @param network The JSON configuration to undeploy. For the configuration format see
   *        the project documentation.
   * @return The cluster manager.
   */
  ClusterManager undeployNetwork(JsonObject network);

  /**
   * Undeploys a network from json.<p>
   *
   * The JSON configuration will immediately be converted to a {@link NetworkConfig} prior
   * to undeploying the network. In order to undeploy the entire network, the configuration
   * should be the same as the deployed configuration. Vertigo will use configuration
   * components to determine whether two configurations are identical. If the given
   * configuration is not identical to the running configuration, any components or
   * connections in the json configuration that are present in the running network
   * will be closed and removed from the running network.
   *
   * @param network The JSON configuration to undeploy. For the configuration format see
   *        the project documentation.
   * @param doneHandler An asynchronous handler to be called once the configuration is undeployed.
   * @return The cluster manager.
   */
  ClusterManager undeployNetwork(JsonObject network, Handler<AsyncResult<Void>> doneHandler);

  /**
   * Undeploys a network from the cluster.<p>
   *
   * This method supports both partial and complete undeployment of networks. When
   * undeploying networks by specifying a {@link NetworkConfig}, the network configuration
   * should contain all components and connections that are being undeployed. If the
   * configuration's components and connections match all deployed components and
   * connections then the entire network will be undeployed.
   *
   * @param network The network configuration to undeploy.
   * @return The cluster manager.
   */
  ClusterManager undeployNetwork(NetworkConfig network);

  /**
   * Undeploys a network from the cluster.<p>
   *
   * This method supports both partial and complete undeployment of networks. When
   * undeploying networks by specifying a {@link NetworkConfig}, the network configuration
   * should contain all components and connections that are being undeployed. If the
   * configuration's components and connections match all deployed components and
   * connections then the entire network will be undeployed.
   *
   * @param network The network configuration to undeploy.
   * @param doneHandler An asynchronous handler to be called once the configuration is undeployed.
   * @return The cluster manager.
   */
  ClusterManager undeployNetwork(NetworkConfig network, Handler<AsyncResult<Void>> doneHandler);

}
