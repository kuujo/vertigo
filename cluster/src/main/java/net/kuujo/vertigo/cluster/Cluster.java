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

import net.kuujo.vertigo.cluster.data.AsyncCounter;
import net.kuujo.vertigo.cluster.data.AsyncList;
import net.kuujo.vertigo.cluster.data.AsyncMap;
import net.kuujo.vertigo.cluster.data.AsyncQueue;
import net.kuujo.vertigo.cluster.data.AsyncSet;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Container;

/**
 * The cluster is a low level API for deployments and shared data access.<p>
 *
 * In opposition to core Vert.x {@link Container} behavior, Vertigo cluster deployments
 * support absolute deployment identifiers. This allows deployments to be referenced
 * through faults. Additionally, clusters must follow the Vert.x HA grouping method,
 * allowing modules and verticles to be deployed remotely to HA groups.<p>
 *
 * Vertigo clusters also expose asynchronous data structures for synchronization. See
 * cluster implementations for specifics regarding how data structures are implemented.
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
   * Checks whether a module or verticle is deployed in the cluster.
   *
   * @param deploymentID The deployment ID to check.
   * @param resultHandler An asynchronous handler to be called with a result.
   * @return The cluster client.
   */
  Cluster isDeployed(String deploymentID, Handler<AsyncResult<Boolean>> resultHandler);

  /**
   * Deploys a module to the cluster.
   *
   * @param deploymentID The unique module deployment ID.
   * @param moduleName The module name.
   * @return The cluster client.
   */
  Cluster deployModule(String deploymentID, String moduleName);

  /**
   * Deploys a module to the cluster.
   *
   * @param deploymentID The unique module deployment ID.
   * @param moduleName The module name.
   * @param config The module configuration.
   * @return The cluster client.
   */
  Cluster deployModule(String deploymentID, String moduleName, JsonObject config);

  /**
   * Deploys a module to the cluster.
   *
   * @param deploymentID The unique module deployment ID.
   * @param moduleName The module name.
   * @param instances The number of instances to deploy.
   * @return The cluster client.
   */
  Cluster deployModule(String deploymentID, String moduleName, int instances);

  /**
   * Deploys a module to the cluster.
   *
   * @param deploymentID The unique module deployment ID.
   * @param moduleName The module name.
   * @param config The module configuration.
   * @param instances The number of instances to deploy.
   * @return The cluster client.
   */
  Cluster deployModule(String deploymentID, String moduleName, JsonObject config, int instances);

  /**
   * Deploys a module to the cluster.
   *
   * @param deploymentID The unique module deployment ID.
   * @param moduleName The module name.
   * @param doneHandler An asynchronous handler to be called once deployment is complete.
   * @return The cluster client.
   */
  Cluster deployModule(String deploymentID, String moduleName, Handler<AsyncResult<String>> doneHandler);

  /**
   * Deploys a module to the cluster.
   *
   * @param deploymentID The unique module deployment ID.
   * @param moduleName The module name.
   * @param config The module configuration.
   * @param doneHandler An asynchronous handler to be called once deployment is complete.
   * @return The cluster client.
   */
  Cluster deployModule(String deploymentID, String moduleName, JsonObject config, Handler<AsyncResult<String>> doneHandler);

  /**
   * Deploys a module to the cluster.
   *
   * @param deploymentID The unique module deployment ID.
   * @param moduleName The module name.
   * @param instances The number of instances to deploy.
   * @param doneHandler An asynchronous handler to be called once deployment is complete.
   * @return The cluster client.
   */
  Cluster deployModule(String deploymentID, String moduleName, int instances, Handler<AsyncResult<String>> doneHandler);

  /**
   * Deploys a module to the cluster.
   *
   * @param deploymentID The unique module deployment ID.
   * @param moduleName The module name.
   * @param config The module configuration.
   * @param instances The number of instances to deploy.
   * @param doneHandler An asynchronous handler to be called once deployment is complete.
   * @return The cluster client.
   */
  Cluster deployModule(String deploymentID, String moduleName, JsonObject config, int instances, Handler<AsyncResult<String>> doneHandler);

  /**
   * Deploys a module to the cluster.
   *
   * @param deploymentID The unique module deployment ID.
   * @param moduleName The module name.
   * @param config The module configuration.
   * @param instances The number of instances to deploy.
   * @param ha Indicates whether to deploy the module with HA.
   * @param doneHandler An asynchronous handler to be called once deployment is complete.
   * @return The cluster client.
   */
  Cluster deployModule(String deploymentID, String moduleName, JsonObject config, int instances, boolean ha, Handler<AsyncResult<String>> doneHandler);

  /**
   * Deploys a module to a specific HA group in the cluster.
   *
   * @param deploymentID The unique module deployment ID.
   * @param groupID The group to which to deploy the module.
   * @param moduleName The module name.
   * @return The cluster client.
   */
  Cluster deployModuleTo(String deploymentID, String groupID, String moduleName);

  /**
   * Deploys a module to a specific HA group in the cluster.
   *
   * @param deploymentID The unique module deployment ID.
   * @param groupID The group to which to deploy the module.
   * @param moduleName The module name.
   * @param config The module configuration.
   * @return The cluster client.
   */
  Cluster deployModuleTo(String deploymentID, String groupID, String moduleName, JsonObject config);

  /**
   * Deploys a module to a specific HA group in the cluster.
   *
   * @param deploymentID The unique module deployment ID.
   * @param groupID The group to which to deploy the module.
   * @param moduleName The module name.
   * @param instances The number of instances to deploy.
   * @return The cluster client.
   */
  Cluster deployModuleTo(String deploymentID, String groupID, String moduleName, int instances);

  /**
   * Deploys a module to a specific HA group in the cluster.
   *
   * @param deploymentID The unique module deployment ID.
   * @param groupID The group to which to deploy the module.
   * @param moduleName The module name.
   * @param config The module configuration.
   * @param instances The number of instances to deploy.
   * @return The cluster client.
   */
  Cluster deployModuleTo(String deploymentID, String groupID, String moduleName, JsonObject config, int instances);

  /**
   * Deploys a module to a specific HA group in the cluster.
   *
   * @param deploymentID The unique module deployment ID.
   * @param groupID The group to which to deploy the module.
   * @param moduleName The module name.
   * @param doneHandler An asynchronous handler to be called once deployment is complete.
   * @return The cluster client.
   */
  Cluster deployModuleTo(String deploymentID, String groupID, String moduleName, Handler<AsyncResult<String>> doneHandler);

  /**
   * Deploys a module to a specific HA group in the cluster.
   *
   * @param deploymentID The unique module deployment ID.
   * @param groupID The group to which to deploy the module.
   * @param moduleName The module name.
   * @param config The module configuration.
   * @param doneHandler An asynchronous handler to be called once deployment is complete.
   * @return The cluster client.
   */
  Cluster deployModuleTo(String deploymentID, String groupID, String moduleName, JsonObject config, Handler<AsyncResult<String>> doneHandler);

  /**
   * Deploys a module to a specific HA group in the cluster.
   *
   * @param deploymentID The unique module deployment ID.
   * @param groupID The group to which to deploy the module.
   * @param moduleName The module name.
   * @param instances The number of instances to deploy.
   * @param doneHandler An asynchronous handler to be called once deployment is complete.
   * @return The cluster client.
   */
  Cluster deployModuleTo(String deploymentID, String groupID, String moduleName, int instances, Handler<AsyncResult<String>> doneHandler);

  /**
   * Deploys a module to a specific HA group in the cluster.
   *
   * @param deploymentID The unique module deployment ID.
   * @param groupID The group to which to deploy the module.
   * @param moduleName The module name.
   * @param config The module configuration.
   * @param instances The number of instances to deploy.
   * @param doneHandler An asynchronous handler to be called once deployment is complete.
   * @return The cluster client.
   */
  Cluster deployModuleTo(String deploymentID, String groupID, String moduleName, JsonObject config, int instances, Handler<AsyncResult<String>> doneHandler);

  /**
   * Deploys a module to a specific HA group in the cluster.
   *
   * @param deploymentID The unique module deployment ID.
   * @param groupID The group to which to deploy the module.
   * @param moduleName The module name.
   * @param config The module configuration.
   * @param instances The number of instances to deploy.
   * @param ha Indicates whether to deploy the module with HA.
   * @param doneHandler An asynchronous handler to be called once deployment is complete.
   * @return The cluster client.
   */
  Cluster deployModuleTo(String deploymentID, String groupID, String moduleName, JsonObject config, int instances, boolean ha, Handler<AsyncResult<String>> doneHandler);

  /**
   * Deploys a verticle to the cluster.
   *
   * @param deploymentID The unique verticle deployment ID.
   * @param main The verticle main.
   * @return The cluster client.
   */
  Cluster deployVerticle(String deploymentID, String main);

  /**
   * Deploys a verticle to the cluster.
   *
   * @param deploymentID The unique verticle deployment ID.
   * @param main The verticle main.
   * @param config The module configuration.
   * @return The cluster client.
   */
  Cluster deployVerticle(String deploymentID, String main, JsonObject config);

  /**
   * Deploys a verticle to the cluster.
   *
   * @param deploymentID The unique verticle deployment ID.
   * @param main The verticle main.
   * @param instances The number of instances to deploy.
   * @return The cluster client.
   */
  Cluster deployVerticle(String deploymentID, String main, int instances);

  /**
   * Deploys a verticle to the cluster.
   *
   * @param deploymentID The unique verticle deployment ID.
   * @param main The verticle main.
   * @param config The module configuration.
   * @param instances The number of instances to deploy.
   * @return The cluster client.
   */
  Cluster deployVerticle(String deploymentID, String main, JsonObject config, int instances);

  /**
   * Deploys a verticle to the cluster.
   *
   * @param deploymentID The unique verticle deployment ID.
   * @param main The verticle main.
   * @param doneHandler An asynchronous handler to be called once deployment is complete.
   * @return The cluster client.
   */
  Cluster deployVerticle(String deploymentID, String main, Handler<AsyncResult<String>> doneHandler);

  /**
   * Deploys a verticle to the cluster.
   *
   * @param deploymentID The unique verticle deployment ID.
   * @param main The verticle main.
   * @param config The module configuration.
   * @param doneHandler An asynchronous handler to be called once deployment is complete.
   * @return The cluster client.
   */
  Cluster deployVerticle(String deploymentID, String main, JsonObject config, Handler<AsyncResult<String>> doneHandler);

  /**
   * Deploys a verticle to the cluster.
   *
   * @param deploymentID The unique verticle deployment ID.
   * @param main The verticle main.
   * @param instances The number of instances to deploy.
   * @param doneHandler An asynchronous handler to be called once deployment is complete.
   * @return The cluster client.
   */
  Cluster deployVerticle(String deploymentID, String main, int instances, Handler<AsyncResult<String>> doneHandler);

  /**
   * Deploys a verticle to the cluster.
   *
   * @param deploymentID The unique verticle deployment ID.
   * @param main The verticle main.
   * @param config The module configuration.
   * @param instances The number of instances to deploy.
   * @param doneHandler An asynchronous handler to be called once deployment is complete.
   * @return The cluster client.
   */
  Cluster deployVerticle(String deploymentID, String main, JsonObject config, int instances, Handler<AsyncResult<String>> doneHandler);

  /**
   * Deploys a verticle to the cluster.
   *
   * @param deploymentID The unique verticle deployment ID.
   * @param main The verticle main.
   * @param config The module configuration.
   * @param instances The number of instances to deploy.
   * @param ha Indicates whether to deploy the verticle with HA.
   * @param doneHandler An asynchronous handler to be called once deployment is complete.
   * @return The cluster client.
   */
  Cluster deployVerticle(String deploymentID, String main, JsonObject config, int instances, boolean ha, Handler<AsyncResult<String>> doneHandler);

  /**
   * Deploys a verticle to the cluster.
   *
   * @param deploymentID The unique verticle deployment ID.
   * @param groupID The group to which to deploy the verticle.
   * @param main The verticle main.
   * @return The cluster client.
   */
  Cluster deployVerticleTo(String deploymentID, String groupID, String main);

  /**
   * Deploys a verticle to the cluster.
   *
   * @param deploymentID The unique verticle deployment ID.
   * @param groupID The group to which to deploy the verticle.
   * @param main The verticle main.
   * @param config The module configuration.
   * @return The cluster client.
   */
  Cluster deployVerticleTo(String deploymentID, String groupID, String main, JsonObject config);

  /**
   * Deploys a verticle to the cluster.
   *
   * @param deploymentID The unique verticle deployment ID.
   * @param groupID The group to which to deploy the verticle.
   * @param main The verticle main.
   * @param instances The number of instances to deploy.
   * @return The cluster client.
   */
  Cluster deployVerticleTo(String deploymentID, String groupID, String main, int instances);

  /**
   * Deploys a verticle to the cluster.
   *
   * @param deploymentID The unique verticle deployment ID.
   * @param groupID The group to which to deploy the verticle.
   * @param main The verticle main.
   * @param config The module configuration.
   * @param instances The number of instances to deploy.
   * @return The cluster client.
   */
  Cluster deployVerticleTo(String deploymentID, String groupID, String main, JsonObject config, int instances);

  /**
   * Deploys a verticle to the cluster.
   *
   * @param deploymentID The unique verticle deployment ID.
   * @param groupID The group to which to deploy the verticle.
   * @param main The verticle main.
   * @param doneHandler An asynchronous handler to be called once deployment is complete.
   * @return The cluster client.
   */
  Cluster deployVerticleTo(String deploymentID, String groupID, String main, Handler<AsyncResult<String>> doneHandler);

  /**
   * Deploys a verticle to the cluster.
   *
   * @param deploymentID The unique verticle deployment ID.
   * @param groupID The group to which to deploy the verticle.
   * @param main The verticle main.
   * @param config The module configuration.
   * @param doneHandler An asynchronous handler to be called once deployment is complete.
   * @return The cluster client.
   */
  Cluster deployVerticleTo(String deploymentID, String groupID, String main, JsonObject config, Handler<AsyncResult<String>> doneHandler);

  /**
   * Deploys a verticle to the cluster.
   *
   * @param deploymentID The unique verticle deployment ID.
   * @param groupID The group to which to deploy the verticle.
   * @param main The verticle main.
   * @param instances The number of instances to deploy.
   * @param doneHandler An asynchronous handler to be called once deployment is complete.
   * @return The cluster client.
   */
  Cluster deployVerticleTo(String deploymentID, String groupID, String main, int instances, Handler<AsyncResult<String>> doneHandler);

  /**
   * Deploys a verticle to the cluster.
   *
   * @param deploymentID The unique verticle deployment ID.
   * @param groupID The group to which to deploy the verticle.
   * @param main The verticle main.
   * @param config The module configuration.
   * @param instances The number of instances to deploy.
   * @param doneHandler An asynchronous handler to be called once deployment is complete.
   * @return The cluster client.
   */
  Cluster deployVerticleTo(String deploymentID, String groupID, String main, JsonObject config, int instances, Handler<AsyncResult<String>> doneHandler);

  /**
   * Deploys a verticle to the cluster.
   *
   * @param deploymentID The unique verticle deployment ID.
   * @param groupID The group to which to deploy the verticle.
   * @param main The verticle main.
   * @param config The module configuration.
   * @param instances The number of instances to deploy.
   * @param ha Indicates whether to deploy the verticle with HA.
   * @param doneHandler An asynchronous handler to be called once deployment is complete.
   * @return The cluster client.
   */
  Cluster deployVerticleTo(String deploymentID, String groupID, String main, JsonObject config, int instances, boolean ha, Handler<AsyncResult<String>> doneHandler);

  /**
   * Deploys a worker verticle to the cluster.
   *
   * @param deploymentID The unique verticle deployment ID.
   * @param main The verticle main.
   * @return The cluster client.
   */
  Cluster deployWorkerVerticle(String deploymentID, String main);

  /**
   * Deploys a worker verticle to the cluster.
   *
   * @param deploymentID The unique verticle deployment ID.
   * @param main The verticle main.
   * @param config The module configuration.
   * @return The cluster client.
   */
  Cluster deployWorkerVerticle(String deploymentID, String main, JsonObject config);

  /**
   * Deploys a worker verticle to the cluster.
   *
   * @param deploymentID The unique verticle deployment ID.
   * @param main The verticle main.
   * @param instances The number of instances to deploy.
   * @return The cluster client.
   */
  Cluster deployWorkerVerticle(String deploymentID, String main, int instances);

  /**
   * Deploys a worker verticle to the cluster.
   *
   * @param deploymentID The unique verticle deployment ID.
   * @param main The verticle main.
   * @param config The module configuration.
   * @param instances The number of instances to deploy.
   * @param multiThreaded Indicates whether the verticle is multi-threaded.
   * @return The cluster client.
   */
  Cluster deployWorkerVerticle(String deploymentID, String main, JsonObject config, int instances, boolean multiThreaded);

  /**
   * Deploys a worker verticle to the cluster.
   *
   * @param deploymentID The unique verticle deployment ID.
   * @param main The verticle main.
   * @param doneHandler An asynchronous handler to be called once deployment is complete.
   * @return The cluster client.
   */
  Cluster deployWorkerVerticle(String deploymentID, String main, Handler<AsyncResult<String>> doneHandler);

  /**
   * Deploys a worker verticle to the cluster.
   *
   * @param deploymentID The unique verticle deployment ID.
   * @param main The verticle main.
   * @param config The module configuration.
   * @param doneHandler An asynchronous handler to be called once deployment is complete.
   * @return The cluster client.
   */
  Cluster deployWorkerVerticle(String deploymentID, String main, JsonObject config, Handler<AsyncResult<String>> doneHandler);

  /**
   * Deploys a worker verticle to the cluster.
   *
   * @param deploymentID The unique verticle deployment ID.
   * @param main The verticle main.
   * @param instances The number of instances to deploy.
   * @param doneHandler An asynchronous handler to be called once deployment is complete.
   * @return The cluster client.
   */
  Cluster deployWorkerVerticle(String deploymentID, String main, int instances, Handler<AsyncResult<String>> doneHandler);

  /**
   * Deploys a worker verticle to the cluster.
   *
   * @param deploymentID The unique verticle deployment ID.
   * @param main The verticle main.
   * @param config The module configuration.
   * @param instances The number of instances to deploy.
   * @param multiThreaded Indicates whether the verticle is multi-threaded.
   * @param doneHandler An asynchronous handler to be called once deployment is complete.
   * @return The cluster client.
   */
  Cluster deployWorkerVerticle(String deploymentID, String main, JsonObject config, int instances, boolean multiThreaded, Handler<AsyncResult<String>> doneHandler);

  /**
   * Deploys a worker verticle to the cluster.
   *
   * @param deploymentID The unique verticle deployment ID.
   * @param main The verticle main.
   * @param config The module configuration.
   * @param instances The number of instances to deploy.
   * @param multiThreaded Indicates whether the verticle is multi-threaded.
   * @param ha Indicates whether to deploy the verticle with HA.
   * @param doneHandler An asynchronous handler to be called once deployment is complete.
   * @return The cluster client.
   */
  Cluster deployWorkerVerticle(String deploymentID, String main, JsonObject config, int instances, boolean multiThreaded, boolean ha, Handler<AsyncResult<String>> doneHandler);

  /**
   * Deploys a worker verticle to the cluster.
   *
   * @param deploymentID The unique verticle deployment ID.
   * @param groupID The group to which to deploy the verticle.
   * @param main The verticle main.
   * @return The cluster client.
   */
  Cluster deployWorkerVerticleTo(String deploymentID, String groupID, String main);

  /**
   * Deploys a worker verticle to the cluster.
   *
   * @param deploymentID The unique verticle deployment ID.
   * @param groupID The group to which to deploy the verticle.
   * @param main The verticle main.
   * @param config The module configuration.
   * @return The cluster client.
   */
  Cluster deployWorkerVerticleTo(String deploymentID, String groupID, String main, JsonObject config);

  /**
   * Deploys a worker verticle to the cluster.
   *
   * @param deploymentID The unique verticle deployment ID.
   * @param groupID The group to which to deploy the verticle.
   * @param main The verticle main.
   * @param instances The number of instances to deploy.
   * @return The cluster client.
   */
  Cluster deployWorkerVerticleTo(String deploymentID, String groupID, String main, int instances);

  /**
   * Deploys a worker verticle to the cluster.
   *
   * @param deploymentID The unique verticle deployment ID.
   * @param groupID The group to which to deploy the verticle.
   * @param main The verticle main.
   * @param config The module configuration.
   * @param instances The number of instances to deploy.
   * @param multiThreaded Indicates whether the verticle is multi-threaded.
   * @return The cluster client.
   */
  Cluster deployWorkerVerticleTo(String deploymentID, String groupID, String main, JsonObject config, int instances, boolean multiThreaded);

  /**
   * Deploys a worker verticle to the cluster.
   *
   * @param deploymentID The unique verticle deployment ID.
   * @param groupID The group to which to deploy the verticle.
   * @param main The verticle main.
   * @param doneHandler An asynchronous handler to be called once deployment is complete.
   * @return The cluster client.
   */
  Cluster deployWorkerVerticleTo(String deploymentID, String groupID, String main, Handler<AsyncResult<String>> doneHandler);

  /**
   * Deploys a worker verticle to the cluster.
   *
   * @param deploymentID The unique verticle deployment ID.
   * @param groupID The group to which to deploy the verticle.
   * @param main The verticle main.
   * @param config The module configuration.
   * @param doneHandler An asynchronous handler to be called once deployment is complete.
   * @return The cluster client.
   */
  Cluster deployWorkerVerticleTo(String deploymentID, String groupID, String main, JsonObject config, Handler<AsyncResult<String>> doneHandler);

  /**
   * Deploys a worker verticle to the cluster.
   *
   * @param deploymentID The unique verticle deployment ID.
   * @param groupID The group to which to deploy the verticle.
   * @param main The verticle main.
   * @param instances The number of instances to deploy.
   * @param doneHandler An asynchronous handler to be called once deployment is complete.
   * @return The cluster client.
   */
  Cluster deployWorkerVerticleTo(String deploymentID, String groupID, String main, int instances, Handler<AsyncResult<String>> doneHandler);

  /**
   * Deploys a worker verticle to the cluster.
   *
   * @param deploymentID The unique verticle deployment ID.
   * @param groupID The group to which to deploy the verticle.
   * @param main The verticle main.
   * @param config The module configuration.
   * @param instances The number of instances to deploy.
   * @param multiThreaded Indicates whether the verticle is multi-threaded.
   * @param doneHandler An asynchronous handler to be called once deployment is complete.
   * @return The cluster client.
   */
  Cluster deployWorkerVerticleTo(String deploymentID, String groupID, String main, JsonObject config, int instances, boolean multiThreaded, Handler<AsyncResult<String>> doneHandler);

  /**
   * Deploys a worker verticle to the cluster.
   *
   * @param deploymentID The unique verticle deployment ID.
   * @param groupID The group to which to deploy the verticle.
   * @param main The verticle main.
   * @param config The module configuration.
   * @param instances The number of instances to deploy.
   * @param multiThreaded Indicates whether the verticle is multi-threaded.
   * @param ha Indicates whether to deploy the verticle with HA.
   * @param doneHandler An asynchronous handler to be called once deployment is complete.
   * @return The cluster client.
   */
  Cluster deployWorkerVerticleTo(String deploymentID, String groupID, String main, JsonObject config, int instances, boolean multiThreaded, boolean ha, Handler<AsyncResult<String>> doneHandler);

  /**
   * Undeploys a module from the cluster.
   *
   * @param deploymentID The unique module deployment ID.
   * @return The cluster client.
   */
  Cluster undeployModule(String deploymentID);

  /**
   * Undeploys a module from the cluster.
   *
   * @param deploymentID The unique module deployment ID.
   * @param doneHandler An asynchronous handler to be called once the module is undeployed.
   * @return The cluster client.
   */
  Cluster undeployModule(String deploymentID, Handler<AsyncResult<Void>> doneHandler);

  /**
   * Undeploys a verticle from the cluster.
   *
   * @param deploymentID The unique verticle deployment ID.
   * @return The cluster client.
   */
  Cluster undeployVerticle(String deploymentID);

  /**
   * Undeploys a verticle from the cluster.
   *
   * @param deploymentID The unique verticle deployment ID.
   * @param doneHandler An asynchronous handler to be called once the verticle is undeployed.
   * @return The cluster client.
   */
  Cluster undeployVerticle(String deploymentID, Handler<AsyncResult<Void>> doneHandler);

  /**
   * Returns an asynchronous cluster-wide replicated map.
   *
   * @param name The map name.
   * @return An asynchronous cluster-wide replicated map.
   */
  <K, V> AsyncMap<K, V> getMap(String name);

  /**
   * Returns an asynchronous cluster-wide replicated list.
   *
   * @param name The list name.
   * @return An asynchronous cluster-wide replicated list.
   */
  <T> AsyncList<T> getList(String name);

  /**
   * Returns an asynchronous cluster-wide replicated set.
   *
   * @param name The set name.
   * @return An asynchronous cluster-wide replicated set.
   */
  <T> AsyncSet<T> getSet(String name);

  /**
   * Returns an asynchronous cluster-wide replicated queue.
   *
   * @param name The queue name.
   * @return An asynchronous cluster-wide replicated queue.
   */
  <T> AsyncQueue<T> getQueue(String name);

  /**
   * Returns an asynchronous cluster-wide counter.
   *
   * @param name The counter name.
   * @return An asynchronous cluster-wide counter.
   */
  AsyncCounter getCounter(String name);

}
