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
package net.kuujo.vine.java;

import net.kuujo.vine.context.WorkerContext;
import net.kuujo.vine.messaging.JsonMessage;
import net.kuujo.vine.seed.Seed;

import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Verticle;

/**
 * A Java seed verticle implementation.
 *
 * @author Jordan Halterman
 */
abstract class SeedVerticle<T extends Seed> extends Verticle implements Handler<JsonObject> {

  private T seed;

  protected abstract T createSeed();

  @Override
  public void start() {
    seed = createSeed();
    seed.setVertx(vertx);
    seed.setContainer(container);
    seed.setContext(new WorkerContext(container.config()));
    seed.dataHandler(this);
    seed.start();
  }

  /**
   * Emits data to all output streams.
   *
   * @param data
   *   The data to emit.
   */
  public void emit(JsonObject data) {
    seed.emit(data);
  }

  /**
   * Emits multiple sets of data to all output streams.
   *
   * @param data
   *   The data to emit.
   */
  public void emit(JsonObject... data) {
    seed.emit(data);
  }

  /**
   * Emits data to a specific output stream.
   *
   * @param seedName
   *   The seed name to which to emit.
   * @param data
   *   The data to emit.
   */
  public void emitTo(String seedName, JsonObject data) {
    seed.emitTo(seedName, data);
  }

  /**
   * Emits multiple sets of data to a specific output stream.
   *
   * @param seedName
   *   The seed name to which to emit.
   * @param data
   *   The data to emit.
   */
  public void emitTo(String seedName, JsonObject... data) {
    seed.emitTo(seedName, data);
  }

  /**
   * Acknowledges processing of a message.
   *
   * @param data
   *   The data to ack.
   */
  public void ack(JsonObject data) {
    seed.ack((JsonMessage) data);
  }

  /**
   * Acknowledges processing of multiple messages.
   *
   * @param data
   *   The data to ack.
   */
  public void ack(JsonObject... data) {
    seed.ack((JsonMessage[]) data);
  }

  /**
   * Fails processing of a message.
   *
   * @param data
   *   The data to fail.
   */
  public void fail(JsonObject data) {
    seed.fail((JsonMessage) data);
  }

  /**
   * Fails processing of multiple messages.
   *
   * @param data
   *   The data to fail.
   */
  public void fail(JsonObject... data) {
    seed.fail((JsonMessage[]) data);
  }

}
