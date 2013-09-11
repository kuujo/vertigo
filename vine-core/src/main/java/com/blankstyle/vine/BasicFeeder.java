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
 * A basic vine feeder.
 *
 * @author Jordan Halterman
 */
public class BasicFeeder implements Feeder {

  protected String address;

  protected ReliableEventBus eventBus;

  protected Handler<Void> drainHandler;

  protected boolean queueFull;

  public BasicFeeder(String address, EventBus eventBus) {
    this.address = address;
    setEventBus(eventBus);
  }

  public BasicFeeder(String address, EventBus eventBus, Vertx vertx) {
    this.address = address;
    setEventBus(eventBus).setVertx(vertx);
  }

  public Feeder setVertx(Vertx vertx) {
    eventBus.setVertx(vertx);
    return this;
  }

  private ReliableEventBus setEventBus(EventBus eventBus) {
    if (eventBus instanceof ReliableEventBus) {
      this.eventBus = (ReliableEventBus) eventBus;
    }
    else {
      this.eventBus = new WrappedReliableEventBus(eventBus);
    }
    return this.eventBus;
  }

  @Override
  public boolean feedQueueFull() {
    return queueFull;
  }

  @Override
  public Feeder drainHandler(Handler<Void> drainHandler) {
    this.drainHandler = drainHandler;
    return this;
  }

  @Override
  public void feed(JsonObject data) {
    eventBus.send(address, Actions.create("feed", data));
  }

  @Override
  public void feed(JsonObject data, final Handler<AsyncResult<JsonObject>> resultHandler) {
    final Future<JsonObject> future = new DefaultFutureResult<JsonObject>().setHandler(resultHandler);
    eventBus.send(address, Actions.create("feed", data), new Handler<Message<JsonObject>>() {
      @Override
      public void handle(Message<JsonObject> message) {
        JsonObject body = message.body();

        // If a null value was returned then the queue is full.
        if (body == null) {
          queueFull = true;
        }
        else {
          // Otherwise, check the body for an error message.
          String error = body.getString("error");
          if (error != null) {
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
      }
    });
  }

  @Override
  public void feed(JsonObject data, long timeout, final Handler<AsyncResult<JsonObject>> resultHandler) {
    final Future<JsonObject> future = new DefaultFutureResult<JsonObject>().setHandler(resultHandler);
    eventBus.send(address, Actions.create("feed", data), timeout, new AsyncResultHandler<Message<JsonObject>>() {
      @Override
      public void handle(AsyncResult<Message<JsonObject>> result) {
        if (result.succeeded()) {
          Message<JsonObject> message = result.result();
          JsonObject body = message.body();

          // If a null value was returned then the queue is full.
          if (body == null) {
            queueFull = true;
          }
          else {
            // Otherwise, check the body for an error message.
            String error = body.getString("error");
            if (error != null) {
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
        }
        else {
          future.setFailure(result.cause());
        }
      }
    });
  }

}
