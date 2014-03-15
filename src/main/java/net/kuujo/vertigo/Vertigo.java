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

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.platform.Container;
import org.vertx.java.platform.Verticle;

import net.kuujo.vertigo.cluster.Cluster;
import net.kuujo.vertigo.cluster.LocalCluster;
import net.kuujo.vertigo.cluster.RemoteCluster;
import net.kuujo.vertigo.network.Network;
import net.kuujo.vertigo.network.context.InstanceContext;
import net.kuujo.vertigo.network.context.NetworkContext;

/**
 * The primary Vertigo API.
 * <p>
 * 
 * This is the primary API for creating Vertigo networks and component instances. If the
 * current Vert.x verticle is a Vertigo component, the {@link Vertigo} object will contain
 * the component's {@link InstanceContext} as well as the concrete component instance. If
 * the current Vert.x verticle is not a Vertigo component, this API can be used to create
 * and deploy Vertigo networks.
 * 
 * @author Jordan Halterman
 */
public final class Vertigo {
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
   * Creates a new network.
   * 
   * @param address The network address.
   * @return A new network instance.
   */
  public Network createNetwork(String address) {
    return new Network(address);
  }

  /**
   * Deploys a network within the current Vert.x instance.
   * <p>
   * 
   * This deployment method uses the basic {@link LocalCluster} cluster implementation to
   * deploy network verticles and modules using the current Vert.x {@link Container}
   * instance.
   * 
   * @param network The network to deploy.
   * @return The called Vertigo instance.
   */
  public Vertigo deployLocalNetwork(Network network) {
    Cluster cluster = new LocalCluster(vertx, container);
    cluster.deployNetwork(network);
    return this;
  }

  /**
   * Deploys a network within the current Vert.x instance.
   * <p>
   * 
   * This deployment method uses the basic {@link LocalCluster} cluster implementation to
   * deploy network verticles and modules using the current Vert.x {@link Container}
   * instance.
   * 
   * @param network The network to deploy.
   * @param doneHandler An asynchronous handler to be called once deployment is complete.
   *          The handler will be called with the deployed network context which contains
   *          information about the network's component locations and event bus addresses.
   * @return The called Vertigo instance.
   */
  public Vertigo deployLocalNetwork(Network network, Handler<AsyncResult<NetworkContext>> doneHandler) {
    Cluster cluster = new LocalCluster(vertx, container);
    cluster.deployNetwork(network, doneHandler);
    return this;
  }

  /**
   * Undeploys a local network.
   *
   * @param address The address of the network to undeploy.
   * @return The Vertigo instance.
   */
  public Vertigo undeployLocalNetwork(String address) {
    Cluster cluster = new LocalCluster(vertx, container);
    cluster.undeployNetwork(address);
    return this;
  }

  /**
   * Undeploys a local network.
   *
   * @param address The address of the network to undeploy.
   * @param doneHandler An asynchronous handler to be called once the network has been undeployed.
   * @return The Vertigo instance.
   */
  public Vertigo undeployLocalNetwork(String address, Handler<AsyncResult<Void>> doneHandler) {
    Cluster cluster = new LocalCluster(vertx, container);
    cluster.undeployNetwork(address, doneHandler);
    return this;
  }

  /**
   * Undeploys a local network.
   * 
   * @param network The network to undeploy.
   * @return The called Vertigo instance.
   */
  public Vertigo undeployLocalNetwork(Network network) {
    Cluster cluster = new LocalCluster(vertx, container);
    cluster.undeployNetwork(network);
    return this;
  }

  /**
   * Undeploys a local network.
   * 
   * @param network The network to undeploy.
   * @param doneHandler An asynchronous handler to be called once the network has been undeployed.
   * @return The called Vertigo instance.
   */
  public Vertigo undeployLocalNetwork(Network network, Handler<AsyncResult<Void>> doneHandler) {
    Cluster cluster = new LocalCluster(vertx, container);
    cluster.undeployNetwork(network, doneHandler);
    return this;
  }

  /**
   * Deploys a network via the Vert.x event bus.
   * 
   * Deployment is performed using a {@link RemoteCluster} instance which communicates
   * module and verticle deployments over the event bus rather than performing deployments
   * directly using the Vert.x {@link Container}.
   * 
   * @param network The network to deploy.
   * @return The called Vertigo instance.
   */
  public Vertigo deployRemoteNetwork(Network network) {
    Cluster cluster = new RemoteCluster(vertx, container);
    cluster.deployNetwork(network);
    return this;
  }

  /**
   * Deploys a network via the Vert.x event bus.
   * 
   * Deployment is performed using a {@link RemoteCluster} instance which communicates
   * module and verticle deployments over the event bus rather than performing deployments
   * directly using the Vert.x {@link Container}.
   * 
   * @param network The network to deploy.
   * @param doneHandler An asynchronous handler to be called once deployment is complete.
   *          The handler will be called with the deployed network context which contains
   *          information about the network's component locations and event bus addresses.
   * @return The called Vertigo instance.
   */
  public Vertigo deployRemoteNetwork(Network network, Handler<AsyncResult<NetworkContext>> doneHandler) {
    Cluster cluster = new RemoteCluster(vertx, container);
    cluster.deployNetwork(network, doneHandler);
    return this;
  }

  /**
   * Undeploys a network via the Vert.x event bus.
   * 
   * @param address The address of the network to undeploy.
   * @return The called Vertigo instance.
   */
  public Vertigo undeployRemoteNetwork(String address) {
    Cluster cluster = new RemoteCluster(vertx, container);
    cluster.undeployNetwork(address);
    return this;
  }

  /**
   * Undeploys a network via the Vert.x event bus.
   * 
   * @param address The address of the network to undeploy.
   * @param doneHandler An asynchronous handler to be called once the network has been
   *          undeployed.
   * @return The called Vertigo instance.
   */
  public Vertigo undeployRemoteNetwork(String address, Handler<AsyncResult<Void>> doneHandler) {
    Cluster cluster = new RemoteCluster(vertx, container);
    cluster.undeployNetwork(address, doneHandler);
    return this;
  }

  /**
   * Undeploys a network via the Vert.x event bus.
   * 
   * @param network The network configuration to undeploy.
   * @return The called Vertigo instance.
   */
  public Vertigo undeployRemoteNetwork(Network network) {
    Cluster cluster = new RemoteCluster(vertx, container);
    cluster.undeployNetwork(network);
    return this;
  }

  /**
   * Undeploys a network via the Vert.x event bus.
   * 
   * @param network The network configuration to undeploy.
   * @param doneHandler An asynchronous handler to be called once the network has been
   *          undeployed.
   * @return The called Vertigo instance.
   */
  public Vertigo undeployRemoteNetwork(Network network, Handler<AsyncResult<Void>> doneHandler) {
    Cluster cluster = new RemoteCluster(vertx, container);
    cluster.undeployNetwork(network, doneHandler);
    return this;
  }

}
