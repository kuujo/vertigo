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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.kuujo.vertigo.Config;
import net.kuujo.vertigo.cluster.ClusterManager;
import net.kuujo.vertigo.cluster.ClusterManagerException;
import net.kuujo.vertigo.network.ActiveNetwork;
import net.kuujo.vertigo.network.NetworkConfig;
import net.kuujo.vertigo.network.NetworkContext;
import net.kuujo.vertigo.network.impl.DefaultActiveNetwork;
import net.kuujo.vertigo.network.impl.DefaultNetworkConfig;
import net.kuujo.vertigo.network.impl.DefaultNetworkContext;
import net.kuujo.vertigo.util.Configs;
import net.kuujo.vertigo.util.CountingCompletionHandler;
import net.kuujo.vertigo.util.serialization.SerializerFactory;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.impl.DefaultFutureResult;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Container;
import org.vertx.java.platform.Verticle;

/**
 * Default cluster manager implementation.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class DefaultClusterManager implements ClusterManager {
  private static final String DEFAULT_CLUSTER_ADDRESS = "vertigo";
  private static final long DEFAULT_REPLY_TIMEOUT = 30000;
  private final String address;
  private final Vertx vertx;

  public DefaultClusterManager(Verticle verticle) {
    this(DEFAULT_CLUSTER_ADDRESS, verticle.getVertx(), verticle.getContainer());
  }

  public DefaultClusterManager(String address, Verticle verticle) {
    this(address, verticle.getVertx(), verticle.getContainer());
  }

  public DefaultClusterManager(Vertx vertx, Container container) {
    this(DEFAULT_CLUSTER_ADDRESS, vertx, container);
  }

  public DefaultClusterManager(String address, Vertx vertx, Container container) {
    this.address = address;
    this.vertx = vertx;
  }

  @Override
  public String address() {
    return address;
  }

  @Override
  public ClusterManager getNetworks(final Handler<AsyncResult<Collection<ActiveNetwork>>> resultHandler) {
    JsonObject message = new JsonObject()
        .putString("action", "list")
        .putString("type", "network");
    vertx.eventBus().sendWithTimeout(address, message, DEFAULT_REPLY_TIMEOUT, new Handler<AsyncResult<Message<JsonObject>>>() {
      @Override
      public void handle(AsyncResult<Message<JsonObject>> result) {
        if (result.failed()) {
          new DefaultFutureResult<Collection<ActiveNetwork>>(new ClusterManagerException(result.cause())).setHandler(resultHandler);
        } else if (result.result().body().getString("status").equals("error")) {
          new DefaultFutureResult<Collection<ActiveNetwork>>(new ClusterManagerException(result.result().body().getString("message"))).setHandler(resultHandler);
        } else {
          final List<ActiveNetwork> networks = new ArrayList<>();
          JsonArray jsonNetworks = result.result().body().getArray("result");
          final CountingCompletionHandler<Void> counter = new CountingCompletionHandler<Void>(jsonNetworks.size());
          counter.setHandler(new Handler<AsyncResult<Void>>() {
            @Override
            public void handle(AsyncResult<Void> result) {
              if (result.failed()) {
                new DefaultFutureResult<Collection<ActiveNetwork>>(result.cause()).setHandler(resultHandler);
              } else {
                new DefaultFutureResult<Collection<ActiveNetwork>>(networks).setHandler(resultHandler);
              }
            }
          });
          for (Object jsonNetwork : jsonNetworks) {
            createActiveNetwork(DefaultNetworkContext.fromJson((JsonObject) jsonNetwork), new Handler<AsyncResult<ActiveNetwork>>() {
              @Override
              public void handle(AsyncResult<ActiveNetwork> result) {
                if (result.failed()) {
                  counter.fail(result.cause());
                } else {
                  networks.add(result.result());
                  counter.succeed();
                }
              }
            });
          }
        }
      }
    });
    return this;
  }

  @Override
  public ClusterManager getNetwork(String name, final Handler<AsyncResult<ActiveNetwork>> resultHandler) {
    JsonObject message = new JsonObject()
        .putString("action", "load")
        .putString("type", "network")
        .putString("network", name);
    vertx.eventBus().sendWithTimeout(address, message, DEFAULT_REPLY_TIMEOUT, new Handler<AsyncResult<Message<JsonObject>>>() {
      @Override
      public void handle(AsyncResult<Message<JsonObject>> result) {
        if (result.failed()) {
          new DefaultFutureResult<ActiveNetwork>(new ClusterManagerException(result.cause())).setHandler(resultHandler);
        } else if (result.result().body().getString("status").equals("error")) {
          new DefaultFutureResult<ActiveNetwork>(new ClusterManagerException(result.result().body().getString("message"))).setHandler(resultHandler);
        } else {
          createActiveNetwork(DefaultNetworkContext.fromJson(result.result().body().getObject("result")), resultHandler);
        }
      }
    });
    return this;
  }

  @Override
  public ClusterManager isDeployed(String name, final Handler<AsyncResult<Boolean>> resultHandler) {
    JsonObject message = new JsonObject()
        .putString("action", "check")
        .putString("type", "network")
        .putString("network", name);
    vertx.eventBus().sendWithTimeout(address, message, DEFAULT_REPLY_TIMEOUT, new Handler<AsyncResult<Message<JsonObject>>>() {
      @Override
      public void handle(AsyncResult<Message<JsonObject>> result) {
        if (result.failed()) {
          new DefaultFutureResult<Boolean>(new ClusterManagerException(result.cause())).setHandler(resultHandler);
        } else if (result.result().body().getString("status").equals("error")) {
          new DefaultFutureResult<Boolean>(new ClusterManagerException(result.result().body().getString("message"))).setHandler(resultHandler);
        } else {
          new DefaultFutureResult<Boolean>(result.result().body().getBoolean("result")).setHandler(resultHandler);
        }
      }
    });
    return this;
  }

  @Override
  public ClusterManager deployNetwork(String name) {
    return deployNetwork(name, null);
  }

  @Override
  public ClusterManager deployNetwork(String name, Handler<AsyncResult<ActiveNetwork>> doneHandler) {
    return deployNetwork(new DefaultNetworkConfig(name), doneHandler);
  }

  @Override
  public ClusterManager deployNetwork(JsonObject network) {
    return deployNetwork(network, null);
  }

  @Override
  public ClusterManager deployNetwork(JsonObject network, Handler<AsyncResult<ActiveNetwork>> doneHandler) {
    return deployNetwork(Configs.createNetwork(network), doneHandler);
  }

  @Override
  public ClusterManager deployNetwork(NetworkConfig network) {
    return deployNetwork(network, null);
  }

  @Override
  public ClusterManager deployNetwork(NetworkConfig network, final Handler<AsyncResult<ActiveNetwork>> doneHandler) {
    JsonObject message = new JsonObject()
        .putString("action", "deploy")
        .putString("type", "network")
        .putObject("network", SerializerFactory.getSerializer(Config.class).serializeToObject(network));
    vertx.eventBus().sendWithTimeout(address, message, DEFAULT_REPLY_TIMEOUT, new Handler<AsyncResult<Message<JsonObject>>>() {
      @Override
      public void handle(AsyncResult<Message<JsonObject>> result) {
        if (result.failed()) {
          new DefaultFutureResult<ActiveNetwork>(new ClusterManagerException(result.cause())).setHandler(doneHandler);
        } else if (result.result().body().getString("status").equals("error")) {
          new DefaultFutureResult<ActiveNetwork>(new ClusterManagerException(result.result().body().getString("message"))).setHandler(doneHandler);
        } else {
          createActiveNetwork(DefaultNetworkContext.fromJson(result.result().body().getObject("context")), doneHandler);
        }
      }
    });
    return this;
  }

  @Override
  public ClusterManager undeployNetwork(String name) {
    return undeployNetwork(name, null);
  }

  @Override
  public ClusterManager undeployNetwork(String name, final Handler<AsyncResult<Void>> doneHandler) {
    JsonObject message = new JsonObject()
        .putString("action", "undeploy")
        .putString("type", "network")
        .putString("network", name);
    vertx.eventBus().sendWithTimeout(address, message, DEFAULT_REPLY_TIMEOUT, new Handler<AsyncResult<Message<JsonObject>>>() {
      @Override
      public void handle(AsyncResult<Message<JsonObject>> result) {
        if (result.failed()) {
          new DefaultFutureResult<Void>(new ClusterManagerException(result.cause())).setHandler(doneHandler);
        } else if (result.result().body().getString("status").equals("error")) {
          new DefaultFutureResult<Void>(new ClusterManagerException(result.result().body().getString("message"))).setHandler(doneHandler);
        } else {
          new DefaultFutureResult<Void>((Void) null).setHandler(doneHandler);
        }
      }
    });
    return this;
  }

  @Override
  public ClusterManager undeployNetwork(JsonObject network) {
    return undeployNetwork(network, null);
  }

  @Override
  public ClusterManager undeployNetwork(JsonObject network, Handler<AsyncResult<Void>> doneHandler) {
    return undeployNetwork(Configs.createNetwork(network), doneHandler);
  }

  @Override
  public ClusterManager undeployNetwork(NetworkConfig network) {
    return undeployNetwork(network, null);
  }

  @Override
  public ClusterManager undeployNetwork(NetworkConfig network, final Handler<AsyncResult<Void>> doneHandler) {
    JsonObject message = new JsonObject()
        .putString("action", "undeploy")
        .putString("type", "network")
        .putObject("network", SerializerFactory.getSerializer(Config.class).serializeToObject(network));
    vertx.eventBus().sendWithTimeout(address, message, DEFAULT_REPLY_TIMEOUT, new Handler<AsyncResult<Message<JsonObject>>>() {
      @Override
      public void handle(AsyncResult<Message<JsonObject>> result) {
        if (result.failed()) {
          new DefaultFutureResult<Void>(new ClusterManagerException(result.cause())).setHandler(doneHandler);
        } else if (result.result().body().getString("status").equals("error")) {
          new DefaultFutureResult<Void>(new ClusterManagerException(result.result().body().getString("message"))).setHandler(doneHandler);
        } else {
          new DefaultFutureResult<Void>((Void) null).setHandler(doneHandler);
        }
      }
    });
    return this;
  }

  /**
   * Creates and returns an active network.
   */
  private void createActiveNetwork(final NetworkContext context, final Handler<AsyncResult<ActiveNetwork>> doneHandler) {
    final DefaultActiveNetwork active = new DefaultActiveNetwork(context.config(), DefaultClusterManager.this);
    vertx.eventBus().registerHandler(String.format("%s.%s.change", context.name(), context.name()), new Handler<Message<JsonObject>>() {
      @Override
      public void handle(Message<JsonObject> message) {
        String event = message.body().getString("type");
        if (event.equals("change") && message.body().getString("value") != null) {
          active.update(DefaultNetworkContext.fromJson(new JsonObject(message.body().getString("value"))));
        }
      }
    }, new Handler<AsyncResult<Void>>() {
      @Override
      public void handle(AsyncResult<Void> result) {
        if (result.failed()) {
          new DefaultFutureResult<ActiveNetwork>(new ClusterManagerException(result.cause())).setHandler(doneHandler);
        } else {
          new DefaultFutureResult<ActiveNetwork>(active).setHandler(doneHandler);
        }
      }
    });
  }

  @Override
  public boolean equals(Object other) {
    return other instanceof ClusterManager && ((ClusterManager) other).address().equals(address);
  }

  @Override
  public String toString() {
    return address;
  }

}
