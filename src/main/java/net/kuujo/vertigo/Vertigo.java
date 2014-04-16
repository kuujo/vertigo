/*
 * Copyright 2013-2014 the original author or authors.
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

import net.kuujo.vertigo.cluster.LocalClusterManager;
import net.kuujo.vertigo.cluster.RemoteClusterManager;
import net.kuujo.vertigo.cluster.VertigoClusterManager;
import net.kuujo.vertigo.network.ActiveNetwork;
import net.kuujo.vertigo.network.Configs;
import net.kuujo.vertigo.network.NetworkConfig;
import net.kuujo.vertigo.network.impl.DefaultNetworkConfig;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Container;
import org.vertx.java.platform.Verticle;

/**
 * The primary Vertigo API.
 * <p>
 * 
 * This is the primary interface for creating and deploying Vertigo
 * networks.
 * 
 * @author Jordan Halterman
 */
public final class Vertigo {
  private static boolean initialized;
  private static Mode currentMode;
  private final Vertx vertx;
  private final Container container;

  public Vertigo(Verticle verticle) {
    this(verticle.getVertx(), verticle.getContainer());
  }

  public Vertigo(Vertx vertx, Container container) {
    this.vertx = vertx;
    this.container = container;
  }

  /**
   * Vertigo cluster mode.
   *
   * @author Jordan Halterman
   */
  public static enum Mode {

    /**
     * Indicates local mode.
     */
    LOCAL,

    /**
     * Indicates cluster mode.
     */
    CLUSTER;

  }

  public static void init(Mode mode) {
    if (!initialized) {
      currentMode = mode;
      initialized = true;
    } else if (!mode.equals(currentMode)) {
      throw new IllegalStateException("Cannot alter immutable Vertigo cluster mode.");
    }
  }

  /**
   * Returns the current Vertigo cluster mode.<p>
   *
   * The cluster mode will only be available within component instances.
   * If the current verticle was deployed as a component instance, Vertigo
   * will initialize the Vertigo cluster mode based on how the component
   * and the network to which it belongs were deployed.
   *
   * @return The current Vertigo mode.
   */
  public static Mode currentMode() {
    return currentMode;
  }

  /**
   * Creates a new network.
   * 
   * @param name The network name.
   * @return A new network instance.
   */
  public NetworkConfig createNetwork(String name) {
    return new DefaultNetworkConfig(name);
  }

  /**
   * Creates a network configuration from json.
   *
   * @param json A json network configuration.
   * @return A network configuration.
   */
  public NetworkConfig createNetwork(JsonObject json) {
    return Configs.createNetwork(json);
  }

  /**
   * Deploys a bare network within the current Vert.x instance.
   * <p>
   * 
   * This deployment method uses the basic {@link LocalClusterManager} cluster implementation to
   * deploy network verticles and modules using the current Vert.x {@link Container}
   * instance.
   * 
   * @param name The name of the network to deploy.
   * @return The called Vertigo instance.
   */
  public Vertigo deployLocalNetwork(String name) {
    VertigoClusterManager cluster = new LocalClusterManager(vertx, container);
    cluster.deployNetwork(name);
    return this;
  }

  /**
   * Deploys a bare network within the current Vert.x instance.
   * <p>
   * 
   * This deployment method uses the basic {@link LocalClusterManager} cluster implementation to
   * deploy network verticles and modules using the current Vert.x {@link Container}
   * instance.
   * 
   * @param name The name of the network to deploy.
   * @param doneHandler An asynchronous handler to be called once deployment is complete.
   *          The handler will be called with the deployed network context which contains
   *          information about the network's component locations and event bus addresses.
   * @return The called Vertigo instance.
   */
  public Vertigo deployLocalNetwork(String name, Handler<AsyncResult<ActiveNetwork>> doneHandler) {
    VertigoClusterManager cluster = new LocalClusterManager(vertx, container);
    cluster.deployNetwork(name, doneHandler);
    return this;
  }

  /**
   * Deploys a network within the current Vert.x instance.
   * <p>
   *
   * This deployment method uses the basic {@link LocalClusterManager} cluster implementation to
   * deploy network verticles and modules using the current Vert.x {@link Container}
   * instance.<p>
   *
   * If the name given network configuration matches the name of a network
   * that is already running in the local cluster, the given configuration
   * will be merged with the running network configuration and the network
   * structure will be updated asynchronously. This allows networks to be
   * reconfigured using this API by deploying or undeploying partial configurations.
   *
   * @param network The network to deploy.
   * @return The called Vertigo instance.
   */
  public Vertigo deployLocalNetwork(NetworkConfig network) {
    VertigoClusterManager cluster = new LocalClusterManager(vertx, container);
    cluster.deployNetwork(network);
    return this;
  }

  /**
   * Deploys a network within the current Vert.x instance.
   * <p>
   * 
   * This deployment method uses the basic {@link LocalClusterManager} cluster implementation to
   * deploy network verticles and modules using the current Vert.x {@link Container}
   * instance.<p>
   *
   * If the name given network configuration matches the name of a network
   * that is already running in the local cluster, the given configuration
   * will be merged with the running network configuration and the network
   * structure will be updated asynchronously. This allows networks to be
   * reconfigured using this API by deploying or undeploying partial configurations.
   * 
   * @param network The network to deploy.
   * @param doneHandler An asynchronous handler to be called once deployment is complete.
   *          The handler will be called with the deployed network context which contains
   *          information about the network's component locations and event bus addresses.
   * @return The called Vertigo instance.
   */
  public Vertigo deployLocalNetwork(NetworkConfig network, Handler<AsyncResult<ActiveNetwork>> doneHandler) {
    VertigoClusterManager cluster = new LocalClusterManager(vertx, container);
    cluster.deployNetwork(network, doneHandler);
    return this;
  }

  /**
   * Undeploys a complete local network.<p>
   *
   * This method will undeploy all components of the named network.
   * When undeploying a network with a network <code>name</code>, the network
   * does not have to have been deployed from the current verticle instance.
   * Vertigo will use cluster shared data to reference and undeploy the network.
   *
   * @param name The name of the network to undeploy.
   * @return The Vertigo instance.
   */
  public Vertigo undeployLocalNetwork(String name) {
    VertigoClusterManager cluster = new LocalClusterManager(vertx, container);
    cluster.undeployNetwork(name);
    return this;
  }

  /**
   * Undeploys a local network.<p>
   *
   * This method will undeploy all components of the named network.
   * When undeploying a network with a network <code>name</code>, the network
   * does not have to have been deployed from the current verticle instance.
   * Vertigo will use cluster shared data to reference and undeploy the network.
   *
   * @param name The name of the network to undeploy.
   * @param doneHandler An asynchronous handler to be called once the network has been undeployed.
   * @return The Vertigo instance.
   */
  public Vertigo undeployLocalNetwork(String name, Handler<AsyncResult<Void>> doneHandler) {
    VertigoClusterManager cluster = new LocalClusterManager(vertx, container);
    cluster.undeployNetwork(name, doneHandler);
    return this;
  }

  /**
   * Undeploys a local network configuration.<p>
   *
   * The given network configuration must have the name of the network that
   * is being undeployed. If the given configuration does not identify all
   * components and connections of the deployed network, the network will
   * only be partially undeployed using the components and connections defined
   * in the configuration.
   * 
   * @param network The network to undeploy.
   * @return The called Vertigo instance.
   */
  public Vertigo undeployLocalNetwork(NetworkConfig network) {
    VertigoClusterManager cluster = new LocalClusterManager(vertx, container);
    cluster.undeployNetwork(network);
    return this;
  }

  /**
   * Undeploys a local network.<p>
   *
   * The given network configuration must have the name of the network that
   * is being undeployed. If the given configuration does not identify all
   * components and connections of the deployed network, the network will
   * only be partially undeployed using the components and connections defined
   * in the configuration.
   * 
   * @param network The network to undeploy.
   * @param doneHandler An asynchronous handler to be called once the network has been undeployed.
   * @return The called Vertigo instance.
   */
  public Vertigo undeployLocalNetwork(NetworkConfig network, Handler<AsyncResult<Void>> doneHandler) {
    VertigoClusterManager cluster = new LocalClusterManager(vertx, container);
    cluster.undeployNetwork(network, doneHandler);
    return this;
  }

  /**
   * Deploys a bare network to a Xync cluster.<p>
   *
   * This deployment method uses the {@link RemoteClusterManager} to deploy
   * the network to a Xync cluster. To deploy a remote network, the current
   * Vert.x instance must be a clustered instance with the Xync platform
   * manager being available. If the Xync cluster is not available then
   * deployment will automatically fail back to {@link LocalClusterManager}<p>
   *
   * If the name given network configuration matches the name of a network
   * that is already running in the local cluster, the given configuration
   * will be merged with the running network configuration and the network
   * structure will be updated asynchronously. This allows networks to be
   * reconfigured using this API by deploying or undeploying partial configurations.
   *
   * @param name The name of the network to deploy.
   * @return The Vertigo instance.
   */
  public Vertigo deployRemoteNetwork(String name) {
    VertigoClusterManager cluster = new RemoteClusterManager(vertx, container);
    cluster.deployNetwork(name);
    return this;
  }

  /**
   * Deploys a bare network to a Xync cluster.<p>
   *
   * This deployment method uses the {@link RemoteClusterManager} to deploy
   * the network to a Xync cluster. To deploy a remote network, the current
   * Vert.x instance must be a clustered instance with the Xync platform
   * manager being available. If the Xync cluster is not available then
   * deployment will automatically fail back to {@link LocalClusterManager}<p>
   *
   * If the name given network configuration matches the name of a network
   * that is already running in the local cluster, the given configuration
   * will be merged with the running network configuration and the network
   * structure will be updated asynchronously. This allows networks to be
   * reconfigured using this API by deploying or undeploying partial configurations.
   *
   * @param name The name of the network to deploy.
   * @param doneHandler An asynchronous handler to be called once deployment is complete.
   *          The handler will be called with the deployed network context which contains
   *          information about the network's component locations and event bus addresses.
   * @return The Vertigo instance.
   */
  public Vertigo deployRemoteNetwork(String name, Handler<AsyncResult<ActiveNetwork>> doneHandler) {
    VertigoClusterManager cluster = new RemoteClusterManager(vertx, container);
    cluster.deployNetwork(name, doneHandler);
    return this;
  }

  /**
   * Deploys a network to a Xync cluster.
   *
   * This deployment method uses the {@link RemoteClusterManager} to deploy
   * the network to a Xync cluster. To deploy a remote network, the current
   * Vert.x instance must be a clustered instance with the Xync platform
   * manager being available. If the Xync cluster is not available then
   * deployment will automatically fail back to {@link LocalClusterManager}<p>
   *
   * If the name given network configuration matches the name of a network
   * that is already running in the remote cluster, the given configuration
   * will be merged with the running network configuration and the network
   * structure will be updated asynchronously. This allows networks to be
   * reconfigured using this API by deploying or undeploying partial configurations.
   * 
   * @param network The network to deploy.
   * @return The called Vertigo instance.
   */
  public Vertigo deployRemoteNetwork(NetworkConfig network) {
    VertigoClusterManager cluster = new RemoteClusterManager(vertx, container);
    cluster.deployNetwork(network);
    return this;
  }

  /**
   * Deploys a network via the Vert.x event bus.
   *
   * This deployment method uses the {@link RemoteClusterManager} to deploy
   * the network to a Xync cluster. To deploy a remote network, the current
   * Vert.x instance must be a clustered instance with the Xync platform
   * manager being available. If the Xync cluster is not available then
   * deployment will automatically fail back to {@link LocalClusterManager}<p>
   *
   * If the name given network configuration matches the name of a network
   * that is already running in the remote cluster, the given configuration
   * will be merged with the running network configuration and the network
   * structure will be updated asynchronously. This allows networks to be
   * reconfigured using this API by deploying or undeploying partial configurations.
   * 
   * @param network The network to deploy.
   * @param doneHandler An asynchronous handler to be called once deployment is complete.
   *          The handler will be called with the deployed network context which contains
   *          information about the network's component locations and event bus addresses.
   * @return The called Vertigo instance.
   */
  public Vertigo deployRemoteNetwork(NetworkConfig network, Handler<AsyncResult<ActiveNetwork>> doneHandler) {
    VertigoClusterManager cluster = new RemoteClusterManager(vertx, container);
    cluster.deployNetwork(network, doneHandler);
    return this;
  }

  /**
   * Undeploys a network via the Vert.x event bus.<p>
   *
   * This method will undeploy all components of the named network.
   * When undeploying a network with a network <code>name</code>, the network
   * does not have to have been deployed from the current verticle instance.
   * Vertigo will use cluster shared data to reference and undeploy the network.
   * 
   * @param name The name of the network to undeploy.
   * @return The called Vertigo instance.
   */
  public Vertigo undeployRemoteNetwork(String name) {
    VertigoClusterManager cluster = new RemoteClusterManager(vertx, container);
    cluster.undeployNetwork(name);
    return this;
  }

  /**
   * Undeploys a network via the Vert.x event bus.<p>
   *
   * This method will undeploy all components of the named network.
   * When undeploying a network with a network <code>name</code>, the network
   * does not have to have been deployed from the current verticle instance.
   * Vertigo will use cluster shared data to reference and undeploy the network.
   * 
   * @param name The name of the network to undeploy.
   * @param doneHandler An asynchronous handler to be called once the network has been
   *          undeployed.
   * @return The called Vertigo instance.
   */
  public Vertigo undeployRemoteNetwork(String name, Handler<AsyncResult<Void>> doneHandler) {
    VertigoClusterManager cluster = new RemoteClusterManager(vertx, container);
    cluster.undeployNetwork(name, doneHandler);
    return this;
  }

  /**
   * Undeploys a network via the Vert.x event bus.<p>
   *
   * The given network configuration must have the name of the network that
   * is being undeployed. If the given configuration does not identify all
   * components and connections of the deployed network, the network will
   * only be partially undeployed using the components and connections defined
   * in the configuration.
   * 
   * @param network The network configuration to undeploy.
   * @return The called Vertigo instance.
   */
  public Vertigo undeployRemoteNetwork(NetworkConfig network) {
    VertigoClusterManager cluster = new RemoteClusterManager(vertx, container);
    cluster.undeployNetwork(network);
    return this;
  }

  /**
   * Undeploys a network via the Vert.x event bus.<p>
   *
   * The given network configuration must have the name of the network that
   * is being undeployed. If the given configuration does not identify all
   * components and connections of the deployed network, the network will
   * only be partially undeployed using the components and connections defined
   * in the configuration.
   * 
   * @param network The network configuration to undeploy.
   * @param doneHandler An asynchronous handler to be called once the network has been
   *          undeployed.
   * @return The called Vertigo instance.
   */
  public Vertigo undeployRemoteNetwork(NetworkConfig network, Handler<AsyncResult<Void>> doneHandler) {
    VertigoClusterManager cluster = new RemoteClusterManager(vertx, container);
    cluster.undeployNetwork(network, doneHandler);
    return this;
  }

}
