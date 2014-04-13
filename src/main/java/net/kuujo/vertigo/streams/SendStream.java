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
package net.kuujo.vertigo.streams;

import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

/**
 * Send stream.
 *
 * @author Jordan Halterman
 *
 * @param <T> The stream type.
 */
public interface SendStream<T extends SendStream<T>> {

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
   * Sets a drain handler on the stream.
   *
   * @param handler A handler to be called when the stream is prepared to accept
   *        new messages.
   * @return The send stream.
   */
  T drainHandler(Handler<Void> handler);

  /**
   * Sends a message on the stream.
   *
   * @param message The message to send.
   * @return The stream.
   */
  T send(Object message);

  /**
   * Sends a message on the stream.
   *
   * @param message The message to send.
   * @return The stream.
   */
  T send(String message);

  /**
   * Sends a message on the stream.
   *
   * @param message The message to send.
   * @return The stream.
   */
  T send(Short message);

  /**
   * Sends a message on the stream.
   *
   * @param message The message to send.
   * @return The stream.
   */
  T send(Integer message);

  /**
   * Sends a message on the stream.
   *
   * @param message The message to send.
   * @return The stream.
   */
  T send(Long message);

  /**
   * Sends a message on the stream.
   *
   * @param message The message to send.
   * @return The stream.
   */
  T send(Float message);

  /**
   * Sends a message on the stream.
   *
   * @param message The message to send.
   * @return The stream.
   */
  T send(Double message);

  /**
   * Sends a message on the stream.
   *
   * @param message The message to send.
   * @return The stream.
   */
  T send(Boolean message);

  /**
   * Sends a message on the stream.
   *
   * @param message The message to send.
   * @return The stream.
   */
  T send(Byte message);

  /**
   * Sends a message on the stream.
   *
   * @param message The message to send.
   * @return The stream.
   */
  T send(byte[] message);

  /**
   * Sends a message on the stream.
   *
   * @param message The message to send.
   * @return The stream.
   */
  T send(Character message);

  /**
   * Sends a message on the stream.
   *
   * @param message The message to send.
   * @return The stream.
   */
  T send(Buffer message);

  /**
   * Sends a message on the stream.
   *
   * @param message The message to send.
   * @return The stream.
   */
  T send(JsonArray message);

  /**
   * Sends a message on the stream.
   *
   * @param message The message to send.
   * @return The stream.
   */
  T send(JsonObject message);

}
