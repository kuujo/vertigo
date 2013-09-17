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
import com.blankstyle.vine.seed.Seed;

/**
 * A Java seed verticle implementation.
 *
 * @author Jordan Halterman
 */
public abstract class SeedVerticle<T extends Seed> extends Verticle implements Handler<JsonMessage> {

  private T seed;

  /**
   * Creates a new seed instance.
   */
  protected abstract T createSeed();

  @Override
  public void start() {
    seed = createSeed();
    seed.setVertx(vertx);
    seed.setContainer(container);
    seed.init(new WorkerContext(container.config()));
    seed.messageHandler(this);
  }

  @Override
  public void handle(JsonMessage message) {
    process(message);
  }

  /**
   * Processes seed data.
   *
   * @param data
   *   The data to process.
   */
  protected abstract void process(JsonMessage message);

  /**
   * Emits seed data.
   *
   * @param data
   *   The data to emit.
   */
  protected void emit(JsonObject data) {
    seed.emit(data);
  }

  /**
   * Emits a message.
   *
   * @param message
   *   The message to emit.
   */
  protected void emit(JsonMessage message) {
    seed.emit(message);
  }

  /**
   * Emits data to a specific stream.
   *
   * @param seedName
   *   The name of the seed to which to emit the message.
   * @param data
   *   The data to emit.
   */
  protected void emitTo(String seedName, JsonObject data) {
    seed.emitTo(seedName, data);
  }

  /**
   * Emits a message to a specific stream.
   *
   * @param seedName
   *   The name of the seed to which to emit the message.
   * @param message
   *   The message to emit.
   */
  protected void emitTo(String seedName, JsonMessage message) {
    seed.emitTo(seedName, message);
  }

  /**
   * Emits child data to a specific stream.
   *
   * @param seedName
   *   The name of the seed to which to emit the message.
   * @param data
   *   The data to emit.
   * @param parent
   *   The parent message.
   */
  protected void emitTo(String seedName, JsonObject data, JsonMessage parent) {
    seed.emitTo(seedName, data, parent);
  }

  /**
   * Emits a child message to a specific stream.
   *
   * @param seedName
   *   The name of the seed to which to emit the message.
   * @param message
   *   The message to emit.
   * @param parent
   *   The parent message.
   */
  protected void emitTo(String seedName, JsonMessage message, JsonMessage parent) {
    seed.emitTo(seedName, message, parent);
  }

  /**
   * Acks the current seed message.
   *
   * @param message
   *   The message to ack.
   */
  protected void ack(JsonMessage message) {
    seed.ack(message);
  }

  /**
   * Fails the current seed message.
   *
   * @param message
   *   The message to fail.
   */
  protected void fail(JsonMessage message) {
    seed.fail(message);
  }

}
