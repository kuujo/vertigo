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
package com.blankstyle.vine.seed;

import java.util.Collection;

import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Container;

import com.blankstyle.vine.context.WorkerContext;
import com.blankstyle.vine.messaging.JsonMessage;

/**
 * A vine seed. Seeds instances are essentially individual tasks.
 *
 * @author Jordan Halterman
 */
public interface Seed {

  /**
   * Sets the seed vertx instance.
   *
   * @param vertx
   *   A vertx instance.
   * @return
   *   The called seed instance.
   */
  public Seed setVertx(Vertx vertx);

  /**
   * Sets the seed container instance.
   *
   * @param container
   *   A Vert.x container.
   * @return
   *   The called seed instance.
   */
  public Seed setContainer(Container container);

  /**
   * Initializes the seed with the given context.
   *
   * @param context
   *   A seed context.
   * @return
   *   The called seed instance.
   */
  public Seed init(WorkerContext context);

  /**
   * Sets the seed message handler.
   *
   * @param handler
   *   A json message handler.
   * @return
   *   The called seed instance.
   */
  public Seed messageHandler(Handler<JsonMessage> handler);

  /**
   * Emits data from the seed, creating a new JSON message.
   *
   * @param data
   *   The data to emit.
   */
  public void emit(JsonObject data);

  /**
   * Emits a message from the seed.
   *
   * @param message
   *   The message to emit.
   */
  public void emit(JsonMessage message);

  /**
   * Emits a collection of messages from the seed.
   *
   * @param messages
   *   The messages to emit.
   */
  public void emit(Collection<JsonMessage> messages);

  /**
   * Emits child data from the seed, creating a new message.
   *
   * @param data
   *   The data to emit.
   * @param parent
   *   The parent message.
   */
  public void emit(JsonObject data, JsonMessage parent);

  /**
   * Emits a child message from the seed.
   *
   * @param message
   *   The message to emit.
   * @param parent
   *   The parent message.
   */
  public void emit(JsonMessage message, JsonMessage parent);

  /**
   * Emits a collection of child messages from the seed.
   *
   * @param messages
   *   The messages to emit.
   * @param parent
   *   The parent message.
   */
  public void emit(Collection<JsonMessage> messages, JsonMessage parent);

  /**
   * Emits a data to a specific seed, creating a new message.
   *
   * @param seedName
   *   The seed name to which to emit the message.
   * @param data
   *   The data to emit.
   */
  public void emitTo(String seedName, JsonObject data);

  /**
   * Emits a message to a specific seed.
   *
   * @param seedName
   *   The seed name to which to emit the message.
   * @param message
   *   The message to emit.
   */
  public void emitTo(String seedName, JsonMessage message);

  /**
   * Emits a collection of messages to a specific seed.
   *
   * @param seedName
   *   The seed name to which to emit the message.
   * @param messages
   *   The messages to emit.
   */
  public void emitTo(String seedName, Collection<JsonMessage> messages);

  /**
   * Emits child data to a specific seed, creating a new message.
   *
   * @param seedName
   *   The seed name to which to emit the message.
   * @param data
   *   The data to emit.
   * @param parent
   *   The message parent.
   */
  public void emitTo(String seedName, JsonObject data, JsonMessage parent);

  /**
   * Emits a child message to a specific seed.
   *
   * @param seedName
   *   The seed name to which to emit the message.
   * @param message
   *   The message to emit.
   * @param parent
   *   The message parent.
   */
  public void emitTo(String seedName, JsonMessage message, JsonMessage parent);

  /**
   * Emits a collection of child messages to a specific seed.
   *
   * @param seedName
   *   The seed name to which to emit the message.
   * @param messages
   *   The messages to emit.
   * @param parent
   *   The message parent.
   */
  public void emitTo(String seedName, Collection<JsonMessage> messages, JsonMessage parent);

  /**
   * Acks a message.
   *
   * @param message
   *   The message to ack.
   */
  public void ack(JsonMessage message);

  /**
   * Acks multiple messages.
   *
   * @param messages
   *   The messages to ack.
   */
  public void ack(JsonMessage... messages);

  /**
   * Fails a message.
   *
   * @param message
   *   The message to fail.
   */
  public void fail(JsonMessage message);

  /**
   * Fails multiple messages.
   *
   * @param messages
   *   The messages to fail.
   */
  public void fail(JsonMessage... messages);

}
