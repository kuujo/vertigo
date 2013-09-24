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
package net.kuujo.vevent.messaging;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;

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
  public void emit(JsonMessage message) {
    dispatcher.dispatch(message);
  }

  @Override
  public <T> void emit(JsonMessage message, Handler<AsyncResult<Message<T>>> replyHandler) {
    dispatcher.dispatch(message, replyHandler);
  }

  @Override
  public <T> void emit(JsonMessage message, long timeout, Handler<AsyncResult<Message<T>>> replyHandler) {
    dispatcher.dispatch(message, timeout, replyHandler);
  }

  @Override
  public <T> void emit(JsonMessage message, long timeout, boolean retry, Handler<AsyncResult<Message<T>>> replyHandler) {
    dispatcher.dispatch(message, timeout, retry, replyHandler);
  }

  @Override
  public <T> void emit(JsonMessage message, long timeout, boolean retry, int attempts, Handler<AsyncResult<Message<T>>> replyHandler) {
    dispatcher.dispatch(message, timeout, retry, attempts, replyHandler);
  }

}
