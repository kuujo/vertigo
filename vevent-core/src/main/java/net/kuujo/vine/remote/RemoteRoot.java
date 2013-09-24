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
package net.kuujo.vine.remote;

import net.kuujo.vine.Root;
import net.kuujo.vine.VineException;
import net.kuujo.vine.context.VineContext;
import net.kuujo.vine.definition.VineDefinition;
import net.kuujo.vine.eventbus.Actions;
import net.kuujo.vine.eventbus.ReliableEventBus;
import net.kuujo.vine.eventbus.WrappedReliableEventBus;
import net.kuujo.vine.util.Messaging;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.Future;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.impl.DefaultFutureResult;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Container;

/**
 * A remote reference to a root verticle.
 *
 * @author Jordan Halterman
 */
public class RemoteRoot implements Root {

  protected static final long DEFAULT_TIMEOUT = 10000;

  protected Vertx vertx;

  protected Container container;

  protected String address;

  protected ReliableEventBus eventBus;

  public RemoteRoot() {
  }

  public RemoteRoot(String address) {
    this.address = address;
  }

  public RemoteRoot(String address, Vertx vertx) {
    this.address = address;
    this.vertx = vertx;
    setEventBus(vertx.eventBus());
  }

  public RemoteRoot(String address, Vertx vertx, Container container) {
    this.address = address;
    this.vertx = vertx;
    this.container = container;
    setEventBus(vertx.eventBus());
  }

  public RemoteRoot(String address, Vertx vertx, Container container, EventBus eventBus) {
    this.address = address;
    this.vertx = vertx;
    this.container = container;
    setEventBus(eventBus);
  }

  public RemoteRoot setAddress(String address) {
    this.address = address;
    return this;
  }

  public String getAddress() {
    return address;
  }

  @Override
  public Root setVertx(Vertx vertx) {
    this.vertx = vertx;
    if (eventBus != null) {
      eventBus.setVertx(vertx);
    }
    return this;
  }

  @Override
  public Vertx getVertx() {
    return vertx;
  }

  @Override
  public Root setContainer(Container container) {
    this.container = container;
    return this;
  }

  @Override
  public Container getContainer() {
    return container;
  }

  /**
   * Sets the root eventbus.
   *
   * @param eventBus
   *   The event bus.
   * @return
   *   The called root instance.
   */
  public RemoteRoot setEventBus(EventBus eventBus) {
    if (!(eventBus instanceof ReliableEventBus)) {
      eventBus = new WrappedReliableEventBus(eventBus, vertx);
    }
    this.eventBus = (ReliableEventBus) eventBus;
    return this;
  }

  /**
   * Gets the root eventbus.
   *
   * @return
   *   The root eventbus.
   */
  public EventBus getEventBus() {
    return eventBus;
  }

  @Override
  public void deploy(final VineDefinition vine, Handler<AsyncResult<VineContext>> handler) {
    final Future<VineContext> future = new DefaultFutureResult<VineContext>().setHandler(handler);
    eventBus.send(address, Actions.create("deploy", vine.serialize()), new Handler<Message<JsonObject>>() {
      @Override
      public void handle(Message<JsonObject> message) {
        // Check for an error. If no error occurred then deploy the vine task.
        final JsonObject body = message.body();
        String error = body.getString("error");
        if (error != null) {
          future.setFailure(new VineException(error));
        }
        else {
          future.setResult(new VineContext(body.getObject("context")));
        }
      }
    });
  }

  @Override
  public void deploy(final VineDefinition vine, long timeout, Handler<AsyncResult<VineContext>> handler) {
    final Future<VineContext> future = new DefaultFutureResult<VineContext>().setHandler(handler);
    eventBus.send(address, Actions.create("deploy", vine.serialize()), timeout, new AsyncResultHandler<Message<JsonObject>>() {
      @Override
      public void handle(AsyncResult<Message<JsonObject>> result) {
        if (result.failed()) {
          future.setFailure(result.cause());
        }
        else {
          // Check for an error. If no error occurred then deploy the vine task.
          final JsonObject body = result.result().body();
          String error = body.getString("error");
          if (error != null) {
            future.setFailure(new VineException(error));
          }
          else {
            future.setResult(new VineContext(body.getObject("context")));
          }
        }
      }
    });
  }

  @Override
  public void shutdown(VineContext context) {
    shutdown(context, DEFAULT_TIMEOUT, null);
  }

  @Override
  public void shutdown(VineContext context, final Handler<AsyncResult<Void>> doneHandler) {
    shutdown(context, DEFAULT_TIMEOUT, doneHandler);
  }

  @Override
  public void shutdown(VineContext context, long timeout, final Handler<AsyncResult<Void>> doneHandler) {
    // Send a message to the remote root indicating that the vine should be
    // shut down. The remote root will notify the appropriate stem.
    eventBus.send(address, Actions.create("undeploy", context.getAddress()), timeout, new AsyncResultHandler<Message<JsonObject>>() {
      @Override
      public void handle(AsyncResult<Message<JsonObject>> result) {
        if (doneHandler != null) {
          Messaging.checkResponse(result, doneHandler);
        }
      }
    });
  }

}
