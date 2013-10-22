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
import java.util.List;

import net.kuujo.vertigo.input.filter.Filter;
import net.kuujo.vertigo.messaging.JsonMessage;
import net.kuujo.vertigo.output.selector.Selector;

/**
 * A default output channel implementation.
 *
 * @author Jordan Halterman
 */
public class DefaultChannel implements Channel {
  private Output output;
  private Selector selector;
  private List<Filter> filters = new ArrayList<Filter>();
  private List<Connection> connections = new ArrayList<Connection>();

  public DefaultChannel(Output output) {
    this.output = output;
    this.selector = output.getSelector();
    this.filters = output.getFilters();
  }

  @Override
  public Channel addConnection(Connection connection) {
    if (!connections.contains(connection)) {
      connections.add(connection);
    }
    return this;
  }

  @Override
  public Channel removeConnection(Connection connection) {
    if (connections.contains(connection)) {
      connections.remove(connection);
    }
    return this;
  }

  @Override
  public boolean containsConnection(Connection connection) {
    return connections.contains(connection);
  }

  @Override
  public boolean containsConnection(String address) {
    for (Connection connection : connections) {
      if (connection.address().equals(address)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Indicates whether the given message is valid.
   */
  public boolean isValid(JsonMessage message) {
    for (Filter filter : filters) {
      if (!filter.isValid(message)) {
        return false;
      }
    }
    return true;
  }

  @Override
  public Channel publish(JsonMessage message) {
    if (isValid(message)) {
      for (Connection connection : selector.select(message, connections)) {
        connection.write(message);
      }
    }
    return this;
  }

}
