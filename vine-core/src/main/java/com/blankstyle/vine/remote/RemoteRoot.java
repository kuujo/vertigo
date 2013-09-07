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
package com.blankstyle.vine.remote;

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

import com.blankstyle.vine.Feeder;
import com.blankstyle.vine.BasicFeeder;
import com.blankstyle.vine.Root;
import com.blankstyle.vine.Vine;
import com.blankstyle.vine.VineException;
import com.blankstyle.vine.context.VineContext;
import com.blankstyle.vine.definition.VineDefinition;
import com.blankstyle.vine.eventbus.Actions;
import com.blankstyle.vine.eventbus.ReliableEventBus;
import com.blankstyle.vine.eventbus.WrappedReliableEventBus;
import com.blankstyle.vine.eventbus.vine.VineVerticle;

/**
 * A remote reference to a root verticle.
 *
 * @author Jordan Halterman
 */
public class RemoteRoot implements Root {

  protected static final String VINE_VERTICLE_CLASS = VineVerticle.class.getName();

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
  public void deploy(final VineDefinition vine, Handler<AsyncResult<Vine>> handler) {
    final Future<Vine> future = new DefaultFutureResult<Vine>().setHandler(handler);
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
          deployVineVerticle(new VineContext(body.getObject("context")), future);
        }
      }
    });
  }

  @Override
  public void deploy(final VineDefinition vine, long timeout, Handler<AsyncResult<Vine>> handler) {
    final Future<Vine> future = new DefaultFutureResult<Vine>().setHandler(handler);
    eventBus.send(address, Actions.create("deploy", vine.serialize()), timeout, new AsyncResultHandler<Message<JsonObject>>() {
      @Override
      public void handle(AsyncResult<Message<JsonObject>> result) {
        if (result.failed()) {
          future.setFailure(result.cause());
        }
        else {
          // Check for an error. If no error occurred then deploy the vine task.
          final JsonObject context = result.result().body();
          String error = context.getString("error");
          if (error != null) {
            future.setFailure(new VineException(error));
          }
          else {
            deployVineVerticle(new VineContext(context), future);
          }
        }
      }
    });
  }

  /**
   * Deploys a vine verticle.
   *
   * @param context
   *   The vine context.
   * @param future
   *   A future result to be invoked with a vine reference.
   */
  private void deployVineVerticle(final VineContext context, final Future<Vine> future) {
    container.deployVerticle(VINE_VERTICLE_CLASS, context.serialize(), new Handler<AsyncResult<String>>() {
      @Override
      public void handle(AsyncResult<String> result) {
        if (result.succeeded()) {
          future.setResult(createVine(context, result.result()));
        }
        else {
          future.setFailure(result.cause());
        }
      }
    });
  }

  /**
   * Creates a vine.
   *
   * @param context
   *   The vine context.
   * @param stemAddress
   *   The address to the stem used to deploy the vine.
   * @param deploymentID
   *   The vine deployment ID.
   * @return
   *   The vine.
   */
  private Vine createVine(final VineContext context, final String deploymentID) {
    return new Vine() {
      @Override
      public Feeder feeder() {
        return new BasicFeeder(context.getAddress(), vertx.eventBus()).setVertx(vertx);
      }

      @Override
      public void shutdown() {
        shutdown(null);
      }

      @Override
      public void shutdown(Handler<AsyncResult<Void>> doneHandler) {
        final Future<Void> future = new DefaultFutureResult<Void>();
        if (doneHandler != null) {
          future.setHandler(doneHandler);
        }

        eventBus.send(address, Actions.create("undeploy", context.getAddress()), new Handler<Message<JsonObject>>() {
          @Override
          public void handle(Message<JsonObject> reply) {
            JsonObject body = reply.body();
            if (body != null) {
              String error = body.getString("error");
              if (error != null) {
                future.setFailure(new VineException(error));
              }
              else {
                future.setResult(null);
              }
            }
            else {
              future.setResult(null);
            }
          }
        });
      }
    };
  }

}
