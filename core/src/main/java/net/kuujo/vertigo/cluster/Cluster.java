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

import java.util.Collection;

import net.kuujo.vertigo.cluster.data.AsyncCounter;
import net.kuujo.vertigo.cluster.data.AsyncList;
import net.kuujo.vertigo.cluster.data.AsyncMap;
import net.kuujo.vertigo.cluster.data.AsyncMultiMap;
import net.kuujo.vertigo.cluster.data.AsyncQueue;
import net.kuujo.vertigo.cluster.data.AsyncSet;
import net.kuujo.vertigo.network.ActiveNetwork;
import net.kuujo.vertigo.network.NetworkConfig;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonObject;

/**
 * Client for communicating with the Vertigo cluster.<p>
 *
 * This client is the primary API for communicating with a Vertigo cluster.
 * Users can use the API to deploy modules, verticles, and networks or
 * access cluster-wide shared data.<p>
 *
 * The Vertigo cluster is a collection of modules that coordinate and
 * communicate to remotely deploy modules and verticles and provide
 * access to cluster-wide shared data. The Vertigo cluster does not have
 * any master. Users can communicate with a cluster using this API or by
 * sending messages directly to the cluster over the event bus.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public interface Cluster {

  /**
   * Returns the cluster address.
   *
   * @return The cluster address.
   */
  String address();

  /**
   * Registers a handler to be called when a node joins the cluster.<p>
   *
   * You can also register an event bus handler to listen for nodes joining
   * the cluster by registering a handler at <code>{cluster}.join</code>.
   * The handler will receive a {@link String} message containing the
   * absolute event bus address of the node that joined the cluster.
   *
   * @param handler A handler to be called when the node joins the cluster.
   * @return The cluster.
   */
  Cluster registerJoinHandler(Handler<Node> handler);

  /**
   * Registers a handler to be called when a node joins the cluster.<p>
   *
   * You can also register an event bus handler to listen for nodes joining
   * the cluster by registering a handler at <code>{cluster}.join</code>.
   * The handler will receive a {@link String} message containing the
   * absolute event bus address of the node that joined the cluster.
   *
   * @param handler A handler to be called when the node joins the cluster.
   * @param doneHandler A handler to be called once the handler has been registered.
   * @return The cluster.
   */
  Cluster registerJoinHandler(Handler<Node> handler, Handler<AsyncResult<Void>> doneHandler);

  /**
   * Unregisters a node join handler.
   *
   * @param handler The handler to unregister.
   * @return The cluster.
   */
  Cluster unregisterJoinHandler(Handler<Node> handler);

  /**
   * Unregisters a node join handler.
   *
   * @param handler The handler to unregister.
   * @param doneHandler A handler to be called once the handle has been unregistered.
   * @return The cluster.
   */
  Cluster unregisterJoinHandler(Handler<Node> handler, Handler<AsyncResult<Void>> doneHandler);

  /**
   * Registers a handler to be called when a node leaves the cluster.<p>
   *
   * You can also register an event bus handler to listen for nodes leaving
   * the cluster by registering a handler at <code>{cluster}.leave</code>.
   * The handler will receive a {@link String} message containing the
   * absolute event bus address of the node that left the cluster.
   *
   * @param handler A handler to be called when the node leaves the cluster.
   * @return The cluster.
   */
  Cluster registerLeaveHandler(Handler<Node> handler);

  /**
   * Registers a handler to be called when a node leaves the cluster.<p>
   *
   * You can also register an event bus handler to listen for nodes leaving
   * the cluster by registering a handler at <code>{cluster}.leave</code>.
   * The handler will receive a {@link String} message containing the
   * absolute event bus address of the node that left the cluster.
   *
   * @param handler A handler to be called when the node leaves the cluster.
   * @param doneHandler A handler to be called once the handler has been registered.
   * @return The cluster.
   */
  Cluster registerLeaveHandler(Handler<Node> handler, Handler<AsyncResult<Void>> doneHandler);

  /**
   * Unregisters a node leave handler.
   *
   * @param handler The handler to unregister.
   * @return The cluster.
   */
  Cluster unregisterLeaveHandler(Handler<Node> handler);

  /**
   * Unregisters a node leave handler.
   *
   * @param handler The handler to unregister.
   * @param doneHandler A handler to be called once the handle has been unregistered.
   * @return The cluster.
   */
  Cluster unregisterLeaveHandler(Handler<Node> handler, Handler<AsyncResult<Void>> doneHandler);

  /**
   * Pings the cluster.
   *
   * @param resultHandler A handler to be called once the cluster responds.
   * @return The cluster.
   */
  Cluster ping(Handler<AsyncResult<Cluster>> resultHandler);

  /**
   * Gets a group in the cluster.
   *
   * @param address The address of the group.
   * @param resultHandler A handler to be called with the result. If the group
   *        does not exist then a {@link ClusterException} will occur.
   * @return The cluster.
   */
  Cluster getGroup(String address, Handler<AsyncResult<Group>> resultHandler);

  /**
   * Gets a collection of groups in the cluster.
   *
   * @param resultHandler A handler to be called once the groups are loaded.
   * @return The cluster.
   */
  Cluster getGroups(Handler<AsyncResult<Collection<Group>>> resultHandler);

  /**
   * Selects a group in the cluster according to the given key.<p>
   *
   * The cluster guarantees that the same group will always be selected given
   * the same key so long as the group exists in the cluster. Once the group
   * leaves the cluster a new group will be selected.
   *
   * @param key The key from which to select the group.
   * @param resultHandler A handler to be called once complete.
   * @return The cluster.
   */
  Cluster selectGroup(Object key, Handler<AsyncResult<Group>> resultHandler);

  /**
   * Gets a node in the cluster.
   *
   * @param address The address of the node.
   * @param resultHandler A handler to be called with the result. If the node
   *        does not exist then a {@link ClusterException} will occur.
   * @return The cluster.
   */
  Cluster getNode(String address, Handler<AsyncResult<Node>> resultHandler);

  /**
   * Gets a collection of nodes in the cluster.
   *
   * @param resultHandler A handler to be called once the nodes are loaded.
   * @return The cluster.
   */
  Cluster getNodes(Handler<AsyncResult<Collection<Node>>> resultHandler);

  /**
   * Selects a node in the cluster according to the given key.<p>
   *
   * The cluster guarantees that the same node will always be selected given
   * the same key so long as the node exists in the cluster. Once the node
   * leaves the cluster a new node will be selected.
   *
   * @param key The key from which to select the group.
   * @param resultHandler A handler to be called once complete.
   * @return The cluster.
   */
  Cluster selectNode(Object key, Handler<AsyncResult<Node>> resultHandler);

  /**
   * Installs a local module to all nodes the cluster.<p>
   *
   * The module being installed must be available on the local classpath in
   * the normal Vert.x <code>mods</code> directory. If the module is already
   * installed on a given node then the node will be skipped, otherwise the
   * local module will be zipped, uploaded, and installed to the node.
   *
   * @param moduleName The name of the module to install.
   * @return The cluster.
   */
  Cluster installModule(String moduleName);

  /**
   * Installs a local module to all nodes in the cluster.<p>
   *
   * The module being installed must be available on the local classpath in
   * the normal Vert.x <code>mods</code> directory. If the module is already
   * installed on a given node then the node will be skipped, otherwise the
   * local module will be zipped, uploaded, and installed to the node.
   *
   * @param moduleName The name of the module to install.
   * @param doneHandler A handler to be called once the module has been installed.
   * @return The cluster.
   */
  Cluster installModule(String moduleName, Handler<AsyncResult<Void>> doneHandler);

  /**
   * Uninstalls a module from all nodes in the cluster.<p>
   *
   * When the module is uninstalled, it will be <em>removed from the classpath</em>
   * of the remote node. This means you should not uninstall modules that were
   * originally installed on any given node.
   *
   * @param moduleName The name of the module to uninstall.
   * @return The cluster.
   */
  Cluster uninstallModule(String moduleName);

  /**
   * Uninstalls a module from all nodes in the cluster.<p>
   *
   * When the module is uninstalled, it will be <em>removed from the classpath</em>
   * of the remote node. This means you should not uninstall modules that were
   * originally installed on any given node.
   *
   * @param moduleName The name of the module to uninstall.
   * @param doneHandler A handler to be called once the module has been uninstalled.
   * @return The cluster.
   */
  Cluster uninstallModule(String moduleName, Handler<AsyncResult<Void>> doneHandler);

  /**
   * Deploys a module to the cluster.<p>
   *
   * The module must be available on whichever node receives the deploy message.
   * This means ideally the module should be installed on <em>all</em> nodes in
   * the cluster. You can also select a node using the helper methods provided.
   *
   * @param moduleName The name of the module to deploy.
   * @return The cluster.
   */
  Cluster deployModule(String moduleName);

  /**
   * Deploys a module to the cluster.<p>
   *
   * The module must be available on whichever node receives the deploy message.
   * This means ideally the module should be installed on <em>all</em> nodes in
   * the cluster. You can also select a node using the helper methods provided.
   *
   * @param moduleName The name of the module to deploy.
   * @param config The module configuration.
   * @return The cluster.
   */
  Cluster deployModule(String moduleName, JsonObject config);

  /**
   * Deploys a module to the cluster.<p>
   *
   * The module must be available on whichever node receives the deploy message.
   * This means ideally the module should be installed on <em>all</em> nodes in
   * the cluster. You can also select a node using the helper methods provided.
   *
   * @param moduleName The name of the module to deploy.
   * @param instances The number of instances to deploy.
   * @return The cluster.
   */
  Cluster deployModule(String moduleName, int instances);

  /**
   * Deploys a module to the cluster.<p>
   *
   * The module must be available on whichever node receives the deploy message.
   * This means ideally the module should be installed on <em>all</em> nodes in
   * the cluster. You can also select a node using the helper methods provided.
   *
   * @param moduleName The name of the module to deploy.
   * @param config The module configuration.
   * @param instances The number of instances to deploy.
   * @return The cluster.
   */
  Cluster deployModule(String moduleName, JsonObject config, int instances);

  /**
   * Deploys a module to the cluster.<p>
   *
   * The module must be available on whichever node receives the deploy message.
   * This means ideally the module should be installed on <em>all</em> nodes in
   * the cluster. You can also select a node using the helper methods provided.
   *
   * @param moduleName The name of the module to deploy.
   * @param doneHandler A handler to be called once the module has been deployed.
   *        The handler will be called with the unique deployment ID if successful.
   * @return The cluster.
   */
  Cluster deployModule(String moduleName, Handler<AsyncResult<String>> doneHandler);

  /**
   * Deploys a module to the cluster.<p>
   *
   * The module must be available on whichever node receives the deploy message.
   * This means ideally the module should be installed on <em>all</em> nodes in
   * the cluster. You can also select a node using the helper methods provided.
   *
   * @param moduleName The name of the module to deploy.
   * @param config The module configuration.
   * @param doneHandler A handler to be called once the module has been deployed.
   *        The handler will be called with the unique deployment ID if successful.
   * @return The cluster.
   */
  Cluster deployModule(String moduleName, JsonObject config, Handler<AsyncResult<String>> doneHandler);

  /**
   * Deploys a module to the cluster.<p>
   *
   * The module must be available on whichever node receives the deploy message.
   * This means ideally the module should be installed on <em>all</em> nodes in
   * the cluster. You can also select a node using the helper methods provided.
   *
   * @param moduleName The name of the module to deploy.
   * @param instances The number of instances to deploy.
   * @param doneHandler A handler to be called once the module has been deployed.
   *        The handler will be called with the unique deployment ID if successful.
   * @return The cluster.
   */
  Cluster deployModule(String moduleName, int instances, Handler<AsyncResult<String>> doneHandler);

  /**
   * Deploys a module to the cluster.<p>
   *
   * The module must be available on whichever node receives the deploy message.
   * This means ideally the module should be installed on <em>all</em> nodes in
   * the cluster. You can also select a node using the helper methods provided.
   *
   * @param moduleName The name of the module to deploy.
   * @param config The module configuration.
   * @param instances The number of instances to deploy.
   * @param doneHandler A handler to be called once the module has been deployed.
   *        The handler will be called with the unique deployment ID if successful.
   * @return The cluster.
   */
  Cluster deployModule(String moduleName, JsonObject config, int instances, Handler<AsyncResult<String>> doneHandler);

  /**
   * Deploys a verticle to the cluster.<p>
   *
   * The verticle must be available on whichever node receives the deploy message.
   * If the verticle is not available on the classpath, a {@link ClusterException}
   * will occur. You can deploy a verticle to a specific node by using the node
   * selection API, or by wrapping a verticle in a module Vertigo can upload and
   * deploy the module to a specific node.
   *
   * @param main The verticle main.
   * @return The cluster.
   */
  Cluster deployVerticle(String main);

  /**
   * Deploys a verticle to the cluster.<p>
   *
   * The verticle must be available on whichever node receives the deploy message.
   * If the verticle is not available on the classpath, a {@link ClusterException}
   * will occur. You can deploy a verticle to a specific node by using the node
   * selection API, or by wrapping a verticle in a module Vertigo can upload and
   * deploy the module to a specific node.
   *
   * @param main The verticle main.
   * @param instances The number of instances to deploy.
   * @return The cluster.
   */
  Cluster deployVerticle(String main, JsonObject config);

  /**
   * Deploys a verticle to the cluster.<p>
   *
   * The verticle must be available on whichever node receives the deploy message.
   * If the verticle is not available on the classpath, a {@link ClusterException}
   * will occur. You can deploy a verticle to a specific node by using the node
   * selection API, or by wrapping a verticle in a module Vertigo can upload and
   * deploy the module to a specific node.
   *
   * @param main The verticle main.
   * @param config The verticle configuration.
   * @return The cluster.
   */
  Cluster deployVerticle(String main, int instances);

  /**
   * Deploys a verticle to the cluster.<p>
   *
   * The verticle must be available on whichever node receives the deploy message.
   * If the verticle is not available on the classpath, a {@link ClusterException}
   * will occur. You can deploy a verticle to a specific node by using the node
   * selection API, or by wrapping a verticle in a module Vertigo can upload and
   * deploy the module to a specific node.
   *
   * @param main The verticle main.
   * @param config The verticle configuration.
   * @param instances The number of instances to deploy.
   * @return The cluster.
   */
  Cluster deployVerticle(String main, JsonObject config, int instances);

  /**
   * Deploys a verticle to the cluster.<p>
   *
   * The verticle must be available on whichever node receives the deploy message.
   * If the verticle is not available on the classpath, a {@link ClusterException}
   * will occur. You can deploy a verticle to a specific node by using the node
   * selection API, or by wrapping a verticle in a module Vertigo can upload and
   * deploy the module to a specific node.
   *
   * @param main The verticle main.
   * @param doneHandler A handler to be called once the verticle has been deployed.
   *        The handler will be called with the unique deployment ID if successful.
   * @return The cluster.
   */
  Cluster deployVerticle(String main, Handler<AsyncResult<String>> doneHandler);

  /**
   * Deploys a verticle to the cluster.<p>
   *
   * The verticle must be available on whichever node receives the deploy message.
   * If the verticle is not available on the classpath, a {@link ClusterException}
   * will occur. You can deploy a verticle to a specific node by using the node
   * selection API, or by wrapping a verticle in a module Vertigo can upload and
   * deploy the module to a specific node.
   *
   * @param main The verticle main.
   * @param config The verticle configuration.
   * @param doneHandler A handler to be called once the verticle has been deployed.
   *        The handler will be called with the unique deployment ID if successful.
   * @return The cluster.
   */
  Cluster deployVerticle(String main, JsonObject config, Handler<AsyncResult<String>> doneHandler);

  /**
   * Deploys a verticle to the cluster.<p>
   *
   * The verticle must be available on whichever node receives the deploy message.
   * If the verticle is not available on the classpath, a {@link ClusterException}
   * will occur. You can deploy a verticle to a specific node by using the node
   * selection API, or by wrapping a verticle in a module Vertigo can upload and
   * deploy the module to a specific node.
   *
   * @param main The verticle main.
   * @param instances The number of instances to deploy.
   * @param doneHandler A handler to be called once the verticle has been deployed.
   *        The handler will be called with the unique deployment ID if successful.
   * @return The cluster.
   */
  Cluster deployVerticle(String main, int instances, Handler<AsyncResult<String>> doneHandler);

  /**
   * Deploys a verticle to the cluster.<p>
   *
   * The verticle must be available on whichever node receives the deploy message.
   * If the verticle is not available on the classpath, a {@link ClusterException}
   * will occur. You can deploy a verticle to a specific node by using the node
   * selection API, or by wrapping a verticle in a module Vertigo can upload and
   * deploy the module to a specific node.
   *
   * @param main The verticle main.
   * @param config The verticle configuration.
   * @param instances The number of instances to deploy.
   * @param doneHandler A handler to be called once the verticle has been deployed.
   *        The handler will be called with the unique deployment ID if successful.
   * @return The cluster.
   */
  Cluster deployVerticle(String main, JsonObject config, int instances, Handler<AsyncResult<String>> doneHandler);

  /**
   * Deploys a worker verticle to the cluster.<p>
   *
   * The verticle must be available on whichever node receives the deploy message.
   * If the verticle is not available on the classpath, a {@link ClusterException}
   * will occur. You can deploy a verticle to a specific node by using the node
   * selection API, or by wrapping a verticle in a module Vertigo can upload and
   * deploy the module to a specific node.
   *
   * @param main The verticle main.
   * @return The cluster.
   */
  Cluster deployWorkerVerticle(String main);

  /**
   * Deploys a worker verticle to the cluster.<p>
   *
   * The verticle must be available on whichever node receives the deploy message.
   * If the verticle is not available on the classpath, a {@link ClusterException}
   * will occur. You can deploy a verticle to a specific node by using the node
   * selection API, or by wrapping a verticle in a module Vertigo can upload and
   * deploy the module to a specific node.
   *
   * @param main The verticle main.
   * @param config The verticle configuration.
   * @return The cluster.
   */
  Cluster deployWorkerVerticle(String main, JsonObject config);

  /**
   * Deploys a worker verticle to the cluster.<p>
   *
   * The verticle must be available on whichever node receives the deploy message.
   * If the verticle is not available on the classpath, a {@link ClusterException}
   * will occur. You can deploy a verticle to a specific node by using the node
   * selection API, or by wrapping a verticle in a module Vertigo can upload and
   * deploy the module to a specific node.
   *
   * @param main The verticle main.
   * @param instances The number of instances to deploy.
   * @return The cluster.
   */
  Cluster deployWorkerVerticle(String main, int instances);

  /**
   * Deploys a worker verticle to the cluster.<p>
   *
   * The verticle must be available on whichever node receives the deploy message.
   * If the verticle is not available on the classpath, a {@link ClusterException}
   * will occur. You can deploy a verticle to a specific node by using the node
   * selection API, or by wrapping a verticle in a module Vertigo can upload and
   * deploy the module to a specific node.
   *
   * @param main The verticle main.
   * @param config The verticle configuration.
   * @param instances The number of instances to deploy.
   * @param multiThreaded Indicates whether the worker should be multi-threaded.
   * @return The cluster.
   */
  Cluster deployWorkerVerticle(String main, JsonObject config, int instances, boolean multiThreaded);

  /**
   * Deploys a worker verticle to the cluster.<p>
   *
   * The verticle must be available on whichever node receives the deploy message.
   * If the verticle is not available on the classpath, a {@link ClusterException}
   * will occur. You can deploy a verticle to a specific node by using the node
   * selection API, or by wrapping a verticle in a module Vertigo can upload and
   * deploy the module to a specific node.
   *
   * @param main The verticle main.
   * @param doneHandler A handler to be called once the verticle has been deployed.
   *        The handler will be called with the unique deployment ID if successful.
   * @return The cluster.
   */
  Cluster deployWorkerVerticle(String main, Handler<AsyncResult<String>> doneHandler);

  /**
   * Deploys a worker verticle to the cluster.<p>
   *
   * The verticle must be available on whichever node receives the deploy message.
   * If the verticle is not available on the classpath, a {@link ClusterException}
   * will occur. You can deploy a verticle to a specific node by using the node
   * selection API, or by wrapping a verticle in a module Vertigo can upload and
   * deploy the module to a specific node.
   *
   * @param main The verticle main.
   * @param config The verticle configuration.
   * @param doneHandler A handler to be called once the verticle has been deployed.
   *        The handler will be called with the unique deployment ID if successful.
   * @return The cluster.
   */
  Cluster deployWorkerVerticle(String main, JsonObject config, Handler<AsyncResult<String>> doneHandler);

  /**
   * Deploys a worker verticle to the cluster.<p>
   *
   * The verticle must be available on whichever node receives the deploy message.
   * If the verticle is not available on the classpath, a {@link ClusterException}
   * will occur. You can deploy a verticle to a specific node by using the node
   * selection API, or by wrapping a verticle in a module Vertigo can upload and
   * deploy the module to a specific node.
   *
   * @param main The verticle main.
   * @param instances The number of instances to deploy.
   * @param doneHandler A handler to be called once the verticle has been deployed.
   *        The handler will be called with the unique deployment ID if successful.
   * @return The cluster.
   */
  Cluster deployWorkerVerticle(String main, int instances, Handler<AsyncResult<String>> doneHandler);

  /**
   * Deploys a worker verticle to the cluster.<p>
   *
   * The verticle must be available on whichever node receives the deploy message.
   * If the verticle is not available on the classpath, a {@link ClusterException}
   * will occur. You can deploy a verticle to a specific node by using the node
   * selection API, or by wrapping a verticle in a module Vertigo can upload and
   * deploy the module to a specific node.
   *
   * @param main The verticle main.
   * @param config The verticle configuration.
   * @param instances The number of instances to deploy.
   * @param multiThreaded Indicates whether the worker should be multi-threaded.
   * @param doneHandler A handler to be called once the verticle has been deployed.
   *        The handler will be called with the unique deployment ID if successful.
   * @return The cluster.
   */
  Cluster deployWorkerVerticle(String main, JsonObject config, int instances, boolean multiThreaded, Handler<AsyncResult<String>> doneHandler);

  /**
   * Undeploys a module from the cluster.<p>
   *
   * Given the unique module deployment ID, Vertigo will ensure that the deployment
   * is undeployed from the appropriate node in the cluster.
   *
   * @param deploymentID The deployment ID of the module to undeploy.
   * @return The cluster.
   */
  Cluster undeployModule(String deploymentID);

  /**
   * Undeploys a module from the cluster.<p>
   *
   * Given the unique module deployment ID, Vertigo will ensure that the deployment
   * is undeployed from the appropriate node in the cluster.
   *
   * @param deploymentID The deployment ID of the module to undeploy.
   * @param doneHandler A handler to be called once complete.
   * @return The cluster.
   */
  Cluster undeployModule(String deploymentID, Handler<AsyncResult<Void>> doneHandler);

  /**
   * Undeploys a verticle from the cluster.<p>
   *
   * Given the unique verticle deployment ID, Vertigo will ensure that the deployment
   * is undeployed from the appropriate node in the cluster.
   *
   * @param deploymentID The deployment ID of the verticle to undeploy.
   * @return The cluster.
   */
  Cluster undeployVerticle(String deploymentID);

  /**
   * Undeploys a verticle from the cluster.<p>
   *
   * Given the unique verticle deployment ID, Vertigo will ensure that the deployment
   * is undeployed from the appropriate node in the cluster.
   *
   * @param deploymentID The deployment ID of the verticle to undeploy.
   * @param doneHandler A handler to be called once complete.
   * @return The cluster.
   */
  Cluster undeployVerticle(String deploymentID, Handler<AsyncResult<Void>> doneHandler);

  /**
   * Gets a list of deployed networks from the cluster.
   *
   * @param resultHandler An asynchronous handler to be called with a collection of
   *        active networks currently deployed in the cluster.
   * @return The cluster.
   */
  Cluster getNetworks(Handler<AsyncResult<Collection<ActiveNetwork>>> resultHandler);

  /**
   * Loads a network from the cluster.
   *
   * @param name The name of the network to load.
   * @param resultHandler An asynchronous handler to be called with the active network.
   *        If the network is not running in the cluster then a {@link ClusterException}
   *        will occur.
   * @return The cluster.
   */
  Cluster getNetwork(String name, Handler<AsyncResult<ActiveNetwork>> resultHandler);

  /**
   * Deploys a bare network to the cluster.<p>
   *
   * The network will be deployed with no components and no connections. You
   * can add components and connections to the network with an {@link ActiveNetwork}
   * instance.
   *
   * @param name The name of the network to deploy.
   * @return The cluster.
   */
  Cluster deployNetwork(String name);

  /**
   * Deploys a bare network to the cluster.<p>
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
   * @return The cluster.
   */
  Cluster deployNetwork(String name, Handler<AsyncResult<ActiveNetwork>> doneHandler);

  /**
   * Deploys a JSON network to the cluster.<p>
   *
   * The JSON network configuration will be converted to a {@link NetworkConfig} before
   * being deployed to the cluster. The conversion is done synchronously, so if the
   * configuration is invalid then this method may throw an exception.
   *
   * @param network The JSON network configuration. For the configuration format see
   *        the project documentation.
   * @return The cluster.
   */
  Cluster deployNetwork(JsonObject network);

  /**
   * Deploys a JSON network to the cluster.<p>
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
   * @return The cluster.
   */
  Cluster deployNetwork(JsonObject network, Handler<AsyncResult<ActiveNetwork>> doneHandler);

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
   * @return The cluster.
   */
  Cluster deployNetwork(NetworkConfig network);

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
   * @param doneHandler An asynchronous handler to be called once the network has
   *        completed deployment. The handler will be called with an {@link ActiveNetwork}
   *        instance which can be used to add or remove components and connections from
   *        the network.
   * @return The cluster.
   */
  Cluster deployNetwork(NetworkConfig network, Handler<AsyncResult<ActiveNetwork>> doneHandler);

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
   * @return The cluster.
   */
  Cluster undeployNetwork(String name);

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
   * @return The cluster.
   */
  Cluster undeployNetwork(String name, Handler<AsyncResult<Void>> doneHandler);


  /**
   * Undeploys a network from the cluster from a json configuration.<p>
   *
   * The JSON configuration will immediately be converted to a {@link NetworkConfig} prior
   * to undeploying the network. In order to undeploy the entire network, the configuration
   * should be the same as the deployed configuration. Vertigo will use configuration
   * components to determine whether two configurations are identical. If the given
   * configuration is not identical to the running configuration, any components or
   * connections in the json configuration that are present in the running network
   * will be closed and removed from the running network.
   *
   * @param network The JSON configuration to undeploy. For the configuration format see
   *        the project documentation.
   * @return The cluster.
   */
  Cluster undeployNetwork(JsonObject network);

  /**
   * Undeploys a network from the cluster from a json configuration.<p>
   *
   * The JSON configuration will immediately be converted to a {@link NetworkConfig} prior
   * to undeploying the network. In order to undeploy the entire network, the configuration
   * should be the same as the deployed configuration. Vertigo will use configuration
   * components to determine whether two configurations are identical. If the given
   * configuration is not identical to the running configuration, any components or
   * connections in the json configuration that are present in the running network
   * will be closed and removed from the running network.
   *
   * @param network The JSON configuration to undeploy. For the configuration format see
   *        the project documentation.
   * @param doneHandler An asynchronous handler to be called once the configuration is undeployed.
   * @return The cluster.
   */
  Cluster undeployNetwork(JsonObject network, Handler<AsyncResult<Void>> doneHandler);

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
   * @return The cluster.
   */
  Cluster undeployNetwork(NetworkConfig network);

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
   * @param doneHandler An asynchronous handler to be called once the configuration is undeployed.
   * @return The cluster.
   */
  Cluster undeployNetwork(NetworkConfig network, Handler<AsyncResult<Void>> doneHandler);

  /**
   * Gets an asynchronous map from the cluster.
   *
   * @param name The name of the map to get.
   * @return The async map.
   */
  <K, V> AsyncMap<K, V> getMap(String name);

  /**
   * Gets an asynchronous multi-map from the cluster.
   *
   * @param name The name of the multi-map to get.
   * @return The async multi-map.
   */
  <K, V> AsyncMultiMap<K, V> getMultiMap(String name);

  /**
   * Gets an asynchronous set from the cluster.
   *
   * @param name The name of the set to get.
   * @return The async set.
   */
  <T> AsyncSet<T> getSet(String name);

  /**
   * Gets an asynchronous list from the cluster.
   *
   * @param name The name of the list to get.
   * @return The async list.
   */
  <T> AsyncList<T> getList(String name);

  /**
   * Gets an asynchronous queue from the cluster.
   *
   * @param name The name of the queue to get.
   * @return The async queue.
   */
  <T> AsyncQueue<T> getQueue(String name);

  /**
   * Gets an asynchronous counter from the cluster.
   *
   * @param name The name of the counter to get.
   * @return The async counter.
   */
  AsyncCounter getCounter(String name);

}
