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

import net.kuujo.vertigo.cluster.Cluster;
import net.kuujo.vertigo.cluster.ClusterManager;
import net.kuujo.vertigo.cluster.impl.DefaultClusterFactory;
import net.kuujo.vertigo.cluster.impl.DefaultClusterManagerFactory;
import net.kuujo.vertigo.network.ActiveNetwork;
import net.kuujo.vertigo.network.NetworkConfig;
import net.kuujo.vertigo.network.impl.DefaultNetworkConfig;
import net.kuujo.vertigo.util.Configs;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Container;
import org.vertx.java.platform.Verticle;

/**
 * The core Vertigo API.<p>
 * 
 * This is the primary interface for creating and deploying Vertigo networks.<p>
 *
 * Vertigo provides its own cluster abstraction that allows networks to be deployed
 * remotely over the event bus. Vertigo networks can be deployed using the cluster
 * abstraction regardless of whether Vert.x is actually clustered. If Vert.x is
 * not clustered, Vertigo will use Vert.x shared data to mimic a real cluster.<p>
 *
 * Clusters can be used to create logical separation between networks of the same
 * name. Networks of the same name within the same cluster will always be considered
 * the same network. However, networks of the same name in different Vertigo clusters
 * are logically separate. This means you can run two instances of the same network
 * within a single Vert.x cluster by using multiple Vertigo clusters, and Vertigo
 * will manage the two networks separately, ensuring that event bus addresses
 * do not clash.<p>
 *
 * This API also supports active network configuration changes. Networks can be
 * partially deployed and undeployed using the same methods as are used to deploy
 * or undeploy entire networks. When a network is being deployed, Vertigo will
 * determine whether a network of the same name is already running in the cluster.
 * If the network is already running, Vertigo will <em>merge</em> the new network's
 * configuration with the running network's configuration and update the running
 * network's components and connections. Similarly, when a network is undeployed
 * Vertigo will unmerge the undeployed network configuration from the running
 * network configuration and only completely undeploy the network if all components
 * have been undeployed.<p>
 *
 * Please note that <em>Vertigo only supports the default Hazelcast cluster
 * manager</em>. Attempting to use the Vertigo cluster in a Vert.x cluster with
 * any other cluster manager will simply fail.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public final class Vertigo {
  private static final String DEFAULT_CLUSTER_ADDRESS = "vertigo";
  private final ClusterManager manager;
  private final Cluster cluster;

  public Vertigo(Verticle verticle) {
    this(DEFAULT_CLUSTER_ADDRESS, verticle.getVertx(), verticle.getContainer());
  }

  public Vertigo(Vertx vertx, Container container) {
    this(DEFAULT_CLUSTER_ADDRESS, vertx, container);
  }

  public Vertigo(String address, Verticle verticle) {
    this(address, verticle.getVertx(), verticle.getContainer());
  }

  public Vertigo(String address, Vertx vertx, Container container) {
    this.cluster = new DefaultClusterFactory(vertx, container).createCluster(address);
    this.manager = new DefaultClusterManagerFactory(vertx, container).createClusterManager(address);
  }

  /**
   * Returns the Vertigo cluster address.<p>
   *
   * This is the address of the Vertigo cluster to which this Vertigo
   * instance communicates. This address defaults to <code>vertigo</code>.
   *
   * @return The Vertigo cluster address.
   */
  public String address() {
    return cluster.address();
  }

  /**
   * Returns the internal cluster.<p>
   *
   * The {@link Cluster} is the API used by Vertigo internally to deploy modules
   * and verticles within the Vertigo cluster. This cluster can be similarly used
   * to deploy custom modules or verticles within the Vertigo cluster.
   *
   * @return The cluster.
   */
  public Cluster cluster() {
    return cluster;
  }

  /**
   * Creates a new network.
   * 
   * @param name The network name.
   * @return A new network instance.
   * @see Vertigo#createNetwork(JsonObject)
   */
  public NetworkConfig createNetwork(String name) {
    return new DefaultNetworkConfig(name);
  }

  /**
   * Creates a network configuration from json.
   *
   * @param json A json network configuration.
   * @return A network configuration.
   * @see Vertigo#createNetwork(String)
   */
  public NetworkConfig createNetwork(JsonObject json) {
    return Configs.createNetwork(json);
  }

  /**
   * Deploys a Vertigo node.<p>
   *
   * The Vertigo node is a special worker verticle that manages Vertigo
   * network, module, and verticle deployments and provides an API for
   * cluster-wide shared data structures.
   *
   * @return The Vertigo instance.
   * @see Vertigo#undeployNode(String)
   * @see Vertigo#undeployNode(String, Handler
   */
  public Vertigo deployNode() {
    manager.deployNode(null);
    return this;
  }

  /**
   * Deploys a Vertigo node.<p>
   *
   * The Vertigo node is a special worker verticle that manages Vertigo
   * network, module, and verticle deployments and provides an API for
   * cluster-wide shared data structures.
   *
   * @param doneHandler An asynchronous handler to be called once complete.
   *        The handler will be called with the deployment ID of the node.
   *        This can be used to undeploy the node with the
   *        {@link Vertigo#undeployNode(String)} method.
   * @return The Vertigo instance.
   * @see Vertigo#undeployNode(String)
   * @see Vertigo#undeployNode(String, Handler)
   */
  public Vertigo deployNode(Handler<AsyncResult<String>> doneHandler) {
    manager.deployNode(doneHandler);
    return this;
  }

  /**
   * Undeploys a Vertigo node.
   *
   * @param id The unique node ID of the node to undeploy.
   * @return The Vertigo instance.
   * @see Vertigo#deployNode()
   * @see Vertigo#deployNode(Handler)
   */
  public Vertigo undeployNode(String id) {
    manager.undeployNode(id, null);
    return this;
  }

  /**
   * Undeploys a Vertigo node.
   *
   * @param id The unique ID of the node to undeploy.
   * @param doneHandler An asynchronous handler to be called once complete.
   * @return The Vertigo instance.
   * @see Vertigo#deployNode()
   * @see Vertigo#deployNode(Handler)
   */
  public Vertigo undeployNode(String id, Handler<AsyncResult<Void>> doneHandler) {
    manager.undeployNode(id, doneHandler);
    return this;
  }

  /**
   * Checks whether a network is deployed in the cluster.
   *
   * In order to load the network Vertigo will first attempt to load the network's
   * configuration from the current cluster. If the network's configuration is found,
   * Vertigo will load the configuration and check to ensure that the network's
   * manager is running in the cluster.
   *
   * @param name The name of the network to check.
   * @param resultHandler An asynchronous handler to be called with the result.
   * @return The Vertigo instance.
   * @see Vertigo#isDeployed(NetworkConfig, Handler)
   */
  public Vertigo isDeployed(String name, Handler<AsyncResult<Boolean>> resultHandler) {
    return isDeployed(createNetwork(name), resultHandler);
  }

  /**
   * Checks whether a network is deployed in the cluster.<p>
   *
   * In order to load the network Vertigo will first attempt to load the network's
   * configuration from the current cluster. If the network's configuration is found,
   * Vertigo will load the configuration and check to ensure that the network's
   * manager is running in the cluster.<p>
   *
   * Note that the configuration provided to this method is mostly irrelevant
   * as only the given configuration's <code>name</code> is used to determine
   * whether the network is running. This method will <em>not</em> indicate
   * whether the entire given configuration is running.
   *
   * @param network The network to check.
   * @param resultHandler An asynchronous handler to be called with the result.
   * @return The Vertigo instance.
   * @see Vertigo#isDeployed(String, Handler)
   */
  public Vertigo isDeployed(NetworkConfig network, Handler<AsyncResult<Boolean>> resultHandler) {
    manager.isRunning(network.getName(), resultHandler);
    return this;
  }

  /**
   * Loads an active network from the cluster.<p>
   *
   * In order to load the network Vertigo will first attempt to load the network's
   * configuration from the current cluster. If the network's configuration is found,
   * Vertigo will load the configuration and check to ensure that the network's
   * manager is running in the cluster. If the network is running then an
   * {@link ActiveNetwork} will be provided. The active network can be used to
   * reconfigure the network.
   *
   * @param name The name of the network to load.
   * @param resultHandler An asynchronous handler to be called with the result.
   * @return The Vertigo instance.
   */
  public Vertigo getNetwork(String name, Handler<AsyncResult<ActiveNetwork>> resultHandler) {
    manager.getNetwork(name, resultHandler);
    return this;
  }

  /**
   * Deploys a bare network to the Vertigo cluster.<p>
   *
   * This method will deploy an empty network to the current Vertigo cluster.
   * If the current Vert.x cluster is a Hazelcast cluster (the Vert.x default),
   * the network will be deployed across the cluster. This means that any runtime
   * configuration changes to the network will result in components being deployed
   * on various nodes within the cluster rather than within the current Vert.x
   * instance.<p>
   *
   * @param name The name of the network to deploy. If a network with the
   *        given name is already deployed in the Vertigo cluster then the
   *        deployment will simply have no effect.
   * @return The Vertigo instance.
   * @see Vertigo#deployNetwork(String, Handler)
   * @see Vertigo#deployNetwork(JsonObject)
   * @see Vertigo#deployNetwork(JsonObject, Handler)
   * @see Vertigo#deployNetwork(NetworkConfig)
   * @see Vertigo#deployNetwork(NetworkConfig, Handler)
   */
  public Vertigo deployNetwork(String name) {
    return deployNetwork(createNetwork(name));
  }

  /**
   * Deploys a bare network to the Vertigo cluster.<p>
   *
   * This method will deploy an empty network to the current Vertigo cluster.
   * If the current Vert.x cluster is a Hazelcast cluster (the Vert.x default),
   * the network will be deployed across the cluster. This means that any runtime
   * configuration changes to the network will result in components being deployed
   * on various nodes within the cluster rather than within the current Vert.x
   * instance.<p>
   *
   * Once the deployment is complete, an {@link ActiveNetwork} will be provided.
   * The active network can be used to reconfigure the live network just like
   * a normal network configuration. When active network configurations change,
   * the live network is notified and asynchronously reconfigures itself to
   * handle the configuration changes.
   *
   * @param name The name of the network to deploy. if a network with the
   *        given name is already deployed in the Vertigo cluster then the
   *        deployment will simply have no effect.
   * @param doneHandler An asynchronous handler to be called once complete. If
   *        the network is successfully deployed, the handler will be passed an
   *        {@link ActiveNetwork} which can be used to reconfigure the network
   *        asynchronously without shutdown.
   * @return The Vertigo instance.
   * @see Vertigo#deployNetwork(String)
   * @see Vertigo#deployNetwork(JsonObject)
   * @see Vertigo#deployNetwork(JsonObject, Handler)
   * @see Vertigo#deployNetwork(NetworkConfig)
   * @see Vertigo#deployNetwork(NetworkConfig, Handler)
   */
  public Vertigo deployNetwork(String name, Handler<AsyncResult<ActiveNetwork>> doneHandler) {
    return deployNetwork(createNetwork(name), doneHandler);
  }

  /**
   * Deploys a complete or partial network from a JSON configuration.<p>
   *
   * If the given network's name matches the name of a network that's already
   * running within the cluster then the configuration will be merged with the
   * existing network's configuration. This allows components or connections to
   * be added to existing networks by deploying separate configurations.<p>
   *
   * Note that only components and connections may be added or removed from
   * running networks. Changes to things like cluster configurations will have
   * no impact on the network as they are core to the operation of the network
   * and thus require a complete shutdown for configuration changes.
   *
   * @param network The JSON configuration for the network to deploy.
   * @return The Vertigo instance.
   * @see Vertigo#createNetwork(JsonObject)
   * @see Vertigo#deployNetwork(String)
   * @see Vertigo#deployNetwork(String, Handler)
   * @see Vertigo#deployNetwork(JsonObject, Handler)
   * @see Vertigo#deployNetwork(NetworkConfig)
   * @see Vertigo#deployNetwork(NetworkConfig, Handler)
   */
  public Vertigo deployNetwork(JsonObject network) {
    return deployNetwork(createNetwork(network));
  }

  /**
   * Deploys a complete or partial network from a JSON configuration.<p>
   *
   * If the given network's name matches the name of a network that's already
   * running within the cluster then the configuration will be merged with the
   * existing network's configuration. This allows components or connections to
   * be added to existing networks by deploying separate configurations.<p>
   *
   * Note that only components and connections may be added or removed from
   * running networks. Changes to things like cluster configurations will have
   * no impact on the network as they are core to the operation of the network
   * and thus require a complete shutdown for configuration changes.<p>
   *
   * Once the deployment is complete, an {@link ActiveNetwork} will be provided.
   * The active network can be used to reconfigure the live network just like
   * a normal network configuration. When active network configurations change,
   * the live network is notified and asynchronously reconfigures itself to
   * handle the configuration changes.
   *
   * @param network The JSON configuration for the network to deploy.
   * @param doneHandler An asynchronous handler to be called once complete. If
   *        the network is successfully deployed, the handler will be passed an
   *        {@link ActiveNetwork} which can be used to reconfigure the network
   *        asynchronously without shutdown.
   * @return The Vertigo instance.
   * @see Vertigo#createNetwork(JsonObject)
   * @see Vertigo#deployNetwork(String)
   * @see Vertigo#deployNetwork(String, Handler)
   * @see Vertigo#deployNetwork(JsonObject)
   * @see Vertigo#deployNetwork(NetworkConfig)
   * @see Vertigo#deployNetwork(NetworkConfig, Handler)
   */
  public Vertigo deployNetwork(JsonObject network, Handler<AsyncResult<ActiveNetwork>> doneHandler) {
    return deployNetwork(createNetwork(network), doneHandler);
  }

  /**
   * Deploys a complete or partial network to the Vertigo cluster.<p>
   *
   * If the given network's name matches the name of a network that's already
   * running within the cluster then the configuration will be merged with the
   * existing network's configuration. This allows components or connections to
   * be added to existing networks by deploying separate configurations.<p>
   *
   * Note that only components and connections may be added or removed from
   * running networks. Changes to things like cluster configurations will have
   * no impact on the network as they are core to the operation of the network
   * and thus require a complete shutdown for configuration changes.
   *
   * @param network The network configuration to deploy.
   * @return The Vertigo instance.
   * @see Vertigo#deployNetwork(String)
   * @see Vertigo#deployNetwork(String, Handler)
   * @see Vertigo#deployNetwork(JsonObject)
   * @see Vertigo#deployNetwork(JsonObject, Handler)
   * @see Vertigo#deployNetwork(NetworkConfig, Handler)
   */
  public Vertigo deployNetwork(NetworkConfig network) {
    return deployNetwork(network, null);
  }

  /**
   * Deploys a complete or partial network to the Vertigo cluster.<p>
   *
   * If the given network's name matches the name of a network that's already
   * running within the cluster then the configuration will be merged with the
   * existing network's configuration. This allows components or connections to
   * be added to existing networks by deploying separate configurations.<p>
   *
   * Note that only components and connections may be added or removed from
   * running networks. Changes to things like cluster configurations will have
   * no impact on the network as they are core to the operation of the network
   * and thus require a complete shutdown for configuration changes.<p>
   *
   * Once the deployment is complete, an {@link ActiveNetwork} will be provided.
   * The active network can be used to reconfigure the live network just like
   * a normal network configuration. When active network configurations change,
   * the live network is notified and asynchronously reconfigures itself to
   * handle the configuration changes.
   *
   * @param network The network configuration to deploy.
   * @param doneHandler An asynchronous handler to be called once complete. If
   *        the network is successfully deployed, the handler will be passed an
   *        {@link ActiveNetwork} which can be used to reconfigure the network
   *        asynchronously without shutdown.
   * @return The Vertigo instance.
   * @see Vertigo#deployNetwork(String)
   * @see Vertigo#deployNetwork(String, Handler)
   * @see Vertigo#deployNetwork(JsonObject)
   * @see Vertigo#deployNetwork(JsonObject, Handler)
   * @see Vertigo#deployNetwork(NetworkConfig)
   */
  public Vertigo deployNetwork(NetworkConfig network, Handler<AsyncResult<ActiveNetwork>> doneHandler) {
    manager.deployNetwork(network, doneHandler);
    return this;
  }

  /**
   * Undeploys a complete network from the Vertigo cluster.<p>
   *
   * The network will be undeployed in its entirety from the current cluster.
   * When this method is called, Vertigo will load the network's full configuration
   * from the internal coordination cluster. The full configuration will be used
   * to undeploy the proper components and the network manager.
   *
   * @param name The name of the network to undeploy.
   * @return The Vertigo instance.
   * @see Vertigo#undeployNetwork(String, Handler)
   * @see Vertigo#undeployNetwork(JsonObject)
   * @see Vertigo#undeployNetwork(JsonObject, Handler)
   * @see Vertigo#undeployNetwork(NetworkConfig)
   * @see Vertigo#undeployNetwork(NetworkConfig, Handler)
   */
  public Vertigo undeployNetwork(String name) {
    return undeployNetwork(createNetwork(name));
  }

  /**
   * Undeploys a complete network from the Vertigo cluster.<p>
   *
   * The network will be undeployed in its entirety from the current cluster.
   * When this method is called, Vertigo will load the network's full configuration
   * from the internal coordination cluster. The full configuration will be used
   * to undeploy the proper components and the network manager. If the network's
   * configuration cannot be found then the call will fail.
   *
   * @param name The name of the network to undeploy.
   * @param doneHandler An asynchronous handler to be called once complete.
   * @return The Vertigo instance.
   * @see Vertigo#undeployNetwork(String)
   * @see Vertigo#undeployNetwork(JsonObject)
   * @see Vertigo#undeployNetwork(JsonObject, Handler)
   * @see Vertigo#undeployNetwork(NetworkConfig)
   * @see Vertigo#undeployNetwork(NetworkConfig, Handler)
   */
  public Vertigo undeployNetwork(final String name, final Handler<AsyncResult<Void>> doneHandler) { 
    manager.undeployNetwork(name, doneHandler);
    return this;
  }

  /**
   * Undeploys a complete or partial network from a JSON configuration.<p>
   *
   * The network will be undeployed from the current cluster.
   * If the configuration is a complete match to the configuration of the deployed
   * network, the complete network will be undeployed. Alternatively, if the
   * given configuration is only a partial configuration compared to the complete
   * deployed configuration, only the components and connections defined in the
   * given configuration will be undeployed from the running network. This allows the
   * network to be asynchronously reconfigured without undeploying the network.
   *
   * @param network The network configuration to undeploy.
   * @return The Vertigo instance.
   * @see Vertigo#undeployNetwork(String)
   * @see Vertigo#undeployNetwork(String, Handler)
   * @see Vertigo#undeployNetwork(JsonObject, Handler)
   * @see Vertigo#undeployNetwork(NetworkConfig)
   * @see Vertigo#undeployNetwork(NetworkConfig, Handler)
   */
  public Vertigo undeployNetwork(JsonObject network) {
    return undeployNetwork(createNetwork(network));
  }

  /**
   * Undeploys a complete or partial network from a JSON configuration.<p>
   *
   * The network will be undeployed from the current cluster.
   * If the configuration is a complete match to the configuration of the deployed
   * network, the complete network will be undeployed. Alternatively, if the
   * given configuration is only a partial configuration compared to the complete
   * deployed configuration, only the components and connections defined in the
   * given configuration will be undeployed from the running network. This allows the
   * network to be asynchronously reconfigured without undeploying the network.
   *
   * @param network The network configuration to undeploy.
   * @return The Vertigo instance.
   * @see Vertigo#undeployNetwork(String)
   * @see Vertigo#undeployNetwork(String, Handler)
   * @see Vertigo#undeployNetwork(JsonObject)
   * @see Vertigo#undeployNetwork(NetworkConfig)
   * @see Vertigo#undeployNetwork(NetworkConfig, Handler)
   */
  public Vertigo undeployNetwork(JsonObject network, Handler<AsyncResult<Void>> doneHandler) {
    return undeployNetwork(createNetwork(network), doneHandler);
  }

  /**
   * Undeploys a complete or partial network from the Vertigo cluster.<p>
   *
   * The network will be undeployed from the current cluster.
   * If the configuration is a complete match to the configuration of the deployed
   * network, the complete network will be undeployed. Alternatively, if the
   * given configuration is only a partial configuration compared to the complete
   * deployed configuration, only the components and connections defined in the
   * given configuration will be undeployed from the running network. This allows the
   * network to be asynchronously reconfigured without undeploying the network.
   *
   * @param network The network configuration to undeploy.
   * @return The Vertigo instance.
   * @see Vertigo#undeployNetwork(String)
   * @see Vertigo#undeployNetwork(String, Handler)
   * @see Vertigo#undeployNetwork(JsonObject)
   * @see Vertigo#undeployNetwork(JsonObject, Handler)
   * @see Vertigo#undeployNetwork(NetworkConfig, Handler)
   */
  public Vertigo undeployNetwork(NetworkConfig network) {
    return undeployNetwork(network, null);
  }

  /**
   * Undeploys a complete or partial network from the Vertigo cluster.<p>
   *
   * The network will be undeployed from the current cluster.
   * If the configuration is a complete match to the configuration of the deployed
   * network, the complete network will be undeployed. Alternatively, if the
   * given configuration is only a partial configuration compared to the complete
   * deployed configuration, only the components and connections defined in the
   * given configuration will be undeployed from the running network. This allows the
   * network to be asynchronously reconfigured without undeploying the network.
   *
   * @param network The network configuration to undeploy.
   * @return The Vertigo instance.
   * @see Vertigo#undeployNetwork(String)
   * @see Vertigo#undeployNetwork(String, Handler)
   * @see Vertigo#undeployNetwork(JsonObject)
   * @see Vertigo#undeployNetwork(JsonObject, Handler)
   * @see Vertigo#undeployNetwork(NetworkConfig)
   */
  public Vertigo undeployNetwork(final NetworkConfig network, final Handler<AsyncResult<Void>> doneHandler) {
    manager.undeployNetwork(network, doneHandler);
    return this;
  }

}
