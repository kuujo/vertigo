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

import net.kuujo.vertigo.network.ActiveNetwork;
import net.kuujo.vertigo.network.NetworkConfig;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonObject;

/**
 * Vertigo node client.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public interface Node {

  String address();

  Node ping(Handler<AsyncResult<Node>> resultHandler);

  Node installModule(String moduleName);

  Node installModule(String moduleName, Handler<AsyncResult<Void>> doneHandler);

  Node uninstallModule(String moduleName);

  Node uninstallModule(String moduleName, Handler<AsyncResult<Void>> doneHandler);

  Node deployModule(String moduleName);

  Node deployModule(String moduleName, JsonObject config);

  Node deployModule(String moduleName, int instances);

  Node deployModule(String moduleName, JsonObject config, int instances);

  Node deployModule(String moduleName, Handler<AsyncResult<String>> doneHandler);

  Node deployModule(String moduleName, JsonObject config, Handler<AsyncResult<String>> doneHandler);

  Node deployModule(String moduleName, int instances, Handler<AsyncResult<String>> doneHandler);

  Node deployModule(String moduleName, JsonObject config, int instances, Handler<AsyncResult<String>> doneHandler);

  Node deployVerticle(String main);

  Node deployVerticle(String main, JsonObject config);

  Node deployVerticle(String main, int instances);

  Node deployVerticle(String main, JsonObject config, int instances);

  Node deployVerticle(String main, Handler<AsyncResult<String>> doneHandler);

  Node deployVerticle(String main, JsonObject config, Handler<AsyncResult<String>> doneHandler);

  Node deployVerticle(String main, int instances, Handler<AsyncResult<String>> doneHandler);

  Node deployVerticle(String main, JsonObject config, int instances, Handler<AsyncResult<String>> doneHandler);

  Node deployWorkerVerticle(String main);

  Node deployWorkerVerticle(String main, JsonObject config);

  Node deployWorkerVerticle(String main, int instances);

  Node deployWorkerVerticle(String main, JsonObject config, int instances, boolean multiThreaded);

  Node deployWorkerVerticle(String main, Handler<AsyncResult<String>> doneHandler);

  Node deployWorkerVerticle(String main, JsonObject config, Handler<AsyncResult<String>> doneHandler);

  Node deployWorkerVerticle(String main, int instances, Handler<AsyncResult<String>> doneHandler);

  Node deployWorkerVerticle(String main, JsonObject config, int instances, boolean multiThreaded, Handler<AsyncResult<String>> doneHandler);

  Node undeployModule(String deploymentID);

  Node undeployModule(String deploymentID, Handler<AsyncResult<Void>> doneHandler);

  Node undeployVerticle(String deploymentID);

  Node undeployVerticle(String deploymentID, Handler<AsyncResult<Void>> doneHandler);

  Node getNetworks(Handler<AsyncResult<Collection<ActiveNetwork>>> resultHandler);

  Node getNetwork(String name, Handler<AsyncResult<ActiveNetwork>> resultHandler);

  Node installNetwork(JsonObject network);

  Node installNetwork(JsonObject network, Handler<AsyncResult<Void>> doneHandler);

  Node installNetwork(NetworkConfig network);

  Node installNetwork(NetworkConfig network, Handler<AsyncResult<Void>> doneHandler);

  Node uninstallNetwork(NetworkConfig network);

  Node uninstallNetwork(NetworkConfig network, Handler<AsyncResult<Void>> doneHandler);

  Node deployNetwork(String name);

  Node deployNetwork(String name, Handler<AsyncResult<ActiveNetwork>> doneHandler);

  Node deployNetwork(NetworkConfig network);

  Node deployNetwork(NetworkConfig network, Handler<AsyncResult<ActiveNetwork>> doneHandler);

  Node undeployNetwork(String name);

  Node undeployNetwork(String name, Handler<AsyncResult<Void>> doneHandler);

  Node undeployNetwork(NetworkConfig network);

  Node undeployNetwork(NetworkConfig network, Handler<AsyncResult<Void>> doneHandler);

}
