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

import net.kuujo.vertigo.util.Factories;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.impl.DefaultFutureResult;
import org.vertx.java.platform.Container;

/**
 * Factory for creating cluster managers.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class ClusterManagerFactory {
  private final Vertx vertx;
  private final Container container;
  private final ClusterFactory clusterFactory;

  public ClusterManagerFactory(Vertx vertx, Container container) {
    this.vertx = vertx;
    this.container = container;
    this.clusterFactory = new ClusterFactory(vertx, container);
  }

  /**
   * Loads the current cluster scope.
   *
   * @param vertx The current Vert.x instance.
   * @param container The current Vert.x container.
   * @param resultHandler An asynchronous handler to be called with the result.
   */
  public void getCurrentScope(Handler<AsyncResult<ClusterScope>> resultHandler) {
    clusterFactory.getCurrentScope(resultHandler);
  }

  /**
   * Loads the cluster manager for the current scope.
   *
   * @param vertx The current Vert.x instance.
   * @param container The current Vert.x container.
   * @param doneHandler An asynchronous handler to be called once the cluster manager is loaded.
   */
  public void getCurrentClusterManager(final Handler<AsyncResult<ClusterManager>> resultHandler) {
    clusterFactory.getCurrentScope(new Handler<AsyncResult<ClusterScope>>() {
      @Override
      public void handle(AsyncResult<ClusterScope> result) {
        if (result.failed()) {
          new DefaultFutureResult<ClusterManager>(result.cause()).setHandler(resultHandler);
        } else {
          new DefaultFutureResult<ClusterManager>(createClusterManager(result.result())).setHandler(resultHandler);
        }
      }
    });
  }

  /**
   * Creates a cluster manager for the given scope.
   *
   * @param scope The scope for which to create the cluster manager.
   * @param vertx The current Vert.x instance.
   * @param container The current Vert.x container.
   * @return A cluster manager for the given scope.
   */
  public ClusterManager createClusterManager(ClusterScope scope) {
    return Factories.createObject(scope, ClusterManager.class, vertx, container);
  }

}
