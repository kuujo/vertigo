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

import java.util.List;

import net.kuujo.vertigo.message.JsonMessage;
import net.kuujo.vertigo.message.MessageId;

/**
 * A uni-directional communication channel.
 *
 * A channel represents a set of connections represented by a single output.
 * When a message is emitted from a component, the message is published to all
 * component channels. Within each channel, the channel selects the appropriate
 * {@link Connection} to which to publish the message and validates that the
 * target component is interested in receiving the message.
 *
 * @author Jordan Halterman
 */
public interface Channel {

  /**
   * Gets the channel identifier.
   *
   * @return
   *   The unique channel identifier.
   */
  String id();

  /**
   * Sets the target channel connection count.
   *
   * @param connectionCount
   *   The target channel connection count.
   * @return
   *   The called channel instance.
   */
  Channel setConnectionCount(int connectionCount);

  /**
   * Adds a connection to the channel.
   *
   * @param connection
   *   The connection to add.
   * @return
   *   The called channel instance.
   */
  Channel addConnection(Connection connection);

  /**
   * Removes a connection from the channel.
   *
   * @param connection
   *   The connection to remove.
   * @return
   *   The called channel instance.
   */
  Channel removeConnection(Connection connection);

  /**
   * Returns a boolean indicating whether the channel contains a connection by address.
   *
   * @param address
   *   The connection address.
   * @return
   *   A boolean indicating whether the channel contains a connection at that address.
   */
  boolean containsConnection(String address);

  /**
   * Returns a connection by address.
   *
   * @param address
   *   The connection address.
   * @return
   *   A connection instance or null if the connection doesn't exist.
   */
  Connection getConnection(String address);

  /**
   * Publishes a message to the channel.
   *
   * @param message
   *   The message to publish to the channel.
   * @return
   *   A list of written message identifiers.
   */
  List<MessageId> publish(JsonMessage message);

}
