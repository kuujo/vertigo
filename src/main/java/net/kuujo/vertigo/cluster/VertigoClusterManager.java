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

import net.kuujo.vertigo.annotations.ClusterTypeInfo;
import net.kuujo.vertigo.annotations.LocalTypeInfo;
import net.kuujo.vertigo.network.ActiveNetwork;
import net.kuujo.vertigo.network.NetworkConfig;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;

/**
 * The Vertigo cluster manager handles deployment and undeployment of complete
 * or partial Vertigo networks and provides an interface for accessing existing
 * networks in the Vertigo cluster.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
@LocalTypeInfo(defaultImpl=LocalClusterManager.class)
@ClusterTypeInfo(defaultImpl=RemoteClusterManager.class)
public interface VertigoClusterManager {

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
  VertigoClusterManager getNetwork(String name, Handler<AsyncResult<ActiveNetwork>> resultHandler);

  /**
   * Checks whether a network is running in the cluster.
   *
   * @param name The name of the network to check.
   * @param resultHandler An asynchronous handler to be called with the result.
   * @return The cluster manager.
   */
  VertigoClusterManager isRunning(String name, Handler<AsyncResult<Boolean>> resultHandler);

  /**
   * Deploys an empty network to the cluster.<p>
   *
   * This method deploys a no-component network to the cluster. This can be
   * useful if another verticle needs to reference and update the running network
   * asynchronously rather than deploying a predefined network.
   *
   * @param name The name of the network to deploy.
   * @return The cluster manager.
   */
  VertigoClusterManager deployNetwork(String name);

  /**
   * Deploys an empty network to the cluster.<p>
   *
   * This method deploys a no-component network to the cluster. This can be
   * useful if you want to reference and update the running network asynchronously
   * using the {@link ActiveNetwork} rather than deploying a predefined network.
   *
   * @param name The name of the network to deploy.
   * @param doneHandler An asynchronous handler to be called once the deployment is complete.
   * @return The cluster manager.
   */
  VertigoClusterManager deployNetwork(String name, Handler<AsyncResult<ActiveNetwork>> doneHandler);

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
  VertigoClusterManager deployNetwork(NetworkConfig network);

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
  VertigoClusterManager deployNetwork(NetworkConfig network, Handler<AsyncResult<ActiveNetwork>> doneHandler);

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
  VertigoClusterManager undeployNetwork(String name);

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
  VertigoClusterManager undeployNetwork(String name, Handler<AsyncResult<Void>> doneHandler);

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
  VertigoClusterManager undeployNetwork(NetworkConfig network);

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
   * @param doneHandler An asynchronous handler to be called once the network is undeployed.
   * @return The cluster manager.
   */
  VertigoClusterManager undeployNetwork(NetworkConfig network, Handler<AsyncResult<Void>> doneHandler);

}
