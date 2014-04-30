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
import net.kuujo.vertigo.cluster.ClusterScope;
import net.kuujo.vertigo.cluster.DeploymentException;
import net.kuujo.vertigo.cluster.LocalType;
import net.kuujo.vertigo.cluster.data.AsyncCounter;
import net.kuujo.vertigo.cluster.data.AsyncList;
import net.kuujo.vertigo.cluster.data.AsyncMap;
import net.kuujo.vertigo.cluster.data.AsyncQueue;
import net.kuujo.vertigo.cluster.data.AsyncSet;
import net.kuujo.vertigo.cluster.data.impl.SharedDataCounter;
import net.kuujo.vertigo.cluster.data.impl.SharedDataList;
import net.kuujo.vertigo.cluster.data.impl.SharedDataMap;
import net.kuujo.vertigo.cluster.data.impl.SharedDataQueue;
import net.kuujo.vertigo.cluster.data.impl.SharedDataSet;
import net.kuujo.vertigo.util.Factory;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.impl.DefaultFutureResult;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.shareddata.ConcurrentSharedMap;
import org.vertx.java.core.shareddata.SharedData;
import org.vertx.java.platform.Container;
import org.vertx.java.platform.Verticle;

/**
 * Local cluster implementation.<p>
 *
 * The local cluster simply uses the Vert.x {@link Container} for deployments
 * and {@link SharedData} for synchronization data structures.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
@LocalType
public class LocalCluster implements Cluster {
  private final Vertx vertx;
  private final Container container;
  private final ConcurrentSharedMap<String, String> deployments;
  @SuppressWarnings("rawtypes")
  private final Map<String, AsyncMap> maps = new HashMap<>();
  @SuppressWarnings("rawtypes")
  private final Map<String, AsyncList> lists = new HashMap<>();
  @SuppressWarnings("rawtypes")
  private final Map<String, AsyncQueue> queues = new HashMap<>();
  @SuppressWarnings("rawtypes")
  private final Map<String, AsyncSet> sets = new HashMap<>();
  private final Map<String, AsyncCounter> counters = new HashMap<>();

  @Factory
  public static Cluster factory(Vertx vertx, Container container) {
    return new LocalCluster(vertx, container);
  }

  public LocalCluster(Verticle verticle) {
    this(verticle.getVertx(), verticle.getContainer());
  }

  public LocalCluster(Vertx vertx, Container container) {
    this.vertx = vertx;
    this.container = container;
    this.deployments = vertx.sharedData().getMap("__deployments__");
  }

  @Override
  public String address() {
    return null;
  }

  @Override
  public ClusterScope scope() {
    return ClusterScope.LOCAL;
  }

  @Override
  public Cluster isDeployed(final String deploymentID, final Handler<AsyncResult<Boolean>> resultHandler) {
    vertx.runOnContext(new Handler<Void>() {
      @Override
      public void handle(Void _) {
        new DefaultFutureResult<Boolean>(deployments.containsKey(deploymentID)).setHandler(resultHandler);
      }
    });
    return this;
  }

  @Override
  public Cluster deployModule(String deploymentID, String moduleName) {
    return deployModule(deploymentID, moduleName, null, 1, null);
  }

  @Override
  public Cluster deployModule(String deploymentID, String moduleName, JsonObject config) {
    return deployModule(deploymentID, moduleName, config, 1, null);
  }

  @Override
  public Cluster deployModule(String deploymentID, String moduleName, int instances) {
    return deployModule(deploymentID, moduleName, null, instances, null);
  }

  @Override
  public Cluster deployModule(String deploymentID, String moduleName, JsonObject config, int instances) {
    return deployModule(deploymentID, moduleName, config, instances, null);
  }

  @Override
  public Cluster deployModule(String deploymentID, String moduleName, Handler<AsyncResult<String>> doneHandler) {
    return deployModule(deploymentID, moduleName, null, 1, doneHandler);
  }

  @Override
  public Cluster deployModule(String deploymentID, String moduleName, JsonObject config, Handler<AsyncResult<String>> doneHandler) {
    return deployModule(deploymentID, moduleName, config, 1, doneHandler);
  }

  @Override
  public Cluster deployModule(String deploymentID, String moduleName, int instances, Handler<AsyncResult<String>> doneHandler) {
    return deployModule(deploymentID, moduleName, null, instances, doneHandler);
  }

  @Override
  public Cluster deployModule(String deploymentID, String moduleName, JsonObject config, int instances, Handler<AsyncResult<String>> doneHandler) {
    return deployModule(deploymentID, moduleName, config, instances, false, doneHandler);
  }

  @Override
  public Cluster deployModule(final String deploymentID, String moduleName, JsonObject config,
      int instances, boolean ha, final Handler<AsyncResult<String>> doneHandler) {
    if (deployments.containsKey(deploymentID)) {
      vertx.runOnContext(new Handler<Void>() {
        @Override
        public void handle(Void _) {
          new DefaultFutureResult<String>(new DeploymentException("Deployment ID already exists.")).setHandler(doneHandler);
        }
      });
    } else {
      deployments.put(deploymentID, new JsonObject()
          .putString("type", "module")
          .putString("module", moduleName)
          .putObject("config", config)
          .putNumber("instances", instances).encode());
      container.deployModule(moduleName, config, instances, new Handler<AsyncResult<String>>() {
        @Override
        public void handle(AsyncResult<String> result) {
          if (result.failed()) {
            deployments.remove(deploymentID);
            new DefaultFutureResult<String>(result.cause()).setHandler(doneHandler);
          } else {
            String sdeployment = deployments.get(deploymentID);
            if (sdeployment != null) {
              JsonObject deployment = new JsonObject(sdeployment);
              deployment.putString("id", result.result());
              deployments.put(deploymentID, deployment.encode());
            }
            new DefaultFutureResult<String>(deploymentID).setHandler(doneHandler);
          }
        }
      });
    }
    return this;
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
  public Cluster deployModuleTo(String deploymentID, String groupID, String moduleName, JsonObject config, int instances, Handler<AsyncResult<String>> doneHandler) {
    return deployModule(deploymentID, moduleName, config, instances, doneHandler);
  }

  @Override
  public Cluster deployModuleTo(String deploymentID, String groupID, String moduleName, JsonObject config, int instances, boolean ha, Handler<AsyncResult<String>> doneHandler) {
    return deployModule(deploymentID, moduleName, config, instances, ha, doneHandler);
  }

  @Override
  public Cluster deployVerticle(String deploymentID, String main) {
    return deployVerticle(deploymentID, main, null, 1, null);
  }

  @Override
  public Cluster deployVerticle(String deploymentID, String main, JsonObject config) {
    return deployVerticle(deploymentID, main, config, 1, null);
  }

  @Override
  public Cluster deployVerticle(String deploymentID, String main, int instances) {
    return deployVerticle(deploymentID, main, null, instances, null);
  }

  @Override
  public Cluster deployVerticle(String deploymentID, String main, JsonObject config, int instances) {
    return deployVerticle(deploymentID, main, config, instances, null);
  }

  @Override
  public Cluster deployVerticle(String deploymentID, String main, Handler<AsyncResult<String>> doneHandler) {
    return deployVerticle(deploymentID, main, null, 1, doneHandler);
  }

  @Override
  public Cluster deployVerticle(String deploymentID, String main, JsonObject config, Handler<AsyncResult<String>> doneHandler) {
    return deployVerticle(deploymentID, main, config, 1, doneHandler);
  }

  @Override
  public Cluster deployVerticle(String deploymentID, String main, int instances, Handler<AsyncResult<String>> doneHandler) {
    return deployVerticle(deploymentID, main, null, instances, doneHandler);
  }

  @Override
  public Cluster deployVerticle(final String deploymentID, String main,
      JsonObject config, int instances, final Handler<AsyncResult<String>> doneHandler) {
    return deployVerticle(deploymentID, main, config, instances, false, doneHandler);
  }

    @Override
    public Cluster deployVerticle(final String deploymentID, String main,
        JsonObject config, int instances, boolean ha, final Handler<AsyncResult<String>> doneHandler) {
    if (deployments.containsKey(deploymentID)) {
      vertx.runOnContext(new Handler<Void>() {
        @Override
        public void handle(Void _) {
          new DefaultFutureResult<String>(new DeploymentException("Deployment ID already exists.")).setHandler(doneHandler);
        }
      });
    } else {
      deployments.put(deploymentID, new JsonObject()
          .putString("type", "verticle")
          .putString("main", main)
          .putObject("config", config)
          .putNumber("instances", instances).encode());
      container.deployVerticle(main, config, instances, new Handler<AsyncResult<String>>() {
        @Override
        public void handle(AsyncResult<String> result) {
          if (result.failed()) {
            deployments.remove(deploymentID);
            new DefaultFutureResult<String>(result.cause()).setHandler(doneHandler);
          } else {
            String sdeployment = deployments.get(deploymentID);
            if (sdeployment != null) {
              JsonObject deployment = new JsonObject(sdeployment);
              deployment.putString("id", result.result());
              deployments.put(deploymentID, deployment.encode());
            }
            new DefaultFutureResult<String>(deploymentID).setHandler(doneHandler);
          }
        }
      });
    }
    return this;
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
  public Cluster deployVerticleTo(String deploymentID, String groupID, String main, JsonObject config, int instances, Handler<AsyncResult<String>> doneHandler) {
    return deployVerticle(deploymentID, main, config, instances, doneHandler);
  }

  @Override
  public Cluster deployVerticleTo(String deploymentID, String groupID, String main, JsonObject config, int instances, boolean ha, Handler<AsyncResult<String>> doneHandler) {
    return deployVerticle(deploymentID, main, config, instances, ha, doneHandler);
  }

  @Override
  public Cluster deployWorkerVerticle(String deploymentID, String main) {
    return deployWorkerVerticle(deploymentID, main, null, 1, false, null);
  }

  @Override
  public Cluster deployWorkerVerticle(String deploymentID, String main, JsonObject config) {
    return deployWorkerVerticle(deploymentID, main, config, 1, false, null);
  }

  @Override
  public Cluster deployWorkerVerticle(String deploymentID, String main, int instances) {
    return deployWorkerVerticle(deploymentID, main, null, instances, false, null);
  }

  @Override
  public Cluster deployWorkerVerticle(String deploymentID, String main, JsonObject config, int instances, boolean multiThreaded) {
    return deployWorkerVerticle(deploymentID, main, config, instances, multiThreaded, null);
  }

  @Override
  public Cluster deployWorkerVerticle(String deploymentID, String main, Handler<AsyncResult<String>> doneHandler) {
    return deployWorkerVerticle(deploymentID, main, null, 1, false, doneHandler);
  }

  @Override
  public Cluster deployWorkerVerticle(String deploymentID, String main, JsonObject config, Handler<AsyncResult<String>> doneHandler) {
    return deployWorkerVerticle(deploymentID, main, config, 1, false, doneHandler);
  }

  @Override
  public Cluster deployWorkerVerticle(String deploymentID, String main, int instances, Handler<AsyncResult<String>> doneHandler) {
    return deployWorkerVerticle(deploymentID, main, null, instances, false, doneHandler);
  }

  @Override
  public Cluster deployWorkerVerticle(final String deploymentID, String main,
      JsonObject config, int instances, boolean multiThreaded, final Handler<AsyncResult<String>> doneHandler) {
    return deployWorkerVerticle(deploymentID, main, config, instances, multiThreaded, false, doneHandler);
  }

    @Override
    public Cluster deployWorkerVerticle(final String deploymentID, String main,
        JsonObject config, int instances, boolean multiThreaded, boolean ha, final Handler<AsyncResult<String>> doneHandler) {
    if (deployments.containsKey(deploymentID)) {
      vertx.runOnContext(new Handler<Void>() {
        @Override
        public void handle(Void _) {
          new DefaultFutureResult<String>(new DeploymentException("Deployment ID already exists.")).setHandler(doneHandler);
        }
      });
    } else {
      deployments.put(deploymentID, new JsonObject()
          .putString("type", "verticle")
          .putString("main", main)
          .putObject("config", config)
          .putNumber("instances", instances)
          .putBoolean("worker", true)
          .putBoolean("multi-threaded", multiThreaded).encode());
      container.deployWorkerVerticle(main, config, instances, multiThreaded, new Handler<AsyncResult<String>>() {
        @Override
        public void handle(AsyncResult<String> result) {
          if (result.failed()) {
            deployments.remove(deploymentID);
            new DefaultFutureResult<String>(result.cause()).setHandler(doneHandler);
          } else {
            String sdeployment = deployments.get(deploymentID);
            if (sdeployment != null) {
              JsonObject deployment = new JsonObject(sdeployment);
              deployment.putString("id", result.result());
              deployments.put(deploymentID, deployment.encode());
            }
            new DefaultFutureResult<String>(deploymentID).setHandler(doneHandler);
          }
        }
      });
    }
    return this;
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
  public Cluster deployWorkerVerticleTo(String deploymentID, String groupID, String main, JsonObject config, int instances, boolean multiThreaded, Handler<AsyncResult<String>> doneHandler) {
    return deployWorkerVerticle(deploymentID, main, config, instances, multiThreaded, doneHandler);
  }

  @Override
  public Cluster deployWorkerVerticleTo(String deploymentID, String groupID, String main, JsonObject config, int instances, boolean multiThreaded, boolean ha, Handler<AsyncResult<String>> doneHandler) {
    return deployWorkerVerticle(deploymentID, main, config, instances, multiThreaded, ha, doneHandler);
  }

  @Override
  public Cluster undeployModule(String deploymentID) {
    return undeployModule(deploymentID, null);
  }

  @Override
  public Cluster undeployModule(String deploymentID, final Handler<AsyncResult<Void>> doneHandler) {
    if (!deployments.containsKey(deploymentID)) {
      vertx.runOnContext(new Handler<Void>() {
        @Override
        public void handle(Void _) {
          new DefaultFutureResult<Void>(new DeploymentException("Invalid deployment ID.")).setHandler(doneHandler);
        }
      });
    } else {
      String sdeploymentInfo = deployments.remove(deploymentID);
      JsonObject deploymentInfo = new JsonObject(sdeploymentInfo);
      String id = deploymentInfo.getString("id");
      if (id != null) {
        container.undeployModule(id, doneHandler);
      }
    }
    return this;
  }

  @Override
  public Cluster undeployVerticle(String deploymentID) {
    return undeployVerticle(deploymentID, null);
  }

  @Override
  public Cluster undeployVerticle(String deploymentID, final Handler<AsyncResult<Void>> doneHandler) {
    if (!deployments.containsKey(deploymentID)) {
      vertx.runOnContext(new Handler<Void>() {
        @Override
        public void handle(Void _) {
          new DefaultFutureResult<Void>(new DeploymentException("Invalid deployment ID.")).setHandler(doneHandler);
        }
      });
    } else {
      String sdeploymentInfo = deployments.remove(deploymentID);
      JsonObject deploymentInfo = new JsonObject(sdeploymentInfo);
      String id = deploymentInfo.getString("id");
      if (id != null) {
        container.undeployVerticle(id, doneHandler);
      }
    }
    return this;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <K, V> AsyncMap<K, V> getMap(String name) {
    AsyncMap<K, V> map = maps.get(name);
    if (map == null) {
      map = new SharedDataMap<K, V>(name, vertx);
      maps.put(name, map);
    }
    return map;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> AsyncList<T> getList(String name) {
    AsyncList<T> list = lists.get(name);
    if (list == null) {
      list = new SharedDataList<T>(name, vertx);
      lists.put(name, list);
    }
    return list;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> AsyncSet<T> getSet(String name) {
    AsyncSet<T> set = sets.get(name);
    if (set == null) {
      set = new SharedDataSet<T>(name, vertx);
      sets.put(name, set);
    }
    return set;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> AsyncQueue<T> getQueue(String name) {
    AsyncQueue<T> queue = queues.get(name);
    if (queue == null) {
      queue = new SharedDataQueue<T>(name, vertx);
      queues.put(name, queue);
    }
    return queue;
  }

  @Override
  public AsyncCounter getCounter(String name) {
    AsyncCounter counter = counters.get(name);
    if (counter == null) {
      counter = new SharedDataCounter(name, vertx);
      counters.put(name, counter);
    }
    return counter;
  }

}
