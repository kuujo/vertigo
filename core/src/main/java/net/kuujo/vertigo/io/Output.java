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
package net.kuujo.vertigo.io;

import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

/**
 * Output interface.<p>
 *
 * This is the base interface for all output interfaces. It exposes basic
 * methods for grouping and sending messages.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public interface Output<T extends Output<T>> {

  /**
   * Returns the output's Vertx instance.
   *
   * @return A vertx instance.
   */
  Vertx vertx();

  /**
   * Returns the current connection send queue size.
   *
   * @return The current connection send queue size.
   */
  int size();

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
   * Sets a drain handler on the output.<p>
   *
   * When the output's send queue becomes full, the output will be temporarily
   * paused while its output queue is empty. Once the queue size decreases
   * back to 50% of the maximum queue size the drain handler will be called
   * so that normal operation can resume.
   *
   * @param handler A handler to be called when the stream is prepared to accept
   *        new messages.
   * @return The send stream.
   */
  T drainHandler(Handler<Void> handler);

  /**
   * Sends a message on the output.
   *
   * @param message The message to send.
   * @return The output.
   */
  T send(Object message);

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
   * @return The output.
   */
  T send(Short message);

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
   * @return The output.
   */
  T send(Long message);

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
   * @return The output.
   */
  T send(Double message);

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
   * @return The output.
   */
  T send(Byte message);

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
   * @return The output.
   */
  T send(Character message);

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
   * @return The output.
   */
  T send(JsonArray message);

  /**
   * Sends a message on the output.
   *
   * @param message The message to send.
   * @return The output.
   */
  T send(JsonObject message);

}
