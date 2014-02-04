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
package net.kuujo.vertigo.cluster;

import java.util.Set;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Future;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.impl.DefaultFutureResult;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.impl.LoggerFactory;
import org.vertx.java.platform.Container;
import org.vertx.java.platform.Verticle;

/**
 * A remote cluster implementation.<p>
 *
 * The remote cluster deploys networks to a cluster of Vert.x instances using
 * standardized event bus actions.
 *
 * @author Jordan Halterman
 */
public class RemoteCluster extends AbstractCluster {
  private final String address;

  public RemoteCluster(String address, EventBus eventBus) {
    super(eventBus, LoggerFactory.getLogger(String.format("%s-%s", RemoteCluster.class.getCanonicalName(), address)));
    this.address = address;
  }

  public RemoteCluster(String address, Vertx vertx) {
    this(address, vertx.eventBus());
  }

  public RemoteCluster(String address, Verticle verticle) {
    this(address, verticle.getVertx());
  }

  @Deprecated
  public RemoteCluster(Vertx vertx, Container container, String address) {
    this(address, vertx);
  }

  @Override
  protected void deployVerticle(String main, JsonObject config, int instances, Handler<AsyncResult<String>> doneHandler) {
    final Future<String> future = new DefaultFutureResult<String>().setHandler(doneHandler);
    eventBus.sendWithTimeout(address, new JsonObject().putString("action", "deploy")
        .putString("type", "verticle").putString("main", main)
        .putNumber("instances", instances).putObject("config", config), 15000, new Handler<AsyncResult<Message<JsonObject>>>() {
      @Override
      public void handle(AsyncResult<Message<JsonObject>> result) {
        if (result.failed()) {
          future.setFailure(result.cause());
        }
        else {
          final String status = result.result().body().getString("status");
          if (status.equals("ok")) {
            final String id = result.result().body().getString("id");
            if (id != null) {
              future.setResult(id);
            }
            else {
              future.setFailure(new DeploymentException("No deployment ID provided."));
            }
          }
          else {
            future.setFailure(new DeploymentException(result.result().body().getString("message")));
          }
        }
      }
    });
  }

  @Override
  protected void deployVerticleTo(Set<String> nodes, String main, JsonObject config, int instances,
      Handler<AsyncResult<String>> doneHandler) {
    final Future<String> future = new DefaultFutureResult<String>().setHandler(doneHandler);
    final JsonArray nodeList = new JsonArray(nodes.toArray());
    eventBus.sendWithTimeout(address, new JsonObject().putString("action", "deploy")
        .putString("type", "verticle").putString("main", main).putNumber("instances", instances)
        .putObject("config", config).putArray("targets", nodeList), 15000, new Handler<AsyncResult<Message<JsonObject>>>() {
      @Override
      public void handle(AsyncResult<Message<JsonObject>> result) {
        if (result.failed()) {
          future.setFailure(result.cause());
        }
        else {
          final String status = result.result().body().getString("status");
          if (status.equals("ok")) {
            final String id = result.result().body().getString("id");
            if (id != null) {
              future.setResult(id);
            }
            else {
              future.setFailure(new DeploymentException("No deployment ID provided."));
            }
          }
          else {
            future.setFailure(new DeploymentException(result.result().body().getString("message")));
          }
        }
      }
    });
  }

  @Override
  protected void deployWorkerVerticle(String main, JsonObject config, int instances, boolean multiThreaded,
      Handler<AsyncResult<String>> doneHandler) {
    final Future<String> future = new DefaultFutureResult<String>().setHandler(doneHandler);
    eventBus.sendWithTimeout(address, new JsonObject().putString("action", "deploy")
        .putString("type", "verticle").putString("main", main).putBoolean("worker", true)
        .putBoolean("multi-threaded", multiThreaded).putNumber("instances", instances).putObject("config", config),
        15000, new Handler<AsyncResult<Message<JsonObject>>>() {
      @Override
      public void handle(AsyncResult<Message<JsonObject>> result) {
        if (result.failed()) {
          future.setFailure(result.cause());
        }
        else {
          final String status = result.result().body().getString("status");
          if (status.equals("ok")) {
            final String id = result.result().body().getString("id");
            if (id != null) {
              future.setResult(id);
            }
            else {
              future.setFailure(new DeploymentException("No deployment ID provided."));
            }
          }
          else {
            future.setFailure(new DeploymentException(result.result().body().getString("message")));
          }
        }
      }
    });
  }

  @Override
  protected void deployWorkerVerticleTo(Set<String> nodes, String main, JsonObject config, int instances,
      boolean multiThreaded, Handler<AsyncResult<String>> doneHandler) {
    final Future<String> future = new DefaultFutureResult<String>().setHandler(doneHandler);
    final JsonArray nodeList = new JsonArray(nodes.toArray());
    eventBus.sendWithTimeout(address, new JsonObject().putString("action", "deploy")
        .putString("type", "verticle").putString("main", main).putBoolean("worker", true)
        .putBoolean("multi-threaded", multiThreaded).putNumber("instances", instances)
        .putObject("config", config).putArray("targets", nodeList), 15000, new Handler<AsyncResult<Message<JsonObject>>>() {
      @Override
      public void handle(AsyncResult<Message<JsonObject>> result) {
        if (result.failed()) {
          future.setFailure(result.cause());
        }
        else {
          final String status = result.result().body().getString("status");
          if (status.equals("ok")) {
            final String id = result.result().body().getString("id");
            if (id != null) {
              future.setResult(id);
            }
            else {
              future.setFailure(new DeploymentException("No deployment ID provided."));
            }
          }
          else {
            future.setFailure(new DeploymentException(result.result().body().getString("message")));
          }
        }
      }
    });
  }

  @Override
  protected void deployModule(String moduleName, JsonObject config, int instances, Handler<AsyncResult<String>> doneHandler) {
    final Future<String> future = new DefaultFutureResult<String>().setHandler(doneHandler);
    eventBus.sendWithTimeout(address, new JsonObject().putString("action", "deploy")
        .putString("type", "module").putString("module", moduleName)
        .putNumber("instances", instances).putObject("config", config), 15000, new Handler<AsyncResult<Message<JsonObject>>>() {
      @Override
      public void handle(AsyncResult<Message<JsonObject>> result) {
        if (result.failed()) {
          future.setFailure(result.cause());
        }
        else {
          final String status = result.result().body().getString("status");
          if (status.equals("ok")) {
            final String id = result.result().body().getString("id");
            if (id != null) {
              future.setResult(id);
            }
            else {
              future.setFailure(new DeploymentException("No deployment ID provided."));
            }
          }
          else {
            future.setFailure(new DeploymentException(result.result().body().getString("message")));
          }
        }
      }
    });
  }

  @Override
  protected void deployModuleTo(Set<String> nodes, String moduleName, JsonObject config, int instances,
      Handler<AsyncResult<String>> doneHandler) {
    final Future<String> future = new DefaultFutureResult<String>().setHandler(doneHandler);
    final JsonArray nodeList = new JsonArray(nodes.toArray());
    eventBus.sendWithTimeout(address, new JsonObject().putString("action", "deploy")
        .putString("type", "module").putString("module", moduleName).putNumber("instances", instances)
        .putObject("config", config).putArray("targets", nodeList), 15000, new Handler<AsyncResult<Message<JsonObject>>>() {
      @Override
      public void handle(AsyncResult<Message<JsonObject>> result) {
        if (result.failed()) {
          future.setFailure(result.cause());
        }
        else {
          final String status = result.result().body().getString("status");
          if (status.equals("ok")) {
            final String id = result.result().body().getString("id");
            if (id != null) {
              future.setResult(id);
            }
            else {
              future.setFailure(new DeploymentException("No deployment ID provided."));
            }
          }
          else {
            future.setFailure(new DeploymentException(result.result().body().getString("message")));
          }
        }
      }
    });
  }

}
