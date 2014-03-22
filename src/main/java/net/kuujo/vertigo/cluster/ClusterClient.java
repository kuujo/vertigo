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
 * Cluster client.
 *
 * @author Jordan Halterman
 */
public interface ClusterClient {

  /**
   * Checks whether a module or verticle is deployed in the cluster.
   *
   * @param deploymentID The deployment ID to check.
   * @param resultHandler An asynchronous handler to be called with a result.
   * @return The cluster client.
   */
  ClusterClient isDeployed(String deploymentID, Handler<AsyncResult<Boolean>> resultHandler);

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
  ClusterClient deployModule(String deploymentID, String moduleName, JsonObject config, int instances, Handler<AsyncResult<String>> doneHandler);

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
  ClusterClient deployModuleTo(String deploymentID, String groupID, String moduleName, JsonObject config, int instances, Handler<AsyncResult<String>> doneHandler);

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
  ClusterClient deployVerticle(String deploymentID, String main, JsonObject config, int instances, Handler<AsyncResult<String>> doneHandler);

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
  ClusterClient deployVerticleTo(String deploymentID, String groupID, String main, JsonObject config, int instances, Handler<AsyncResult<String>> doneHandler);

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
  ClusterClient deployWorkerVerticle(String deploymentID, String main, JsonObject config, int instances, boolean multiThreaded, Handler<AsyncResult<String>> doneHandler);

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
  ClusterClient deployWorkerVerticleTo(String deploymentID, String groupID, String main, JsonObject config, int instances, boolean multiThreaded, Handler<AsyncResult<String>> doneHandler);

  /**
   * Undeploys a module from the cluster.
   *
   * @param deploymentID The unique module deployment ID.
   * @param doneHandler An asynchronous handler to be called once the module is undeployed.
   * @return The cluster client.
   */
  ClusterClient undeployModule(String deploymentID, Handler<AsyncResult<Void>> doneHandler);

  /**
   * Undeploys a verticle from the cluster.
   *
   * @param deploymentID The unique verticle deployment ID.
   * @param doneHandler An asynchronous handler to be called once the verticle is undeployed.
   * @return The cluster client.
   */
  ClusterClient undeployVerticle(String deploymentID, Handler<AsyncResult<Void>> doneHandler);

  /**
   * Sets a key in the cluster.
   *
   * @param key The key to set.
   * @param value The value to set.
   * @return The cluster client.
   */
  ClusterClient set(String key, Object value);

  /**
   * Sets a key in the cluster.
   *
   * @param key The key to set.
   * @param value The value to set.
   * @param doneHandler An asynchronous handler to be called once complete.
   * @return The cluster client.
   */
  ClusterClient set(String key, Object value, Handler<AsyncResult<Void>> doneHandler);

  /**
   * Gets the value of a key in the cluster.
   *
   * @param key The key to get.
   * @param resultHandler An asynchronous handler to be called with the key value.
   * @return The cluster client.
   */
  <T> ClusterClient get(String key, Handler<AsyncResult<T>> resultHandler);

  /**
   * Gets the value of a key in the cluster.
   *
   * @param key The key to get.
   * @param def The default value if the key is not set.
   * @param resultHandler An asynchronous handler to be called with the key value.
   * @return The cluster client.
   */
  <T> ClusterClient get(String key, Object def, Handler<AsyncResult<T>> resultHandler);

  /**
   * Deletes a key in the cluster asynchronously.
   *
   * @param key The key to delete.
   * @return The cluster client.
   */
  ClusterClient delete(String key);

  /**
   * Deletes a key in the cluster asynchronously.
   *
   * @param key The key to delete.
   * @param doneHandler An asynchronous handler to be called once complete.
   * @return The cluster client.
   */
  ClusterClient delete(String key, Handler<AsyncResult<Void>> doneHandler);

  /**
   * Checks whether a key exists in the cluster.
   *
   * @param key The name of the key to check.
   * @param resultHandler An asynchronous handler to be called once complete.
   * @return The cluster client.
   */
  ClusterClient exists(String key, Handler<AsyncResult<Boolean>> resultHandler);

  /**
   * Watches a key for all events.
   *
   * @param key The key to watch.
   * @param handler A handler to call when an event occurs on the key.
   * @return The cluster client.
   */
  ClusterClient watch(String key, Handler<ClusterEvent> handler);

  /**
   * Watches a key for an event.
   *
   * @param key The key to watch.
   * @param event The event type to watch.
   * @param handler A handler to call when an event occurs on the key.
   * @return The cluster client.
   */
  ClusterClient watch(String key, ClusterEvent.Type event, Handler<ClusterEvent> handler);

  /**
   * Watches a key for all events.
   *
   * @param key The key to watch.
   * @param handler A handler to call when an event occurs on the key.
   * @param doneHandler An asynchronous handler to be called once the watch has been registered.
   * @return The cluster client.
   */
  ClusterClient watch(String key, Handler<ClusterEvent> handler, Handler<AsyncResult<Void>> doneHandler);

  /**
   * Watches a key for an event.
   *
   * @param key The key to watch.
   * @param event The event type to watch.
   * @param handler A handler to call when an event occurs on the key.
   * @param doneHandler An asynchronous handler to be called once the watch has been registered.
   * @return The cluster client.
   */
  ClusterClient watch(String key, ClusterEvent.Type event, Handler<ClusterEvent> handler, Handler<AsyncResult<Void>> doneHandler);

  /**
   * Unwatches a key for an event.
   *
   * @param key The key to unwatch.
   * @param handler The handler to unregister.
   * @return The cluster client.
   */
  ClusterClient unwatch(String key, Handler<ClusterEvent> handler);

  /**
   * Unwatches a key for an event.
   *
   * @param key The key to unwatch.
   * @param event The event type to unwatch.
   * @param handler The handler to unregister.
   * @return The cluster client.
   */
  ClusterClient unwatch(String key, ClusterEvent.Type event, Handler<ClusterEvent> handler);

  /**
   * Unwatches a key for an event.
   *
   * @param key The key to unwatch.
   * @param handler The handler to unregister.
   * @param doneHandler An asynchronous handler to be called once the watch has been unregistered.
   * @return The cluster client.
   */
  ClusterClient unwatch(String key, Handler<ClusterEvent> handler, Handler<AsyncResult<Void>> doneHandler);

  /**
   * Unwatches a key for an event.
   *
   * @param key The key to unwatch.
   * @param event The event type to unwatch.
   * @param handler The handler to unregister.
   * @param doneHandler An asynchronous handler to be called once the watch has been unregistered.
   * @return The cluster client.
   */
  ClusterClient unwatch(String key, ClusterEvent.Type event, Handler<ClusterEvent> handler, Handler<AsyncResult<Void>> doneHandler);

}
