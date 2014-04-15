/*
 * Copyright 2014 the original author or authors.
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
package net.kuujo.vertigo.output;

import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

/**
 * Output interface.<p>
 *
 * This is the base interface for all output interfaces. It exposes basic
 * methods for grouping and sending messages.
 *
 * @author Jordan Halterman
 */
public interface Output<T extends Output<T>> {

  /**
   * Sets the send queue max size.
   *
   * @param maxSize The send queue max size.
   * @return The send stream.
   */
  T setSendQueueMaxSize(int maxSize);

  /**
   * Returns the send queue max size.
   *
   * @return The send queue max size.
   */
  int getSendQueueMaxSize();

  /**
   * Returns a boolean indicating whether the send queue is full.
   *
   * @return Indicates whether the send queue is full.
   */
  boolean sendQueueFull();

  /**
   * Sets a drain handler on The output.
   *
   * @param handler A handler to be called when the stream is prepared to accept
   *        new messages.
   * @return The send stream.
   */
  T drainHandler(Handler<Void> handler);

  /**
   * Sends a message on The output.
   *
   * @param message The message to send.
   * @return The output.
   */
  T send(Object message);

  /**
   * Sends a message on the output.
   *
   * @param message The message to send.
   * @param key A key with which to route the message.
   * @return The output.
   */
  T send(Object message, String key);

  /**
   * Sends a message on the output.
   *
   * @param message The message to send.
   * @return The output.
   */
  T send(String message);

  /**
   * Sends a message on the output.
   *
   * @param message The message to send.
   * @param key A key with which to route the message.
   * @return The output.
   */
  T send(String message, String key);

  /**
   * Sends a message on the output.
   *
   * @param message The message to send.
   * @return The output.
   */
  T send(Short message);

  /**
   * Sends a message on the output.
   *
   * @param message The message to send.
   * @param key A key with which to route the message.
   * @return The output.
   */
  T send(Short message, String key);

  /**
   * Sends a message on the output.
   *
   * @param message The message to send.
   * @return The output.
   */
  T send(Integer message);

  /**
   * Sends a message on the output.
   *
   * @param message The message to send.
   * @param key A key with which to route the message.
   * @return The output.
   */
  T send(Integer message, String key);

  /**
   * Sends a message on the output.
   *
   * @param message The message to send.
   * @return The output.
   */
  T send(Long message);

  /**
   * Sends a message on the output.
   *
   * @param message The message to send.
   * @param key A key with which to route the message.
   * @return The output.
   */
  T send(Long message, String key);

  /**
   * Sends a message on the output.
   *
   * @param message The message to send.
   * @return The output.
   */
  T send(Float message);

  /**
   * Sends a message on the output.
   *
   * @param message The message to send.
   * @param key A key with which to route the message.
   * @return The output.
   */
  T send(Float message, String key);

  /**
   * Sends a message on the output.
   *
   * @param message The message to send.
   * @return The output.
   */
  T send(Double message);

  /**
   * Sends a message on the output.
   *
   * @param message The message to send.
   * @param key A key with which to route the message.
   * @return The output.
   */
  T send(Double message, String key);

  /**
   * Sends a message on the output.
   *
   * @param message The message to send.
   * @return The output.
   */
  T send(Boolean message);

  /**
   * Sends a message on the output.
   *
   * @param message The message to send.
   * @param key A key with which to route the message.
   * @return The output.
   */
  T send(Boolean message, String key);

  /**
   * Sends a message on the output.
   *
   * @param message The message to send.
   * @return The output.
   */
  T send(Byte message);

  /**
   * Sends a message on the output.
   *
   * @param message The message to send.
   * @param key A key with which to route the message.
   * @return The output.
   */
  T send(Byte message, String key);

  /**
   * Sends a message on the output.
   *
   * @param message The message to send.
   * @return The output.
   */
  T send(byte[] message);

  /**
   * Sends a message on the output.
   *
   * @param message The message to send.
   * @param key A key with which to route the message.
   * @return The output.
   */
  T send(byte[] message, String key);

  /**
   * Sends a message on the output.
   *
   * @param message The message to send.
   * @return The output.
   */
  T send(Character message);

  /**
   * Sends a message on the output.
   *
   * @param message The message to send.
   * @param key A key with which to route the message.
   * @return The output.
   */
  T send(Character message, String key);

  /**
   * Sends a message on the output.
   *
   * @param message The message to send.
   * @return The output.
   */
  T send(Buffer message);

  /**
   * Sends a message on the output.
   *
   * @param message The message to send.
   * @param key A key with which to route the message.
   * @return The output.
   */
  T send(Buffer message, String key);

  /**
   * Sends a message on the output.
   *
   * @param message The message to send.
   * @return The output.
   */
  T send(JsonArray message);

  /**
   * Sends a message on the output.
   *
   * @param message The message to send.
   * @param key A key with which to route the message.
   * @return The output.
   */
  T send(JsonArray message, String key);

  /**
   * Sends a message on the output.
   *
   * @param message The message to send.
   * @return The output.
   */
  T send(JsonObject message);

  /**
   * Sends a message on the output.
   *
   * @param message The message to send.
   * @param key A key with which to route the message.
   * @return The output.
   */
  T send(JsonObject message, String key);

  /**
   * Creates an output group.
   *
   * @param name The output group name.
   * @param handler A handler to be called once the output group is created.
   * @return The output buffer.
   */
  T group(String name, Handler<OutputGroup> handler);

}
