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
package net.kuujo.vevent.messaging;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.Future;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.impl.DefaultFutureResult;
import org.vertx.java.core.json.JsonObject;

/**
 * An eventbus-based connection.
 *
 * @author Jordan Halterman
 */
public class EventBusConnection implements Connection {

  protected String address;

  protected EventBus eventBus;

  public EventBusConnection(String address) {
    this.address = address;
  }

  public EventBusConnection(String address, EventBus eventBus) {
    this.address = address;
    this.eventBus = eventBus;
  }

  public void setEventBus(EventBus eventBus) {
    this.eventBus = eventBus;
  }

  @Override
  public String getAddress() {
    return address;
  }

  @Override
  public Connection write(JsonMessage message) {
    eventBus.send(address, new JsonObject().putString("action", "receive").putObject("message", message.serialize()));
    return this;
  }

  @Override
  public Connection write(JsonMessage message, Handler<AsyncResult<Boolean>> replyHandler) {
    doSend(message, 0, false, 0, new DefaultFutureResult<Boolean>().setHandler(replyHandler));
    return this;
  }

  @Override
  public Connection write(JsonMessage message, long timeout, Handler<AsyncResult<Boolean>> replyHandler) {
    doSend(message, timeout, false, 0, new DefaultFutureResult<Boolean>().setHandler(replyHandler));
    return this;
  }

  @Override
  public Connection write(JsonMessage message, long timeout, boolean retry, Handler<AsyncResult<Boolean>> replyHandler) {
    doSend(message, timeout, retry, -1, new DefaultFutureResult<Boolean>().setHandler(replyHandler));
    return this;
  }

  @Override
  public Connection write(JsonMessage message, long timeout, boolean retry, int attempts, Handler<AsyncResult<Boolean>> replyHandler) {
    doSend(message, timeout, retry, attempts, new DefaultFutureResult<Boolean>().setHandler(replyHandler));
    return this;
  }

  private <T> void doSend(final JsonMessage message, final long timeout, final boolean retry, final int attempts, final Future<Boolean> future) {
    if (timeout > 0) {
      eventBus.sendWithTimeout(address, new JsonObject().putString("action", "receive").putObject("message", message.serialize()), timeout, new AsyncResultHandler<Message<Boolean>>() {
        @Override
        public void handle(AsyncResult<Message<Boolean>> result) {
          if (result.failed()) {
            if (retry && attempts == -1 || attempts > 0) {
              doSend(message, timeout, retry, attempts == -1 ? attempts : attempts-1, future);
            }
            else {
              future.setFailure(result.cause());
            }
          }
          else {
            future.setResult(result.result().body());
          }
        }
      });
    }
    else {
      eventBus.send(address, new JsonObject().putString("action", "receive").putObject("message", message.serialize()), new Handler<Message<Boolean>>() {
        @Override
        public void handle(Message<Boolean> message) {
          future.setResult(message.body());
        }
      });
    }
  }

}
