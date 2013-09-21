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
package net.kuujo.vine.feeder;

import net.kuujo.vine.context.VineContext;
import net.kuujo.vine.messaging.JsonMessage;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.impl.DefaultFutureResult;
import org.vertx.java.core.json.JsonObject;

/**
 * An unreliable feeder implementation.
 *
 * @author Jordan Halterman
 */
public class UnreliableFeeder extends AbstractFeeder implements Feeder {

  public UnreliableFeeder(VineContext context, Vertx vertx) {
    super(context, vertx);
    init();
  }

  public UnreliableFeeder(VineContext context, EventBus eventBus) {
    super(context, eventBus);
    init();
  }

  public UnreliableFeeder(VineContext context, Vertx vertx, EventBus eventBus) {
    super(context, vertx, eventBus);
    init();
  }

  @Override
  public Feeder setFeedQueueMaxSize(long maxSize) {
    return this;
  }

  @Override
  public boolean feedQueueFull() {
    return false;
  }

  @Override
  public Feeder feed(JsonObject data) {
    output.emit(new JsonMessage(data));
    return this;
  }

  @Override
  public Feeder feed(JsonObject data, Handler<AsyncResult<Void>> doneHandler) {
    output.emit(new JsonMessage(data));
    new DefaultFutureResult<Void>().setHandler(doneHandler).setResult(null);
    return this;
  }

  @Override
  public Feeder feed(JsonObject data, long timeout, Handler<AsyncResult<Void>> doneHandler) {
    return feed(data, doneHandler);
  }

  @Override
  public Feeder drainHandler(Handler<Void> handler) {
    return this;
  }

}
