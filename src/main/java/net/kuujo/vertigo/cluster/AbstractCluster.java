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

import net.kuujo.vertigo.context.NetworkContext;
import net.kuujo.vertigo.context.impl.ContextBuilder;
import net.kuujo.vertigo.network.MalformedNetworkException;
import net.kuujo.vertigo.network.Network;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Future;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.impl.DefaultFutureResult;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Container;
import org.vertx.java.platform.Verticle;

/**
 * An abstract cluster.
 *
 * @author Jordan Halterman
 */
abstract class AbstractCluster implements Cluster {
  private EventBus eventBus;
  private Container container;
  protected String coordinator;
  protected String master;

  public AbstractCluster(Verticle verticle) {
    this.eventBus = verticle.getVertx().eventBus();
    this.container = verticle.getContainer();
  }

  public AbstractCluster(Vertx vertx, Container container) {
    this.eventBus = vertx.eventBus();
    this.container = container;
  }

  @Override
  public void deploy(Network network) {
    try {
      final NetworkContext context = ContextBuilder.buildContext(network);
      container.deployVerticle(coordinator, NetworkContext.toJson(context));
    }
    catch (MalformedNetworkException e) {
      container.logger().error(e);
    }
  }

  @Override
  public void deploy(final Network network, Handler<AsyncResult<NetworkContext>> doneHandler) {
    final Future<NetworkContext> future = new DefaultFutureResult<NetworkContext>().setHandler(doneHandler);
    try {
      final NetworkContext context = ContextBuilder.buildContext(network);
      container.deployVerticle(coordinator, NetworkContext.toJson(context), new Handler<AsyncResult<String>>() {
        @Override
        public void handle(AsyncResult<String> result) {
          if (result.failed()) {
            future.setFailure(result.cause());
          }
          else {
            future.setResult(context);
          }
        }
      });
    }
    catch (MalformedNetworkException e) {
      future.setFailure(e);
    }
  }

  @Override
  public void shutdown(NetworkContext context) {
    eventBus.send(context.address(), new JsonObject().putString("action", "shutdown"));
  }

  @Override
  public void shutdown(final NetworkContext context, Handler<AsyncResult<Void>> doneHandler) {
    final Future<Void> future = new DefaultFutureResult<Void>().setHandler(doneHandler);
    eventBus.sendWithTimeout(context.address(), new JsonObject().putString("action", "shutdown"), 30000, new Handler<AsyncResult<Message<Boolean>>>() {
      @Override
      public void handle(AsyncResult<Message<Boolean>> result) {
        if (result.failed()) {
          future.setFailure(result.cause());
        }
        else {
          future.setResult(null);
        }
      }
    });
  }

}
