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
package net.kuujo.vevent.java;

import net.kuujo.vevent.context.WorkerContext;
import net.kuujo.vevent.messaging.JsonMessage;
import net.kuujo.vevent.node.BasicWorker;
import net.kuujo.vevent.node.Worker;

import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Verticle;

/**
 * A Java worker verticle implementation.
 *
 * @author Jordan Halterman
 */
public abstract class WorkerVerticle extends Verticle implements Handler<JsonMessage> {

  private Worker worker;

  @Override
  public void start() {
    worker = new BasicWorker(vertx, container, new WorkerContext(container.config()));
    worker.dataHandler(this);
    worker.start();
  }

  /**
   * Emits data to all output streams.
   *
   * @param data
   *   The data to emit.
   */
  protected void emit(JsonObject data) {
    worker.emit(data);
  }

  /**
   * Emits data from the worker with a tag.
   *
   * @param data
   *   The data to emit.
   * @param tag
   *   A tag to apply to the message.
   */
  protected void emit(JsonObject data, String tag) {
    worker.emit(data, tag);
  }

  /**
   * Emits child data from the worker.
   *
   * @param data
   *   The data to emit.
   * @param parent
   *   The parent message.
   */
  protected void emit(JsonObject data, JsonMessage parent) {
    worker.emit(data, parent);
  }

  /**
   * Emits child data from the worker with a tag.
   *
   * @param data
   *   The data to emit.
   * @param tag
   *   A tag to apply to the message.
   * @param parent
   *   The parent message.
   */
  protected void emit(JsonObject data, String tag, JsonMessage parent) {
    worker.emit(data, tag, parent);
  }

  /**
   * Acknowledges processing of a message.
   *
   * @param message
   *   The message to ack.
   */
  protected void ack(JsonMessage message) {
    worker.ack(message);
  }

  /**
   * Acknowledges processing of multiple messages.
   *
   * @param message
   *   The message to ack.
   */
  protected void ack(JsonMessage... messages) {
    worker.ack(messages);
  }

  /**
   * Fails processing of a message.
   *
   * @param message
   *   The message to fail.
   */
  protected void fail(JsonMessage message) {
    worker.fail(message);
  }

  /**
   * Fails processing of multiple messages.
   *
   * @param message
   *   The message to fail.
   */
  protected void fail(JsonMessage... messages) {
    worker.fail(messages);
  }

}
