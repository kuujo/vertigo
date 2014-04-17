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
import net.kuujo.xync.XyncCluster;
import net.kuujo.xync.impl.DefaultXyncCluster;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.impl.DefaultFutureResult;
import org.vertx.java.platform.Container;

/**
 * Cluster factory.
 *
 * @author Jordan Halterman
 */
public class ClusterFactory {
  private static Cluster currentCluster;
  private final Vertx vertx;
  private final Container container;

  public ClusterFactory(Vertx vertx, Container container) {
    this.vertx = vertx;
    this.container = container;
  }

  /**
   * Loads the cluster for the current scope.
   *
   * @param doneHandler An asynchronous handler to be called once the cluster is loaded.
   */
  public void getCurrentCluster(final Handler<AsyncResult<Cluster>> resultHandler) {
    if (currentCluster != null) {
      new DefaultFutureResult<Cluster>(currentCluster).setHandler(resultHandler);
    } else {
      XyncCluster cluster = new DefaultXyncCluster(vertx);
      cluster.isCluster(new Handler<AsyncResult<Boolean>>() {
        @Override
        public void handle(AsyncResult<Boolean> result) {
          ClusterScope currentScope;
          if (result.result()) {
            currentScope = ClusterScope.CLUSTER;
          } else {
            currentScope = ClusterScope.LOCAL;
          }
          currentCluster = createCluster(currentScope);
          new DefaultFutureResult<Cluster>(currentCluster).setHandler(resultHandler);
        }
      });
    }
  }

  /**
   * Loads the current cluster scope.
   *
   * @param doneHandler An asynchronous handler to be called once the scope is loaded.
   */
  public void getCurrentScope(final Handler<AsyncResult<ClusterScope>> resultHandler) {
    getCurrentCluster(new Handler<AsyncResult<Cluster>>() {
      @Override
      public void handle(AsyncResult<Cluster> result) {
        if (result.failed()) {
          new DefaultFutureResult<ClusterScope>(result.cause()).setHandler(resultHandler);
        } else {
          new DefaultFutureResult<ClusterScope>(result.result().scope()).setHandler(resultHandler);
        }
      }      
    });
  }

  /**
   * Creates a cluster for the given scope.
   *
   * @param scope The cluster scope.
   * @return A cluster for the given scope.
   */
  public Cluster createCluster(ClusterScope scope) {
    return Factories.createObject(scope, Cluster.class, vertx, container);
  }

}
