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

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.ProxyIgnore;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.ServiceHelper;
import io.vertx.core.Vertx;
import net.kuujo.vertigo.network.Network;
import net.kuujo.vertigo.network.NetworkReference;
import net.kuujo.vertigo.spi.ClusterFactory;

/**
 * Vertigo cluster.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
@VertxGen
@ProxyGen
public interface Cluster {

  /**
   * Creates a new Vertigo cluster client.
   *
   * @param vertx The Vert.x instance.
   * @param options The cluster options.
   * @return The Vertigo cluster client.
   */
  static Cluster create(Vertx vertx, ClusterOptions options) {
    return factory.createCluster(vertx, options);
  }

  /**
   * Creates a new Vertigo cluster proxy.
   *
   * @param vertx The Vert.x instance.
   * @paran address The cluster address.
   * @return The Vertigo cluster proxy.
   */
  static Cluster createProxy(Vertx vertx, String address) {
    return factory.createClusterProxy(vertx, address);
  }

  /**
   * Deploys a network to the cluster.
   *
   * @param network The network to deploy.
   * @param doneHandler An asynchronous handler to be called once the network has been deployed.
   * @return The cluster instance.
   */
  @Fluent
  Cluster deployNetwork(Network network, Handler<AsyncResult<NetworkReference>> doneHandler);

  /**
   * Undeploys a network from the cluster.
   *
   * @param id The unique ID of the network to undeploy.
   * @param doneHandler An asynchronous handler to be called once complete.
   * @return The cluster instance.
   */
  @Fluent
  Cluster undeployNetwork(String id, Handler<AsyncResult<Void>> doneHandler);

  /**
   * Undeploys a network from the cluster.
   *
   * @param network The network to undeploy.
   * @param doneHandler An asynchronous handler to be called once complete.
   * @return The cluster instance.
   */
  @Fluent
  Cluster undeployNetwork(Network network, Handler<AsyncResult<Void>> doneHandler);

  /**
   * Starts the cluster instance.
   *
   * @param doneHandler An asynchronous handler to be called once complete.
   */
  @ProxyIgnore
  void start(Handler<AsyncResult<Void>> doneHandler);

  /**
   * Stops the cluster instance.
   *
   * @param doneHandler An asynchronous handler to be called once complete.
   */
  @ProxyIgnore
  void stop(Handler<AsyncResult<Void>> doneHandler);

  static ClusterFactory factory = ServiceHelper.loadFactory(ClusterFactory.class);

}
