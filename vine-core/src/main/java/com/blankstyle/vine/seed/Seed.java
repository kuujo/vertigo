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

import org.vertx.java.core.AsyncResult;
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
   * Sets the seed context.
   *
   * @param context
   *   A seed context.
   * @return
   *   The called seed instance.
   */
  public Seed setContext(WorkerContext context);

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
   * Gets the seed auto ack setting.
   *
   * @return
   *   A boolean indicating whether auto acking is enabled.
   */
  public boolean autoAck();

  /**
   * Sets the seed auto ack setting.
   *
   * @param ack
   *   A boolean indicating whether to automatically ack all messages.
   * @return
   *   The called seed instance.
   */
  public Seed autoAck(boolean ack);

  /**
   * Gets the seed auto fork setting.
   *
   * @return
   *   A boolean indicating whether auto forking is enabled.
   */
  public boolean autoFork();

  /**
   * Sets the seed auto fork setting.
   *
   * @param cardinality
   *   A boolean indicating whether to automatically fork all messages.
   * @return
   *   The called seed instance.
   */
  public Seed autoFork(boolean cardinality);

  /**
   * Emits data from the seed, creating a new JSON message.
   *
   * @param data
   *   The data to emit.
   */
  public void emit(JsonObject data);

  /**
   * Emits data from the seed, providing an acknowledgment handler.
   *
   * @param data
   *   The data to emit.
   * @param ackHandler
   *   An asynchronous handler to be invoked when the message is acked.
   */
  public void emit(JsonObject data, Handler<AsyncResult<Void>> ackHandler);

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
   * Emits child data from the seed, providing an acknowledgment handler.
   *
   * @param data
   *   The data to emit.
   * @param parent
   *   The parent message.
   * @param ackHandler
   *   An asynchronous handler to be invoked when the message is acked.
   */
  public void emit(JsonObject data, JsonMessage parent, Handler<AsyncResult<Void>> ackHandler);

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
   * Emits a data to a specific seed, creating a new message.
   *
   * @param seedName
   *   The seed name to which to emit the message.
   * @param data
   *   The data to emit.
   * @param ackHandler
   *   An asynchronous handler to be invoked when the message is acked.
   */
  public void emitTo(String seedName, JsonObject data, Handler<AsyncResult<Void>> ackHandler);

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
   * Emits child data to a specific seed, creating a new message.
   *
   * @param seedName
   *   The seed name to which to emit the message.
   * @param data
   *   The data to emit.
   * @param parent
   *   The message parent.
   * @param ackHandler
   *   An asynchronous handler to be invoked when the message is acked.
   */
  public void emitTo(String seedName, JsonObject data, JsonMessage parent, Handler<AsyncResult<Void>> ackHandler);

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
