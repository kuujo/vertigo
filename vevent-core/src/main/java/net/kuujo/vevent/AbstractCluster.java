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
package net.kuujo.vevent;

import net.kuujo.vevent.context.NetworkContext;
import net.kuujo.vevent.definition.MalformedDefinitionException;
import net.kuujo.vevent.definition.NetworkDefinition;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Future;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.impl.DefaultFutureResult;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Container;

/**
 * A local cluster implementation.
 *
 * @author Jordan Halterman
 */
abstract class AbstractCluster implements Cluster {

  private EventBus eventBus;

  private Container container;

  protected String coordinator;

  public AbstractCluster(Vertx vertx, Container container) {
    this.eventBus = vertx.eventBus();
    this.container = container;
  }

  @Override
  public void deploy(NetworkDefinition network, Handler<AsyncResult<NetworkContext>> doneHandler) {
    final Future<NetworkContext> future = new DefaultFutureResult<NetworkContext>().setHandler(doneHandler);
    try {
      final NetworkContext context = network.createContext();
      container.deployVerticle(coordinator, context.serialize(), new Handler<AsyncResult<String>>() {
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
    catch (MalformedDefinitionException e) {
      future.setFailure(e);
    }
  }

  @Override
  public void shutdown(NetworkContext context) {
    eventBus.send(context.getAddress(), new JsonObject().putString("action", "shutdown"));
  }

  @Override
  public void shutdown(NetworkContext context, Handler<AsyncResult<Void>> doneHandler) {
    final Future<Void> future = new DefaultFutureResult<Void>().setHandler(doneHandler);
    eventBus.sendWithTimeout(context.getAddress(), new JsonObject().putString("action", "shutdown"), 30000, new Handler<AsyncResult<Message<Boolean>>>() {
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
