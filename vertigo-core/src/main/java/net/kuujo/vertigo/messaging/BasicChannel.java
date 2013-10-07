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

import java.util.HashSet;
import java.util.Set;

import net.kuujo.vertigo.dispatcher.Dispatcher;
import net.kuujo.vertigo.filter.Filter;

/**
 * A basic channel.
 *
 * @author Jordan Halterman
 */
public class BasicChannel implements Channel {

  protected Dispatcher dispatcher;

  protected Set<Filter> filters = new HashSet<>();

  protected ConnectionPool connections = new ConnectionSet();

  public BasicChannel(Dispatcher dispatcher) {
    this.dispatcher = dispatcher;
  }

  @Override
  public void setDispatcher(Dispatcher dispatcher) {
    this.dispatcher = dispatcher;
  }

  @Override
  public Dispatcher getDispatcher() {
    return dispatcher;
  }

  @Override
  public Channel addFilter(Filter filter) {
    filters.add(filter);
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
  public boolean isValid(JsonMessage message) {
    for (Filter filter : filters) {
      if (!filter.valid(message)) {
        return false;
      }
    }
    return true;
  }

  @Override
  public Channel addConnection(Connection connection) {
    if (!connections.contains(connection)) {
      connections.add(connection);
    }
    dispatcher.init(connections);
    return this;
  }

  @Override
  public Channel removeConnection(Connection connection) {
    if (connections.contains(connection)) {
      connections.remove(connection);
    }
    dispatcher.init(connections);
    return this;
  }

  @Override
  public Channel write(JsonMessage message) {
    dispatcher.dispatch(message);
    return this;
  }

}
