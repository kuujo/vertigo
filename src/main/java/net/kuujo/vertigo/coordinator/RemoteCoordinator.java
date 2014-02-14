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
import org.vertx.java.core.Future;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.impl.DefaultFutureResult;
import org.vertx.java.core.json.JsonObject;

/**
 * A remote coordinator implementation.
 *
 * @author Jordan Halterman
 */
public class RemoteCoordinator extends AbstractCoordinator {
  private String address;
  private EventBus eventBus;

  @Override
  public void start() {
    super.start();
    address = config.getString("master");
    eventBus = vertx.eventBus();
  }

  @Override
  protected void deployVerticle(String main, JsonObject config) {
    eventBus.send(address, createAction("deploy").putString("type", "verticle")
        .putString("main", main).putObject("config", config));
  }

  @Override
  protected void deployVerticle(String main, JsonObject config,
      Handler<AsyncResult<String>> doneHandler) {
    eventBus.send(address, createAction("deploy").putString("type", "verticle")
        .putString("main", main).putObject("config", config),
        createDeployHandler(new DefaultFutureResult<String>().setHandler(doneHandler)));
  }

  @Override
  protected void deployWorkerVerticle(String main, JsonObject config, boolean multiThreaded) {
    eventBus.send(address, createAction("deploy").putString("type", "verticle")
        .putString("main", main).putObject("config", config).putBoolean("worker", true));
  }

  @Override
  protected void deployWorkerVerticle(String main, JsonObject config, boolean multiThreaded,
      Handler<AsyncResult<String>> doneHandler) {
    eventBus.send(address, createAction("deploy").putString("type", "verticle")
        .putString("main", main).putObject("config", config).putBoolean("worker", true),
        createDeployHandler(new DefaultFutureResult<String>().setHandler(doneHandler)));
  }

  @Override
  protected void deployModule(String moduleName, JsonObject config) {
    eventBus.send(address, createAction("deploy").putString("type", "module")
        .putString("module", moduleName).putObject("config", config));
  }

  @Override
  protected void deployModule(String moduleName, JsonObject config,
      Handler<AsyncResult<String>> doneHandler) {
    eventBus.send(address, createAction("deploy").putString("type", "module")
        .putString("module", moduleName).putObject("config", config),
        createDeployHandler(new DefaultFutureResult<String>().setHandler(doneHandler)));
  }

  /**
   * Creates a new deployment handler.
   */
  private Handler<Message<String>> createDeployHandler(final Future<String> future) {
    return new Handler<Message<String>>() {
      @Override
      public void handle(Message<String> message) {
        String deploymentID = message.body();
        if (deploymentID != null) {
          future.setResult(deploymentID);
        }
        else {
          future.setFailure(null);
        }
      }
    };
  }

  @Override
  protected void undeployVerticle(String deploymentId) {
    eventBus.send(address, createAction("undeploy").putString("type", "verticle")
        .putString("id", deploymentId));
  }

  @Override
  protected void undeployVerticle(String deploymentId,
      Handler<AsyncResult<Void>> doneHandler) {
    eventBus.send(address, createAction("undeploy").putString("type", "verticle")
        .putString("id", deploymentId),
        createUndeployHandler(new DefaultFutureResult<Void>().setHandler(doneHandler)));
  }

  @Override
  protected void undeployModule(String deploymentId) {
    eventBus.send(address, createAction("undeploy").putString("type", "module")
        .putString("id", deploymentId));
  }

  @Override
  protected void undeployModule(String deploymentId,
      Handler<AsyncResult<Void>> doneHandler) {
    eventBus.send(address, createAction("undeploy").putString("type", "module")
        .putString("id", deploymentId),
        createUndeployHandler(new DefaultFutureResult<Void>().setHandler(doneHandler)));
  }

  /**
   * Creates an undeploy handler.
   */
  private Handler<Message<Boolean>> createUndeployHandler(final Future<Void> future) {
    return new Handler<Message<Boolean>>() {
      @Override
      public void handle(Message<Boolean> message) {
        Boolean succeeded = message.body();
        if (succeeded) {
          future.setResult(null);
        }
        else {
          future.setFailure(null);
        }
      }
    };
  }

  /**
   * Creates a new JSON action.
   */
  private JsonObject createAction(String actionName) {
    return new JsonObject().putString("action", actionName);
  }

}