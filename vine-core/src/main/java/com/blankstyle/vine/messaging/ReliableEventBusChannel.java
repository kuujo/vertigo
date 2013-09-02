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
package com.blankstyle.vine.messaging;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;

/**
 * A default channel implementation.
 *
 * @author Jordan Halterman
 */
public class ReliableEventBusChannel implements ReliableChannel<ReliableEventBusConnection> {

  protected Dispatcher dispatcher;

  protected ConnectionPool<ReliableEventBusConnection> connections = new ReliableEventBusConnectionPool();

  public ReliableEventBusChannel(Dispatcher dispatcher) {
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
  public void addConnection(ReliableEventBusConnection connection) {
    if (!connections.contains(connection)) {
      connections.add(connection);
    }
    dispatcher.init(connections);
  }

  @Override
  public void removeConnection(ReliableEventBusConnection connection) {
    if (connections.contains(connection)) {
      connections.remove(connection);
    }
    dispatcher.init(connections);
  }

  @Override
  public void publish(JsonMessage message) {
    dispatcher.dispatch(message);
  }

  @Override
  public void publish(JsonMessage message, Handler<AsyncResult<Void>> resultHandler) {
    dispatcher.dispatch(message, resultHandler);
  }

  @Override
  public void publish(JsonMessage message, long timeout, Handler<AsyncResult<Void>> resultHandler) {
    dispatcher.dispatch(message, timeout, resultHandler);
  }

  @Override
  public void publish(JsonMessage message, long timeout, boolean retry, Handler<AsyncResult<Void>> resultHandler) {
    dispatcher.dispatch(message, timeout, retry, resultHandler);
  }

  @Override
  public void publish(JsonMessage message, long timeout, boolean retry, int attempts,
      Handler<AsyncResult<Void>> resultHandler) {
    dispatcher.dispatch(message, timeout, retry, attempts, resultHandler);
  }

}
