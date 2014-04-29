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

import net.kuujo.vertigo.cluster.ClusterConfig;
import net.kuujo.vertigo.cluster.ClusterManager;
import net.kuujo.vertigo.cluster.ClusterScope;
import net.kuujo.vertigo.cluster.impl.ClusterManagerFactory;
import net.kuujo.vertigo.network.ActiveNetwork;
import net.kuujo.vertigo.network.NetworkConfig;
import net.kuujo.vertigo.network.impl.DefaultNetworkConfig;
import net.kuujo.vertigo.util.Configs;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.impl.DefaultFutureResult;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Container;
import org.vertx.java.platform.Verticle;

/**
 * The core Vertigo API.<p>
 * 
 * This is the primary interface for creating and deploying Vertigo networks.<p>
 *
 * Vertigo provides its own cluster abstraction that allows cluster modes to
 * be separated from network configurations. When deploying, undeploying, or
 * otherwise operating on Vertigo networks, Vertigo will attempt to automatically
 * detect and use the current Vert.x cluster state. See method documentation
 * for more information.
 * 
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public final class Vertigo {
  private final ClusterManagerFactory clusterFactory;

  public Vertigo(Verticle verticle) {
    this(verticle.getVertx(), verticle.getContainer());
  }

  public Vertigo(Vertx vertx, Container container) {
    this.clusterFactory = new ClusterManagerFactory(vertx, container);
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
   * Checks whether a network is deployed in the cluster.
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
   * Vertigo determines whether the network is running by first attempting to
   * load the network's configuration from the internal coordination cluster.
   * If the network's configuration is stored in the cluster, Vertigo loads
   * the network's cluster and checks whether the network's manager is deployed
   * in the appropriate cluster. If so, the network is considered deployed.
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
  public Vertigo isDeployed(final NetworkConfig network, final Handler<AsyncResult<Boolean>> resultHandler) {
    // The status of a network is checked by checking the current cluster manager.
    // Even if the current scope is CLUSTER, but the network was deployed as a LOCAL
    // network, the network's configuration will be stored in the CLUSTER.
    clusterFactory.getCurrentClusterManager(new Handler<AsyncResult<ClusterManager>>() {
      @Override
      public void handle(AsyncResult<ClusterManager> result) {
        if (result.failed()) {
          new DefaultFutureResult<Boolean>(result.cause()).setHandler(resultHandler);
        } else {
          result.result().isRunning(network.getName(), resultHandler);
        }
      }
    });
    return this;
  }

  /**
   * Loads an active network from the cluster.<p>
   *
   * In order to load the network, Vertigo will first attempt to load the network's
   * configuration from the internal coordination cluster. The coordination cluster
   * stores network configurations regardless of the scope in which a network was
   * deployed. If the network's configuration is found, Vertigo loads the network's
   * cluster scope and checks to ensure the network is running in that cluster. If
   * the network configuration is found and the network is running in the appropriate
   * cluster, an {@link ActiveNetwork} will be provided for the network. The active
   * network can be used to reconfigure the network.
   *
   * @param name The name of the network to load.
   * @param resultHandler An asynchronous handler to be called with the result.
   * @return The Vertigo instance.
   */
  public Vertigo getNetwork(final String name, final Handler<AsyncResult<ActiveNetwork>> resultHandler) {
    clusterFactory.getCurrentClusterManager(new Handler<AsyncResult<ClusterManager>>() {
      @Override
      public void handle(AsyncResult<ClusterManager> result) {
        if (result.failed()) {
          new DefaultFutureResult<ActiveNetwork>(result.cause()).setHandler(resultHandler);
        } else {
          result.result().getNetwork(name, resultHandler);
        }
      }
    });
    return this;
  }

  /**
   * Deploys a bare network to the Vertigo cluster.<p>
   *
   * This method will deploy an empty network to the current Vertigo cluster.
   * The current cluster is determined by whether the Vert.x instance was
   * deployed as a Hazelcast clustered Vert.x instance. If the current Vert.x
   * cluster is a Hazelcast cluster (the Vert.x default), the network will
   * be deployed across the cluster. This means that any runtime configuration
   * changes to the network will result in components being deployed on various
   * nodes within the cluster rather than within the current Vert.x instance.
   *
   * @param name The name of the network to deploy. If a network with the
   *        given name is already deployed in the Vertigo cluster then the
   *        deployment will simply have no effect.
   * @return The Vertigo instance.
   * @see Vertigo#deployNetwork(String, Handler)
   * @see Vertigo#deployNetwork(String, ClusterScope)
   * @see Vertigo#deployNetwork(String, ClusterScope, Handler)
   * @see Vertigo#deployNetwork(String, ClusterConfig)
   * @see Vertigo#deployNetwork(String, ClusterConfig, Handler)
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
   * The current cluster is determined by whether the Vert.x instance was
   * deployed as a Hazelcast clustered Vert.x instance. If the current Vert.x
   * cluster is a Hazelcast cluster (the Vert.x default), the network will
   * be deployed across the cluster. This means that any runtime configuration
   * changes to the network will result in components being deployed on various
   * nodes within the cluster rather than within the current Vert.x instance.<p>
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
   * @see Vertigo#deployNetwork(String, ClusterScope)
   * @see Vertigo#deployNetwork(String, ClusterScope, Handler)
   * @see Vertigo#deployNetwork(String, ClusterConfig)
   * @see Vertigo#deployNetwork(String, ClusterConfig, Handler)
   * @see Vertigo#deployNetwork(JsonObject)
   * @see Vertigo#deployNetwork(JsonObject, Handler)
   * @see Vertigo#deployNetwork(NetworkConfig)
   * @see Vertigo#deployNetwork(NetworkConfig, Handler)
   */
  public Vertigo deployNetwork(String name, Handler<AsyncResult<ActiveNetwork>> doneHandler) {
    return deployNetwork(createNetwork(name), doneHandler);
  }

  /**
   * Deploys a bare network to the Vertigo cluster.<p>
   *
   * This method will deploy an empty network to the given cluster scope.
   * If the current Vert.x instance is a Hazelcast clustered instance then
   * the cluster scope may be either <code>LOCAL</code> or <code>CLUSTER</code>,
   * but if the current Vert.x instance is not clustered then the cluster
   * scope will fall back to <code>LOCAL</code> regardless of the value
   * provided.
   *
   * @param name The name of the network to deploy. If a network with the
   *        given name is already deployed in the Vertigo cluster then the
   *        deployment will simply have no effect.
   * @param scope The cluster scope in which to deploy the network.
   * @return The Vertigo instance.
   * @see Vertigo#deployNetwork(String)
   * @see Vertigo#deployNetwork(String, Handler)
   * @see Vertigo#deployNetwork(String, ClusterScope, Handler)
   * @see Vertigo#deployNetwork(String, ClusterConfig)
   * @see Vertigo#deployNetwork(String, ClusterConfig, Handler)
   * @see Vertigo#deployNetwork(JsonObject)
   * @see Vertigo#deployNetwork(JsonObject, Handler)
   * @see Vertigo#deployNetwork(NetworkConfig)
   * @see Vertigo#deployNetwork(NetworkConfig, Handler)
   */
  public Vertigo deployNetwork(String name, ClusterScope scope) {
    NetworkConfig network = createNetwork(name);
    network.getClusterConfig().setScope(scope);
    return deployNetwork(network);
  }

  /**
   * Deploys a bare network to the Vertigo cluster.<p>
   *
   * This method will deploy an empty network to the given cluster scope.
   * If the current Vert.x instance is a Hazelcast clustered instance then
   * the cluster scope may be either <code>LOCAL</code> or <code>CLUSTER</code>,
   * but if the current Vert.x instance is not clustered then the cluster
   * scope will fall back to <code>LOCAL</code> regardless of the value
   * provided.<p>
   *
   * Once the deployment is complete, an {@link ActiveNetwork} will be provided.
   * The active network can be used to reconfigure the live network just like
   * a normal network configuration. When active network configurations change,
   * the live network is notified and asynchronously reconfigures itself to
   * handle the configuration changes.
   *
   * @param name The name of the network to deploy. If a network with the
   *        given name is already deployed in the Vertigo cluster then the
   *        deployment will simply have no effect.
   * @param scope The cluster scope in which to deploy the network.
   * @param doneHandler An asynchronous handler to be called once complete. If
   *        the network is successfully deployed, the handler will be passed an
   *        {@link ActiveNetwork} which can be used to reconfigure the network
   *        asynchronously without shutdown.
   * @return The Vertigo instance.
   * @see Vertigo#deployNetwork(String)
   * @see Vertigo#deployNetwork(String, Handler)
   * @see Vertigo#deployNetwork(String, ClusterScope)
   * @see Vertigo#deployNetwork(String, ClusterConfig)
   * @see Vertigo#deployNetwork(String, ClusterConfig, Handler)
   * @see Vertigo#deployNetwork(NetworkConfig)
   * @see Vertigo#deployNetwork(NetworkConfig, Handler)
   */
  public Vertigo deployNetwork(String name, ClusterScope scope, Handler<AsyncResult<ActiveNetwork>> doneHandler) {
    NetworkConfig network = createNetwork(name);
    network.getClusterConfig().setScope(scope);
    return deployNetwork(network, doneHandler);
  }

  /**
   * Deploys a bare network to the Vertigo cluster.<p>
   *
   * This method will deploy an empty network to the given cluster If the
   * current Vert.x instance is a Hazelcast clustered instance then the
   * cluster may be of either <code>LOCAL</code> or <code>CLUSTER</code>
   * scope, but if the current Vert.x instance is not clustered then the
   * cluster scope will fall back to <code>LOCAL</code> regardless of the
   * cluster configuration.
   *
   * @param name The name of the network to deploy. If a network with the
   *        given name is already deployed in the Vertigo cluster then the
   *        deployment will simply have no effect.
   * @param cluster The cluster configuration for the cluster to which to deploy
   *        the network.
   * @return The Vertigo instance.
   * @see Vertigo#deployNetwork(String)
   * @see Vertigo#deployNetwork(String, Handler)
   * @see Vertigo#deployNetwork(String, ClusterScope)
   * @see Vertigo#deployNetwork(String, ClusterScope, Handler)
   * @see Vertigo#deployNetwork(String, ClusterConfig, Handler)
   * @see Vertigo#deployNetwork(JsonObject)
   * @see Vertigo#deployNetwork(JsonObject, Handler)
   * @see Vertigo#deployNetwork(NetworkConfig)
   * @see Vertigo#deployNetwork(NetworkConfig, Handler)
   */
  public Vertigo deployNetwork(String name, ClusterConfig cluster) {
    return deployNetwork(name, cluster, null);
  }

  /**
   * Deploys a bare network to the Vertigo cluster.<p>
   *
   * This method will deploy an empty network to the given cluster If the
   * current Vert.x instance is a Hazelcast clustered instance then the
   * cluster may be of either <code>LOCAL</code> or <code>CLUSTER</code>
   * scope, but if the current Vert.x instance is not clustered then the
   * cluster scope will fall back to <code>LOCAL</code> regardless of the
   * cluster configuration.<p>
   *
   * Once the deployment is complete, an {@link ActiveNetwork} will be provided.
   * The active network can be used to reconfigure the live network just like
   * a normal network configuration. When active network configurations change,
   * the live network is notified and asynchronously reconfigures itself to
   * handle the configuration changes.
   *
   * @param name The name of the network to deploy. If a network with the
   *        given name is already deployed in the Vertigo cluster then the
   *        deployment will simply have no effect.
   * @param cluster The cluster configuration for the cluster to which to deploy
   *        the network.
   * @param doneHandler An asynchronous handler to be called once complete. If
   *        the network is successfully deployed, the handler will be passed an
   *        {@link ActiveNetwork} which can be used to reconfigure the network
   *        asynchronously without shutdown.
   * @return The Vertigo instance.
   * @see Vertigo#deployNetwork(String)
   * @see Vertigo#deployNetwork(String, Handler)
   * @see Vertigo#deployNetwork(String, ClusterScope)
   * @see Vertigo#deployNetwork(String, ClusterScope, Handler)
   * @see Vertigo#deployNetwork(String, ClusterConfig)
   * @see Vertigo#deployNetwork(JsonObject)
   * @see Vertigo#deployNetwork(JsonObject, Handler)
   * @see Vertigo#deployNetwork(NetworkConfig)
   * @see Vertigo#deployNetwork(NetworkConfig, Handler)
   */
  public Vertigo deployNetwork(String name, ClusterConfig cluster, Handler<AsyncResult<ActiveNetwork>> doneHandler) {
    NetworkConfig network = createNetwork(name);
    network.setClusterConfig(cluster);
    return deployNetwork(network, doneHandler);
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
   * When the network is deployed, Vertigo will use the network's cluster
   * configuration to determine how deployments occur. If the network's cluster
   * scope is <code>CLUSTER</code> and the current Vert.x instance is a Hazelcast
   * clustered instance then Vertigo will attempt to perform deployments over
   * the event bus at the cluster address provided in the configuration.<p>
   *
   * If the current Vert.x instance is not clustered then Vertigo will deploy
   * the network using the Vert.x {@link Container} and coordination will
   * occur strictly through Vert.x shared data structures.
   *
   * @param network The JSON configuration for the network to deploy.
   * @return The Vertigo instance.
   * @see Vertigo#createNetwork(JsonObject)
   * @see Vertigo#deployNetwork(String)
   * @see Vertigo#deployNetwork(String, Handler)
   * @see Vertigo#deployNetwork(String, ClusterScope)
   * @see Vertigo#deployNetwork(String, ClusterScope, Handler)
   * @see Vertigo#deployNetwork(String, ClusterConfig)
   * @see Vertigo#deployNetwork(String, ClusterConfig, Handler)
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
   * When the network is deployed, Vertigo will use the network's cluster
   * configuration to determine how deployments occur. If the network's cluster
   * scope is <code>CLUSTER</code> and the current Vert.x instance is a Hazelcast
   * clustered instance then Vertigo will attempt to perform deployments over
   * the event bus at the cluster address provided in the configuration.<p>
   *
   * If the current Vert.x instance is not clustered then Vertigo will deploy
   * the network using the Vert.x {@link Container} and coordination will
   * occur strictly through Vert.x shared data structures.<p>
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
   * @see Vertigo#deployNetwork(String, ClusterScope)
   * @see Vertigo#deployNetwork(String, ClusterScope, Handler)
   * @see Vertigo#deployNetwork(String, ClusterConfig)
   * @see Vertigo#deployNetwork(String, ClusterConfig, Handler)
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
   * and thus require a complete shutdown for configuration changes.<p>
   *
   * When the network is deployed, Vertigo will use the network's cluster
   * configuration to determine how deployments occur. If the network's cluster
   * scope is <code>CLUSTER</code> and the current Vert.x instance is a Hazelcast
   * clustered instance then Vertigo will attempt to perform deployments over
   * the event bus at the cluster address provided in the configuration.<p>
   *
   * If the current Vert.x instance is not clustered then Vertigo will deploy
   * the network using the Vert.x {@link Container} and coordination will
   * occur strictly through Vert.x shared data structures.
   *
   * @param network The network configuration to deploy.
   * @return The Vertigo instance.
   * @see Vertigo#deployNetwork(String)
   * @see Vertigo#deployNetwork(String, Handler)
   * @see Vertigo#deployNetwork(String, ClusterScope)
   * @see Vertigo#deployNetwork(String, ClusterScope, Handler)
   * @see Vertigo#deployNetwork(String, ClusterConfig)
   * @see Vertigo#deployNetwork(String, ClusterConfig, Handler)
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
   * When the network is deployed, Vertigo will use the network's cluster
   * configuration to determine how deployments occur. If the network's cluster
   * scope is <code>CLUSTER</code> and the current Vert.x instance is a Hazelcast
   * clustered instance then Vertigo will attempt to perform deployments over
   * the event bus at the cluster address provided in the configuration.<p>
   *
   * If the current Vert.x instance is not clustered then Vertigo will deploy
   * the network using the Vert.x {@link Container} and coordination will
   * occur strictly through Vert.x shared data structures.<p>
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
   * @see Vertigo#deployNetwork(String, ClusterScope)
   * @see Vertigo#deployNetwork(String, ClusterScope, Handler)
   * @see Vertigo#deployNetwork(String, ClusterConfig)
   * @see Vertigo#deployNetwork(String, ClusterConfig, Handler)
   * @see Vertigo#deployNetwork(JsonObject)
   * @see Vertigo#deployNetwork(JsonObject, Handler)
   * @see Vertigo#deployNetwork(NetworkConfig)
   */
  public Vertigo deployNetwork(final NetworkConfig network, final Handler<AsyncResult<ActiveNetwork>> doneHandler) {
    clusterFactory.getCurrentClusterManager(new Handler<AsyncResult<ClusterManager>>() {
      @Override
      public void handle(AsyncResult<ClusterManager> result) {
        if (result.failed()) {
          new DefaultFutureResult<ActiveNetwork>(result.cause()).setHandler(doneHandler);
        } else {
          result.result().deployNetwork(network, doneHandler);
        }
      }
    });
    return this;
  }

  /**
   * Undeploys a complete network from the Vertigo cluster.<p>
   *
   * The network will be undeployed in its entirety from the scope in which
   * it was originally deployed. When this method is called, Vertigo will load
   * the network's full configuration from the internal coordination cluster.
   * The full configuration will be used to undeploy the proper components
   * and the network manager.
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
   * The network will be undeployed in its entirety from the scope in which
   * it was originally deployed. When this method is called, Vertigo will load
   * the network's full configuration from the internal coordination cluster.
   * The full configuration will be used to undeploy the proper components
   * and the network manager. If the network's configuration cannot be found
   * then the call will fail.
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
    clusterFactory.getCurrentClusterManager(new Handler<AsyncResult<ClusterManager>>() {
      @Override
      public void handle(AsyncResult<ClusterManager> result) {
        if (result.failed()) {
          new DefaultFutureResult<Void>(result.cause()).setHandler(doneHandler);
        } else {
          result.result().undeployNetwork(name, doneHandler);
        }
      }
    });
    return this;
  }

  /**
   * Undeploys a complete or partial network from a JSON configuration.<p>
   *
   * The network will be undeployed from the scope in which is was originally
   * deployed. In order to guarantee the network is undeployed from the proper
   * scope, Vertigo will load the network's original configuration from its
   * internal coordination cluster and use the original cluster configuration
   * for undeployment. This means cluster configurations in the undeployed
   * configuration provided to this method have no effect.<p>
   *
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
   * The network will be undeployed from the scope in which is was originally
   * deployed. In order to guarantee the network is undeployed from the proper
   * scope, Vertigo will load the network's original configuration from its
   * internal coordination cluster and use the original cluster configuration
   * for undeployment. This means cluster configurations in the undeployed
   * configuration provided to this method have no effect.<p>
   *
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
   * The network will be undeployed from the scope in which is was originally
   * deployed. In order to guarantee the network is undeployed from the proper
   * scope, Vertigo will load the network's original configuration from its
   * internal coordination cluster and use the original cluster configuration
   * for undeployment. This means cluster configurations in the undeployed
   * configuration provided to this method have no effect.<p>
   *
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
   * The network will be undeployed from the scope in which is was originally
   * deployed. In order to guarantee the network is undeployed from the proper
   * scope, Vertigo will load the network's original configuration from its
   * internal coordination cluster and use the original cluster configuration
   * for undeployment. This means cluster configurations in the undeployed
   * configuration provided to this method have no effect.<p>
   *
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
    clusterFactory.getCurrentClusterManager(new Handler<AsyncResult<ClusterManager>>() {
      @Override
      public void handle(AsyncResult<ClusterManager> result) {
        if (result.failed()) {
          new DefaultFutureResult<Void>(result.cause()).setHandler(doneHandler);
        } else {
          result.result().undeployNetwork(network, doneHandler);
        }
      }
    });
    return this;
  }

}
