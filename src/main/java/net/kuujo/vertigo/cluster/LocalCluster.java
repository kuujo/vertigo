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
import net.kuujo.vertigo.annotations.LocalType;
import net.kuujo.vertigo.data.AsyncIdGenerator;
import net.kuujo.vertigo.data.AsyncList;
import net.kuujo.vertigo.data.AsyncLock;
import net.kuujo.vertigo.data.AsyncQueue;
import net.kuujo.vertigo.data.AsyncSet;
import net.kuujo.vertigo.data.WatchableAsyncMap;
import net.kuujo.vertigo.data.impl.SharedDataIdGenerator;
import net.kuujo.vertigo.data.impl.SharedDataList;
import net.kuujo.vertigo.data.impl.SharedDataLock;
import net.kuujo.vertigo.data.impl.SharedDataMap;
import net.kuujo.vertigo.data.impl.SharedDataQueue;
import net.kuujo.vertigo.data.impl.SharedDataSet;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.impl.DefaultFutureResult;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.shareddata.ConcurrentSharedMap;
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
public class LocalCluster implements VertigoCluster {
  private final Vertx vertx;
  private final Container container;
  private final ConcurrentSharedMap<String, String> deployments;

  @Factory
  public static VertigoCluster factory(Vertx vertx, Container container) {
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
  public VertigoCluster isDeployed(final String deploymentID, final Handler<AsyncResult<Boolean>> resultHandler) {
    vertx.runOnContext(new Handler<Void>() {
      @Override
      public void handle(Void _) {
        new DefaultFutureResult<Boolean>(deployments.containsKey(deploymentID)).setHandler(resultHandler);
      }
    });
    return this;
  }

  @Override
  public VertigoCluster deployModule(final String deploymentID, String moduleName, JsonObject config,
      int instances, final Handler<AsyncResult<String>> doneHandler) {
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
  public VertigoCluster deployModuleTo(String deploymentID, String groupID, String moduleName, JsonObject config, int instances, Handler<AsyncResult<String>> doneHandler) {
    return deployModule(deploymentID, moduleName, config, instances, doneHandler);
  }

  @Override
  public VertigoCluster deployVerticle(final String deploymentID, String main,
      JsonObject config, int instances, final Handler<AsyncResult<String>> doneHandler) {
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
  public VertigoCluster deployVerticleTo(String deploymentID, String groupID, String main, JsonObject config, int instances, Handler<AsyncResult<String>> doneHandler) {
    return deployVerticle(deploymentID, main, config, instances, doneHandler);
  }

  @Override
  public VertigoCluster deployWorkerVerticle(final String deploymentID, String main,
      JsonObject config, int instances, boolean multiThreaded, final Handler<AsyncResult<String>> doneHandler) {
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
  public VertigoCluster deployWorkerVerticleTo(String deploymentID, String groupID, String main, JsonObject config, int instances, boolean multiThreaded, Handler<AsyncResult<String>> doneHandler) {
    return deployWorkerVerticle(deploymentID, main, config, instances, multiThreaded, doneHandler);
  }

  @Override
  public VertigoCluster undeployModule(String deploymentID, final Handler<AsyncResult<Void>> doneHandler) {
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
  public VertigoCluster undeployVerticle(String deploymentID, final Handler<AsyncResult<Void>> doneHandler) {
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
  public <K, V> WatchableAsyncMap<K, V> getMap(String name) {
    return SharedDataMap.factory(name, vertx);
  }

  @Override
  public <T> AsyncList<T> getList(String name) {
    return SharedDataList.factory(name, vertx);
  }

  @Override
  public <T> AsyncSet<T> getSet(String name) {
    return SharedDataSet.factory(name, vertx);
  }

  @Override
  public <T> AsyncQueue<T> getQueue(String name) {
    return SharedDataQueue.factory(name, vertx);
  }

  @Override
  public AsyncIdGenerator getIdGenerator(String name) {
    return SharedDataIdGenerator.factory(name, vertx);
  }

  @Override
  public AsyncLock getLock(String name) {
    return SharedDataLock.factory(name, vertx);
  }

}
