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
import net.kuujo.vine.seed.DefaultSeed;
import net.kuujo.vine.seed.Seed;

import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Verticle;

/**
 * A Java seed verticle implementation.
 *
 * @author Jordan Halterman
 */
public abstract class SeedVerticle extends Verticle implements Handler<JsonMessage> {

  private Seed seed;

  @Override
  public void start() {
    seed = new DefaultSeed();
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
   * Emits data from the seed with a tag.
   *
   * @param data
   *   The data to emit.
   * @param tag
   *   A tag to apply to the message.
   */
  public void emit(JsonObject data, String tag) {
    seed.emit(data, tag);
  }

  /**
   * Emits data from the seed with a tag.
   *
   * @param data
   *   The data to emit.
   * @param tags
   *   An array of tags to apply to the message.
   */
  public void emit(JsonObject data, String[] tags) {
    seed.emit(data, tags);
  }

  /**
   * Emits child data from the seed.
   *
   * @param data
   *   The data to emit.
   * @param parent
   *   The parent message.
   */
  public void emit(JsonObject data, JsonMessage parent) {
    seed.emit(data, parent);
  }

  /**
   * Emits child data from the seed with a tag.
   *
   * @param data
   *   The data to emit.
   * @param tag
   *   A tag to apply to the message.
   * @param parent
   *   The parent message.
   */
  public void emit(JsonObject data, String tag, JsonMessage parent) {
    seed.emit(data, tag, parent);
  }

  /**
   * Emits child data from the seed with a tag.
   *
   * @param data
   *   The data to emit.
   * @param tags
   *   An array of tags to apply to the message.
   * @param parent
   *   The parent message.
   */
  public void emit(JsonObject data, String[] tags, JsonMessage parent) {
    seed.emit(data, tags, parent);
  }

  /**
   * Acknowledges processing of a message.
   *
   * @param message
   *   The message to ack.
   */
  public void ack(JsonMessage message) {
    seed.ack(message);
  }

  /**
   * Acknowledges processing of multiple messages.
   *
   * @param message
   *   The message to ack.
   */
  public void ack(JsonMessage... messages) {
    seed.ack(messages);
  }

  /**
   * Fails processing of a message.
   *
   * @param message
   *   The message to fail.
   */
  public void fail(JsonMessage message) {
    seed.fail(message);
  }

  /**
   * Fails processing of multiple messages.
   *
   * @param message
   *   The message to fail.
   */
  public void fail(JsonMessage... messages) {
    seed.fail(messages);
  }

}
