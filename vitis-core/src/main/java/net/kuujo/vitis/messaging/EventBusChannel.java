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
package net.kuujo.vitis.messaging;

/**
 * An eventbus based stream.
 *
 * @author Jordan Halterman
 */
public class EventBusChannel implements Channel<EventBusConnection> {

  protected Dispatcher dispatcher;

  protected ConnectionPool<EventBusConnection> connections = new EventBusConnectionPool();

  public EventBusChannel(Dispatcher dispatcher) {
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
  public void addConnection(EventBusConnection connection) {
    if (!connections.contains(connection)) {
      connections.add(connection);
    }
    dispatcher.init(connections);
  }

  @Override
  public void removeConnection(EventBusConnection connection) {
    if (connections.contains(connection)) {
      connections.remove(connection);
    }
    dispatcher.init(connections);
  }

  @Override
  public Channel<?> write(JsonMessage message) {
    dispatcher.dispatch(message);
    return this;
  }

}
