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
package net.kuujo.vertigo.cluster.manager.impl;

import java.util.UUID;

import net.kuujo.vertigo.cluster.manager.ClusterManager;
import net.kuujo.vertigo.cluster.manager.GroupManager;
import net.kuujo.vertigo.cluster.manager.NodeManager;
import net.kuujo.vertigo.platform.PlatformManager;
import net.kuujo.vertigo.platform.impl.DefaultPlatformManager;
import net.kuujo.vertigo.util.ContextManager;
import net.kuujo.vertigo.util.CountingCompletionHandler;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Future;
import org.vertx.java.core.Handler;
import org.vertx.java.platform.Verticle;

/**
 * Vertigo cluster verticle.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class ClusterAgent extends Verticle {

  @Override
  public void start(final Future<Void> startResult) {
    String clusterName = container.config().getString("cluster", "vertigo");
    String groupName = container.config().getString("group", "__DEFAULT__");
    String nodeAddress = container.config().getString("node", UUID.randomUUID().toString());
    PlatformManager platform = new DefaultPlatformManager(vertx, container);
    ClusterListener listener = new ClusterListenerFactory(vertx).createClusterListener();
    ClusterData data = new ClusterDataFactory(vertx).createClusterData();
    ClusterManager cluster = new DefaultClusterManager(clusterName, vertx, new ContextManager(vertx), platform, listener, data);
    GroupManager group = new DefaultGroupManager(String.format("%s.%s", clusterName, groupName), clusterName, vertx, new ContextManager(vertx), platform, listener, data);
    NodeManager node = new DefaultNodeManager(String.format("%s.%s.%s", clusterName, groupName, nodeAddress), String.format("%s.%s", clusterName, groupName), clusterName, vertx, new ContextManager(vertx), platform, listener, data);
    final CountingCompletionHandler<Void> counter = new CountingCompletionHandler<Void>(3);
    counter.setHandler(new Handler<AsyncResult<Void>>() {
      @Override
      public void handle(AsyncResult<Void> result) {
        if (result.failed()) {
          startResult.setFailure(result.cause());
        } else {
          startResult.setResult((Void) null);
        }
      }
    });
    cluster.start(counter);
    group.start(counter);
    node.start(counter);
  }

}
