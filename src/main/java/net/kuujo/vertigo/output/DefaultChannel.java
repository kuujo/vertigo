package net.kuujo.vertigo.output;

import java.util.ArrayList;
import java.util.List;

import net.kuujo.vertigo.filter.Filter;
import net.kuujo.vertigo.messaging.JsonMessage;
import net.kuujo.vertigo.selector.Selector;

/**
 * A default output channel implementation.
 *
 * @author Jordan Halterman
 */
public class DefaultChannel implements Channel {

  private Selector selector;
  private List<Filter> filters = new ArrayList<Filter>();
  private List<Connection> connections = new ArrayList<Connection>();

  public DefaultChannel() {
  }

  @Override
  public void setSelector(Selector selector) {
    this.selector = selector;
  }

  @Override
  public Channel addFilter(Filter filter) {
    if (!filters.contains(filter)) {
      filters.add(filter);
    }
    return this;
  }

  @Override
  public Channel removeFilter(Filter filter) {
    if (filters.contains(filter)) {
      filters.remove(filter);
    }
    return this;
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
  public boolean isValid(JsonMessage message) {
    for (Filter filter : filters) {
      if (!filter.isValid(message)) {
        return false;
      }
    }
    return true;
  }

  @Override
  public Channel write(JsonMessage message) {
    if (isValid(message)) {
      for (Connection connection : selector.select(message, connections)) {
        connection.write(message);
      }
    }
    return this;
  }

}
