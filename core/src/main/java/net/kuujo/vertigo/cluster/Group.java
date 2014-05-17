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
 * Deployment group client.
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

  Group getNode(String address, Handler<AsyncResult<Node>> resultHandler);

  Group getNodes(Handler<AsyncResult<Collection<Node>>> resultHandler);

  Group selectNode(Object key, Handler<AsyncResult<Node>> resultHandler);

  Group deployModule(String moduleName);

  Group deployModule(String moduleName, JsonObject config);

  Group deployModule(String moduleName, int instances);

  Group deployModule(String moduleName, JsonObject config, int instances);

  Group deployModule(String moduleName, Handler<AsyncResult<String>> doneHandler);

  Group deployModule(String moduleName, JsonObject config, Handler<AsyncResult<String>> doneHandler);

  Group deployModule(String moduleName, int instances, Handler<AsyncResult<String>> doneHandler);

  Group deployModule(String moduleName, JsonObject config, int instances, Handler<AsyncResult<String>> doneHandler);

  Group deployVerticle(String main);

  Group deployVerticle(String main, JsonObject config);

  Group deployVerticle(String main, int instances);

  Group deployVerticle(String main, JsonObject config, int instances);

  Group deployVerticle(String main, Handler<AsyncResult<String>> doneHandler);

  Group deployVerticle(String main, JsonObject config, Handler<AsyncResult<String>> doneHandler);

  Group deployVerticle(String main, int instances, Handler<AsyncResult<String>> doneHandler);

  Group deployVerticle(String main, JsonObject config, int instances, Handler<AsyncResult<String>> doneHandler);

  Group deployWorkerVerticle(String main);

  Group deployWorkerVerticle(String main, JsonObject config, int instances, boolean multiThreaded);

  Group deployWorkerVerticle(String main, Handler<AsyncResult<String>> doneHandler);

  Group deployWorkerVerticle(String main, JsonObject config, int instances, boolean multiThreaded, Handler<AsyncResult<String>> doneHandler);

  Group undeployModule(String deploymentID);

  Group undeployModule(String deploymentID, Handler<AsyncResult<Void>> doneHandler);

  Group undeployVerticle(String deploymentID);

  Group undeployVerticle(String deploymentID, Handler<AsyncResult<Void>> doneHandler);

}
