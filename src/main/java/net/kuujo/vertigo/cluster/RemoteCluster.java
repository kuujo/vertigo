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

import net.kuujo.vertigo.annotations.Factory;
import net.kuujo.vertigo.cluster.data.AsyncIdGenerator;
import net.kuujo.vertigo.cluster.data.AsyncList;
import net.kuujo.vertigo.cluster.data.AsyncLock;
import net.kuujo.vertigo.cluster.data.AsyncQueue;
import net.kuujo.vertigo.cluster.data.AsyncSet;
import net.kuujo.vertigo.cluster.data.WatchableAsyncMap;
import net.kuujo.vertigo.cluster.data.impl.EventBusIdGenerator;
import net.kuujo.vertigo.cluster.data.impl.EventBusList;
import net.kuujo.vertigo.cluster.data.impl.EventBusLock;
import net.kuujo.vertigo.cluster.data.impl.EventBusMap;
import net.kuujo.vertigo.cluster.data.impl.EventBusQueue;
import net.kuujo.vertigo.cluster.data.impl.EventBusSet;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.impl.DefaultFutureResult;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Container;

/**
 * Remote cluster client implementation.
 *
 * @author Jordan Halterman
 */
public class RemoteCluster implements VertigoCluster {
  private static final String CLUSTER_ADDRESS = "__CLUSTER__";
  private final EventBus eventBus;

  @Factory
  public static VertigoCluster factory(Vertx vertx, Container container) {
    return new RemoteCluster(vertx);
  }

  public RemoteCluster(Vertx vertx) {
    this.eventBus = vertx.eventBus();
  }

  @Override
  public VertigoCluster isDeployed(String deploymentID, final Handler<AsyncResult<Boolean>> resultHandler) {
    JsonObject message = new JsonObject()
        .putString("action", "check")
        .putString("id", deploymentID);
    eventBus.sendWithTimeout(CLUSTER_ADDRESS, message, 30000, new Handler<AsyncResult<Message<JsonObject>>>() {
      @Override
      public void handle(AsyncResult<Message<JsonObject>> result) {
        if (result.failed()) {
          new DefaultFutureResult<Boolean>(result.cause()).setHandler(resultHandler);
        } else if (result.result().body().getString("status").equals("ok")) {
          new DefaultFutureResult<Boolean>(result.result().body().getBoolean("result")).setHandler(resultHandler);
        } else {
          new DefaultFutureResult<Boolean>(new DeploymentException(result.result().body().getString("message"))).setHandler(resultHandler);
        }
      }
    });
    return this;
  }

  @Override
  public VertigoCluster deployModule(String deploymentID, String moduleName, JsonObject config,
      int instances, final Handler<AsyncResult<String>> doneHandler) {
    return deployModuleTo(deploymentID, null, moduleName, config, instances, doneHandler);
  }

  @Override
  public VertigoCluster deployModuleTo(String deploymentID, String groupID, String moduleName, JsonObject config, int instances,
      final Handler<AsyncResult<String>> doneHandler) {
    JsonObject message = new JsonObject()
        .putString("action", "deploy")
        .putString("id", deploymentID)
        .putString("group", groupID)
        .putString("type", "module")
        .putString("module", moduleName)
        .putObject("config", config)
        .putNumber("instances", instances);
    eventBus.sendWithTimeout(CLUSTER_ADDRESS, message, 30000, new Handler<AsyncResult<Message<JsonObject>>>() {
      @Override
      public void handle(AsyncResult<Message<JsonObject>> result) {
        if (result.failed()) {
          new DefaultFutureResult<String>(result.cause()).setHandler(doneHandler);
        } else if (result.result().body().getString("status").equals("ok")) {
          new DefaultFutureResult<String>(result.result().body().getString("id")).setHandler(doneHandler);
        } else {
          new DefaultFutureResult<String>(new DeploymentException(result.result().body().getString("message"))).setHandler(doneHandler);
        }
      }
    });
    return this;
  }

  @Override
  public VertigoCluster deployVerticle(String deploymentID, String main, JsonObject config, int instances,
      Handler<AsyncResult<String>> doneHandler) {
    return deployVerticleTo(deploymentID, null, main, config, instances, doneHandler);
  }

  @Override
  public VertigoCluster deployVerticleTo(String deploymentID, String groupID, String main, JsonObject config,
      int instances, final Handler<AsyncResult<String>> doneHandler) {
    JsonObject message = new JsonObject()
        .putString("action", "deploy")
        .putString("id", deploymentID)
        .putString("group", groupID)
        .putString("type", "verticle")
        .putString("main", main)
        .putObject("config", config)
        .putNumber("instances", instances);
    eventBus.sendWithTimeout(CLUSTER_ADDRESS, message, 30000, new Handler<AsyncResult<Message<JsonObject>>>() {
      @Override
      public void handle(AsyncResult<Message<JsonObject>> result) {
        if (result.failed()) {
          new DefaultFutureResult<String>(result.cause()).setHandler(doneHandler);
        } else if (result.result().body().getString("status").equals("ok")) {
          new DefaultFutureResult<String>(result.result().body().getString("id")).setHandler(doneHandler);
        } else {
          new DefaultFutureResult<String>(new DeploymentException(result.result().body().getString("message"))).setHandler(doneHandler);
        }
      }
    });
    return this;
  }

  @Override
  public VertigoCluster deployWorkerVerticle(String deploymentID, String main, JsonObject config,
      int instances, boolean multiThreaded, Handler<AsyncResult<String>> doneHandler) {
    return deployWorkerVerticleTo(deploymentID, null, main, config, instances, multiThreaded, doneHandler);
  }

  @Override
  public VertigoCluster deployWorkerVerticleTo(String deploymentID, String groupID, String main,
      JsonObject config, int instances, boolean multiThreaded, final Handler<AsyncResult<String>> doneHandler) {
    JsonObject message = new JsonObject()
        .putString("action", "deploy")
        .putString("id", deploymentID)
        .putString("group", groupID)
        .putString("type", "verticle")
        .putString("main", main)
        .putObject("config", config)
        .putNumber("instances", instances)
        .putBoolean("worker", true)
        .putBoolean("multi-threaded", multiThreaded);
    eventBus.sendWithTimeout(CLUSTER_ADDRESS, message, 30000, new Handler<AsyncResult<Message<JsonObject>>>() {
      @Override
      public void handle(AsyncResult<Message<JsonObject>> result) {
        if (result.failed()) {
          new DefaultFutureResult<String>(result.cause()).setHandler(doneHandler);
        } else if (result.result().body().getString("status").equals("ok")) {
          new DefaultFutureResult<String>(result.result().body().getString("id")).setHandler(doneHandler);
        } else {
          new DefaultFutureResult<String>(new DeploymentException(result.result().body().getString("message"))).setHandler(doneHandler);
        }
      }
    });
    return this;
  }

  @Override
  public VertigoCluster undeployModule(String deploymentID, final Handler<AsyncResult<Void>> doneHandler) {
    JsonObject message = new JsonObject()
        .putString("action", "undeploy")
        .putString("id", deploymentID)
        .putString("type", "module");
    eventBus.sendWithTimeout(CLUSTER_ADDRESS, message, 30000, new Handler<AsyncResult<Message<JsonObject>>>() {
      @Override
      public void handle(AsyncResult<Message<JsonObject>> result) {
        if (result.failed()) {
          new DefaultFutureResult<Void>(result.cause()).setHandler(doneHandler);
        } else if (result.result().body().getString("status").equals("ok")) {
          new DefaultFutureResult<Void>((Void) null).setHandler(doneHandler);
        } else {
          new DefaultFutureResult<Void>(new DeploymentException(result.result().body().getString("message"))).setHandler(doneHandler);
        }
      }
    });
    return this;
  }

  @Override
  public VertigoCluster undeployVerticle(String deploymentID, final Handler<AsyncResult<Void>> doneHandler) {
    JsonObject message = new JsonObject()
        .putString("action", "undeploy")
        .putString("id", deploymentID)
        .putString("type", "verticle");
    eventBus.sendWithTimeout(CLUSTER_ADDRESS, message, 30000, new Handler<AsyncResult<Message<JsonObject>>>() {
      @Override
      public void handle(AsyncResult<Message<JsonObject>> result) {
        if (result.failed()) {
          new DefaultFutureResult<Void>(result.cause()).setHandler(doneHandler);
        } else if (result.result().body().getString("status").equals("ok")) {
          new DefaultFutureResult<Void>((Void) null).setHandler(doneHandler);
        } else {
          new DefaultFutureResult<Void>(new DeploymentException(result.result().body().getString("message"))).setHandler(doneHandler);
        }
      }
    });
    return this;
  }

  @Override
  public <T> AsyncList<T> getList(String name) {
    return new EventBusList<T>(name, eventBus);
  }

  @Override
  public <K, V> WatchableAsyncMap<K, V> getMap(String name) {
    return new EventBusMap<K, V>(name, eventBus);
  }

  @Override
  public <T> AsyncSet<T> getSet(String name) {
    return new EventBusSet<T>(name, eventBus);
  }

  @Override
  public <T> AsyncQueue<T> getQueue(String name) {
    return new EventBusQueue<T>(name, eventBus);
  }

  @Override
  public AsyncIdGenerator getIdGenerator(String name) {
    return new EventBusIdGenerator(name, eventBus);
  }

  @Override
  public AsyncLock getLock(String name) {
    return new EventBusLock(name, eventBus);
  }

}
