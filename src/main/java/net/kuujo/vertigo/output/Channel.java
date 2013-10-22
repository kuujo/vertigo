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
package net.kuujo.vertigo.output;

import net.kuujo.vertigo.messaging.JsonMessage;

/**
 * A uni-directional communication channel.
 *
 * @author Jordan Halterman
 */
public interface Channel {

  /**
   * Adds a connection to the channel.
   *
   * @param connection
   *   The connection to add.
   * @return
   *   The called channel instance.
   */
  public Channel addConnection(Connection connection);

  /**
   * Removes a connection from the channel.
   *
   * @param connection
   *   The connection to remove.
   * @return
   *   The called channel instance.
   */
  public Channel removeConnection(Connection connection);

  /**
   * Returns a boolean indicating whether the channel contains a connection.
   *
   * @param connection
   *   The connection to check.
   * @return
   *   A boolean indicating whether the channel contains the connection.
   */
  public boolean containsConnection(Connection connection);

  /**
   * Returns a boolean indicating whether the channel contains a connection by address.
   *
   * @param address
   *   The connection address.
   * @return
   *   A boolean indicating whether the channel contains a connection at that address.
   */
  public boolean containsConnection(String address);

  /**
   * Returns a connection by address.
   *
   * @param address
   *   The connection address.
   * @return
   *   A connection instance or null if the connection doesn't exist.
   */
  public Connection getConnection(String address);

  /**
   * Publishes a message to the channel.
   *
   * @param message
   *   The message to publish to the channel.
   * @return
   *   The called channel instance.
   */
  public Channel publish(JsonMessage message);

}
