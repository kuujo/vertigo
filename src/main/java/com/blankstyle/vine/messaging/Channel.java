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

import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

/**
 * A bi-directional communication channel.
 *
 * @author Jordan Halterman
 */
public interface Channel {

  /**
   * Sets the channel dispatcher.
   *
   * @param dispatcher
   *   The channel dispatcher.
   */
  public void setDispatcher(Dispatcher dispatcher);

  /**
   * Gets the channel dispatcher.
   *
   * @return
   *   The channel message dispatcher.
   */
  public Dispatcher getDispatcher();

  /**
   * Adds a connection to the channel.
   *
   * @param connection
   *   The connection to add.
   */
  public void addConnection(Connection connection);

  /**
   * Removes a connection from the channel.
   *
   * @param connection
   *   The connection to remove.
   */
  public void removeConnection(Connection connection);

  /**
   * Publishes a message to the channel.
   *
   * @param message
   *   The message to publish.
   */
  public void publish(Object message);

  /**
   * Publishes a message to the channel.
   *
   * @param message
   *   The message to publish.
   */
  public void publish(JsonObject message);

  /**
   * Sends a message through the channel.
   *
   * @param message
   *   The message to publish.
   */
  public void publish(JsonArray message);

  /**
   * Sends a message through the channel.
   *
   * @param message
   *   The message to publish.
   */
  public void publish(Buffer message);

  /**
   * Sends a message through the channel.
   *
   * @param message
   *   The message to publish.
   */
  public void publish(byte[] message);

  /**
   * Sends a message through the channel.
   *
   * @param message
   *   The message to publish.
   */
  public void publish(String message);

  /**
   * Sends a message through the channel.
   *
   * @param message
   *   The message to publish.
   */
  public void publish(Integer message);

  /**
   * Sends a message through the channel.
   *
   * @param message
   *   The message to publish.
   */
  public void publish(Long message);

  /**
   * Sends a message through the channel.
   *
   * @param message
   *   The message to publish.
   */
  public void publish(Float message);

  /**
   * Sends a message through the channel.
   *
   * @param message
   *   The message to publish.
   */
  public void publish(Double message);

  /**
   * Sends a message through the channel.
   *
   * @param message
   *   The message to publish.
   */
  public void publish(Boolean message);

  /**
   * Sends a message through the channel.
   *
   * @param message
   *   The message to publish.
   */
  public void publish(Short message);

  /**
   * Sends a message through the channel.
   *
   * @param message
   *   The message to publish.
   */
  public void publish(Character message);

  /**
   * Sends a message through the channel.
   *
   * @param message
   *   The message to publish.
   */
  public void publish(Byte message);

}
