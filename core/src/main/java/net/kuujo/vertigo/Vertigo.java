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
import net.kuujo.vertigo.cluster.impl.DefaultCluster;
import net.kuujo.vertigo.cluster.manager.impl.ClusterAgent;
import net.kuujo.vertigo.network.ActiveNetwork;
import net.kuujo.vertigo.network.NetworkConfig;
import net.kuujo.vertigo.network.impl.DefaultNetworkConfig;
import net.kuujo.vertigo.util.Addresses;
import net.kuujo.vertigo.util.Configs;
import net.kuujo.vertigo.util.ContextUri;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.impl.DefaultFutureResult;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Container;
import org.vertx.java.platform.Verticle;

/**
 * The primary Vertigo API.<p>
 *
 * This is the core API for working with Vertigo clusters and networks.
 * To create a {@link Vertigo} instance within a Vert.x {@link Verticle},
 * simply pass the <code>Verticle</code> to the <code>Vertigo</code>
 * constructor.<p>
 *
 * <pre>
 * {@code
 * Vertigo vertigo = new Vertigo(this);
 * }
 * </pre><p>
 *
 * To create a new network use the {@link Vertigo#createNetwork(String)}
 * methods.<p>
 *
 * All networks must be deployed to named clusters. Vertigo clusters are
 * simple collections of verticles that handle deployment and monitoring
 * of components within a Vertigo network. Cluster nodes can either be
 * deployed using the <code>vertx</code> command with the <code>vertigo-cluster</code>
 * module or programmatically using this API.<p>
 *
 * To deploy a cluster use the {@link Vertigo#deployCluster(String, Handler)} methods.<p>
 *
 * <pre>
 * {@code
 * vertigo.deployCluster("test-cluster", new Handler<AsyncResult<Cluster>>() {
 *   public void handle(AsyncResult<Cluster> result) {
 *     if (result.succeeded()) {
 *       Cluster cluster = result.result();
 *     }
 *   }
 * });
 * }
 * </pre><p>
 *
 * The {@link Cluster} can be used to operate on a specific cluster,
 * or networks can be deployed to named clusters through this API. To get
 * a running cluster call the {@link Vertigo#getCluster(String, Handler)} method. To
 * deploy a network to a cluster use the {@link Vertigo#deployNetwork(String, NetworkConfig)}
 * methods.<p>
 *
 * <pre>
 * {@code
 * cluster.deployNetwork(network, new Handler<AsyncResult<ActiveNetwork>>() {
 *   public void handle(AsyncResult<ActiveNetwork> result) {
 *     ActiveNetwork network = result.result();
 *   }
 * });
 * }
 * </pre><p>
 *
 * When a network is deployed to a cluster, the cluster first checks to determine
 * whether the network is already running in the cluster. If the network is already
 * running then <em>the new configuration will be merged with the running configuration.</em>
 * This is important to remember. It allows users to reconfigure running networks,
 * but it also means that a network deployment will not fail if a network of the
 * same name is already running in the cluster. Networks should be named carefully
 * in order to prevent merging two unrelated networks.<p>
 *
 * Clusters also provide a logical separation of networks within the Vert.x cluster.
 * While Vertigo will merge networks of the same name within a single cluster, networks
 * of the same name can be deployed in separate clusters simultaneously. Even if the
 * network configurations are the same, Vertigo will ensure that event bus addresses
 * do not clash across clusters, so it's okay to deploy, for instance, a "test" and
 * "live" cluster within the same Vert.x cluster.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class Vertigo {
  private static final String CLUSTER_MAIN_PROPERTY_NAME = "net.kuujo.vertigo.cluster";
  private final Vertx vertx;
  private final Container container;
  private static String clusterMain;

  public Vertigo(Verticle verticle) {
    this(verticle.getVertx(), verticle.getContainer());
  }

  public Vertigo(Vertx vertx, Container container) {
    this.vertx = vertx;
    this.container = container;
  }

  /**
   * Creates a new randomy named network.
   *
   * @return A new network instance.
   * @see Vertigo#createNetework(String)
   */
  public NetworkConfig createNetwork() {
    return new DefaultNetworkConfig();
  }

  /**
   * Creates a new network.
   * 
   * @param name The network name.
   * @return A new network instance.
   * @see Vertigo#createNetwork()
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
   * Loads the current cluster verticle.
   */
  private String getClusterMain() {
    if (clusterMain == null) {
      try {
        clusterMain = System.getProperty(CLUSTER_MAIN_PROPERTY_NAME);
      } catch (Exception e) {
      }
      clusterMain = ClusterAgent.class.getName();
    }
    return clusterMain;
  }

  /**
   * Deploys a randomly named unique cluster.
   *
   * @param doneHandler An asynchronous handler to be called once the cluster
   *        has been deployed. The handler will be called with a {@link Cluster}
   *        which can be used to manage networks running in the cluster.
   * @return The Vertigo instance.
   */
  public Vertigo deployCluster(Handler<AsyncResult<Cluster>> doneHandler) {
    return deployCluster(ContextUri.createUniqueScheme(), doneHandler);
  }

  /**
   * Deploys a single node cluster at the given address.
   *
   * @param cluster The cluster event bus address.
   * @return The Vertigo instance.
   */
  public Vertigo deployCluster(String cluster) {
    return deployCluster(cluster, null, 1, null);
  }

  /**
   * Deploys a single node cluster at the given address.
   *
   * @param cluster The cluster event bus address.
   * @param doneHandler An asynchronous handler to be called once the cluster
   *        has been deployed. The handler will be called with a {@link Cluster}
   *        which can be used to manage networks running in the cluster.
   * @return The Vertigo instance.
   */
  public Vertigo deployCluster(String cluster, Handler<AsyncResult<Cluster>> doneHandler) {
    return deployCluster(cluster, null, 1, doneHandler);
  }

  /**
   * Deploys a single node cluster to a specific cluster group.
   *
   * @param cluster The cluster event bus address.
   * @param group The cluster group to which to deploy the node.
   * @return The Vertigo instance.
   */
  public Vertigo deployCluster(String cluster, String group) {
    return deployCluster(cluster, group, 1, null);
  }

  /**
   * Deploys a single node cluster to a specific cluster group.
   *
   * @param cluster The cluster event bus address.
   * @param group The cluster group to which to deploy the node.
   * @param doneHandler An asynchronous handler to be called once the cluster
   *        has been deployed. The handler will be called with a {@link Cluster}
   *        which can be used to manage networks running in the cluster.
   * @return The Vertigo instance.
   */
  public Vertigo deployCluster(String cluster, String group, Handler<AsyncResult<Cluster>> doneHandler) {
    return deployCluster(cluster, group, 1, doneHandler);
  }

  /**
   * Deploys multiple nodes within a cluster at the given address.
   *
   * @param cluster The cluster event bus address.
   * @param nodes The number of nodes to deploy.
   * @return The Vertigo instance.
   */
  public Vertigo deployCluster(String cluster, int nodes) {
    return deployCluster(cluster, null, nodes, null);
  }

  /**
   * Deploys multiple nodes within a cluster at the given address.
   *
   * @param cluster The cluster event bus address.
   * @param nodes The number of nodes to deploy.
   * @param doneHandler An asynchronous handler to be called once the cluster
   *        has been deployed. The handler will be called with a {@link Cluster}
   *        which can be used to manage networks running in the cluster.
   * @return The Vertigo instance.
   */
  public Vertigo deployCluster(String cluster, int nodes, Handler<AsyncResult<Cluster>> doneHandler) {
    return deployCluster(cluster, null, nodes, doneHandler);
  }

  /**
   * Deploys multiple cluster nodes to a specific cluster group.
   *
   * @param cluster The cluster event bus address.
   * @param group The cluster group to which to deploy the nodes.
   * @param nodes The number of nodes to deploy.
   * @return The Vertigo instance.
   */
  public Vertigo deployCluster(String cluster, String group, int nodes) {
    return deployCluster(cluster, group, nodes, null);
  }

  /**
   * Deploys multiple cluster nodes to a specific cluster group.
   *
   * @param cluster The cluster event bus address.
   * @param group The cluster group to which to deploy the nodes.
   * @param nodes The number of nodes to deploy.
   * @param doneHandler An asynchronous handler to be called once the cluster
   *        has been deployed. The handler will be called with a {@link Cluster}
   *        which can be used to manage networks running in the cluster.
   * @return The Vertigo instance.
   */
  public Vertigo deployCluster(final String cluster, final String group, int nodes, final Handler<AsyncResult<Cluster>> doneHandler) {
    JsonObject config = new JsonObject()
        .putString("cluster", cluster)
        .putString("group", group);
    container.deployVerticle(getClusterMain(), config, nodes, new Handler<AsyncResult<String>>() {
      @Override
      public void handle(AsyncResult<String> result) {
        if (result.failed()) {
          new DefaultFutureResult<Cluster>(result.cause()).setHandler(doneHandler);
        } else {
          getCluster(cluster, doneHandler);
        }
      }
    });
    return this;
  }

  /**
   * Loads a cluster.
   *
   * @param address The cluster address.
   * @param resultHandler An asynchronous handler to be called once complete.
   * @return The Vertigo instance.
   */
  public Vertigo getCluster(String address, Handler<AsyncResult<Cluster>> resultHandler) {
    Cluster cluster = new DefaultCluster(address, vertx, container);
    cluster.ping(resultHandler);
    return this;
  }

  /**
   * Deploys a bare network to an anonymous local-only cluster.<p>
   *
   * The network will be deployed with no components and no connections. You
   * can add components and connections to the network with an {@link ActiveNetwork}
   * instance.
   *
   * @param name The name of the network to deploy.
   * @return The Vertigo instance.
   */
  public Vertigo deployNetwork(String name) {
    return deployNetwork(name, (Handler<AsyncResult<ActiveNetwork>>) null);
  }

  /**
   * Deploys a bare network to an anonymous local-only cluster.<p>
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
   * @return The Vertigo instance.
   */
  public Vertigo deployNetwork(final String name, final Handler<AsyncResult<ActiveNetwork>> doneHandler) {
    final String cluster = Addresses.createUniqueAddress();
    container.deployVerticle(ClusterAgent.class.getName(), new JsonObject().putString("cluster", cluster).putBoolean("local", true), new Handler<AsyncResult<String>>() {
      @Override
      public void handle(AsyncResult<String> result) {
        if (result.failed()) {
          new DefaultFutureResult<ActiveNetwork>(result.cause()).setHandler(doneHandler);
        } else {
          deployNetwork(cluster, name, doneHandler);
        }
      }
    });
    return this;
  }

  /**
   * Deploys a json network to an anonynous local-only cluster.<p>
   *
   * The JSON network configuration will be converted to a {@link NetworkConfig} before
   * being deployed to the cluster. The conversion is done synchronously, so if the
   * configuration is invalid then this method may throw an exception.
   *
   * @param network The JSON network configuration. For the configuration format see
   *        the project documentation.
   * @return The Vertigo instance.
   */
  public Vertigo deployNetwork(JsonObject network) {
    return deployNetwork(network, null);
  }

  /**
   * Deploys a json network to an anonynous local-only cluster.<p>
   *
   * The JSON network configuration will be converted to a {@link NetworkConfig} before
   * being deployed to the cluster. The conversion is done synchronously, so if the
   * configuration is invalid then this method may throw an exception.
   *
   * @param network The JSON network configuration. For the configuration format see
   *        the project documentation.
   * @param doneHandler An asynchronous handler to be called once the network has
   *        completed deployment. The handler will be called with an {@link ActiveNetwork}
   *        instance which can be used to add or remove components and connections from
   *        the network.
   * @return The Vertigo instance.
   */
  public Vertigo deployNetwork(final JsonObject network, final Handler<AsyncResult<ActiveNetwork>> doneHandler) {
    final String cluster = Addresses.createUniqueAddress();
    container.deployVerticle(ClusterAgent.class.getName(), new JsonObject().putString("cluster", cluster).putBoolean("local", true), new Handler<AsyncResult<String>>() {
      @Override
      public void handle(AsyncResult<String> result) {
        if (result.failed()) {
          new DefaultFutureResult<ActiveNetwork>(result.cause()).setHandler(doneHandler);
        } else {
          deployNetwork(cluster, network, doneHandler);
        }
      }
    });
    return this;
  }

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
  public Vertigo deployNetwork(NetworkConfig network) {
    return deployNetwork(network, null);
  }

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
   *        completed deployment. The handler will be called with an {@link ActiveNetwork}
   *        instance which can be used to add or remove components and connections from
   *        the network.
   * @return The Vertigo instance.
   */
  public Vertigo deployNetwork(final NetworkConfig network, final Handler<AsyncResult<ActiveNetwork>> doneHandler) {
    final String cluster = Addresses.createUniqueAddress();
    container.deployVerticle(ClusterAgent.class.getName(), new JsonObject().putString("cluster", cluster).putBoolean("local", true), new Handler<AsyncResult<String>>() {
      @Override
      public void handle(AsyncResult<String> result) {
        if (result.failed()) {
          new DefaultFutureResult<ActiveNetwork>(result.cause()).setHandler(doneHandler);
        } else {
          deployNetwork(cluster, network, doneHandler);
        }
      }
    });
    return this;
  }

  /**
   * Deploys a bare network to a specific cluster.<p>
   *
   * The network will be deployed with no components and no connections. You
   * can add components and connections to the network with an {@link ActiveNetwork}
   * instance.
   *
   * @param cluster The cluster to which to deploy the network.
   * @param name The name of the network to deploy.
   * @return The Vertigo instance.
   */
  public Vertigo deployNetwork(String cluster, String name) {
    return deployNetwork(cluster, name, null);
  }

  /**
   * Deploys a bare network to a specific cluster.<p>
   *
   * The network will be deployed with no components and no connections. You
   * can add components and connections to the network with an {@link ActiveNetwork}
   * instance.
   *
   * @param cluster The cluster to which to deploy the network.
   * @param name The name of the network to deploy.
   * @param doneHandler An asynchronous handler to be called once the network has
   *        completed deployment. The handler will be called with an {@link ActiveNetwork}
   *        instance which can be used to add or remove components and connections from
   *        the network.
   * @return The Vertigo instance.
   */
  public Vertigo deployNetwork(String cluster, final String name, final Handler<AsyncResult<ActiveNetwork>> doneHandler) {
    getCluster(cluster, new Handler<AsyncResult<Cluster>>() {
      @Override
      public void handle(AsyncResult<Cluster> result) {
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
   * Deploys a json network to a specific cluster.<p>
   *
   * The JSON network configuration will be converted to a {@link NetworkConfig} before
   * being deployed to the cluster. The conversion is done synchronously, so if the
   * configuration is invalid then this method may throw an exception.
   *
   * @param cluster The cluster to which to deploy the network.
   * @param network The JSON network configuration. For the configuration format see
   *        the project documentation.
   * @return The Vertigo instance.
   */
  public Vertigo deployNetwork(String cluster, JsonObject network) {
    return deployNetwork(cluster, network, null);
  }

  /**
   * Deploys a json network to a specific cluster.<p>
   *
   * The JSON network configuration will be converted to a {@link NetworkConfig} before
   * being deployed to the cluster. The conversion is done synchronously, so if the
   * configuration is invalid then this method may throw an exception.
   *
   * @param cluster The cluster to which to deploy the network.
   * @param network The JSON network configuration. For the configuration format see
   *        the project documentation.
   * @param doneHandler An asynchronous handler to be called once the network has
   *        completed deployment. The handler will be called with an {@link ActiveNetwork}
   *        instance which can be used to add or remove components and connections from
   *        the network.
   * @return The Vertigo instance.
   */
  public Vertigo deployNetwork(String cluster, final JsonObject network, final Handler<AsyncResult<ActiveNetwork>> doneHandler) {
    getCluster(cluster, new Handler<AsyncResult<Cluster>>() {
      @Override
      public void handle(AsyncResult<Cluster> result) {
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
  public Vertigo deployNetwork(String cluster, NetworkConfig network) {
    return deployNetwork(cluster, network, null);
  }

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
   *        completed deployment. The handler will be called with an {@link ActiveNetwork}
   *        instance which can be used to add or remove components and connections from
   *        the network.
   * @return The Vertigo instance.
   */
  public Vertigo deployNetwork(String cluster, final NetworkConfig network, final Handler<AsyncResult<ActiveNetwork>> doneHandler) {
    getCluster(cluster, new Handler<AsyncResult<Cluster>>() {
      @Override
      public void handle(AsyncResult<Cluster> result) {
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
  public Vertigo undeployNetwork(String cluster, String name) {
    return undeployNetwork(cluster, name, null);
  }

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
  public Vertigo undeployNetwork(String cluster, final String name, final Handler<AsyncResult<Void>> doneHandler) {
    getCluster(cluster, new Handler<AsyncResult<Cluster>>() {
      @Override
      public void handle(AsyncResult<Cluster> result) {
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
   * Undeploys a network from the given cluster from a json configuration.<p>
   *
   * The JSON configuration will immediately be converted to a {@link NetworkConfig} prior
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
  public Vertigo undeployNetwork(String cluster, JsonObject network) {
    return undeployNetwork(cluster, network, null);
  }

  /**
   * Undeploys a network from the given cluster from a json configuration.<p>
   *
   * The JSON configuration will immediately be converted to a {@link NetworkConfig} prior
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
  public Vertigo undeployNetwork(String cluster, final JsonObject network, final Handler<AsyncResult<Void>> doneHandler) {
    getCluster(cluster, new Handler<AsyncResult<Cluster>>() {
      @Override
      public void handle(AsyncResult<Cluster> result) {
        if (result.failed()) {
          new DefaultFutureResult<Void>(result.cause()).setHandler(doneHandler);
        } else {
          result.result().undeployNetwork(network, doneHandler);
        }
      }
    });
    return this;
  }

  /**
   * Undeploys a network from the given cluster.<p>
   *
   * This method supports both partial and complete undeployment of networks. When
   * undeploying networks by specifying a {@link NetworkConfig}, the network configuration
   * should contain all components and connections that are being undeployed. If the
   * configuration's components and connections match all deployed components and
   * connections then the entire network will be undeployed.
   *
   * @param cluster The cluster from which to undeploy the network.
   * @param network The network configuration to undeploy.
   * @return The Vertigo instance.
   */
  public Vertigo undeployNetwork(String cluster, NetworkConfig network) {
    return undeployNetwork(cluster, network, null);
  }

  /**
   * Undeploys a network from the given cluster.<p>
   *
   * This method supports both partial and complete undeployment of networks. When
   * undeploying networks by specifying a {@link NetworkConfig}, the network configuration
   * should contain all components and connections that are being undeployed. If the
   * configuration's components and connections match all deployed components and
   * connections then the entire network will be undeployed.
   *
   * @param cluster The cluster from which to undeploy the network.
   * @param network The network configuration to undeploy.
   * @param doneHandler An asynchronous handler to be called once the configuration is undeployed.
   * @return The Vertigo instance.
   */
  public Vertigo undeployNetwork(String cluster, final NetworkConfig network, final Handler<AsyncResult<Void>> doneHandler) {
    getCluster(cluster, new Handler<AsyncResult<Cluster>>() {
      @Override
      public void handle(AsyncResult<Cluster> result) {
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
