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
import java.util.Map;
import java.util.UUID;

import net.kuujo.vertigo.annotations.Factory;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.VertxException;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.impl.DefaultFutureResult;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Container;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Remote cluster client implementation.
 *
 * @author Jordan Halterman
 */
public class RemoteClusterClient implements ClusterClient {
  private static final String CLUSTER_ADDRESS = "__CLUSTER__";
  @JsonIgnore
  private final EventBus eventBus;
  @JsonIgnore
  private final Map<String, Map<Handler<ClusterEvent>, HandlerWrapper>> watchHandlers = new HashMap<>();

  private static class HandlerWrapper {
    private final String address;
    private final Handler<Message<JsonObject>> messageHandler;

    private HandlerWrapper(String address, Handler<Message<JsonObject>> messageHandler) {
      this.address = address;
      this.messageHandler = messageHandler;
    }
  }

  @Factory
  public static ClusterClient factory(Vertx vertx, Container container) {
    return new RemoteClusterClient(vertx);
  }

  public RemoteClusterClient(Vertx vertx) {
    this.eventBus = vertx.eventBus();
  }

  @Override
  public ClusterClient isDeployed(String deploymentID, final Handler<AsyncResult<Boolean>> resultHandler) {
    JsonObject message = new JsonObject()
        .putString("action", "check")
        .putString("id", deploymentID);
    eventBus.sendWithTimeout(CLUSTER_ADDRESS, message, 30000, new Handler<AsyncResult<Message<JsonObject>>>() {
      @Override
      public void handle(AsyncResult<Message<JsonObject>> result) {
        if (result.failed()) {
          new DefaultFutureResult<Boolean>(result.cause()).setHandler(resultHandler);
        }
        else if (result.result().body().getString("status").equals("ok")) {
          new DefaultFutureResult<Boolean>(result.result().body().getBoolean("result")).setHandler(resultHandler);
        }
        else {
          new DefaultFutureResult<Boolean>(new DeploymentException(result.result().body().getString("message"))).setHandler(resultHandler);
        }
      }
    });
    return this;
  }

  @Override
  public ClusterClient deployModule(String deploymentID, String moduleName, JsonObject config,
      int instances, final Handler<AsyncResult<String>> doneHandler) {
    return deployModuleTo(deploymentID, null, moduleName, config, instances, doneHandler);
  }

  @Override
  public ClusterClient deployModuleTo(String deploymentID, String groupID, String moduleName, JsonObject config, int instances,
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
        }
        else if (result.result().body().getString("status").equals("ok")) {
          new DefaultFutureResult<String>(result.result().body().getString("id")).setHandler(doneHandler);
        }
        else {
          new DefaultFutureResult<String>(new DeploymentException(result.result().body().getString("message"))).setHandler(doneHandler);
        }
      }
    });
    return this;
  }

  @Override
  public ClusterClient deployVerticle(String deploymentID, String main, JsonObject config, int instances,
      Handler<AsyncResult<String>> doneHandler) {
    return deployVerticleTo(deploymentID, null, main, config, instances, doneHandler);
  }

  @Override
  public ClusterClient deployVerticleTo(String deploymentID, String groupID, String main, JsonObject config,
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
        }
        else if (result.result().body().getString("status").equals("ok")) {
          new DefaultFutureResult<String>(result.result().body().getString("id")).setHandler(doneHandler);
        }
        else {
          new DefaultFutureResult<String>(new DeploymentException(result.result().body().getString("message"))).setHandler(doneHandler);
        }
      }
    });
    return this;
  }

  @Override
  public ClusterClient deployWorkerVerticle(String deploymentID, String main, JsonObject config,
      int instances, boolean multiThreaded, Handler<AsyncResult<String>> doneHandler) {
    return deployWorkerVerticleTo(deploymentID, null, main, config, instances, multiThreaded, doneHandler);
  }

  @Override
  public ClusterClient deployWorkerVerticleTo(String deploymentID, String groupID, String main,
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
        }
        else if (result.result().body().getString("status").equals("ok")) {
          new DefaultFutureResult<String>(result.result().body().getString("id")).setHandler(doneHandler);
        }
        else {
          new DefaultFutureResult<String>(new DeploymentException(result.result().body().getString("message"))).setHandler(doneHandler);
        }
      }
    });
    return this;
  }

  @Override
  public ClusterClient undeployModule(String deploymentID, final Handler<AsyncResult<Void>> doneHandler) {
    JsonObject message = new JsonObject()
        .putString("action", "undeploy")
        .putString("id", deploymentID)
        .putString("type", "module");
    eventBus.sendWithTimeout(CLUSTER_ADDRESS, message, 30000, new Handler<AsyncResult<Message<JsonObject>>>() {
      @Override
      public void handle(AsyncResult<Message<JsonObject>> result) {
        if (result.failed()) {
          new DefaultFutureResult<Void>(result.cause()).setHandler(doneHandler);
        }
        else if (result.result().body().getString("status").equals("ok")) {
          new DefaultFutureResult<Void>((Void) null).setHandler(doneHandler);
        }
        else {
          new DefaultFutureResult<Void>(new DeploymentException(result.result().body().getString("message"))).setHandler(doneHandler);
        }
      }
    });
    return this;
  }

  @Override
  public ClusterClient undeployVerticle(String deploymentID, final Handler<AsyncResult<Void>> doneHandler) {
    JsonObject message = new JsonObject()
        .putString("action", "undeploy")
        .putString("id", deploymentID)
        .putString("type", "verticle");
    eventBus.sendWithTimeout(CLUSTER_ADDRESS, message, 30000, new Handler<AsyncResult<Message<JsonObject>>>() {
      @Override
      public void handle(AsyncResult<Message<JsonObject>> result) {
        if (result.failed()) {
          new DefaultFutureResult<Void>(result.cause()).setHandler(doneHandler);
        }
        else if (result.result().body().getString("status").equals("ok")) {
          new DefaultFutureResult<Void>((Void) null).setHandler(doneHandler);
        }
        else {
          new DefaultFutureResult<Void>(new DeploymentException(result.result().body().getString("message"))).setHandler(doneHandler);
        }
      }
    });
    return this;
  }

  @Override
  public ClusterClient set(String key, Object value) {
    return set(key, value, null);
  }

  @Override
  public ClusterClient set(String key, Object value, final Handler<AsyncResult<Void>> doneHandler) {
    JsonObject message = new JsonObject()
        .putString("action", "set")
        .putString("key", key)
        .putValue("value", value);
    eventBus.sendWithTimeout(CLUSTER_ADDRESS, message, 30000, new Handler<AsyncResult<Message<JsonObject>>>() {
      @Override
      public void handle(AsyncResult<Message<JsonObject>> result) {
        if (result.failed()) {
          new DefaultFutureResult<Void>(result.cause()).setHandler(doneHandler);
        }
        else {
          String status = result.result().body().getString("status");
          if (status.equals("ok")) {
            new DefaultFutureResult<Void>((Void) null).setHandler(doneHandler);
          }
          else {
            new DefaultFutureResult<Void>(new VertxException(result.result().body().getString("message"))).setHandler(doneHandler);
          }
        }
      }
    });
    return this;
  }

  @Override
  public <T> ClusterClient get(String key, Handler<AsyncResult<T>> resultHandler) {
    return get(key, null, resultHandler);
  }

  @Override
  public <T> ClusterClient get(String key, Object def, final Handler<AsyncResult<T>> resultHandler) {
    JsonObject message = new JsonObject()
        .putString("action", "get")
        .putString("key", key)
        .putValue("default", def);
    eventBus.sendWithTimeout(CLUSTER_ADDRESS, message, 30000, new Handler<AsyncResult<Message<JsonObject>>>() {
      @Override
      @SuppressWarnings("unchecked")
      public void handle(AsyncResult<Message<JsonObject>> result) {
        if (result.failed()) {
          new DefaultFutureResult<T>(result.cause()).setHandler(resultHandler);
        }
        else {
          String status = result.result().body().getString("status");
          if (status.equals("ok")) {
            new DefaultFutureResult<T>((T) result.result().body().getValue("result")).setHandler(resultHandler);
          }
          else {
            new DefaultFutureResult<T>(new VertxException(result.result().body().getString("message"))).setHandler(resultHandler);
          }
        }
      }
    });
    return this;
  }

  @Override
  public ClusterClient delete(String key) {
    return delete(key, null);
  }

  @Override
  public ClusterClient delete(String key, final Handler<AsyncResult<Void>> doneHandler) {
    JsonObject message = new JsonObject()
        .putString("action", "delete")
        .putString("key", key);
    eventBus.sendWithTimeout(CLUSTER_ADDRESS, message, 30000, new Handler<AsyncResult<Message<JsonObject>>>() {
      @Override
      public void handle(AsyncResult<Message<JsonObject>> result) {
        if (result.failed()) {
          new DefaultFutureResult<Void>(result.cause()).setHandler(doneHandler);
        }
        else {
          String status = result.result().body().getString("status");
          if (status.equals("ok")) {
            new DefaultFutureResult<Void>((Void) null).setHandler(doneHandler);
          }
          else {
            new DefaultFutureResult<Void>(new VertxException(result.result().body().getString("message"))).setHandler(doneHandler);
          }
        }
      }
    });
    return this;
  }

  @Override
  public ClusterClient exists(String key, final Handler<AsyncResult<Boolean>> resultHandler) {
    JsonObject message = new JsonObject()
        .putString("action", "exists")
        .putString("key", key);
    eventBus.sendWithTimeout(CLUSTER_ADDRESS, message, 30000, new Handler<AsyncResult<Message<JsonObject>>>() {
      @Override
      public void handle(AsyncResult<Message<JsonObject>> result) {
        if (result.failed()) {
          new DefaultFutureResult<Boolean>(result.cause()).setHandler(resultHandler);
        }
        else {
          String status = result.result().body().getString("status");
          if (status.equals("ok")) {
            new DefaultFutureResult<Boolean>(result.result().body().getBoolean("result")).setHandler(resultHandler);
          }
          else {
            new DefaultFutureResult<Boolean>(new VertxException(result.result().body().getString("message"))).setHandler(resultHandler);
          }
        }
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
    final String id = UUID.randomUUID().toString();
    final Handler<Message<JsonObject>> watchHandler = new Handler<Message<JsonObject>>() {
      @Override
      public void handle(Message<JsonObject> message) {
        handler.handle(new ClusterEvent(ClusterEvent.Type.parse(message.body().getString("event")), message.body().getString("key"), message.body().getValue("value")));
      }
    };

    final HandlerWrapper wrapper = new HandlerWrapper(id, watchHandler);

    eventBus.registerHandler(id, watchHandler, new Handler<AsyncResult<Void>>() {
      @Override
      public void handle(AsyncResult<Void> result) {
        if (result.failed()) {
          new DefaultFutureResult<Void>(result.cause()).setHandler(doneHandler);
        }
        else {
          final Map<Handler<ClusterEvent>, HandlerWrapper> handlers;
          if (watchHandlers.containsKey(key)) {
            handlers = watchHandlers.get(key);
          }
          else {
            handlers = new HashMap<>();
            watchHandlers.put(key, handlers);
          }
          handlers.put(handler, wrapper);
          JsonObject message = new JsonObject().putString("action", "watch").putString("key", key)
              .putString("event", event != null ? event.toString() : null).putString("address", id);
          eventBus.sendWithTimeout(CLUSTER_ADDRESS, message, 30000, new Handler<AsyncResult<Message<JsonObject>>>() {
            @Override
            public void handle(AsyncResult<Message<JsonObject>> result) {
              if (result.failed()) {
                eventBus.unregisterHandler(id, watchHandler);
                handlers.remove(handler);
                new DefaultFutureResult<Void>(result.cause()).setHandler(doneHandler);
              }
              else {
                JsonObject body = result.result().body();
                if (body.getString("status").equals("error")) {
                  eventBus.unregisterHandler(id, watchHandler);
                  handlers.remove(handler);
                  new DefaultFutureResult<Void>(new VertxException(body.getString("message"))).setHandler(doneHandler);
                }
                else {
                  new DefaultFutureResult<Void>((Void) null).setHandler(doneHandler);
                }
              }
            }
          });
        }
      }
    });
    return this;
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
    final Map<Handler<ClusterEvent>, HandlerWrapper> handlers = watchHandlers.get(key);
    if (!handlers.containsKey(handler)) {
      new DefaultFutureResult<Void>((Void) null).setHandler(doneHandler);
      return this;
    }

    JsonObject message = new JsonObject().putString("action", "unwatch").putString("key", key)
        .putString("event", event != null ? event.toString() : null).putString("address", handlers.get(handler).address);
    eventBus.sendWithTimeout(CLUSTER_ADDRESS, message, 30000, new Handler<AsyncResult<Message<JsonObject>>>() {
      @Override
      public void handle(AsyncResult<Message<JsonObject>> result) {
        if (result.failed()) {
          HandlerWrapper wrapper = handlers.remove(handler);
          eventBus.unregisterHandler(wrapper.address, wrapper.messageHandler);
          new DefaultFutureResult<Void>(result.cause()).setHandler(doneHandler);
        }
        else {
          HandlerWrapper wrapper = handlers.remove(handler);
          eventBus.unregisterHandler(wrapper.address, wrapper.messageHandler, doneHandler);
        }
      }
    });
    return this;
  }

}
