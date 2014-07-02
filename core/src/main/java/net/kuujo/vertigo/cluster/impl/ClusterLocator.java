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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.impl.DefaultFutureResult;
import org.vertx.java.core.json.JsonObject;

/**
 * Locates the closest cluster node in the cluster.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class ClusterLocator {
  private final Vertx vertx;

  public ClusterLocator(Vertx vertx) {
    this.vertx = vertx;
  }

  /**
   * Locates the local address of the nearest node if one exists.
   *
   * @param cluster The cluster in which to search for the node.
   * @param doneHandler A handler to be called once the address has been located.
   */
  public void locateCluster(String cluster, Handler<AsyncResult<String>> doneHandler) {
    Set<String> registry = vertx.sharedData().getSet(cluster);
    synchronized (registry) {
      if (!registry.isEmpty()) {
        locateNode(cluster, new HashSet<>(registry), doneHandler);
      }
    }
  }

  /**
   * Locates the local address of the nearest group node if one exists.
   *
   * @param cluster The cluster in which to search for the node.
   * @param group The group to search.
   * @param doneHandler A handler to be called once the address has been located.
   */
  public void locateGroup(String cluster, String group, Handler<AsyncResult<String>> doneHandler) {
    locateGroup(String.format("%s.%s", cluster, group), doneHandler);
  }

  /**
   * Locates the local address of the nearest group node if one exists.
   *
   * @param groupAddress The full address of the group to search.
   * @param doneHandler A handler to be called once the address has been located.
   */
  public void locateGroup(String groupAddress, Handler<AsyncResult<String>> doneHandler) {
    Set<String> registry = vertx.sharedData().getSet(groupAddress);
    synchronized (registry) {
      if (!registry.isEmpty()) {
        locateNode(groupAddress, new HashSet<>(registry), doneHandler);
      }
    }
  }

  /**
   * Locates an active local node in a set of nodes.
   */
  private void locateNode(String address, Set<String> nodes, Handler<AsyncResult<String>> doneHandler) {
    locateNode(address, nodes.iterator(), doneHandler);
  }

  /**
   * Locates an active local node in a list of nodes.
   */
  private void locateNode(final String address, final Iterator<String> nodes, final Handler<AsyncResult<String>> doneHandler) {
    if (nodes.hasNext()) {
      final String node = nodes.next();
      vertx.eventBus().sendWithTimeout(node, new JsonObject().putString("action", "ping"), 1000, new Handler<AsyncResult<Message<JsonObject>>>() {
        @Override
        public void handle(AsyncResult<Message<JsonObject>> result) {
          if (result.failed() || result.result().body().getString("status", "ok").equals("error")) {
            removeNode(address, node);
            locateNode(address, nodes, doneHandler);
          } else {
            new DefaultFutureResult<String>(node).setHandler(doneHandler);
          }
        }
      });
    } else {
      new DefaultFutureResult<String>(new IllegalStateException("No local nodes found")).setHandler(doneHandler);
    }
  }

  /**
   * Removes a node from the local registry.
   */
  private void removeNode(String address, String node) {
    Set<String> registry = vertx.sharedData().getSet(address);
    registry.remove(node);
  }

}
