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

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;

/**
 * A point-to-point message bus.
 *
 * @author Jordan Halterman
 */
public interface MessageBus {

  /**
   * Sends a message on the message bus.
   *
   * @param address
   *   The address to which to send the message.
   * @param message
   *   The message to send.
   * @return
   *   The called message bus instance.
   */
  public MessageBus send(String address, JsonMessage message);

  /**
   * Sends a message on the message bus with an ack handler.
   *
   * @param address
   *   The address to which to send the message.
   * @param message
   *   The message to send.
   * @param ackHandler
   *   A message ack handler.
   * @return
   *   The called message bus instance.
   */
  public MessageBus send(String address, JsonMessage message, Handler<Boolean> ackHandler);

  /**
   * Sends a message on the message bus with an ack handler.
   *
   * @param address
   *   The address to which to send the message.
   * @param message
   *   The message to send.
   * @param timeout
   *   A message ack timeout.
   * @param ackHandler
   *   A message ack handler.
   * @return
   *   The called message bus instance.
   */
  public MessageBus send(String address, JsonMessage message, long timeout, Handler<Boolean> ackHandler);

  /**
   * Sends a message on the message bus with an ack handler.
   *
   * @param address
   *   The address to which to send the message.
   * @param message
   *   The message to send.
   * @param timeout
   *   A message ack timeout.
   * @param retry
   *   Indicates whether to retry sending the message if a timeout occurs.
   * @param ackHandler
   *   A message ack handler.
   * @return
   *   The called message bus instance.
   */
  public MessageBus send(String address, JsonMessage message, long timeout, boolean retry, Handler<Boolean> ackHandler);

  /**
   * Sends a message on the message bus with an ack handler.
   *
   * @param address
   *   The address to which to send the message.
   * @param message
   *   The message to send.
   * @param timeout
   *   A message ack timeout.
   * @param retry
   *   Indicates whether to retry sending the message if a timeout occurs.
   * @param attempts
   *   Indicates the number of attempts when resending timed out messages.
   * @param ackHandler
   *   A message ack handler.
   * @return
   *   The called message bus instance.
   */
  public MessageBus send(String address, JsonMessage message, long timeout, boolean retry, int attempts, Handler<Boolean> ackHandler);

  /**
   * Registers a message handler.
   *
   * @param address
   *   The address at which to register the handler.
   * @param handler
   *   A message handler.
   * @return
   *   The called message bus instance.
   */
  public MessageBus registerHandler(String address, Handler<JsonMessage> handler);

  /**
   * Registers a message handler.
   *
   * @param address
   *   The address at which to register the handler.
   * @param handler
   *   A message handler.
   * @param doneHandler
   *   A handler to be invoked once the registration has been propagated around
   *   the cluster.
   * @return
   *   The called message bus instance.
   */
  public MessageBus registerHandler(String address, Handler<JsonMessage> handler, Handler<AsyncResult<Void>> doneHandler);

  /**
   * Registers a local message handler.
   *
   * @param address
   *   The address at which to register the handler.
   * @param handler
   *   A message handler.
   * @return
   *   The called message bus instance.
   */
  public MessageBus registerLocalHandler(String address, Handler<JsonMessage> handler);

}
