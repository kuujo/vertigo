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
package net.kuujo.vertigo.impl;

import io.vertx.core.*;
import io.vertx.core.shareddata.LocalMap;
import net.kuujo.vertigo.ContextManager;
import net.kuujo.vertigo.VertigoException;
import net.kuujo.vertigo.component.ComponentContext;
import net.kuujo.vertigo.component.PartitionContext;
import net.kuujo.vertigo.network.NetworkContext;
import net.kuujo.vertigo.util.CountingCompletionHandler;

/**
 * Local context manager implementation.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class LocalContextManager implements ContextManager {
  private static final String NETWORKS_KEY = "vertigo";
  private final Vertx vertx;

  LocalContextManager(Vertx vertx) {
    this.vertx = vertx;
  }

  @Override
  public ContextManager getNetwork(String id, Handler<AsyncResult<NetworkContext>> doneHandler) {
    NetworkContext context = vertx.sharedData().<String, NetworkContext>getLocalMap(NETWORKS_KEY).get(id);
    if (context == null) {
      Future.<NetworkContext>completedFuture(new VertigoException(String.format("Invalid network %s: Network not found", id))).setHandler(doneHandler);
    } else {
      Future.completedFuture(context).setHandler(doneHandler);
    }
    return this;
  }

  @Override
  public ContextManager deployNetwork(NetworkContext network, Handler<AsyncResult<Void>> doneHandler) {
    int partitionCount = countPartitions(network);

    CountingCompletionHandler<Void> counter = new CountingCompletionHandler<Void>(partitionCount).setHandler(result -> {
      if (result.succeeded()) {
        vertx.sharedData().<String, NetworkContext>getLocalMap(NETWORKS_KEY).put(network.id(), network);
      }
      doneHandler.handle(result);
    });

    for (ComponentContext component : network.components()) {
      for (PartitionContext partition : component.partitions()) {
        // TODO: Pass the partition context in as part of the verticle configuration.
        DeploymentOptions options = new DeploymentOptions();
        options.setConfig(component.config());
        options.setWorker(component.isWorker());
        options.setMultiThreaded(component.isMultiThreaded());
        vertx.deployVerticle(component.main(), options, result -> {
          if (result.failed()) {
            counter.fail(result.cause());
          } else {
            vertx.sharedData().<String, String>getLocalMap(network.id()).put(partition.address(), result.result());
            counter.succeed();
          }
        });
      }
    }
    return this;
  }

  @Override
  public ContextManager undeployNetwork(NetworkContext network, Handler<AsyncResult<Void>> doneHandler) {
    LocalMap<String, String> deploymentIds = vertx.sharedData().getLocalMap(network.id());

    CountingCompletionHandler<Void> counter = new CountingCompletionHandler<Void>(deploymentIds.size()).setHandler(result -> {
      if (result.succeeded()) {
        vertx.sharedData().<String, NetworkContext>getLocalMap(NETWORKS_KEY).remove(network.id());
        vertx.sharedData().<String, String>getLocalMap(network.id()).clear();
      }
      doneHandler.handle(result);
    });

    for (ComponentContext component : network.components()) {
      for (PartitionContext partition : component.partitions()) {
        String deploymentId = deploymentIds.get(partition.address());
        if (deploymentId != null) {
          vertx.undeployVerticle(deploymentId, counter);
        }
      }
    }
    return this;
  }

  /**
   * Counts the total number of partitions in the network.
   */
  private int countPartitions(NetworkContext network) {
    int partitionCount = 0;
    for (ComponentContext component : network.components()) {
      partitionCount += component.partitions().size();
    }
    return partitionCount;
  }

}
