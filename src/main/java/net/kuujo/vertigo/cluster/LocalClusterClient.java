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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import net.kuujo.vertigo.VertigoException;
import net.kuujo.vertigo.annotations.Factory;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.impl.DefaultFutureResult;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.shareddata.ConcurrentSharedMap;
import org.vertx.java.platform.Container;

/**
 * Local cluster client implementation.
 *
 * @author Jordan Halterman
 */
public class LocalClusterClient implements ClusterClient {
  private final Vertx vertx;
  private final Container container;
  private final ConcurrentSharedMap<String, String> deployments;
  private final ConcurrentSharedMap<String, Object> data;
  private final ConcurrentSharedMap<String, String> watchers;
  private final Map<Handler<ClusterEvent>, String> watchAddresses = new HashMap<>();
  private final Map<String, Handler<Message<JsonObject>>> messageHandlers = new HashMap<>();

  @Factory
  public static ClusterClient factory(Vertx vertx, Container container) {
    return new LocalClusterClient(vertx, container);
  }

  public LocalClusterClient(Vertx vertx, Container container) {
    this.vertx = vertx;
    this.container = container;
    this.deployments = vertx.sharedData().getMap("__deployments__");
    this.data = vertx.sharedData().getMap("__data__");
    this.watchers = vertx.sharedData().getMap("__watchers__");
  }

  @Override
  public ClusterClient isDeployed(final String deploymentID, final Handler<AsyncResult<Boolean>> resultHandler) {
    vertx.runOnContext(new Handler<Void>() {
      @Override
      public void handle(Void _) {
        new DefaultFutureResult<Boolean>(deployments.containsKey(deploymentID)).setHandler(resultHandler);
      }
    });
    return this;
  }

  @Override
  public ClusterClient deployModule(final String deploymentID, String moduleName, JsonObject config,
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
          .putNumber("instances", instances).encode());
      container.deployModule(moduleName, config, instances, new Handler<AsyncResult<String>>() {
        @Override
        public void handle(AsyncResult<String> result) {
          if (result.failed()) {
            deployments.remove(deploymentID);
            new DefaultFutureResult<String>(result.cause()).setHandler(doneHandler);
          }
          else {
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
  public ClusterClient deployModuleTo(String deploymentID, String groupID, String moduleName, JsonObject config, int instances, Handler<AsyncResult<String>> doneHandler) {
    return deployModule(deploymentID, moduleName, config, instances, doneHandler);
  }

  @Override
  public ClusterClient deployVerticle(final String deploymentID, String main,
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
          .putNumber("instances", instances).encode());
      container.deployVerticle(main, config, instances, new Handler<AsyncResult<String>>() {
        @Override
        public void handle(AsyncResult<String> result) {
          if (result.failed()) {
            deployments.remove(deploymentID);
            new DefaultFutureResult<String>(result.cause()).setHandler(doneHandler);
          }
          else {
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
  public ClusterClient deployVerticleTo(String deploymentID, String groupID, String main, JsonObject config, int instances, Handler<AsyncResult<String>> doneHandler) {
    return deployVerticle(deploymentID, main, config, instances, doneHandler);
  }

  @Override
  public ClusterClient deployWorkerVerticle(final String deploymentID, String main,
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
          .putBoolean("multi-threaded", multiThreaded).encode());
      container.deployWorkerVerticle(main, config, instances, multiThreaded, new Handler<AsyncResult<String>>() {
        @Override
        public void handle(AsyncResult<String> result) {
          if (result.failed()) {
            deployments.remove(deploymentID);
            new DefaultFutureResult<String>(result.cause()).setHandler(doneHandler);
          }
          else {
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
  public ClusterClient deployWorkerVerticleTo(String deploymentID, String groupID, String main, JsonObject config, int instances, boolean multiThreaded, Handler<AsyncResult<String>> doneHandler) {
    return deployWorkerVerticle(deploymentID, main, config, instances, multiThreaded, doneHandler);
  }

  @Override
  public ClusterClient undeployModule(String deploymentID, final Handler<AsyncResult<Void>> doneHandler) {
    if (!deployments.containsKey(deploymentID)) {
      vertx.runOnContext(new Handler<Void>() {
        @Override
        public void handle(Void _) {
          new DefaultFutureResult<Void>(new DeploymentException("Invalid deployment ID.")).setHandler(doneHandler);
        }
      });
    }
    else {
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
  public ClusterClient undeployVerticle(String deploymentID, final Handler<AsyncResult<Void>> doneHandler) {
    if (!deployments.containsKey(deploymentID)) {
      vertx.runOnContext(new Handler<Void>() {
        @Override
        public void handle(Void _) {
          new DefaultFutureResult<Void>(new DeploymentException("Invalid deployment ID.")).setHandler(doneHandler);
        }
      });
    }
    else {
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
  public ClusterClient set(String key, Object value) {
    return set(key, value, null);
  }

  @Override
  public ClusterClient set(final String key, final Object value, final Handler<AsyncResult<Void>> doneHandler) {
    vertx.runOnContext(new Handler<Void>() {
      @Override
      public void handle(Void _) {
        if (!data.containsKey(key)) {
          data.put(key, value);
          triggerEvent(ClusterEvent.Type.CHANGE, key, value);
          triggerEvent(ClusterEvent.Type.CREATE, key, value);
        }
        else {
          data.put(key, value);
          triggerEvent(ClusterEvent.Type.CHANGE, key, value);
          triggerEvent(ClusterEvent.Type.UPDATE, key, value);
        }
        new DefaultFutureResult<Void>((Void) null).setHandler(doneHandler);
      }
    });
    return this;
  }

  @Override
  public <T> ClusterClient get(String key, Handler<AsyncResult<T>> resultHandler) {
    return get(key, null, resultHandler);
  }

  @Override
  public <T> ClusterClient get(final String key, final Object def, final Handler<AsyncResult<T>> resultHandler) {
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
  public ClusterClient delete(String key) {
    return delete(key, null);
  }

  @Override
  public ClusterClient delete(final String key, final Handler<AsyncResult<Void>> doneHandler) {
    vertx.runOnContext(new Handler<Void>() {
      @Override
      public void handle(Void _) {
        Object value = data.remove(key);
        triggerEvent(ClusterEvent.Type.CHANGE, key, value);
        triggerEvent(ClusterEvent.Type.DELETE, key, value);
        new DefaultFutureResult<Void>((Void) null).setHandler(doneHandler);
      }
    });
    return this;
  }

  @Override
  public ClusterClient exists(final String key, final Handler<AsyncResult<Boolean>> resultHandler) {
    vertx.runOnContext(new Handler<Void>() {
      @Override
      public void handle(Void _) {
        new DefaultFutureResult<Boolean>(data.containsKey(key)).setHandler(resultHandler);
      }
    });
    return this;
  }

  @Override
  public ClusterClient watch(String key, Handler<ClusterEvent> handler) {
    return watch(key, null, handler, null);
  }

  @Override
  public ClusterClient watch(String key, ClusterEvent.Type event, Handler<ClusterEvent> handler) {
    return watch(key, event, handler, null);
  }

  @Override
  public ClusterClient watch(String key, Handler<ClusterEvent> handler, Handler<AsyncResult<Void>> doneHandler) {
    return watch(key, null, handler, doneHandler);
  }

  @Override
  public ClusterClient watch(final String key, final ClusterEvent.Type event, final Handler<ClusterEvent> handler, final Handler<AsyncResult<Void>> doneHandler) {
    vertx.runOnContext(new Handler<Void>() {
      @Override
      public void handle(Void _) {
        String swatchers = LocalClusterClient.this.watchers.get(key);
        JsonObject watchers = swatchers != null ? new JsonObject(swatchers) : null;
        if (swatchers == null) {
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
        final Handler<Message<JsonObject>> messageHandler = new Handler<Message<JsonObject>>() {
          @Override
          public void handle(Message<JsonObject> message) {
            handler.handle(new ClusterEvent(ClusterEvent.Type.parse(message.body().getString("type")), message.body().getString("key"), message.body().getValue("value")));
          }
        };
        vertx.eventBus().registerLocalHandler(address, messageHandler);
        LocalClusterClient.this.watchers.put(key, watchers.encode());
        messageHandlers.put(address, messageHandler);
        watchAddresses.put(handler, address);
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
  public ClusterClient unwatch(String key, Handler<ClusterEvent> handler) {
    return unwatch(key, null, handler, null);
  }

  @Override
  public ClusterClient unwatch(String key, ClusterEvent.Type event, Handler<ClusterEvent> handler) {
    return unwatch(key, event, handler, null);
  }

  @Override
  public ClusterClient unwatch(String key, Handler<ClusterEvent> handler, Handler<AsyncResult<Void>> doneHandler) {
    return unwatch(key, null, handler, doneHandler);
  }

  @Override
  public ClusterClient unwatch(final String key, final ClusterEvent.Type event, final Handler<ClusterEvent> handler, final Handler<AsyncResult<Void>> doneHandler) {
    vertx.runOnContext(new Handler<Void>() {
      @Override
      public void handle(Void _) {
        if (watchAddresses.containsKey(handler)) {
          String address = watchAddresses.remove(handler);
          String swatchers = LocalClusterClient.this.watchers.get(key);
          JsonObject watchers = swatchers != null ? new JsonObject(swatchers) : null;
          if (swatchers == null) {
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
          Handler<Message<JsonObject>> messageHandler = messageHandlers.remove(address);
          if (messageHandler != null) {
            vertx.eventBus().unregisterHandler(address, messageHandler);
          }
          LocalClusterClient.this.watchers.put(key, watchers.encode());
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

  /**
   * Triggers a cluster event.
   */
  private void triggerEvent(ClusterEvent.Type type, String key, Object value) {
    String swatchers = this.watchers.get(key);
    JsonObject watchers = swatchers != null ? new JsonObject(swatchers) : null;
    if (swatchers == null) {
      watchers = new JsonObject();
    }
    JsonArray addresses = watchers.getArray(type.toString());
    if (addresses != null) {
      for (Object address : addresses) {
        vertx.eventBus().send((String) address, new JsonObject().putString("type", type.toString()).putString("key", key).putValue("value", value));
      }
    }
  }

}
