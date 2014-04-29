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

import static net.kuujo.xync.util.Cluster.isHazelcastCluster;
import net.kuujo.vertigo.cluster.Cluster;
import net.kuujo.vertigo.cluster.ClusterAgent;
import net.kuujo.vertigo.cluster.ClusterScope;

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
 * Factory for creating clusters for deployment and shared data access.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class ClusterFactory {
  private static final String VERTIGO_CLUSTER_ADDRESS = "vertigo";
  private Cluster currentCluster;
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
      if (isHazelcastCluster()) {
        // Check to make sure the cluster is available on the event bus. If the cluster
        // isn't available then deploy a new cluster agent that can be used to coordinate.
        vertx.eventBus().sendWithTimeout(VERTIGO_CLUSTER_ADDRESS, new JsonObject(), 1, new Handler<AsyncResult<Message<JsonObject>>>() {
          @Override
          public void handle(AsyncResult<Message<JsonObject>> result) {
            if (result.failed() && ((ReplyException) result.cause()).failureType().equals(ReplyFailure.NO_HANDLERS)) {
              container.deployWorkerVerticle(ClusterAgent.class.getName(), new JsonObject().putString("cluster", VERTIGO_CLUSTER_ADDRESS), 1, false, new Handler<AsyncResult<String>>() {
                @Override
                public void handle(AsyncResult<String> result) {
                  if (result.failed()) {
                    new DefaultFutureResult<Cluster>(result.cause()).setHandler(resultHandler);
                  } else {
                    currentCluster = createCluster(VERTIGO_CLUSTER_ADDRESS, ClusterScope.CLUSTER);
                    new DefaultFutureResult<Cluster>(currentCluster).setHandler(resultHandler);
                  }
                }
              });
            } else {
              currentCluster = createCluster(VERTIGO_CLUSTER_ADDRESS, ClusterScope.CLUSTER);
              new DefaultFutureResult<Cluster>(currentCluster).setHandler(resultHandler);
            }
          }
        });
      } else {
        currentCluster = createCluster(VERTIGO_CLUSTER_ADDRESS, ClusterScope.LOCAL);
        new DefaultFutureResult<Cluster>(currentCluster).setHandler(resultHandler);
      }
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
  public Cluster createCluster(String address, ClusterScope scope) {
    switch (scope) {
      case LOCAL:
        return new LocalCluster(vertx, container);
      case CLUSTER:
        return new RemoteCluster(address, vertx, container);
      default:
        throw new IllegalArgumentException("Invalid cluster scope.");
    }
  }

}
