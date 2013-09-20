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

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;

/**
 * A basic executor implementation.
 *
 * @author Jordan Halterman
 */
public class ReliableExecutor extends AbstractExecutor implements Executor {

  public ReliableExecutor(VineContext context, Vertx vertx) {
    super(context, vertx);
  }

  public ReliableExecutor(VineContext context, EventBus eventBus) {
    super(context, eventBus);
  }

  public ReliableExecutor(VineContext context, Vertx vertx, EventBus eventBus) {
    super(context, vertx, eventBus);
  }

  @Override
  public Executor setFeedQueueMaxSize(long maxSize) {
    return this;
  }

  @Override
  public boolean feedQueueFull() {
    return false;
  }

  @Override
  public Executor execute(JsonObject args, Handler<AsyncResult<JsonObject>> resultHandler) {
    return this;
  }

  @Override
  public Executor execute(JsonObject args, long timeout, Handler<AsyncResult<JsonObject>> resultHandler) {
    return this;
  }

  @Override
  protected void outputReceived(Message<JsonObject> message) {
    
  }

  @Override
  public Executor drainHandler(Handler<Void> handler) {
    return this;
  }

}
