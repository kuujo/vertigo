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

import net.kuujo.vertigo.cluster.ClusterManager;
import net.kuujo.vertigo.cluster.ClusterManagerFactory;
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
 * The core Vertigo API.
 * <p>
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
   * Though a network configuration is provided to this method, the check only
   * consists of checking the network named against current deployments. The
   * structure of the given network configuration has no effect on the result.
   *
   * @param network The network to check.
   * @param resultHandler An asynchronous handler to be called with the result
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
   * Deploys a bare network to the Vertigo cluster.<p>
   *
   * This method will deploy an empty network to the current Vertigo cluster.
   * The current cluster is determined by whether the Vert.x instance was
   * deployed as a clustered Xync instance. If the current Vert.x cluster is
   * a Xync backed cluster, the network will be deployed as a remote network.
   * This means that any runtime configuration changes to the network will
   * result in components being deployed on various nodes within the Vert.x
   * cluster rather than within the current Vert.x instance.
   *
   * @param name The name of the network to deploy. If a network with the
   *        given name is already deployed in the current cluster then the
   *        deployment will simply have no effect.
   * @return The Vertigo instance.
   * @see Vertigo#deployNetwork(String, Handler)
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
   * deployed as a clustered Xync instance. If the current Vert.x cluster is
   * a Xync backed cluster, the network will be deployed as a remote network.
   * This means that any runtime configuration changes to the network will
   * result in components being deployed on various nodes within the Vert.x
   * cluster rather than within the current Vert.x instance.<p>
   *
   * Once the deployment is complete, an {@link ActiveNetwork} will be provided.
   * The active network can be used to reconfigure the live network just like
   * a normal network configuration. When active network configurations change,
   * the live network is notified and asynchronously reconfigures itself to
   * handle the configuration changes.
   *
   * @param name The name of the network to deploy. if a network with the
   *        given name is already deployed in the current cluster then the
   *        deployment will simply have no effect.
   * @param doneHandler An asynchronous handler to be called once complete.
   * @return The Vertigo instance.
   * @see Vertigo#deployNetwork(String)
   * @see Vertigo#deployNetwork(NetworkConfig)
   * @see Vertigo#deployNetwork(NetworkConfig, Handler)
   */
  public Vertigo deployNetwork(final String name, final Handler<AsyncResult<ActiveNetwork>> doneHandler) {
    clusterFactory.getCurrentClusterManager(new Handler<AsyncResult<ClusterManager>>() {
      @Override
      public void handle(AsyncResult<ClusterManager> result) {
        if (result.failed()) {
          new DefaultFutureResult<ActiveNetwork>(result.cause()).setHandler(doneHandler);
        } else {
          result.result().deployNetwork(name, doneHandler);
        }
      }
    });
    return this;
  }

  /**
   * Deploys a complete or partial network to the Vertigo cluster.<p>
   *
   * If the given network's name matches the name of a network that's already
   * running within the cluster then the configuration will be merged with the
   * existing network's configuration. This allows components or connections to
   * be added to existing networks by deploying separate configurations.<p>
   *
   * If the network is not already running, Vertigo will resolve the network's
   * cluster scope by evaluating the current Vert.x instance and the network
   * configuration. If the current Vert.x instance is a clustered Xync instance
   * then Vertigo will attempt to deploy the network as a clustered network.
   * This means that components will be deployed on various nodes within the
   * Vert.x cluster. This feature can be optionally overridden by specifying
   * the cluster scope in the network configuration.<p>
   *
   * If the current Vert.x instance is not a Xync clustered instance then all
   * networks will fall back to local deployment.
   *
   * @param network The network configuration to deploy.
   * @return The Vertigo instance.
   * @see Vertigo#deployNetwork(String)
   * @see Vertigo#deployNetwork(String, Handler)
   * @see Vertigo#deployNetwork(NetworkConfig)
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
   * If the network is not already running, Vertigo will resolve the network's
   * cluster scope by evaluating the current Vert.x instance and the network
   * configuration. If the current Vert.x instance is a clustered Xync instance
   * then Vertigo will attempt to deploy the network as a clustered network.
   * This means that components will be deployed on various nodes within the
   * Vert.x cluster. This feature can be optionally overridden by specifying
   * the cluster scope in the network configuration.<p>
   *
   * If the network is already running then the network configuration change
   * will occur within the deployed network's cluster scope regardless of the
   * given network configuration.<p>
   *
   * If the current Vert.x instance is not a Xync clustered instance then all
   * networks will fall back to local deployment.<p>
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
   * The network will be undeployed in its entirety from the scope in
   * which it is currently deployed.
   *
   * @param name The name of the network to undeploy.
   * @return The Vertigo instance.
   * @see Vertigo#undeployNetwork(String, Handler)
   * @see Vertigo#undeployNetwork(NetworkConfig)
   * @see Vertigo#undeployNetwork(NetworkConfig, Handler)
   */
  public Vertigo undeployNetwork(String name) {
    return undeployNetwork(createNetwork(name));
  }

  /**
   * Undeploys a complete network from the Vertigo cluster.<p>
   *
   * The network will be undeployed in its entirety from the scope in
   * which it is currently deployed. If the network is not currently deployed
   * then the undeployment will fail.
   *
   * @param name The name of the network to undeploy.
   * @param doneHandler An asynchronous handler to be called once complete.
   * @return The Vertigo instance.
   * @see Vertigo#undeployNetwork(String)
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
   * Undeploys a complete or partial network from the Vertigo cluster.<p>
   *
   * The network will be undeployed from the scope in which it is currently
   * deployed.<p>
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
   * @see Vertigo#undeployNetwork(NetworkConfig, Handler)
   */
  public Vertigo undeployNetwork(NetworkConfig network) {
    return undeployNetwork(network, null);
  }

  /**
   * Undeploys a complete or partial network from the Vertigo cluster.<p>
   *
   * The network will be undeployed from the scope in which it is currently
   * deployed. If the network is not currently deployed then the undeployment will
   * fail.<p>
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
