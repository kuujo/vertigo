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
package net.kuujo.vertigo.output.selector;

import java.util.List;

import net.kuujo.vertigo.messaging.JsonMessage;
import net.kuujo.vertigo.output.Connection;
import net.kuujo.vertigo.serializer.Serializable;

/**
 * An output selector.
 *
 * @author Jordan Halterman
 */
public interface Selector extends Serializable {

  /**
   * Returns the selector grouping.
   *
   * @return
   *   The unique selector grouping identifier.
   */
  String getGrouping();

  /**
   * Returns the selector connection count.
   *
   * @return
   *   The selector connection count.
   */
  int getConnectionCount();

  /**
   * Selects a list of connections to which to emit messages.
   *
   * @param message
   *   The message being emitted.
   * @param connections
   *   A list of connections from which to select.
   * @return
   *   A list of selected connections.
   */
  List<Connection> select(JsonMessage message, List<Connection> connections);

}
