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
package net.kuujo.vertigo.coordinator.impl;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import net.kuujo.vertigo.VertigoException;
import net.kuujo.vertigo.annotations.Factory;
import net.kuujo.vertigo.coordinator.ClusterCoordinator;
import net.kuujo.vertigo.coordinator.ClusterEvent;
import net.kuujo.vertigo.coordinator.DeploymentException;
import net.kuujo.vertigo.coordinator.ClusterEvent.Type;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.impl.DefaultFutureResult;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.shareddata.ConcurrentSharedMap;
import org.vertx.java.platform.Container;

/**
 * Local cluster coordinator implementation.
 *
 * @author Jordan Halterman
 */
public class LocalClusterCoordinator implements ClusterCoordinator {
  private final Vertx vertx;
  private final Container container;
  private final ConcurrentSharedMap<String, JsonObject> deployments;
  private final ConcurrentSharedMap<String, Object> data;
  private final ConcurrentSharedMap<String, JsonObject> watchers;
  private final Map<String, Handler<ClusterEvent>> watchHandlers = new HashMap<>();
  private final Map<Handler<ClusterEvent>, String> handlerMap = new HashMap<>();

  @Factory
  public static ClusterCoordinator factory(Vertx vertx, Container container) {
    return new LocalClusterCoordinator(vertx, container);
  }

  public LocalClusterCoordinator(Vertx vertx, Container container) {
    this.vertx = vertx;
    this.container = container;
    this.deployments = vertx.sharedData().getMap("__deployments__");
    this.data = vertx.sharedData().getMap("__data__");
    this.watchers = vertx.sharedData().getMap("__watchers__");
  }

  @Override
  public ClusterCoordinator isDeployed(final String deploymentID, final Handler<AsyncResult<Boolean>> resultHandler) {
    vertx.runOnContext(new Handler<Void>() {
      @Override
      public void handle(Void _) {
        new DefaultFutureResult<Boolean>(deployments.containsKey(deploymentID)).setHandler(resultHandler);
      }
    });
    return this;
  }

  @Override
  public ClusterCoordinator deployModule(final String deploymentID, String moduleName, JsonObject config,
      int instances, final Handler<AsyncResult<String>> doneHandler) {
    if (deployments.containsKey(deploymentID)) {
      vertx.runOnContext(new Handler<Void>() {
        @Override
        public void handle(Void _) {
          new DefaultFutureResult<String>(new DeploymentException("Deployment ID already exists.")).setHandler(doneHandler);
        }
      });
    }
    else {
      deployments.put(deploymentID, new JsonObject()
          .putString("type", "module")
          .putString("module", moduleName)
          .putObject("config", config)
          .putNumber("instances", instances));
      container.deployModule(moduleName, config, instances, new Handler<AsyncResult<String>>() {
        @Override
        public void handle(AsyncResult<String> result) {
          if (result.failed()) {
            deployments.remove(deploymentID);
          }
          else {
            deployments.get(deploymentID).putString("id", result.result());
            new DefaultFutureResult<String>(deploymentID).setHandler(doneHandler);
          }
        }
      });
    }
    return this;
  }

  @Override
  public ClusterCoordinator deployVerticle(final String deploymentID, String main,
      JsonObject config, int instances, final Handler<AsyncResult<String>> doneHandler) {
    if (deployments.containsKey(deploymentID)) {
      vertx.runOnContext(new Handler<Void>() {
        @Override
        public void handle(Void _) {
          new DefaultFutureResult<String>(new DeploymentException("Deployment ID already exists.")).setHandler(doneHandler);
        }
      });
    }
    else {
      deployments.put(deploymentID, new JsonObject()
          .putString("type", "verticle")
          .putString("main", main)
          .putObject("config", config)
          .putNumber("instances", instances));
      container.deployVerticle(main, config, instances, new Handler<AsyncResult<String>>() {
        @Override
        public void handle(AsyncResult<String> result) {
          if (result.failed()) {
            deployments.remove(deploymentID);
          }
          else {
            deployments.get(deploymentID).putString("id", result.result());
            new DefaultFutureResult<String>(deploymentID).setHandler(doneHandler);
          }
        }
      });
    }
    return this;
  }

  @Override
  public ClusterCoordinator deployWorkerVerticle(final String deploymentID, String main,
      JsonObject config, int instances, boolean multiThreaded, final Handler<AsyncResult<String>> doneHandler) {
    if (deployments.containsKey(deploymentID)) {
      vertx.runOnContext(new Handler<Void>() {
        @Override
        public void handle(Void _) {
          new DefaultFutureResult<String>(new DeploymentException("Deployment ID already exists.")).setHandler(doneHandler);
        }
      });
    }
    else {
      deployments.put(deploymentID, new JsonObject()
          .putString("type", "verticle")
          .putString("main", main)
          .putObject("config", config)
          .putNumber("instances", instances)
          .putBoolean("worker", true)
          .putBoolean("multi-threaded", multiThreaded));
      container.deployWorkerVerticle(main, config, instances, multiThreaded, new Handler<AsyncResult<String>>() {
        @Override
        public void handle(AsyncResult<String> result) {
          if (result.failed()) {
            deployments.remove(deploymentID);
          }
          else {
            deployments.get(deploymentID).putString("id", result.result());
            new DefaultFutureResult<String>(deploymentID).setHandler(doneHandler);
          }
        }
      });
    }
    return this;
  }

  @Override
  public ClusterCoordinator undeployModule(String deploymentID, final Handler<AsyncResult<Void>> doneHandler) {
    if (!deployments.containsKey(deploymentID)) {
      vertx.runOnContext(new Handler<Void>() {
        @Override
        public void handle(Void _) {
          new DefaultFutureResult<Void>(new DeploymentException("Invalid deployment ID.")).setHandler(doneHandler);
        }
      });
    }
    else {
      JsonObject deploymentInfo = deployments.remove(deploymentID);
      String id = deploymentInfo.getString("id");
      if (id != null) {
        container.undeployModule(id, doneHandler);
      }
    }
    return this;
  }

  @Override
  public ClusterCoordinator undeployVerticle(String deploymentID, final Handler<AsyncResult<Void>> doneHandler) {
    if (!deployments.containsKey(deploymentID)) {
      vertx.runOnContext(new Handler<Void>() {
        @Override
        public void handle(Void _) {
          new DefaultFutureResult<Void>(new DeploymentException("Invalid deployment ID.")).setHandler(doneHandler);
        }
      });
    }
    else {
      JsonObject deploymentInfo = deployments.remove(deploymentID);
      String id = deploymentInfo.getString("id");
      if (id != null) {
        container.undeployVerticle(id, doneHandler);
      }
    }
    return this;
  }

  @Override
  public ClusterCoordinator set(String key, Object value) {
    return set(key, value, null);
  }

  @Override
  public ClusterCoordinator set(final String key, final Object value, final Handler<AsyncResult<Void>> doneHandler) {
    vertx.runOnContext(new Handler<Void>() {
      @Override
      public void handle(Void _) {
        data.put(key, value);
        new DefaultFutureResult<Void>((Void) null).setHandler(doneHandler);
      }
    });
    return this;
  }

  @Override
  public <T> ClusterCoordinator get(String key, Handler<AsyncResult<T>> resultHandler) {
    return get(key, null, resultHandler);
  }

  @Override
  public <T> ClusterCoordinator get(final String key, final Object def, final Handler<AsyncResult<T>> resultHandler) {
    vertx.runOnContext(new Handler<Void>() {
      @SuppressWarnings("unchecked")
      @Override
      public void handle(Void _) {
        Object value = data.get(key);
        if (value == null) {
          value = def;
        }
        new DefaultFutureResult<T>((T) value).setHandler(resultHandler);
      }
    });
    return this;
  }

  @Override
  public ClusterCoordinator delete(String key) {
    return delete(key, null);
  }

  @Override
  public ClusterCoordinator delete(final String key, final Handler<AsyncResult<Void>> doneHandler) {
    vertx.runOnContext(new Handler<Void>() {
      @Override
      public void handle(Void _) {
        data.remove(key);
        new DefaultFutureResult<Void>((Void) null).setHandler(doneHandler);
      }
    });
    return this;
  }

  @Override
  public ClusterCoordinator exists(final String key, final Handler<AsyncResult<Boolean>> resultHandler) {
    vertx.runOnContext(new Handler<Void>() {
      @Override
      public void handle(Void _) {
        new DefaultFutureResult<Boolean>(data.containsKey(key)).setHandler(resultHandler);
      }
    });
    return this;
  }

  @Override
  public ClusterCoordinator watch(String key, Handler<ClusterEvent> handler) {
    return watch(key, null, handler, null);
  }

  @Override
  public ClusterCoordinator watch(String key, Type event, Handler<ClusterEvent> handler) {
    return watch(key, event, handler, null);
  }

  @Override
  public ClusterCoordinator watch(String key, Handler<ClusterEvent> handler, Handler<AsyncResult<Void>> doneHandler) {
    return watch(key, null, handler, doneHandler);
  }

  @Override
  public ClusterCoordinator watch(final String key, final Type event, final Handler<ClusterEvent> handler, final Handler<AsyncResult<Void>> doneHandler) {
    vertx.runOnContext(new Handler<Void>() {
      @Override
      public void handle(Void _) {
        JsonObject watchers = LocalClusterCoordinator.this.watchers.get(key);
        if (watchers == null) {
          watchers = new JsonObject();
        }

        final String address = UUID.randomUUID().toString();
        if (event == null) {
          addWatcher(watchers, ClusterEvent.Type.CREATE, address);
          addWatcher(watchers, ClusterEvent.Type.UPDATE, address);
          addWatcher(watchers, ClusterEvent.Type.CHANGE, address);
          addWatcher(watchers, ClusterEvent.Type.DELETE, address);
        }
        else {
          addWatcher(watchers, event, address);
        }
        watchHandlers.put(address, handler);
        handlerMap.put(handler, address);
        new DefaultFutureResult<Void>((Void) null).setHandler(doneHandler);
      }
    });
    return this;
  }

  private void addWatcher(JsonObject watchers, ClusterEvent.Type event, String address) {
    JsonArray addresses = watchers.getArray(event.toString());
    if (addresses == null) {
      addresses = new JsonArray();
      watchers.putArray(event.toString(), addresses);
    }
    if (!addresses.contains(address)) {
      addresses.add(address);
    }
  }

  @Override
  public ClusterCoordinator unwatch(String key, Handler<ClusterEvent> handler) {
    return unwatch(key, null, handler, null);
  }

  @Override
  public ClusterCoordinator unwatch(String key, ClusterEvent.Type event, Handler<ClusterEvent> handler) {
    return unwatch(key, event, handler, null);
  }

  @Override
  public ClusterCoordinator unwatch(String key, Handler<ClusterEvent> handler, Handler<AsyncResult<Void>> doneHandler) {
    return unwatch(key, null, handler, doneHandler);
  }

  @Override
  public ClusterCoordinator unwatch(final String key, final ClusterEvent.Type event, final Handler<ClusterEvent> handler, final Handler<AsyncResult<Void>> doneHandler) {
    vertx.runOnContext(new Handler<Void>() {
      @Override
      public void handle(Void _) {
        if (handlerMap.containsKey(handler)) {
          String address = handlerMap.remove(handler);
          JsonObject watchers = LocalClusterCoordinator.this.watchers.get(key);
          if (watchers == null) {
            watchers = new JsonObject();
          }
          if (event == null) {
            removeWatcher(watchers, ClusterEvent.Type.CREATE, address);
            removeWatcher(watchers, ClusterEvent.Type.UPDATE, address);
            removeWatcher(watchers, ClusterEvent.Type.CHANGE, address);
            removeWatcher(watchers, ClusterEvent.Type.DELETE, address);
          }
          else {
            removeWatcher(watchers, event, address);
          }
          watchHandlers.remove(address);
        }
        else {
          new DefaultFutureResult<Void>(new VertigoException("Handler not registered."));
        }
      }
    });
    return this;
  }

  private void removeWatcher(JsonObject watchers, ClusterEvent.Type event, String address) {
    JsonArray addresses = watchers.getArray(event.toString());
    if (addresses == null) {
      addresses = new JsonArray();
      watchers.putArray(event.toString(), addresses);
    }
    Iterator<Object> iter = addresses.iterator();
    while (iter.hasNext()) {
      if (iter.next().equals(address)) {
        iter.remove();
      }
    }
    if (addresses.size() == 0) {
      watchers.removeField(event.toString());
    }
  }

}
