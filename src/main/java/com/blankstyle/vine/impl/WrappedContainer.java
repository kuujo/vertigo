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
package com.blankstyle.vine.impl;

import java.util.Map;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.platform.Container;

import com.blankstyle.vine.Feeder;
import com.blankstyle.vine.VineContainer;

public class WrappedContainer implements VineContainer {

  protected Container container;

  public WrappedContainer(Container container) {
    this.container = container;
  }

  @Override
  public void deployWorkerVerticle(String main) {
    container.deployWorkerVerticle(main);
  }

  @Override
  public void deployWorkerVerticle(String main, int instances) {
    container.deployWorkerVerticle(main, instances);
  }

  @Override
  public void deployWorkerVerticle(String main, JsonObject config) {
    container.deployWorkerVerticle(main, config);
  }

  @Override
  public void deployWorkerVerticle(String main, JsonObject config, int instances) {
    container.deployWorkerVerticle(main, config, instances);
  }

  @Override
  public void deployWorkerVerticle(String main, JsonObject config,
      int instances, boolean multiThreaded) {
    container.deployWorkerVerticle(main, config, instances, multiThreaded);
  }

  @Override
  public void deployWorkerVerticle(String main, JsonObject config,
      int instances, boolean multiThreaded,
      Handler<AsyncResult<String>> doneHandler) {
    container.deployWorkerVerticle(main, config, instances, multiThreaded, doneHandler);
  }

  @Override
  public void deployModule(String moduleName) {
    container.deployModule(moduleName);
  }

  @Override
  public void deployModule(String moduleName, int instances) {
    container.deployModule(moduleName, instances);
  }

  @Override
  public void deployModule(String moduleName, JsonObject config) {
    container.deployModule(moduleName, config);
  }

  @Override
  public void deployModule(String moduleName, JsonObject config, int instances) {
    container.deployModule(moduleName, config, instances);
  }

  @Override
  public void deployModule(String moduleName, JsonObject config, int instances,
      Handler<AsyncResult<String>> doneHandler) {
    container.deployModule(moduleName, config, instances, doneHandler);
  }

  @Override
  public void deployModule(String moduleName,
      Handler<AsyncResult<String>> doneHandler) {
    container.deployModule(moduleName, doneHandler);
  }

  @Override
  public void deployModule(String moduleName, JsonObject config,
      Handler<AsyncResult<String>> doneHandler) {
    container.deployModule(moduleName, config, doneHandler);
  }

  @Override
  public void deployModule(String moduleName, int instances,
      Handler<AsyncResult<String>> doneHandler) {
    container.deployModule(moduleName, instances, doneHandler);
  }

  @Override
  public void deployVerticle(String main) {
    container.deployVerticle(main);
  }

  @Override
  public void deployVerticle(String main, int instances) {
    container.deployVerticle(main, instances);
  }

  @Override
  public void deployVerticle(String main, JsonObject config) {
    container.deployVerticle(main, config);
  }

  @Override
  public void deployVerticle(String main, JsonObject config, int instances) {
    container.deployVerticle(main, config, instances);
  }

  @Override
  public void deployVerticle(String main, JsonObject config, int instances,
      Handler<AsyncResult<String>> doneHandler) {
    container.deployVerticle(main, config, instances, doneHandler);
  }

  @Override
  public void deployVerticle(String main,
      Handler<AsyncResult<String>> doneHandler) {
    container.deployVerticle(main, doneHandler);
  }

  @Override
  public void deployVerticle(String main, JsonObject config,
      Handler<AsyncResult<String>> doneHandler) {
    container.deployVerticle(main, config, doneHandler);
  }

  @Override
  public void deployVerticle(String main, int instances,
      Handler<AsyncResult<String>> doneHandler) {
    container.deployVerticle(main, instances, doneHandler);
  }

  @Override
  public void undeployVerticle(String deploymentID) {
    container.undeployVerticle(deploymentID);
  }

  @Override
  public void undeployVerticle(String deploymentID,
      Handler<AsyncResult<Void>> doneHandler) {
    container.undeployVerticle(deploymentID, doneHandler);
  }

  @Override
  public void undeployModule(String deploymentID) {
    container.undeployModule(deploymentID);
  }

  @Override
  public void undeployModule(String deploymentID,
      Handler<AsyncResult<Void>> doneHandler) {
    container.undeployModule(deploymentID, doneHandler);
  }

  @Override
  public JsonObject config() {
    return container.config();
  }

  @Override
  public Logger logger() {
    return container.logger();
  }

  @Override
  public void exit() {
    container.exit();
  }

  @Override
  public Map<String, String> env() {
    return container.env();
  }

  @Override
  public void deployVine(Handler<Feeder> handler) {
    
  }

  @Override
  public void undeployVine(Handler<Void> doneHandler) {
    
  }

}
