/*
 * Copyright 2013 the original author or authors.
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

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;

import net.kuujo.vertigo.context.NetworkContext;
import net.kuujo.vertigo.network.Network;

/**
 * The cluster is the primary interface for deploying Vertigo networks. Clusters
 * handle deploying network coordinators which handle deployment and monitoring
 * of network component instances.
 *
 * @author Jordan Halterman
 */
public interface Cluster {

  /**
   * Deploys a network to the cluster.
   *
   * @param network
   *   The network configuration.
   */
  void deployNetwork(Network network);

  @Deprecated
  void deploy(Network network);

  /**
   * Deploys a network to the cluster.
   *
   * @param network
   *   The network configuration.
   * @param doneHandler
   *   A handler to be called once the deployment is complete. This handler will
   *   be passed the deployed network context containing information about the
   *   deployed components.
   */
  void deployNetwork(Network network, Handler<AsyncResult<NetworkContext>> doneHandler);

  @Deprecated
  void deploy(Network network, Handler<AsyncResult<NetworkContext>> doneHandler);

  /**
   * Shuts down a network in the cluster.
   *
   * @param context
   *   The context of the network to shutdown.
   */
  void shutdownNetwork(NetworkContext context);

  @Deprecated
  void shutdown(NetworkContext context);

  /**
   * Shuts down a network in the cluster.
   *
   * @param context
   *   The context of the network to shutdown.
   * @param doneHandler
   *   A handler to be called once the shutdown is complete.
   */
  void shutdownNetwork(NetworkContext context, Handler<AsyncResult<Void>> doneHandler);

  @Deprecated
  void shutdown(NetworkContext context, Handler<AsyncResult<Void>> doneHandler);

}
