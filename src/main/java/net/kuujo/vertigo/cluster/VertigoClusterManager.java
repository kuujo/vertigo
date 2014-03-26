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
package net.kuujo.vertigo.cluster;

import net.kuujo.vertigo.network.ActiveNetwork;
import net.kuujo.vertigo.network.NetworkConfig;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;

/**
 * Vertigo cluster manager.
 *
 * @author Jordan Halterman
 */
public interface VertigoClusterManager {

  /**
   * Gets a network context for a deployed network.
   *
   * @param name The name of the network to get.
   * @param resultHandler An asynchronous handler to be called with the result.
   * @return The cluster manager.
   */
  VertigoClusterManager getNetwork(String name, Handler<AsyncResult<ActiveNetwork>> resultHandler);

  /**
   * Deploys a network to the cluster.
   *
   * @param name The network name.
   * @return The cluster manager.
   */
  VertigoClusterManager deployNetwork(String name);

  /**
   * Deploys a network to the cluster.
   *
   * @param name The network name.
   * @param doneHandler An asynchronous handler to be called once the deployment is complete.
   * @return The cluster manager.
   */
  VertigoClusterManager deployNetwork(String name, Handler<AsyncResult<ActiveNetwork>> doneHandler);

  /**
   * Deploys a network to the cluster.
   *
   * @param network The network configuration.
   * @return The cluster manager.
   */
  VertigoClusterManager deployNetwork(NetworkConfig network);

  /**
   * Deploys a network to the cluster.
   *
   * @param network The network configuration.
   * @param doneHandler An asynchronous handler to be called once the deployment is complete.
   * @return The cluster manager.
   */
  VertigoClusterManager deployNetwork(NetworkConfig network, Handler<AsyncResult<ActiveNetwork>> doneHandler);

  /**
   * Undeploys a network from the cluster.
   *
   * @param name The name of the network to undeploy.
   * @return The cluster manager.
   */
  VertigoClusterManager undeployNetwork(String name);

  /**
   * Undeploys a network from the cluster.
   *
   * @param name The name of the network to undeploy.
   * @param doneHandler An asynchronous handler to be called once the network is undeployed.
   * @return The cluster manager.
   */
  VertigoClusterManager undeployNetwork(String name, Handler<AsyncResult<Void>> doneHandler);

  /**
   * Undeploys a network from the cluster.
   *
   * @param network The network configuration to undeploy.
   * @return The cluster manager.
   */
  VertigoClusterManager undeployNetwork(NetworkConfig network);

  /**
   * Undeploys a network from the cluster.
   *
   * @param network The network configuration to undeploy.
   * @param doneHandler An asynchronous handler to be called once the network is undeployed.
   * @return The cluster manager.
   */
  VertigoClusterManager undeployNetwork(NetworkConfig network, Handler<AsyncResult<Void>> doneHandler);

}
