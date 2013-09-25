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
package net.kuujo.vitis.feeder;

import net.kuujo.vitis.context.VineContext;

import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;

/**
 * An abstract executor.
 *
 * @author Jordan Halterman
 */
public abstract class AbstractExecutor extends AbstractFeeder {

  public AbstractExecutor(VineContext context, Vertx vertx) {
    super(context, vertx);
  }

  public AbstractExecutor(VineContext context, EventBus eventBus) {
    super(context, eventBus);
  }

  public AbstractExecutor(VineContext context, Vertx vertx, EventBus eventBus) {
    super(context, vertx, eventBus);
  }

  @Override
  protected void init() {
    super.init();
    setupInputs();
  }

  /**
   * Sets up input handlers.
   */
  protected void setupInputs() {
    eventBus.registerHandler(context.getAddress(), new Handler<Message<JsonObject>>() {
      @Override
      public void handle(Message<JsonObject> message) {
        outputReceived(message);
      }
    });
  }

  /**
   * Called when output is received from a vine.
   *
   * @param message
   *   The output message.
   */
  protected abstract void outputReceived(Message<JsonObject> message);

}
