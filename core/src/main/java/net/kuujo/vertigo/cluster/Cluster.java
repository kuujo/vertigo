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
 * Cluster client.
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

  Cluster ping(Handler<AsyncResult<Cluster>> resultHandler);

  Cluster getGroup(String address, Handler<AsyncResult<Group>> resultHandler);

  Cluster getGroups(Handler<AsyncResult<Collection<Group>>> resultHandler);

  Cluster selectGroup(Object key, Handler<AsyncResult<Group>> resultHandler);

  Cluster getNode(String address, Handler<AsyncResult<Node>> resultHandler);

  Cluster getNodes(Handler<AsyncResult<Collection<Node>>> resultHandler);

  Cluster selectNode(Object key, Handler<AsyncResult<Node>> resultHandler);

  Cluster deployModule(String moduleName);

  Cluster deployModule(String moduleName, JsonObject config);

  Cluster deployModule(String moduleName, int instances);

  Cluster deployModule(String moduleName, JsonObject config, int instances);

  Cluster deployModule(String moduleName, Handler<AsyncResult<String>> doneHandler);

  Cluster deployModule(String moduleName, JsonObject config, Handler<AsyncResult<String>> doneHandler);

  Cluster deployModule(String moduleName, int instances, Handler<AsyncResult<String>> doneHandler);

  Cluster deployModule(String moduleName, JsonObject config, int instances, Handler<AsyncResult<String>> doneHandler);

  Cluster deployVerticle(String main);

  Cluster deployVerticle(String main, JsonObject config);

  Cluster deployVerticle(String main, int instances);

  Cluster deployVerticle(String main, JsonObject config, int instances);

  Cluster deployVerticle(String main, Handler<AsyncResult<String>> doneHandler);

  Cluster deployVerticle(String main, JsonObject config, Handler<AsyncResult<String>> doneHandler);

  Cluster deployVerticle(String main, int instances, Handler<AsyncResult<String>> doneHandler);

  Cluster deployVerticle(String main, JsonObject config, int instances, Handler<AsyncResult<String>> doneHandler);

  Cluster deployWorkerVerticle(String main);

  Cluster deployWorkerVerticle(String main, JsonObject config, int instances, boolean multiThreaded);

  Cluster deployWorkerVerticle(String main, Handler<AsyncResult<String>> doneHandler);

  Cluster deployWorkerVerticle(String main, JsonObject config, int instances, boolean multiThreaded, Handler<AsyncResult<String>> doneHandler);

  Cluster undeployModule(String deploymentID);

  Cluster undeployModule(String deploymentID, Handler<AsyncResult<Void>> doneHandler);

  Cluster undeployVerticle(String deploymentID);

  Cluster undeployVerticle(String deploymentID, Handler<AsyncResult<Void>> doneHandler);

  Cluster getNetworks(Handler<AsyncResult<Collection<ActiveNetwork>>> resultHnadler);

  Cluster getNetwork(String name, Handler<AsyncResult<ActiveNetwork>> resultHandler);

  Cluster isDeployed(String name, Handler<AsyncResult<Boolean>> resultHandler);

  Cluster deployNetwork(String name);

  Cluster deployNetwork(String name, Handler<AsyncResult<ActiveNetwork>> doneHandler);

  Cluster deployNetwork(JsonObject network);

  Cluster deployNetwork(JsonObject network, Handler<AsyncResult<ActiveNetwork>> doneHandler);

  Cluster deployNetwork(NetworkConfig network);

  Cluster deployNetwork(NetworkConfig network, Handler<AsyncResult<ActiveNetwork>> doneHandler);

  Cluster undeployNetwork(String name);

  Cluster undeployNetwork(String name, Handler<AsyncResult<Void>> doneHandler);

  Cluster undeployNetwork(JsonObject network);

  Cluster undeployNetwork(JsonObject network, Handler<AsyncResult<Void>> doneHandler);

  Cluster undeployNetwork(NetworkConfig network);

  Cluster undeployNetwork(NetworkConfig network, Handler<AsyncResult<Void>> doneHandler);

  <K, V> AsyncMap<K, V> getMap(String name);

  <K, V> AsyncMultiMap<K, V> getMultiMap(String name);

  <T> AsyncSet<T> getSet(String name);

  <T> AsyncList<T> getList(String name);

  <T> AsyncQueue<T> getQueue(String name);

  AsyncCounter getCounter(String name);

}
