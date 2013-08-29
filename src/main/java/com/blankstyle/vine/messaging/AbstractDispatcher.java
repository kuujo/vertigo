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
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.Future;
import org.vertx.java.core.Handler;
import org.vertx.java.core.impl.DefaultFutureResult;

/**
 * An abstract dispatcher implementation.
 *
 * @author Jordan Halterman
 */
public abstract class AbstractDispatcher implements Dispatcher {

  private static final long DEFAULT_TIMEOUT = 5000;

  /**
   * Returns the next connection to which to dispatch.
   *
   * @return
   *   A connection to which to dispatch a message.
   */
  protected abstract Connection getConnection();

  @Override
  public void dispatch(JsonMessage message) {
    getConnection().send(message);
  }

  @Override
  public void dispatch(JsonMessage message, Handler<AsyncResult<Void>> resultHandler) {
    dispatch(message, DEFAULT_TIMEOUT, false, 0, resultHandler);
  }

  @Override
  public void dispatch(JsonMessage message, long timeout, Handler<AsyncResult<Void>> resultHandler) {
    dispatch(message, timeout, false, 0, resultHandler);
  }

  @Override
  public void dispatch(JsonMessage message, long timeout, boolean retry, Handler<AsyncResult<Void>> resultHandler) {
    dispatch(message, timeout, retry, 1, resultHandler);
  }

  @Override
  public void dispatch(JsonMessage message, long timeout, boolean retry, int attempts,
      Handler<AsyncResult<Void>> resultHandler) {
    final Future<Void> future = new DefaultFutureResult<Void>();

    // Make sure this is a reliable connection. Acknowledgment handlers are only
    // supported on reliable connections.
    Connection connection = getConnection();
    if (!(connection instanceof ReliableConnection)) {
      throw new IllegalArgumentException("Cannot dispatch to unreliable connection.");
    }

    ((ReliableConnection) connection).send(message, timeout, retry, attempts, new AsyncResultHandler<org.vertx.java.core.eventbus.Message<Void>>() {
      @Override
      public void handle(AsyncResult<org.vertx.java.core.eventbus.Message<Void>> result) {
        if (result.succeeded()) {
          future.setResult(null);
        }
        else {
          future.setFailure(result.cause());
        }
      }
    });
  }

}
