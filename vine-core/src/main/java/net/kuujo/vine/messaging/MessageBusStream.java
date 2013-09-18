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
package net.kuujo.vine.messaging;

import org.vertx.java.core.Handler;

/**
 * An eventbus based stream.
 *
 * @author Jordan Halterman
 */
public class MessageBusStream implements Stream<MessageBusConnection> {

  protected Dispatcher dispatcher;

  protected ConnectionPool<MessageBusConnection> connections = new MessageBusConnectionPool();

  public MessageBusStream(Dispatcher dispatcher) {
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
  public void addConnection(MessageBusConnection connection) {
    if (!connections.contains(connection)) {
      connections.add(connection);
    }
    dispatcher.init(connections);
  }

  @Override
  public void removeConnection(MessageBusConnection connection) {
    if (connections.contains(connection)) {
      connections.remove(connection);
    }
    dispatcher.init(connections);
  }

  @Override
  public void emit(JsonMessage message) {
    dispatcher.dispatch(message);
  }

  @Override
  public void emit(JsonMessage message, Handler<Boolean> ackHandler) {
    dispatcher.dispatch(message, ackHandler);
  }

}
