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
package net.kuujo.vertigo.component.worker;

import net.kuujo.vertigo.component.ComponentBase;
import net.kuujo.vertigo.context.ComponentContext;
import net.kuujo.vertigo.messaging.JsonMessage;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Future;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.impl.DefaultFutureResult;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Container;

/**
 * A basic worker implementation.
 *
 * @author Jordan Halterman
 */
public class BasicWorker extends ComponentBase implements Worker {
  protected Handler<JsonMessage> messageHandler;

  public BasicWorker(Vertx vertx, Container container, ComponentContext context) {
    super(vertx, container, context);
  }

  @Override
  public Worker start() {
    start(new Handler<AsyncResult<Worker>>() {
      @Override
      public void handle(AsyncResult<Worker> result) {
        // Do nothing.
      }
    });
    return this;
  }

  @Override
  public Worker start(Handler<AsyncResult<Worker>> doneHandler) {
    final Future<Worker> future = new DefaultFutureResult<Worker>().setHandler(doneHandler);
    setup(new Handler<AsyncResult<Void>>() {
      @Override
      public void handle(AsyncResult<Void> result) {
        if (result.failed()) {
          future.setFailure(result.cause());
        }
        else {
          input.messageHandler(messageHandler);
          future.setResult(BasicWorker.this);
        }
      }
    });
    return this;
  }

  @Override
  public Worker messageHandler(Handler<JsonMessage> handler) {
    messageHandler = handler;
    input.messageHandler(messageHandler);
    return this;
  }

  @Override
  public Worker emit(JsonObject data) {
    output.emit(data);
    return this;
  }

  @Override
  public Worker emit(JsonObject data, String tag) {
    output.emit(data, tag);
    return this;
  }

  @Override
  public Worker emit(JsonObject data, JsonMessage parent) {
    output.emit(data, parent);
    return this;
  }

  @Override
  public Worker emit(JsonObject data, String tag, JsonMessage parent) {
    output.emit(data, tag, parent);
    return this;
  }

  @Override
  public void ack(JsonMessage message) {
    input.ack(message);
  }

  @Override
  public void fail(JsonMessage message) {
    input.fail(message);
  }

}
