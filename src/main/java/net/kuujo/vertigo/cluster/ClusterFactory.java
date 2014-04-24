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
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.eventbus.ReplyException;
import org.vertx.java.core.eventbus.ReplyFailure;
import org.vertx.java.core.impl.DefaultFutureResult;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Container;

/**
 * Cluster factory.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class ClusterFactory {
  private static final String CLUSTER_ADDRESS = "__CLUSTER__";
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
      vertx.eventBus().sendWithTimeout(CLUSTER_ADDRESS, new JsonObject(), 1, new Handler<AsyncResult<Message<JsonObject>>>() {
        @Override
        public void handle(AsyncResult<Message<JsonObject>> result) {
          ClusterScope currentScope;
          if (result.failed() && ((ReplyException) result.cause()).failureType().equals(ReplyFailure.NO_HANDLERS)) {
            currentScope = ClusterScope.LOCAL;
          } else {
            currentScope = ClusterScope.CLUSTER;
          }
          currentCluster = createCluster(currentScope);
          new DefaultFutureResult<Cluster>(currentCluster).setHandler(resultHandler);
        }
      });
    }
  }

  /**
   * Loads the current cluster scope.<p>
   *
   * The current scope is determined based on whether the Xync cluster is
   * available. The Xync API will check whether any handlers for the Xync
   * <code>__CLUSTER__</code> address exist on the event bus using a short
   * timeout, and if so then the current cluster scope will be set to
   * <code>CLUSTER</code>, otherwise the cluster scope will be <code>LOCAL</code>.
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
