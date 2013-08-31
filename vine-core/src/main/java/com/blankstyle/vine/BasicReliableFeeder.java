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
package com.blankstyle.vine;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.Future;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.impl.DefaultFutureResult;
import org.vertx.java.core.json.JsonObject;

import com.blankstyle.vine.eventbus.Actions;
import com.blankstyle.vine.eventbus.ReliableEventBus;
import com.blankstyle.vine.eventbus.WrappedReliableEventBus;

/**
 * A reliable feeder implementation.
 *
 * @author Jordan Halterman
 */
public class BasicReliableFeeder extends BasicFeeder implements ReliableFeeder {

  private ReliableEventBus eventBus;

  public BasicReliableFeeder(String address, EventBus eventBus) {
    super(address, eventBus);
    this.eventBus = new WrappedReliableEventBus(eventBus);
  }

  public BasicReliableFeeder setVertx(Vertx vertx) {
    eventBus.setVertx(vertx);
    return this;
  }

  @Override
  public void feed(JsonObject data, long timeout, final Handler<AsyncResult<JsonObject>> resultHandler) {
    final Future<JsonObject> future = new DefaultFutureResult<JsonObject>().setHandler(resultHandler);
    eventBus.send(address, Actions.create("feed", data), timeout, new AsyncResultHandler<Message<JsonObject>>() {
      @Override
      public void handle(AsyncResult<Message<JsonObject>> result) {
        if (result.succeeded()) {
          Message<JsonObject> message = result.result();
          String error = message.body().getString("error");
          if (error != null) {
            // If a "Vine queue full." error was received, set the queueFull variable
            // and send an error back to the asynchronous handler.
            if (error == "Vine queue full.") {
              queueFull = true;
            }
  
            future.setFailure(new VineException(error));
          }
          else {
            // Invoke the result handler with the JSON message body.
            future.setResult(message.body());
  
            // If a message response is received, the queue should *always* be not full.
            queueFull = false;
            if (drainHandler != null) {
              drainHandler.handle(null);
            }
          }
        }
        else {
          future.setFailure(result.cause());
        }
      }
    });
  }

}
