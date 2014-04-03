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

import net.kuujo.vertigo.context.OutputConnectionContext;
import net.kuujo.vertigo.message.JsonMessage;
import net.kuujo.vertigo.message.impl.ReliableJsonMessage;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;

/**
 * Output connection.
 *
 * @author Jordan Halterman
 */
public interface OutputConnection {

  /**
   * Returns the connection context.
   *
   * @return The connection context.
   */
  OutputConnectionContext context();

  /**
   * Opens the connection.
   *
   * @return The output connection.
   */
  OutputConnection open();

  /**
   * Opens the connection.
   *
   * @param doneHandler An asynchronous handler to be called once complete.
   * @return The output connection.
   */
  OutputConnection open(Handler<AsyncResult<Void>> doneHandler);

  /**
   * Sets the connection send queue maximum size.
   *
   * @param maxSize The maximum size for the connection's send queue.
   * @return The output connection.
   */
  OutputConnection setSendQueueMaxSize(int maxSize);

  /**
   * Returns a boolean indicating whether the send queue is full.
   *
   * @return Indicates whether the connection's send queue is full.
   */
  boolean sendQueueFull();

  /**
   * Sets a full handler on the connection.
   *
   * @param handler A handler to be called when the connection's send queue becomes full.
   * @return The output connection.
   */
  OutputConnection fullHandler(Handler<Void> handler);

  /**
   * Sets a drain handler on the connection.
   *
   * @param handler A handler to be called when the connection's send queue is drained.
   * @return The output connection.
   */
  OutputConnection drainHandler(Handler<Void> handler);

  /**
   * Sends a message.
   *
   * @param message The message to send.
   * @return The sent message identifier.
   */
  String send(JsonMessage message);

  /**
   * Sends a message, awaiting replication.
   *
   * @param message The message to send.
   * @param doneHandler An asynchronous handler to be called once the message has
   *        been sent or enqueued to be sent replicated.
   * @return The sent message identifier.
   */
  String send(JsonMessage message, Handler<AsyncResult<Void>> doneHandler);

  /**
   * Sends a child message.
   *
   * @param message The message to send.
   * @param parent The message parent.
   * @return The sent message identifier.
   */
  String send(JsonMessage message, ReliableJsonMessage parent);

  /**
   * Sends a child message, awaiting replication.
   *
   * @param message The message to send.
   * @param parent The message parent.
   * @param doneHandler An asynchronous handler to be called once the message has
   *        been sent or enqueued to be sent replicated.
   * @return The sent message identifier.
   */
  String send(JsonMessage message, ReliableJsonMessage parent, Handler<AsyncResult<Void>> doneHandler);

  /**
   * Closes the connection.
   */
  void close();

  /**
   * Closes the connection.
   *
   * @param doneHandler An asynchronous handler to be called once the connection is closed.
   */
  void close(Handler<AsyncResult<Void>> doneHandler);

}
