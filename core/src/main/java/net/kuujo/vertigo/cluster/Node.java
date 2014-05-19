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

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonObject;

/**
 * Client for communicating with a single Vertigo cluster node.<p>
 *
 * This client communicates directly with a cluster node over the event bus.
 * Users should get a <code>Node</code> instance by calling
 * {@link Cluster#getNode(String, Handler)} rather than by instantiating a
 * node directly. This will ensure that the node's event bus address is
 * properly namespaced according to the cluster and group to which it belongs.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public interface Node {

  /**
   * Returns the node address.
   *
   * @return The node address.
   */
  String address();

  /**
   * Pings the node.
   *
   * @param resultHandler A handler to be called once the node responds.
   * @return The node.
   */
  Node ping(Handler<AsyncResult<Node>> resultHandler);

  /**
   * Installs a local module to the node.<p>
   *
   * The module being installed must be available on the local classpath in
   * the normal Vert.x <code>mods</code> directory. If the module is already
   * installed on a given node then the node will be skipped, otherwise the
   * local module will be zipped, uploaded, and installed to the node.
   *
   * @param moduleName The name of the module to install.
   * @return The node.
   */
  Node installModule(String moduleName);

  /**
   * Installs a local module to the node.<p>
   *
   * The module being installed must be available on the local classpath in
   * the normal Vert.x <code>mods</code> directory. If the module is already
   * installed on a given node then the node will be skipped, otherwise the
   * local module will be zipped, uploaded, and installed to the node.
   *
   * @param moduleName The name of the module to install.
   * @param doneHandler A handler to be called once the module has been installed.
   * @return The node.
   */
  Node installModule(String moduleName, Handler<AsyncResult<Void>> doneHandler);

  /**
   * Uninstalls a module from the node.<p>
   *
   * When the module is uninstalled, it will be <em>removed from the classpath</em>
   * of the remote node. This means you should not uninstall modules that were
   * originally installed on any given node.
   *
   * @param moduleName The name of the module to uninstall.
   * @return The node.
   */
  Node uninstallModule(String moduleName);

  /**
   * Uninstalls a module from the node.<p>
   *
   * When the module is uninstalled, it will be <em>removed from the classpath</em>
   * of the remote node. This means you should not uninstall modules that were
   * originally installed on any given node.
   *
   * @param moduleName The name of the module to uninstall.
   * @param doneHandler A handler to be called once the module has been uninstalled.
   * @return The node.
   */
  Node uninstallModule(String moduleName, Handler<AsyncResult<Void>> doneHandler);

  /**
   * Deploys a module to the node.<p>
   *
   * The module must be available on the node. You can install the module to
   * the node using the {@link Node#installModule(String, Handler)} method.
   *
   * @param moduleName The name of the module to deploy.
   * @return The node.
   */
  Node deployModule(String moduleName);

  /**
   * Deploys a module to the node.<p>
   *
   * The module must be available on the node. You can install the module to
   * the node using the {@link Node#installModule(String, Handler)} method.
   *
   * @param moduleName The name of the module to deploy.
   * @param config The module configuration.
   * @return The node.
   */
  Node deployModule(String moduleName, JsonObject config);

  /**
   * Deploys a module to the node.<p>
   *
   * The module must be available on the node. You can install the module to
   * the node using the {@link Node#installModule(String, Handler)} method.
   *
   * @param moduleName The name of the module to deploy.
   * @param instances The number of instances to deploy.
   * @return The node.
   */
  Node deployModule(String moduleName, int instances);

  /**
   * Deploys a module to the node.<p>
   *
   * The module must be available on the node. You can install the module to
   * the node using the {@link Node#installModule(String, Handler)} method.
   *
   * @param moduleName The name of the module to deploy.
   * @param config The module configuration.
   * @param instances The number of instances to deploy.
   * @return The node.
   */
  Node deployModule(String moduleName, JsonObject config, int instances);

  /**
   * Deploys a module to the node.<p>
   *
   * The module must be available on the node. You can install the module to
   * the node using the {@link Node#installModule(String, Handler)} method.
   *
   * @param moduleName The name of the module to deploy.
   * @param doneHandler A handler to be called once the module has been deployed.
   *        The handler will be called with the unique deployment ID if successful.
   * @return The node.
   */
  Node deployModule(String moduleName, Handler<AsyncResult<String>> doneHandler);

  /**
   * Deploys a module to the node.<p>
   *
   * The module must be available on the node. You can install the module to
   * the node using the {@link Node#installModule(String, Handler)} method.
   *
   * @param moduleName The name of the module to deploy.
   * @param config The module configuration.
   * @param doneHandler A handler to be called once the module has been deployed.
   *        The handler will be called with the unique deployment ID if successful.
   * @return The node.
   */
  Node deployModule(String moduleName, JsonObject config, Handler<AsyncResult<String>> doneHandler);

  /**
   * Deploys a module to the node.<p>
   *
   * The module must be available on the node. You can install the module to
   * the node using the {@link Node#installModule(String, Handler)} method.
   *
   * @param moduleName The name of the module to deploy.
   * @param instances The number of instances to deploy.
   * @param doneHandler A handler to be called once the module has been deployed.
   *        The handler will be called with the unique deployment ID if successful.
   * @return The node.
   */
  Node deployModule(String moduleName, int instances, Handler<AsyncResult<String>> doneHandler);

  /**
   * Deploys a module to the node.<p>
   *
   * The module must be available on the node. You can install the module to
   * the node using the {@link Node#installModule(String, Handler)} method.
   *
   * @param moduleName The name of the module to deploy.
   * @param config The module configuration.
   * @param instances The number of instances to deploy.
   * @param doneHandler A handler to be called once the module has been deployed.
   *        The handler will be called with the unique deployment ID if successful.
   * @return The node.
   */
  Node deployModule(String moduleName, JsonObject config, int instances, Handler<AsyncResult<String>> doneHandler);

  /**
   * Deploys a verticle to the node.<p>
   *
   * The verticle must be available on the classpath of the node.
   *
   * @param main The verticle main.
   * @return The node.
   */
  Node deployVerticle(String main);

  /**
   * Deploys a verticle to the node.<p>
   *
   * The verticle must be available on the classpath of the node.
   *
   * @param main The verticle main.
   * @param instances The number of instances to deploy.
   * @return The node.
   */
  Node deployVerticle(String main, JsonObject config);

  /**
   * Deploys a verticle to the node.<p>
   *
   * The verticle must be available on the classpath of the node.
   *
   * @param main The verticle main.
   * @param config The verticle configuration.
   * @return The node.
   */
  Node deployVerticle(String main, int instances);

  /**
   * Deploys a verticle to the node.<p>
   *
   * The verticle must be available on the classpath of the node.
   *
   * @param main The verticle main.
   * @param config The verticle configuration.
   * @param instances The number of instances to deploy.
   * @return The node.
   */
  Node deployVerticle(String main, JsonObject config, int instances);

  /**
   * Deploys a verticle to the node.<p>
   *
   * The verticle must be available on the classpath of the node.
   *
   * @param main The verticle main.
   * @param doneHandler A handler to be called once the verticle has been deployed.
   *        The handler will be called with the unique deployment ID if successful.
   * @return The node.
   */
  Node deployVerticle(String main, Handler<AsyncResult<String>> doneHandler);

  /**
   * Deploys a verticle to the node.<p>
   *
   * The verticle must be available on the classpath of the node.
   *
   * @param main The verticle main.
   * @param config The verticle configuration.
   * @param doneHandler A handler to be called once the verticle has been deployed.
   *        The handler will be called with the unique deployment ID if successful.
   * @return The node.
   */
  Node deployVerticle(String main, JsonObject config, Handler<AsyncResult<String>> doneHandler);

  /**
   * Deploys a verticle to the node.<p>
   *
   * The verticle must be available on the classpath of the node.
   *
   * @param main The verticle main.
   * @param instances The number of instances to deploy.
   * @param doneHandler A handler to be called once the verticle has been deployed.
   *        The handler will be called with the unique deployment ID if successful.
   * @return The node.
   */
  Node deployVerticle(String main, int instances, Handler<AsyncResult<String>> doneHandler);

  /**
   * Deploys a verticle to the node.<p>
   *
   * The verticle must be available on the classpath of the node.
   *
   * @param main The verticle main.
   * @param config The verticle configuration.
   * @param instances The number of instances to deploy.
   * @param doneHandler A handler to be called once the verticle has been deployed.
   *        The handler will be called with the unique deployment ID if successful.
   * @return The node.
   */
  Node deployVerticle(String main, JsonObject config, int instances, Handler<AsyncResult<String>> doneHandler);

  /**
   * Deploys a worker verticle to the node.<p>
   *
   * The verticle must be available on the classpath of the node.
   *
   * @param main The verticle main.
   * @return The node.
   */
  Node deployWorkerVerticle(String main);

  /**
   * Deploys a worker verticle to the node.<p>
   *
   * The verticle must be available on the classpath of the node.
   *
   * @param main The verticle main.
   * @param config The verticle configuration.
   * @return The node.
   */
  Node deployWorkerVerticle(String main, JsonObject config);

  /**
   * Deploys a worker verticle to the node.<p>
   *
   * The verticle must be available on the classpath of the node.
   *
   * @param main The verticle main.
   * @param instances The number of instances to deploy.
   * @return The node.
   */
  Node deployWorkerVerticle(String main, int instances);

  /**
   * Deploys a worker verticle to the node.<p>
   *
   * The verticle must be available on the classpath of the node.
   *
   * @param main The verticle main.
   * @param config The verticle configuration.
   * @param instances The number of instances to deploy.
   * @param multiThreaded Indicates whether the worker should be multi-threaded.
   * @return The node.
   */
  Node deployWorkerVerticle(String main, JsonObject config, int instances, boolean multiThreaded);

  /**
   * Deploys a worker verticle to the node.<p>
   *
   * The verticle must be available on the classpath of the node.
   *
   * @param main The verticle main.
   * @param doneHandler A handler to be called once the verticle has been deployed.
   *        The handler will be called with the unique deployment ID if successful.
   * @return The node.
   */
  Node deployWorkerVerticle(String main, Handler<AsyncResult<String>> doneHandler);

  /**
   * Deploys a worker verticle to the node.<p>
   *
   * The verticle must be available on the classpath of the node.
   *
   * @param main The verticle main.
   * @param config The verticle configuration.
   * @param doneHandler A handler to be called once the verticle has been deployed.
   *        The handler will be called with the unique deployment ID if successful.
   * @return The node.
   */
  Node deployWorkerVerticle(String main, JsonObject config, Handler<AsyncResult<String>> doneHandler);

  /**
   * Deploys a worker verticle to the node.<p>
   *
   * The verticle must be available on the classpath of the node.
   *
   * @param main The verticle main.
   * @param instances The number of instances to deploy.
   * @param doneHandler A handler to be called once the verticle has been deployed.
   *        The handler will be called with the unique deployment ID if successful.
   * @return The node.
   */
  Node deployWorkerVerticle(String main, int instances, Handler<AsyncResult<String>> doneHandler);

  /**
   * Deploys a worker verticle to the node.<p>
   *
   * The verticle must be available on the classpath of the node.
   *
   * @param main The verticle main.
   * @param config The verticle configuration.
   * @param instances The number of instances to deploy.
   * @param multiThreaded Indicates whether the worker should be multi-threaded.
   * @param doneHandler A handler to be called once the verticle has been deployed.
   *        The handler will be called with the unique deployment ID if successful.
   * @return The node.
   */
  Node deployWorkerVerticle(String main, JsonObject config, int instances, boolean multiThreaded, Handler<AsyncResult<String>> doneHandler);

  /**
   * Undeploys a module from the node.<p>
   *
   * The deployment ID must be a deployment that was deployed by this node.
   * If the given deployment is not a deployment on the node a {@link ClusterException}
   * will occur.
   *
   * @param deploymentID The deployment ID of the module to undeploy.
   * @return The node.
   */
  Node undeployModule(String deploymentID);

  /**
   * Undeploys a module from the node.<p>
   *
   * The deployment ID must be a deployment that was deployed by this node.
   * If the given deployment is not a deployment on the node a {@link ClusterException}
   * will occur.
   *
   * @param deploymentID The deployment ID of the module to undeploy.
   * @param doneHandler A handler to be called once complete.
   * @return The node.
   */
  Node undeployModule(String deploymentID, Handler<AsyncResult<Void>> doneHandler);

  /**
   * Undeploys a verticle from the node.<p>
   *
   * The deployment ID must be a deployment that was deployed by this node.
   * If the given deployment is not a deployment on the node a {@link ClusterException}
   * will occur.
   *
   * @param deploymentID The deployment ID of the verticle to undeploy.
   * @return The node.
   */
  Node undeployVerticle(String deploymentID);

  /**
   * Undeploys a verticle from the node.<p>
   *
   * The deployment ID must be a deployment that was deployed by this node.
   * If the given deployment is not a deployment on the node a {@link ClusterException}
   * will occur.
   *
   * @param deploymentID The deployment ID of the verticle to undeploy.
   * @param doneHandler A handler to be called once complete.
   * @return The node.
   */
  Node undeployVerticle(String deploymentID, Handler<AsyncResult<Void>> doneHandler);

}
