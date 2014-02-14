/*
 * Copyright 2013 the original author or authors.
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
package net.kuujo.vertigo.coordinator;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonObject;

/**
 * A local coordinator implementation.
 *
 * @author Jordan Halterman
 */
public class LocalCoordinator extends AbstractCoordinator {

  @Override
  protected void deployVerticle(String main, JsonObject config) {
    container.deployVerticle(main, config);
  }

  @Override
  protected void deployVerticle(String main, JsonObject config,
      Handler<AsyncResult<String>> doneHandler) {
    container.deployVerticle(main, config, doneHandler);
  }

  @Override
  protected void deployWorkerVerticle(String main, JsonObject config, boolean multiThreaded) {
    container.deployWorkerVerticle(main, config, 1, multiThreaded);
  }

  @Override
  protected void deployWorkerVerticle(String main, JsonObject config, boolean multiThreaded,
      Handler<AsyncResult<String>> doneHandler) {
    container.deployWorkerVerticle(main, config, 1, multiThreaded, doneHandler);
  }

  @Override
  protected void deployModule(String moduleName, JsonObject config) {
    container.deployModule(moduleName, config);
  }

  @Override
  protected void deployModule(String moduleName, JsonObject config,
      Handler<AsyncResult<String>> doneHandler) {
    container.deployModule(moduleName, config, doneHandler);
  }

  @Override
  protected void undeployVerticle(String deploymentId) {
    container.undeployVerticle(deploymentId);
  }

  @Override
  protected void undeployVerticle(String deploymentId, Handler<AsyncResult<Void>> doneHandler) {
    container.undeployVerticle(deploymentId, doneHandler);
  }

  @Override
  protected void undeployModule(String deploymentId) {
    container.undeployModule(deploymentId);
  }

  @Override
  protected void undeployModule(String deploymentId, Handler<AsyncResult<Void>> doneHandler) {
    container.undeployModule(deploymentId, doneHandler);
  }

}