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
package com.blankstyle.vine.java;

import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Verticle;

import com.blankstyle.vine.context.WorkerContext;
import com.blankstyle.vine.messaging.JsonMessage;
import com.blankstyle.vine.seed.ReliableSeed;
import com.blankstyle.vine.seed.Seed;

/**
 * A basic seed verticle implementation.
 *
 * @author Jordan Halterman
 */
public abstract class BasicSeedVerticle extends Verticle implements Handler<JsonMessage> {

  private Seed seed;

  private JsonMessage currentMessage;

  @Override
  public void handle(JsonMessage message) {
    process(message);
  }

  @Override
  public void start() {
    seed = new ReliableSeed();
    seed.setVertx(vertx);
    seed.setContainer(container);
    seed.setContext(new WorkerContext(container.config()));
    seed.messageHandler(this);
  }

  /**
   * Processes seed data.
   *
   * @param data
   *   The data to process.
   */
  protected void process(JsonMessage message) {
    currentMessage = message;
    process(message.body());
  }

  /**
   * Processes seed data.
   *
   * @param data
   *   The data to process.
   */
  protected abstract void process(JsonObject data);

  protected void emit(JsonObject data) {
    seed.emit(data, currentMessage);
  }

  protected void emitTo(String seedName, JsonObject data) {
    seed.emitTo(seedName, data, currentMessage);
  }

}
