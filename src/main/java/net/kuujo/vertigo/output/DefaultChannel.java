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
