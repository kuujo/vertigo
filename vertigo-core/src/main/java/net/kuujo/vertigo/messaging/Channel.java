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
package net.kuujo.vertigo.messaging;

import net.kuujo.vertigo.dispatcher.Dispatcher;
import net.kuujo.vertigo.filter.Condition;

/**
 * A uni-directional communication channel.
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
   * Adds a condition to the channel.
   *
   * @param condition
   *   A message condition.
   */
  public Channel addCondition(Condition condition);

  /**
   * Removes a condition from the channel.
   *
   * @param condition
   *   A message condition.
   */
  public Channel removeCondition(Condition condition);

  /**
   * Adds a connection to the channel.
   *
   * @param connection
   *   The connection to add.
   */
  public Channel addConnection(Connection connection);

  /**
   * Removes a connection from the channel.
   *
   * @param connection
   *   The connection to remove.
   */
  public Channel removeConnection(Connection connection);

  /**
   * Indicates whether a message is valid for the channel.
   *
   * @param message
   *   The message to validate.
   * @return
   *   A boolean indicating whether the given message is valid for the channel.
   */
  public boolean isValid(JsonMessage message);

  /**
   * Writes a message to the channel.
   *
   * @param message
   *   The message to write to the channel.
   * @return
   *   The called writable instance.
   */
  public Channel write(JsonMessage message);

}
