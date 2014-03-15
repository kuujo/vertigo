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

import net.kuujo.vertigo.network.Network;
import net.kuujo.vertigo.network.context.NetworkContext;
import net.kuujo.vertigo.network.coordinator.ClusterCoordinator;
import net.kuujo.vertigo.network.coordinator.DeploymentException;
import net.kuujo.vertigo.network.manager.NetworkManager;
import net.kuujo.vertigo.util.serializer.Serializer;
import net.kuujo.vertigo.util.serializer.SerializerFactory;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.impl.DefaultFutureResult;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Container;

/**
 * Abstract cluster implementation.
 *
 * @author Jordan Halterman
 */
abstract class AbstractCluster implements Cluster {
  private static final Serializer networkSerializer = SerializerFactory.getSerializer(Network.class);
  private static final Serializer contextSerializer = SerializerFactory.getSerializer(NetworkContext.class);
  protected final Vertx vertx;
  protected final Container container;
  protected final EventBus eventBus;
  protected final ClusterCoordinator cluster;

  protected AbstractCluster(Vertx vertx, Container container, ClusterCoordinator cluster) {
    this.vertx = vertx;
    this.container = container;
    this.eventBus = vertx.eventBus();
    this.cluster = cluster;
  }

  @Override
  public Cluster getNetwork(String address, Handler<AsyncResult<NetworkContext>> resultHandler) {
    return this;
  }

  @Override
  public Cluster deployNetwork(Network network) {
    return deployNetwork(network, null);
  }

  @Override
  public Cluster deployNetwork(final Network network, final Handler<AsyncResult<NetworkContext>> doneHandler) {
    cluster.isDeployed(network.getAddress(), new Handler<AsyncResult<Boolean>>() {
      @Override
      public void handle(AsyncResult<Boolean> result) {
        if (result.failed()) {
          new DefaultFutureResult<NetworkContext>(result.cause()).setHandler(doneHandler);
        }
        else if (result.result()) {
          doDeployNetwork(network, doneHandler);
        }
        else {
          JsonObject config = new JsonObject()
              .putString("address", network.getAddress())
              .putString("cluster", cluster.getClass().getName());
          container.deployVerticle(NetworkManager.class.getName(), config, new Handler<AsyncResult<String>>() {
            @Override
            public void handle(AsyncResult<String> result) {
              if (result.failed()) {
                new DefaultFutureResult<NetworkContext>(result.cause()).setHandler(doneHandler);
              }
              else {
                doDeployNetwork(network, doneHandler);
              }
            }
          });
        }
      }
    });
    return this;
  }

  private void doDeployNetwork(Network network, final Handler<AsyncResult<NetworkContext>> doneHandler) {
    JsonObject message = new JsonObject()
        .putString("action", "deploy")
        .putObject("network", networkSerializer.serializeToObject(network));
    vertx.eventBus().sendWithTimeout(network.getAddress(), message, 60000, new Handler<AsyncResult<Message<JsonObject>>>() {
      @Override
      public void handle(AsyncResult<Message<JsonObject>> result) {
        if (result.failed()) {
          new DefaultFutureResult<NetworkContext>(result.cause()).setHandler(doneHandler);
        }
        else if (result.result().body().getString("status").equals("ok")) {
          new DefaultFutureResult<NetworkContext>(contextSerializer.deserializeObject(result.result().body().getObject("context"), NetworkContext.class));
        }
        else {
          new DefaultFutureResult<NetworkContext>(new DeploymentException(result.result().body().getString("message"))).setHandler(doneHandler);
        }
      }
    });
  }

  @Override
  public Cluster undeployNetwork(String address) {
    return undeployNetwork(address, null);
  }

  @Override
  public Cluster undeployNetwork(String address, Handler<AsyncResult<Void>> doneHandler) {
    return this;
  }

  @Override
  public Cluster undeployNetwork(Network network) {
    return undeployNetwork(network, null);
  }

  @Override
  public Cluster undeployNetwork(Network network, Handler<AsyncResult<Void>> doneHandler) {
    return this;
  }

}
