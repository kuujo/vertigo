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
package net.kuujo.vertigo.network;

import net.kuujo.vertigo.input.grouping.MessageGrouping;

/**
 * Connection contatiner.
 *
 * @author Jordan Halterman
 */
public interface ConnectionConfigurable {

  /**
   * Creates a connection between two components.
   *
   * @param connection The new connection.
   * @return The connection instance.
   */
  ConnectionConfig createConnection(ConnectionConfig connection);

  /**
   * Creates a connection between two components.
   *
   * @param source The source component.
   * @param target The target component.
   * @return A new connection instance.
   */
  ConnectionConfig createConnection(String source, String target);

  /**
   * Creates a connection between two components.
   *
   * @param source The source component.
   * @param target The target component.
   * @param grouping The connection grouping.
   * @return A new connection instance.
   */
  ConnectionConfig createConnection(String source, String target, MessageGrouping grouping);

  /**
   * Creates a connection between two components.
   *
   * @param source The source component.
   * @param out The source output port.
   * @param target The target component.
   * @param in The target output port.
   * @return A new connection instance.
   */
  ConnectionConfig createConnection(String source, String out, String target, String in);

  /**
   * Creates a connection between two components.
   *
   * @param source The source component.
   * @param out The source output port.
   * @param target The target component.
   * @param in The target output port.
   * @param grouping The connection grouping.
   * @return A new connection instance.
   */
  ConnectionConfig createConnection(String source, String out, String target, String in, MessageGrouping grouping);

  /**
   * Destroys a connection between two components.
   *
   * @param connection The connection to destroy.
   * @return The network configuration.
   */
  NetworkConfig destroyConnection(ConnectionConfig connection);

  /**
   * Destroys a connection between two components.
   *
   * @param source The source component name and port.
   * @param target The target component name and port.
   * @return The network configuration.
   */
  NetworkConfig destroyConnection(String source, String target);

  /**
   * Destroys a connection between two components.
   *
   * @param source The source component name.
   * @param out The source component out port.
   * @param target The target component name.
   * @param in The target component in port.
   * @return The network configuration.
   */
  NetworkConfig destroyConnection(String source, String out, String target, String in);

}
