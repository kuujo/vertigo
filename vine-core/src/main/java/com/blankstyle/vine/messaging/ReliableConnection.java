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
package com.blankstyle.vine.messaging;

import org.vertx.java.core.AsyncResultHandler;

/**
 * A reliable point-to-point connection
 *
 * @author Jordan Halterman
 */
public interface ReliableConnection extends Connection {

  /**
   * Sends a message with a default timeout.
   *
   * @param message
   *   The message.
   * @param doneHandler
   *   An asynchronous reply handler. This will be invoked with the message reply,
   *   or with a TimeoutException if the response times out.
   */
  public Connection send(JsonMessage message, AsyncResultHandler<Void> doneHandler);

  /**
   * Sends a message with a timeout.
   *
   * @param message
   *   The message.
   * @param timeout
   *   The message timeout.
   * @param doneHandler
   *   An asynchronous reply handler. This will be invoked with the message reply,
   *   or with a TimeoutException if the response times out.
   */
  public Connection send(JsonMessage message, long timeout, AsyncResultHandler<Void> doneHandler);

  /**
   * Sends a message with a timeout, attempting to re-send the message if a timeout occurs.
   *
   * @param message
   *   The message.
   * @param timeout
   *   The message timeout.
   * @param retry
   *   A boolean indicating whether to attempt to re-send messages.
   * @param doneHandler
   *   An asynchronous reply handler. This will be invoked with the message reply,
   *   or with a TimeoutException if the response times out.
   * @return
   *   The called Connection instance.
   */
  public Connection send(JsonMessage message, long timeout, boolean retry, AsyncResultHandler<Void> doneHandler);

  /**
   * Sends a message with a timeout, attempting to re-send the message if a timeout occurs.
   *
   * @param message
   *   The message.
   * @param timeout
   *   The message timeout.
   * @param retry
   *   A boolean indicating whether to attempt to re-send messages.
   * @param attempts
   *   The number of re-send attempts allowed.
   * @param doneHandler
   *   An asynchronous reply handler. This will be invoked with the message reply,
   *   or with a TimeoutException if the response times out.
   * @return
   *   The called Connection instance.
   */
  public Connection send(JsonMessage message, long timeout, boolean retry, int attempts, AsyncResultHandler<Void> doneHandler);

}
