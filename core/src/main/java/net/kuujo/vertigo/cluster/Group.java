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

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonObject;

/**
 * Client for communicating with cluster groups.<p>
 *
 * This client handles deployment of modules and verticles within a
 * group of nodes in a Vertigo cluster.<p>
 *
 * A single Vertigo cluster can be split into deployment groups. Groups
 * allow users to specify groups of nodes on which a given module or verticle
 * can be deployed. This is an interface for operating on a single group
 * within a specific cluster. Deployments made to the group are guaranteed
 * to be deployed only on nodes within the group.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public interface Group {

  /**
   * Returns the group address.
   *
   * @return The group address.
   */
  String address();

  /**
   * Registers a handler to be called when a node joins the group.<p>
   *
   * You can also register an event bus handler to listen for nodes joining
   * the group by registering a handler at <code>{cluster}.{group}.join</code>.
   * The handler will receive a {@link String} message containing the
   * absolute event bus address of the node that joined the group.
   *
   * @param handler A handler to be called when the node joins the group.
   * @return The group.
   */
  Group registerJoinHandler(Handler<Node> handler);

  /**
   * Registers a handler to be called when a node joins the group.<p>
   *
   * You can also register an event bus handler to listen for nodes joining
   * the group by registering a handler at <code>{cluster}.{group}.join</code>.
   * The handler will receive a {@link String} message containing the
   * absolute event bus address of the node that joined the group.
   *
   * @param handler A handler to be called when the node joins the group.
   * @param doneHandler A handler to be called once the handler has been registered.
   * @return The group.
   */
  Group registerJoinHandler(Handler<Node> handler, Handler<AsyncResult<Void>> doneHandler);

  /**
   * Unregisters a node join handler.
   *
   * @param handler The handler to unregister.
   * @return The group.
   */
  Group unregisterJoinHandler(Handler<Node> handler);

  /**
   * Unregisters a node join handler.
   *
   * @param handler The handler to unregister.
   * @param doneHandler A handler to be called once the handle has been unregistered.
   * @return The group.
   */
  Group unregisterJoinHandler(Handler<Node> handler, Handler<AsyncResult<Void>> doneHandler);

  /**
   * Registers a handler to be called when a node leaves the group.<p>
   *
   * You can also register an event bus handler to listen for nodes leaving
   * the group by registering a handler at <code>{cluster}.{group}.leave</code>.
   * The handler will receive a {@link String} message containing the
   * absolute event bus address of the node that left the group.
   *
   * @param handler A handler to be called when the node leaves the group.
   * @return The group.
   */
  Group registerLeaveHandler(Handler<Node> handler);

  /**
   * Registers a handler to be called when a node leaves the group.<p>
   *
   * You can also register an event bus handler to listen for nodes leaving
   * the group by registering a handler at <code>{cluster}.{group}.leave</code>.
   * The handler will receive a {@link String} message containing the
   * absolute event bus address of the node that left the group.
   *
   * @param handler A handler to be called when the node leaves the group.
   * @param doneHandler A handler to be called once the handler has been registered.
   * @return The group.
   */
  Group registerLeaveHandler(Handler<Node> handler, Handler<AsyncResult<Void>> doneHandler);

  /**
   * Unregisters a node leave handler.
   *
   * @param handler The handler to unregister.
   * @return The group.
   */
  Group unregisterLeaveHandler(Handler<Node> handler);

  /**
   * Unregisters a node leave handler.
   *
   * @param handler The handler to unregister.
   * @param doneHandler A handler to be called once the handle has been unregistered.
   * @return The group.
   */
  Group unregisterLeaveHandler(Handler<Node> handler, Handler<AsyncResult<Void>> doneHandler);

  /**
   * Pings the group.
   *
   * @param resultHandler A handler to be called once the group responds.
   * @return The group.
   */
  Group ping(Handler<AsyncResult<Group>> resultHandler);

  /**
   * Gets a node in the group.
   *
   * @param address The address of the node.
   * @param resultHandler A handler to be called with the result. If the node
   *        does not exist then a {@link ClusterException} will occur.
   * @return The group.
   */
  Group getNode(String address, Handler<AsyncResult<Node>> resultHandler);

  /**
   * Gets a collection of nodes in the group.
   *
   * @param resultHandler A handler to be called once the nodes are loaded.
   * @return The group.
   */
  Group getNodes(Handler<AsyncResult<Collection<Node>>> resultHandler);

  /**
   * Selects a node in the group according to the given key.<p>
   *
   * The group guarantees that the same node will always be selected given
   * the same key so long as the node exists in the group. Once the node
   * leaves the group a new node will be selected.
   *
   * @param key The key from which to select the group.
   * @param resultHandler A handler to be called once complete.
   * @return The group.
   */
  Group selectNode(Object key, Handler<AsyncResult<Node>> resultHandler);

  /**
   * Installs a local module to all nodes in the group.<p>
   *
   * The module being installed must be available on the local classpath in
   * the normal Vert.x <code>mods</code> directory. If the module is already
   * installed on a given node then the node will be skipped, otherwise the
   * local module will be zipped, uploaded, and installed to the node.
   *
   * @param moduleName The name of the module to install.
   * @return The group.
   */
  Group installModule(String moduleName);

  /**
   * Installs a local module to all nodes in the group.<p>
   *
   * The module being installed must be available on the local classpath in
   * the normal Vert.x <code>mods</code> directory. If the module is already
   * installed on a given node then the node will be skipped, otherwise the
   * local module will be zipped, uploaded, and installed to the node.
   *
   * @param moduleName The name of the module to install.
   * @param doneHandler A handler to be called once the module has been installed.
   * @return The group.
   */
  Group installModule(String moduleName, Handler<AsyncResult<Void>> doneHandler);

  /**
   * Uninstalls a module from all nodes in the group.<p>
   *
   * When the module is uninstalled, it will be <em>removed from the classpath</em>
   * of the remote node. This means you should not uninstall modules that were
   * originally installed on any given node.
   *
   * @param moduleName The name of the module to uninstall.
   * @return The group.
   */
  Group uninstallModule(String moduleName);

  /**
   * Uninstalls a module from all nodes in the group.<p>
   *
   * When the module is uninstalled, it will be <em>removed from the classpath</em>
   * of the remote node. This means you should not uninstall modules that were
   * originally installed on any given node.
   *
   * @param moduleName The name of the module to uninstall.
   * @param doneHandler A handler to be called once the module has been uninstalled.
   * @return The group.
   */
  Group uninstallModule(String moduleName, Handler<AsyncResult<Void>> doneHandler);

  /**
   * Deploys a module to the group.<p>
   *
   * The module must be available on whichever node receives the deploy message.
   * This means ideally the module should be installed on <em>all</em> nodes in
   * the group. You can also select a node using the helper methods provided.
   *
   * @param moduleName The name of the module to deploy.
   * @return The group.
   */
  Group deployModule(String moduleName);

  /**
   * Deploys a module to the group.<p>
   *
   * The module must be available on whichever node receives the deploy message.
   * This means ideally the module should be installed on <em>all</em> nodes in
   * the group. You can also select a node using the helper methods provided.
   *
   * @param moduleName The name of the module to deploy.
   * @param config The module configuration.
   * @return The group.
   */
  Group deployModule(String moduleName, JsonObject config);

  /**
   * Deploys a module to the group.<p>
   *
   * The module must be available on whichever node receives the deploy message.
   * This means ideally the module should be installed on <em>all</em> nodes in
   * the group. You can also select a node using the helper methods provided.
   *
   * @param moduleName The name of the module to deploy.
   * @param instances The number of instances to deploy.
   * @return The group.
   */
  Group deployModule(String moduleName, int instances);

  /**
   * Deploys a module to the group.<p>
   *
   * The module must be available on whichever node receives the deploy message.
   * This means ideally the module should be installed on <em>all</em> nodes in
   * the group. You can also select a node using the helper methods provided.
   *
   * @param moduleName The name of the module to deploy.
   * @param config The module configuration.
   * @param instances The number of instances to deploy.
   * @return The group.
   */
  Group deployModule(String moduleName, JsonObject config, int instances);

  /**
   * Deploys a module to the group.<p>
   *
   * The module must be available on whichever node receives the deploy message.
   * This means ideally the module should be installed on <em>all</em> nodes in
   * the group. You can also select a node using the helper methods provided.
   *
   * @param moduleName The name of the module to deploy.
   * @param config The module configuration.
   * @param instances The number of instances to deploy.
   * @param ha Indicates whether to deploy the module with HA.
   * @return The group.
   */
  Group deployModule(String moduleName, JsonObject config, int instances, boolean ha);

  /**
   * Deploys a module to the group.<p>
   *
   * The module must be available on whichever node receives the deploy message.
   * This means ideally the module should be installed on <em>all</em> nodes in
   * the group. You can also select a node using the helper methods provided.
   *
   * @param moduleName The name of the module to deploy.
   * @param doneHandler A handler to be called once the module has been deployed.
   *        The handler will be called with the unique deployment ID if successful.
   * @return The group.
   */
  Group deployModule(String moduleName, Handler<AsyncResult<String>> doneHandler);

  /**
   * Deploys a module to the group.<p>
   *
   * The module must be available on whichever node receives the deploy message.
   * This means ideally the module should be installed on <em>all</em> nodes in
   * the group. You can also select a node using the helper methods provided.
   *
   * @param moduleName The name of the module to deploy.
   * @param config The module configuration.
   * @param doneHandler A handler to be called once the module has been deployed.
   *        The handler will be called with the unique deployment ID if successful.
   * @return The group.
   */
  Group deployModule(String moduleName, JsonObject config, Handler<AsyncResult<String>> doneHandler);

  /**
   * Deploys a module to the group.<p>
   *
   * The module must be available on whichever node receives the deploy message.
   * This means ideally the module should be installed on <em>all</em> nodes in
   * the group. You can also select a node using the helper methods provided.
   *
   * @param moduleName The name of the module to deploy.
   * @param instances The number of instances to deploy.
   * @param doneHandler A handler to be called once the module has been deployed.
   *        The handler will be called with the unique deployment ID if successful.
   * @return The group.
   */
  Group deployModule(String moduleName, int instances, Handler<AsyncResult<String>> doneHandler);

  /**
   * Deploys a module to the group.<p>
   *
   * The module must be available on whichever node receives the deploy message.
   * This means ideally the module should be installed on <em>all</em> nodes in
   * the group. You can also select a node using the helper methods provided.
   *
   * @param moduleName The name of the module to deploy.
   * @param config The module configuration.
   * @param instances The number of instances to deploy.
   * @param doneHandler A handler to be called once the module has been deployed.
   *        The handler will be called with the unique deployment ID if successful.
   * @return The group.
   */
  Group deployModule(String moduleName, JsonObject config, int instances, Handler<AsyncResult<String>> doneHandler);

  /**
   * Deploys a module to the group.<p>
   *
   * The module must be available on whichever node receives the deploy message.
   * This means ideally the module should be installed on <em>all</em> nodes in
   * the group. You can also select a node using the helper methods provided.
   *
   * @param moduleName The name of the module to deploy.
   * @param config The module configuration.
   * @param instances The number of instances to deploy.
   * @param ha Indicates whether to deploy the module with HA.
   * @param doneHandler A handler to be called once the module has been deployed.
   *        The handler will be called with the unique deployment ID if successful.
   * @return The group.
   */
  Group deployModule(String moduleName, JsonObject config, int instances, boolean ha, Handler<AsyncResult<String>> doneHandler);

  /**
   * Deploys a verticle to the group.<p>
   *
   * The verticle must be available on whichever node receives the deploy message.
   * If the verticle is not available on the classpath, a {@link ClusterException}
   * will occur. You can deploy a verticle to a specific node by using the node
   * selection API, or by wrapping a verticle in a module Vertigo can upload and
   * deploy the module to a specific node.
   *
   * @param main The verticle main.
   * @return The group.
   */
  Group deployVerticle(String main);

  /**
   * Deploys a verticle to the group.<p>
   *
   * The verticle must be available on whichever node receives the deploy message.
   * If the verticle is not available on the classpath, a {@link ClusterException}
   * will occur. You can deploy a verticle to a specific node by using the node
   * selection API, or by wrapping a verticle in a module Vertigo can upload and
   * deploy the module to a specific node.
   *
   * @param main The verticle main.
   * @param config The verticle configuration.
   * @return The group.
   */
  Group deployVerticle(String main, JsonObject config);

  /**
   * Deploys a verticle to the group.<p>
   *
   * The verticle must be available on whichever node receives the deploy message.
   * If the verticle is not available on the classpath, a {@link ClusterException}
   * will occur. You can deploy a verticle to a specific node by using the node
   * selection API, or by wrapping a verticle in a module Vertigo can upload and
   * deploy the module to a specific node.
   *
   * @param main The verticle main.
   * @param instances The number of instances to deploy.
   * @return The group.
   */
  Group deployVerticle(String main, int instances);

  /**
   * Deploys a verticle to the group.<p>
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
   * @return The group.
   */
  Group deployVerticle(String main, JsonObject config, int instances);

  /**
   * Deploys a verticle to the group.<p>
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
   * @param ha Indicates whether to deploy the verticle with HA.
   * @return The group.
   */
  Group deployVerticle(String main, JsonObject config, int instances, boolean ha);

  /**
   * Deploys a verticle to the group.<p>
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
   * @return The group.
   */
  Group deployVerticle(String main, Handler<AsyncResult<String>> doneHandler);

  /**
   * Deploys a verticle to the group.<p>
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
   * @return The group.
   */
  Group deployVerticle(String main, JsonObject config, Handler<AsyncResult<String>> doneHandler);

  /**
   * Deploys a verticle to the group.<p>
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
   * @return The group.
   */
  Group deployVerticle(String main, int instances, Handler<AsyncResult<String>> doneHandler);

  /**
   * Deploys a verticle to the group.<p>
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
   * @return The group.
   */
  Group deployVerticle(String main, JsonObject config, int instances, Handler<AsyncResult<String>> doneHandler);

  /**
   * Deploys a verticle to the group.<p>
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
   * @param ha Indicates whether to deploy the verticle with HA.
   * @param doneHandler A handler to be called once the verticle has been deployed.
   *        The handler will be called with the unique deployment ID if successful.
   * @return The group.
   */
  Group deployVerticle(String main, JsonObject config, int instances, boolean ha, Handler<AsyncResult<String>> doneHandler);

  /**
   * Deploys a worker verticle to the group.<p>
   *
   * The verticle must be available on whichever node receives the deploy message.
   * If the verticle is not available on the classpath, a {@link ClusterException}
   * will occur. You can deploy a verticle to a specific node by using the node
   * selection API, or by wrapping a verticle in a module Vertigo can upload and
   * deploy the module to a specific node.
   *
   * @param main The verticle main.
   * @return The group.
   */
  Group deployWorkerVerticle(String main);

  /**
   * Deploys a worker verticle to the group.<p>
   *
   * The verticle must be available on whichever node receives the deploy message.
   * If the verticle is not available on the classpath, a {@link ClusterException}
   * will occur. You can deploy a verticle to a specific node by using the node
   * selection API, or by wrapping a verticle in a module Vertigo can upload and
   * deploy the module to a specific node.
   *
   * @param main The verticle main.
   * @param config The verticle configuration.
   * @return The group.
   */
  Group deployWorkerVerticle(String main, JsonObject config);

  /**
   * Deploys a worker verticle to the group.<p>
   *
   * The verticle must be available on whichever node receives the deploy message.
   * If the verticle is not available on the classpath, a {@link ClusterException}
   * will occur. You can deploy a verticle to a specific node by using the node
   * selection API, or by wrapping a verticle in a module Vertigo can upload and
   * deploy the module to a specific node.
   *
   * @param main The verticle main.
   * @param instances The number of instances to deploy.
   * @return The group.
   */
  Group deployWorkerVerticle(String main, int instances);

  /**
   * Deploys a worker verticle to the group.<p>
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
   * @return The group.
   */
  Group deployWorkerVerticle(String main, JsonObject config, int instances, boolean multiThreaded);

  /**
   * Deploys a worker verticle to the group.<p>
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
   * @param ha Indicates whether to deploy the verticle with HA.
   * @return The group.
   */
  Group deployWorkerVerticle(String main, JsonObject config, int instances, boolean multiThreaded, boolean ha);

  /**
   * Deploys a worker verticle to the group.<p>
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
   * @return The group.
   */
  Group deployWorkerVerticle(String main, Handler<AsyncResult<String>> doneHandler);

  /**
   * Deploys a worker verticle to the group.<p>
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
   * @return The group.
   */
  Group deployWorkerVerticle(String main, JsonObject config, Handler<AsyncResult<String>> doneHandler);

  /**
   * Deploys a worker verticle to the group.<p>
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
   * @return The group.
   */
  Group deployWorkerVerticle(String main, int instances, Handler<AsyncResult<String>> doneHandler);

  /**
   * Deploys a worker verticle to the group.<p>
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
   * @return The group.
   */
  Group deployWorkerVerticle(String main, JsonObject config, int instances, boolean multiThreaded, Handler<AsyncResult<String>> doneHandler);

  /**
   * Deploys a worker verticle to the group.<p>
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
   * @param ha Indicates whether to deploy the verticle with HA.
   * @param doneHandler A handler to be called once the verticle has been deployed.
   *        The handler will be called with the unique deployment ID if successful.
   * @return The group.
   */
  Group deployWorkerVerticle(String main, JsonObject config, int instances, boolean multiThreaded, boolean ha, Handler<AsyncResult<String>> doneHandler);

  /**
   * Undeploys a module from the group.<p>
   *
   * Given the unique module deployment ID, Vertigo will ensure that the deployment
   * is undeployed from the appropriate node in the group.
   *
   * @param deploymentID The deployment ID of the module to undeploy.
   * @return The group.
   */
  Group undeployModule(String deploymentID);

  /**
   * Undeploys a module from the group.<p>
   *
   * Given the unique module deployment ID, Vertigo will ensure that the deployment
   * is undeployed from the appropriate node in the group.
   *
   * @param deploymentID The deployment ID of the module to undeploy.
   * @param doneHandler A handler to be called once complete.
   * @return The group.
   */
  Group undeployModule(String deploymentID, Handler<AsyncResult<Void>> doneHandler);

  /**
   * Undeploys a verticle from the group.<p>
   *
   * Given the unique verticle deployment ID, Vertigo will ensure that the deployment
   * is undeployed from the appropriate node in the group.
   *
   * @param deploymentID The deployment ID of the verticle to undeploy.
   * @return The group.
   */
  Group undeployVerticle(String deploymentID);

  /**
   * Undeploys a verticle from the group.<p>
   *
   * Given the unique verticle deployment ID, Vertigo will ensure that the deployment
   * is undeployed from the appropriate node in the group.
   *
   * @param deploymentID The deployment ID of the verticle to undeploy.
   * @param doneHandler A handler to be called once complete.
   * @return The group.
   */
  Group undeployVerticle(String deploymentID, Handler<AsyncResult<Void>> doneHandler);

}
