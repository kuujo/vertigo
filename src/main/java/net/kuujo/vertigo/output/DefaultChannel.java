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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.vertx.java.core.eventbus.EventBus;

import net.kuujo.vertigo.message.JsonMessage;
import net.kuujo.vertigo.output.condition.Condition;
import net.kuujo.vertigo.output.selector.Selector;

/**
 * A default output channel implementation.
 *
 * @author Jordan Halterman
 */
public class DefaultChannel implements Channel {
  private Selector selector;
  private List<Condition> conditions = new ArrayList<Condition>();
  private int connectionCount;
  private List<Connection> connections = new ArrayList<Connection>();
  private Map<String, Integer> connectionMap = new HashMap<String, Integer>();
  private EventBus eventBus;

  public DefaultChannel(Selector selector, List<Condition> conditions, EventBus eventBus) {
    this.selector = selector;
    this.conditions = conditions;
  }

  @Override
  public Channel setConnectionCount(int connectionCount) {
    this.connectionCount = connectionCount;
    connectionMap = new HashMap<String, Integer>();
    connections = new ArrayList<Connection>();
    for (int i = 0; i < connectionCount; i++) {
      connections.add(new DefaultPseudoConnection(eventBus));
    }
    return this;
  }

  @Override
  public Channel addConnection(Connection connection) {
    // If this connection address already exists in the connections map then
    // simply replace the existing connection.
    if (connectionMap.containsKey(connection.getAddress())) {
      int index = connectionMap.get(connection.getAddress());
      connections.remove(index);
      connections.add(index, connection);
    }
    // If this address has not already been added to connections, and the connections
    // list still has room for another connection, assign an index to the connection
    // and add it.
    else if (!connectionMap.containsKey(connection.getAddress()) && connectionMap.size() < connectionCount) {
      int index = connectionMap.size();
      connectionMap.put(connection.getAddress(), index);
      connections.remove(index);
      connections.add(index, connection);
    }
    return this;
  }

  @Override
  public Channel removeConnection(Connection connection) {
    // If the connection exists in the connections list, remove the connection
    // and replace it with a pseudo-connection. This will allow selectors to
    // continue to operate on the connection list as if it were complete, but
    // will have the effect of failing messages sent to this connection.
    if (connectionMap.containsKey(connection.getAddress())) {
      int index = connectionMap.get(connection.getAddress());
      connections.remove(index);
      connections.add(index, new DefaultPseudoConnection(eventBus));
    }
    return this;
  }

  @Override
  public boolean containsConnection(String address) {
    return connectionMap.containsKey(address);
  }

  @Override
  public Connection getConnection(String address) {
    if (connectionMap.containsKey(address)) {
      Connection connection = connections.get(connectionMap.get(address));
      return connection instanceof PseudoConnection ? null : connection;
    }
    return null;
  }

  /**
   * Indicates whether the given message is valid.
   */
  private boolean isValid(JsonMessage message) {
    for (Condition condition : conditions) {
      if (!condition.isValid(message)) {
        return false;
      }
    }
    return true;
  }

  @Override
  public Channel publish(JsonMessage message) {
    if (isValid(message)) {
      for (Connection connection : selector.select(message, connections)) {
        connection.write(message.copy());
      }
    }
    return this;
  }

}
