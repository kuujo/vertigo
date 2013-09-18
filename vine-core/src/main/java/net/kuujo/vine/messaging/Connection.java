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
package net.kuujo.vine.messaging;

import org.vertx.java.core.Handler;

/**
 * A single point-to-point connection.
 *
 * @author Jordan Halterman
 */
public interface Connection {

  /**
   * Gets the remote channel address.
   *
   * @return
   *   The remote channel address.
   */
  public String getAddress();

  /**
   * Sends a message through the channel.
   *
   * @param message
   *   The message to send.
   */
  public Connection send(JsonMessage message);

  /**
   * Sends a message through the channel, providing a handler for a return value.
   *
   * @param message
   *   The message to send.
   * @param ackHandler
   *   A message ack handler.
   */
  public Connection send(JsonMessage message, Handler<Boolean> ackHandler);

}
