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

import net.kuujo.vertigo.cluster.data.AsyncIdGenerator;
import net.kuujo.vertigo.cluster.data.AsyncList;
import net.kuujo.vertigo.cluster.data.AsyncLock;
import net.kuujo.vertigo.cluster.data.AsyncQueue;
import net.kuujo.vertigo.cluster.data.AsyncSet;
import net.kuujo.vertigo.cluster.data.WatchableAsyncMap;
import net.kuujo.vertigo.cluster.data.impl.XyncIdGenerator;
import net.kuujo.vertigo.cluster.data.impl.XyncList;
import net.kuujo.vertigo.cluster.data.impl.XyncLock;
import net.kuujo.vertigo.cluster.data.impl.XyncMap;
import net.kuujo.vertigo.cluster.data.impl.XyncQueue;
import net.kuujo.vertigo.cluster.data.impl.XyncSet;
import net.kuujo.vertigo.util.Factory;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.impl.DefaultFutureResult;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Container;
import org.vertx.java.platform.Verticle;

/**
 * Xync-based cluster implementation.<p>
 *
 * The Xync cluster is backed by the custom Xync platform manager
 * and Hazelcast data structures via the Vert.x event bus. When running
 * the remote cluster the Vert.x instance <b>must</b> have been run with
 * the custom Xync platform manager.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
@XyncType
public class XyncCluster implements Cluster {
  private static final String CLUSTER_ADDRESS = "__CLUSTER__";
  private final Vertx vertx;

  @Factory
  public static Cluster factory(Vertx vertx, Container container) {
    return new XyncCluster(vertx, container);
  }

  public XyncCluster(Verticle verticle) {
    this(verticle.getVertx(), verticle.getContainer());
  }

  public XyncCluster(Vertx vertx, Container container) {
    this.vertx = vertx;
  }

  @Override
  public Cluster start(final Handler<AsyncResult<Void>> doneHandler) {
    new DefaultFutureResult<Void>((Void) null).setHandler(doneHandler);
    return this;
  }

  @Override
  public void stop(Handler<AsyncResult<Void>> doneHandler) {
    new DefaultFutureResult<Void>((Void) null).setHandler(doneHandler);
  }

  @Override
  public ClusterScope scope() {
    return ClusterScope.XYNC;
  }

  @Override
  public Cluster isDeployed(String deploymentID, final Handler<AsyncResult<Boolean>> resultHandler) {
    JsonObject message = new JsonObject()
        .putString("action", "check")
        .putString("id", deploymentID);
    vertx.eventBus().sendWithTimeout(CLUSTER_ADDRESS, message, 30000, new Handler<AsyncResult<Message<JsonObject>>>() {
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
  public Cluster deployModule(String deploymentID, String moduleName) {
    return deployModuleTo(deploymentID, null, moduleName, null, 1, null);
  }

  @Override
  public Cluster deployModule(String deploymentID, String moduleName, JsonObject config) {
    return deployModuleTo(deploymentID, null, moduleName, config, 1, null);
  }

  @Override
  public Cluster deployModule(String deploymentID, String moduleName, int instances) {
    return deployModuleTo(deploymentID, null, moduleName, null, instances, null);
  }

  @Override
  public Cluster deployModule(String deploymentID, String moduleName, JsonObject config, int instances) {
    return deployModuleTo(deploymentID, null, moduleName, config, instances, null);
  }

  @Override
  public Cluster deployModule(String deploymentID, String moduleName, Handler<AsyncResult<String>> doneHandler) {
    return deployModuleTo(deploymentID, null, moduleName, null, 1, doneHandler);
  }

  @Override
  public Cluster deployModule(String deploymentID, String moduleName, JsonObject config, Handler<AsyncResult<String>> doneHandler) {
    return deployModuleTo(deploymentID, null, moduleName, config, 1, doneHandler);
  }

  @Override
  public Cluster deployModule(String deploymentID, String moduleName, int instances, Handler<AsyncResult<String>> doneHandler) {
    return deployModuleTo(deploymentID, null, moduleName, null, instances, doneHandler);
  }

  @Override
  public Cluster deployModule(String deploymentID, String moduleName, JsonObject config, int instances, final Handler<AsyncResult<String>> doneHandler) {
    return deployModuleTo(deploymentID, null, moduleName, config, instances, doneHandler);
  }

  @Override
  public Cluster deployModuleTo(String deploymentID, String groupID, String moduleName) {
    return deployModuleTo(deploymentID, groupID, moduleName, null, 1, null);
  }

  @Override
  public Cluster deployModuleTo(String deploymentID, String groupID, String moduleName, JsonObject config) {
    return deployModuleTo(deploymentID, groupID, moduleName, config, 1, null);
  }

  @Override
  public Cluster deployModuleTo(String deploymentID, String groupID, String moduleName, int instances) {
    return deployModuleTo(deploymentID, groupID, moduleName, null, instances, null);
  }

  @Override
  public Cluster deployModuleTo(String deploymentID, String groupID, String moduleName, JsonObject config, int instances) {
    return deployModuleTo(deploymentID, groupID, moduleName, config, instances, null);
  }

  @Override
  public Cluster deployModuleTo(String deploymentID, String groupID, String moduleName, Handler<AsyncResult<String>> doneHandler) {
    return deployModuleTo(deploymentID, groupID, moduleName, null, 1, doneHandler);
  }

  @Override
  public Cluster deployModuleTo(String deploymentID, String groupID, String moduleName, JsonObject config, Handler<AsyncResult<String>> doneHandler) {
    return deployModuleTo(deploymentID, groupID, moduleName, config, 1, doneHandler);
  }

  @Override
  public Cluster deployModuleTo(String deploymentID, String groupID, String moduleName, int instances, Handler<AsyncResult<String>> doneHandler) {
    return deployModuleTo(deploymentID, groupID, moduleName, null, instances, doneHandler);
  }

  @Override
  public Cluster deployModuleTo(String deploymentID, String groupID, String moduleName, JsonObject config, int instances, final Handler<AsyncResult<String>> doneHandler) {
    JsonObject message = new JsonObject()
        .putString("action", "deploy")
        .putString("id", deploymentID)
        .putString("group", groupID)
        .putString("type", "module")
        .putString("module", moduleName)
        .putObject("config", config)
        .putNumber("instances", instances);
    vertx.eventBus().sendWithTimeout(CLUSTER_ADDRESS, message, 30000, new Handler<AsyncResult<Message<JsonObject>>>() {
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
  public Cluster deployVerticle(String deploymentID, String main) {
    return deployVerticleTo(deploymentID, null, main, null, 1, null);
  }

  @Override
  public Cluster deployVerticle(String deploymentID, String main, JsonObject config) {
    return deployVerticleTo(deploymentID, null, main, config, 1, null);
  }

  @Override
  public Cluster deployVerticle(String deploymentID, String main, int instances) {
    return deployVerticleTo(deploymentID, null, main, null, instances, null);
  }

  @Override
  public Cluster deployVerticle(String deploymentID, String main, JsonObject config, int instances) {
    return deployVerticleTo(deploymentID, null, main, config, instances, null);
  }

  @Override
  public Cluster deployVerticle(String deploymentID, String main, Handler<AsyncResult<String>> doneHandler) {
    return deployVerticleTo(deploymentID, null, main, null, 1, doneHandler);
  }

  @Override
  public Cluster deployVerticle(String deploymentID, String main, JsonObject config, Handler<AsyncResult<String>> doneHandler) {
    return deployVerticleTo(deploymentID, null, main, config, 1, doneHandler);
  }

  @Override
  public Cluster deployVerticle(String deploymentID, String main, int instances, Handler<AsyncResult<String>> doneHandler) {
    return deployVerticleTo(deploymentID, null, main, null, instances, doneHandler);
  }

  @Override
  public Cluster deployVerticle(String deploymentID, String main, JsonObject config, int instances, Handler<AsyncResult<String>> doneHandler) {
    return deployVerticleTo(deploymentID, null, main, config, instances, doneHandler);
  }

  @Override
  public Cluster deployVerticleTo(String deploymentID, String groupID, String main) {
    return deployVerticleTo(deploymentID, groupID, main, null, 1, null);
  }

  @Override
  public Cluster deployVerticleTo(String deploymentID, String groupID, String main, JsonObject config) {
    return deployVerticleTo(deploymentID, groupID, main, config, 1, null);
  }

  @Override
  public Cluster deployVerticleTo(String deploymentID, String groupID, String main, int instances) {
    return deployVerticleTo(deploymentID, groupID, main, null, instances, null);
  }

  @Override
  public Cluster deployVerticleTo(String deploymentID, String groupID, String main, JsonObject config, int instances) {
    return deployVerticleTo(deploymentID, groupID, main, config, instances, null);
  }

  @Override
  public Cluster deployVerticleTo(String deploymentID, String groupID, String main, Handler<AsyncResult<String>> doneHandler) {
    return deployVerticleTo(deploymentID, groupID, main, null, 1, doneHandler);
  }

  @Override
  public Cluster deployVerticleTo(String deploymentID, String groupID, String main, JsonObject config, Handler<AsyncResult<String>> doneHandler) {
    return deployVerticleTo(deploymentID, groupID, main, config, 1, doneHandler);
  }

  @Override
  public Cluster deployVerticleTo(String deploymentID, String groupID, String main, int instances, Handler<AsyncResult<String>> doneHandler) {
    return deployVerticleTo(deploymentID, groupID, main, null, instances, doneHandler);
  }

  @Override
  public Cluster deployVerticleTo(String deploymentID, String groupID, String main, JsonObject config, int instances, final Handler<AsyncResult<String>> doneHandler) {
    JsonObject message = new JsonObject()
        .putString("action", "deploy")
        .putString("id", deploymentID)
        .putString("group", groupID)
        .putString("type", "verticle")
        .putString("main", main)
        .putObject("config", config)
        .putNumber("instances", instances);
    vertx.eventBus().sendWithTimeout(CLUSTER_ADDRESS, message, 30000, new Handler<AsyncResult<Message<JsonObject>>>() {
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
  public Cluster deployWorkerVerticle(String deploymentID, String main) {
    return deployWorkerVerticleTo(deploymentID, null, main, null, 1, false, null);
  }

  @Override
  public Cluster deployWorkerVerticle(String deploymentID, String main, JsonObject config) {
    return deployWorkerVerticleTo(deploymentID, null, main, config, 1, false, null);
  }

  @Override
  public Cluster deployWorkerVerticle(String deploymentID, String main, int instances) {
    return deployWorkerVerticleTo(deploymentID, null, main, null, instances, false, null);
  }

  @Override
  public Cluster deployWorkerVerticle(String deploymentID, String main, JsonObject config, int instances, boolean multiThreaded) {
    return deployWorkerVerticleTo(deploymentID, null, main, config, instances, multiThreaded, null);
  }

  @Override
  public Cluster deployWorkerVerticle(String deploymentID, String main, Handler<AsyncResult<String>> doneHandler) {
    return deployWorkerVerticleTo(deploymentID, null, main, null, 1, false, doneHandler);
  }

  @Override
  public Cluster deployWorkerVerticle(String deploymentID, String main, JsonObject config, Handler<AsyncResult<String>> doneHandler) {
    return deployWorkerVerticleTo(deploymentID, null, main, config, 1, false, doneHandler);
  }

  @Override
  public Cluster deployWorkerVerticle(String deploymentID, String main, int instances, Handler<AsyncResult<String>> doneHandler) {
    return deployWorkerVerticleTo(deploymentID, null, main, null, instances, false, doneHandler);
  }

  @Override
  public Cluster deployWorkerVerticle(String deploymentID, String main, JsonObject config, int instances, boolean multiThreaded, Handler<AsyncResult<String>> doneHandler) {
    return deployWorkerVerticleTo(deploymentID, null, main, config, instances, multiThreaded, doneHandler);
  }

  @Override
  public Cluster deployWorkerVerticleTo(String deploymentID, String groupID, String main) {
    return deployWorkerVerticleTo(deploymentID, groupID, main, null, 1, false, null);
  }

  @Override
  public Cluster deployWorkerVerticleTo(String deploymentID, String groupID, String main, JsonObject config) {
    return deployWorkerVerticleTo(deploymentID, groupID, main, config, 1, false, null);
  }

  @Override
  public Cluster deployWorkerVerticleTo(String deploymentID, String groupID, String main, int instances) {
    return deployWorkerVerticleTo(deploymentID, groupID, main, null, instances, false, null);
  }

  @Override
  public Cluster deployWorkerVerticleTo(String deploymentID, String groupID, String main, JsonObject config, int instances, boolean multiThreaded) {
    return deployWorkerVerticleTo(deploymentID, groupID, main, config, instances, multiThreaded, null);
  }

  @Override
  public Cluster deployWorkerVerticleTo(String deploymentID, String groupID, String main, Handler<AsyncResult<String>> doneHandler) {
    return deployWorkerVerticleTo(deploymentID, groupID, main, null, 1, false, doneHandler);
  }

  @Override
  public Cluster deployWorkerVerticleTo(String deploymentID, String groupID, String main, JsonObject config, Handler<AsyncResult<String>> doneHandler) {
    return deployWorkerVerticleTo(deploymentID, groupID, main, config, 1, false, doneHandler);
  }

  @Override
  public Cluster deployWorkerVerticleTo(String deploymentID, String groupID, String main, int instances, Handler<AsyncResult<String>> doneHandler) {
    return deployWorkerVerticleTo(deploymentID, groupID, main, null, instances, false, doneHandler);
  }

  @Override
  public Cluster deployWorkerVerticleTo(String deploymentID, String groupID, String main, JsonObject config, int instances, boolean multiThreaded, final Handler<AsyncResult<String>> doneHandler) {
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
    vertx.eventBus().sendWithTimeout(CLUSTER_ADDRESS, message, 30000, new Handler<AsyncResult<Message<JsonObject>>>() {
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
  public Cluster undeployModule(String deploymentID, final Handler<AsyncResult<Void>> doneHandler) {
    JsonObject message = new JsonObject()
        .putString("action", "undeploy")
        .putString("id", deploymentID)
        .putString("type", "module");
    vertx.eventBus().sendWithTimeout(CLUSTER_ADDRESS, message, 30000, new Handler<AsyncResult<Message<JsonObject>>>() {
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
  public Cluster undeployVerticle(String deploymentID, final Handler<AsyncResult<Void>> doneHandler) {
    JsonObject message = new JsonObject()
        .putString("action", "undeploy")
        .putString("id", deploymentID)
        .putString("type", "verticle");
    vertx.eventBus().sendWithTimeout(CLUSTER_ADDRESS, message, 30000, new Handler<AsyncResult<Message<JsonObject>>>() {
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
    return new XyncList<T>(name, vertx);
  }

  @Override
  public <K, V> WatchableAsyncMap<K, V> getMap(String name) {
    return new XyncMap<K, V>(name, vertx);
  }

  @Override
  public <T> AsyncSet<T> getSet(String name) {
    return new XyncSet<T>(name, vertx);
  }

  @Override
  public <T> AsyncQueue<T> getQueue(String name) {
    return new XyncQueue<T>(name, vertx);
  }

  @Override
  public AsyncIdGenerator getIdGenerator(String name) {
    return new XyncIdGenerator(name, vertx);
  }

  @Override
  public AsyncLock getLock(String name) {
    return new XyncLock(name, vertx);
  }

}
