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
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.impl.DefaultFutureResult;
import org.vertx.java.core.json.JsonObject;

/**
 * A basic vine feeder.
 *
 * @author Jordan Halterman
 */
public class BasicFeeder implements Feeder {

  protected String address;

  protected EventBus eventBus;

  protected Handler<Void> drainHandler;

  protected boolean queueFull;

  public BasicFeeder(String address, EventBus eventBus) {
    this.address = address;
    this.eventBus = eventBus;
  }

  @Override
  public boolean feedQueueFull() {
    return false;
  }

  @Override
  public Feeder drainHandler(Handler<Void> drainHandler) {
    this.drainHandler = drainHandler;
    return this;
  }

  @Override
  public void feed(JsonObject data) {
    eventBus.send(address, data);
  }

  @Override
  public void feed(JsonObject data, final Handler<AsyncResult<JsonObject>> resultHandler) {
    eventBus.send(address, new JsonObject().putString("action", "feed").putObject("data", data), new Handler<Message<JsonObject>>() {
      @Override
      public void handle(Message<JsonObject> message) {
        String error = message.body().getString("error");
        if (error != null) {
          // If a "Vine queue full." error was received, set the queueFull variable
          // and send an error back to the asynchronous handler.
          if (error == "Vine queue full.") {
            queueFull = true;
          }

          new DefaultFutureResult<JsonObject>().setHandler(resultHandler).setFailure(new VineException(error));
        }
        else {
          // Invoke the result handler with the JSON message body.
          new DefaultFutureResult<JsonObject>().setHandler(resultHandler).setResult(message.body());

          // If a message response is received, the queue should *always* be not full.
          queueFull = false;
          if (drainHandler != null) {
            drainHandler.handle(null);
          }
        }
      }
    });
  }

}
