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
package net.kuujo.vitis.node.worker;

import net.kuujo.vitis.messaging.JsonMessage;

import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonObject;

/**
 * A vine seed. Nodes instances are essentially individual tasks.
 *
 * @author Jordan Halterman
 */
public interface Worker {

  /**
   * Sets a seed data handler.
   *
   * @param handler
   *   A json data handler.
   * @return 
   *   The called seed instance.
   */
  public Worker dataHandler(Handler<JsonMessage> handler);

  /**
   * Emits data from the seed.
   *
   * @param data
   *   The data to emit.
   */
  public void emit(JsonObject data);

  /**
   * Emits data from the seed with a tag.
   *
   * @param data
   *   The data to emit.
   * @param tag
   *   A tag to apply to the message.
   */
  public void emit(JsonObject data, String tag);

  /**
   * Emits child data from the seed.
   *
   * @param data
   *   The data to emit.
   * @param parent
   *   The parent message.
   */
  public void emit(JsonObject data, JsonMessage parent);

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
  public void emit(JsonObject data, String tag, JsonMessage parent);

  /**
   * Acknowledges processing of a message.
   *
   * @param message
   *   The message to ack.
   */
  public void ack(JsonMessage message);

  /**
   * Acknowledges processing of multiple messages.
   *
   * @param messages
   *   The messages to ack.
   */
  public void ack(JsonMessage... messages);

  /**
   * Fails processing of a message.
   *
   * @param message
   *   The message to fail.
   */
  public void fail(JsonMessage message);

  /**
   * Fails processing of multiple messages.
   *
   * @param messages
   *   The messages to fail.
   */
  public void fail(JsonMessage... messages);

  /**
   * Starts the seed.
   */
  public void start();

}
