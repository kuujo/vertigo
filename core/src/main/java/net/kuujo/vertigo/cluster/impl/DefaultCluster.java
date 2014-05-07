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
package net.kuujo.vertigo.cluster.impl;

import java.util.HashMap;
import java.util.Map;

import net.kuujo.vertigo.cluster.Cluster;
import net.kuujo.vertigo.cluster.DeploymentException;
import net.kuujo.vertigo.cluster.data.AsyncCounter;
import net.kuujo.vertigo.cluster.data.AsyncList;
import net.kuujo.vertigo.cluster.data.AsyncMap;
import net.kuujo.vertigo.cluster.data.AsyncQueue;
import net.kuujo.vertigo.cluster.data.AsyncSet;
import net.kuujo.vertigo.cluster.data.impl.DefaultAsyncCounter;
import net.kuujo.vertigo.cluster.data.impl.DefaultAsyncList;
import net.kuujo.vertigo.cluster.data.impl.DefaultAsyncMap;
import net.kuujo.vertigo.cluster.data.impl.DefaultAsyncQueue;
import net.kuujo.vertigo.cluster.data.impl.DefaultAsyncSet;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.impl.DefaultFutureResult;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Container;
import org.vertx.java.platform.Verticle;

/**
 * Default cluster implementation.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class DefaultCluster implements Cluster {
  private static final String DEFAULT_CLUSTER_ADDRESS = "vertigo";
  private final String address;
  private final Vertx vertx;
  @SuppressWarnings("rawtypes")
  private final Map<String, AsyncMap> maps = new HashMap<>();
  @SuppressWarnings("rawtypes")
  private final Map<String, AsyncList> lists = new HashMap<>();
  @SuppressWarnings("rawtypes")
  private final Map<String, AsyncQueue> queues = new HashMap<>();
  @SuppressWarnings("rawtypes")
  private final Map<String, AsyncSet> sets = new HashMap<>();
  private final Map<String, AsyncCounter> counters = new HashMap<>();

  public DefaultCluster(Verticle verticle) {
    this(DEFAULT_CLUSTER_ADDRESS, verticle);
  }

  public DefaultCluster(Vertx vertx, Container container) {
    this(DEFAULT_CLUSTER_ADDRESS, vertx, container);
  }

  public DefaultCluster(String address, Verticle verticle) {
    this(address, verticle.getVertx(), verticle.getContainer());
  }

  public DefaultCluster(String address, Vertx vertx, Container container) {
    this.address = address;
    this.vertx = vertx;
  }

  @Override
  public String address() {
    return address;
  }

  @Override
  public Cluster isDeployed(String deploymentID, final Handler<AsyncResult<Boolean>> resultHandler) {
    JsonObject message = new JsonObject()
        .putString("action", "check")
        .putString("id", deploymentID);
    vertx.eventBus().sendWithTimeout(address, message, 30000, new Handler<AsyncResult<Message<JsonObject>>>() {
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
  public Cluster deployModule(String deploymentID, String moduleName, JsonObject config, int instances, boolean ha, final Handler<AsyncResult<String>> doneHandler) {
    return deployModuleTo(deploymentID, null, moduleName, config, instances, ha, doneHandler);
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
    return deployModuleTo(deploymentID, groupID, moduleName, config, instances, false, doneHandler);
  }

  @Override
  public Cluster deployModuleTo(String deploymentID, String groupID, String moduleName, JsonObject config, int instances, boolean ha, final Handler<AsyncResult<String>> doneHandler) {
    JsonObject message = new JsonObject()
        .putString("action", "deploy")
        .putString("id", deploymentID)
        .putString("group", groupID)
        .putString("type", "module")
        .putString("module", moduleName)
        .putObject("config", config)
        .putNumber("instances", instances)
        .putBoolean("ha", ha);
    vertx.eventBus().sendWithTimeout(address, message, 30000, new Handler<AsyncResult<Message<JsonObject>>>() {
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
  public Cluster deployVerticle(String deploymentID, String main, JsonObject config, int instances, boolean ha, Handler<AsyncResult<String>> doneHandler) {
    return deployVerticleTo(deploymentID, null, main, config, instances, ha, doneHandler);
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
    return deployVerticleTo(deploymentID, groupID, main, config, instances, false, doneHandler);
  }

  @Override
  public Cluster deployVerticleTo(String deploymentID, String groupID, String main, JsonObject config, int instances, boolean ha, final Handler<AsyncResult<String>> doneHandler) {
    JsonObject message = new JsonObject()
        .putString("action", "deploy")
        .putString("id", deploymentID)
        .putString("group", groupID)
        .putString("type", "verticle")
        .putString("main", main)
        .putObject("config", config)
        .putNumber("instances", instances)
        .putBoolean("ha", ha);
    vertx.eventBus().sendWithTimeout(address, message, 30000, new Handler<AsyncResult<Message<JsonObject>>>() {
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
  public Cluster deployWorkerVerticle(String deploymentID, String main, JsonObject config, int instances, boolean multiThreaded, boolean ha, Handler<AsyncResult<String>> doneHandler) {
    return deployWorkerVerticleTo(deploymentID, null, main, config, instances, multiThreaded, ha, doneHandler);
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
    return deployWorkerVerticleTo(deploymentID, groupID, main, config, instances, multiThreaded, false, doneHandler);
  }

  @Override
  public Cluster deployWorkerVerticleTo(String deploymentID, String groupID, String main, JsonObject config, int instances, boolean multiThreaded, boolean ha, final Handler<AsyncResult<String>> doneHandler) {
    JsonObject message = new JsonObject()
        .putString("action", "deploy")
        .putString("id", deploymentID)
        .putString("group", groupID)
        .putString("type", "verticle")
        .putString("main", main)
        .putObject("config", config)
        .putNumber("instances", instances)
        .putBoolean("worker", true)
        .putBoolean("multi-threaded", multiThreaded)
        .putBoolean("ha", ha);
    vertx.eventBus().sendWithTimeout(address, message, 30000, new Handler<AsyncResult<Message<JsonObject>>>() {
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
  public Cluster undeployModule(String deploymentID) {
    return undeployModule(deploymentID, null);
  }

  @Override
  public Cluster undeployModule(String deploymentID, final Handler<AsyncResult<Void>> doneHandler) {
    JsonObject message = new JsonObject()
        .putString("action", "undeploy")
        .putString("id", deploymentID)
        .putString("type", "module");
    vertx.eventBus().sendWithTimeout(address, message, 30000, new Handler<AsyncResult<Message<JsonObject>>>() {
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
  public Cluster undeployVerticle(String deploymentID) {
    return undeployVerticle(deploymentID, null);
  }

  @Override
  public Cluster undeployVerticle(String deploymentID, final Handler<AsyncResult<Void>> doneHandler) {
    JsonObject message = new JsonObject()
        .putString("action", "undeploy")
        .putString("id", deploymentID)
        .putString("type", "verticle");
    vertx.eventBus().sendWithTimeout(address, message, 30000, new Handler<AsyncResult<Message<JsonObject>>>() {
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
  @SuppressWarnings("unchecked")
  public <K, V> AsyncMap<K, V> getMap(String name) {
    AsyncMap<K, V> map = maps.get(name);
    if (map == null) {
      map = new DefaultAsyncMap<K, V>(address, name, vertx);
      maps.put(name, map);
    }
    return map;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> AsyncList<T> getList(String name) {
    AsyncList<T> list = lists.get(name);
    if (list == null) {
      list = new DefaultAsyncList<T>(address, name, vertx);
      lists.put(name, list);
    }
    return list;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> AsyncSet<T> getSet(String name) {
    AsyncSet<T> set = sets.get(name);
    if (set == null) {
      set = new DefaultAsyncSet<T>(address, name, vertx);
      sets.put(name, set);
    }
    return set;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> AsyncQueue<T> getQueue(String name) {
    AsyncQueue<T> queue = queues.get(name);
    if (queue == null) {
      queue = new DefaultAsyncQueue<T>(address, name, vertx);
      queues.put(name, queue);
    }
    return queue;
  }

  @Override
  public AsyncCounter getCounter(String name) {
    AsyncCounter counter = counters.get(name);
    if (counter == null) {
      counter = new DefaultAsyncCounter(address, name, vertx);
      counters.put(name, counter);
    }
    return counter;
  }

  @Override
  public boolean equals(Object other) {
    return other instanceof Cluster && ((Cluster) other).address().equals(address);
  }

  @Override
  public String toString() {
    return address;
  }

}
