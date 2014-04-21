/*
 * Copyright 2014 the original author or authors.
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
package net.kuujo.vertigo.util;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.impl.DefaultFutureResult;

/**
 * A copy of the Vert.x core counting completion handler for handling
 * multiple remote calls asynchronously.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class CountingCompletionHandler<T> implements Handler<AsyncResult<T>> {
  private int count;
  private int required;
  private Handler<AsyncResult<T>> doneHandler;
  private Throwable cause;
  private boolean failed;

  public CountingCompletionHandler(int required) {
    this.required = required;
  }

  @Override
  public void handle(AsyncResult<T> result) {
    if (result.failed()) {
      fail(result.cause());
    } else {
      succeed();
    }
  }

  /**
   * Indicates that a call succeeded.
   */
  public void succeed() {
    count++;
    checkDone();
  }

  /**
   * Indicates that a call failed. This will immediately fail the handler.
   *
   * @param t The cause of the failure.
   */
  public void fail(Throwable t) {
    if (!failed) {
      if (doneHandler != null) {
        doneHandler.handle(new DefaultFutureResult<T>(t));
      } else {
        cause = t;
      }
      failed = true;
    }
  }

  /**
   * Sets the completion handler.
   *
   * @param doneHandler An asynchronous handler to be called once all calls have completed.
   */
  public CountingCompletionHandler<T> setHandler(Handler<AsyncResult<T>> doneHandler) {
    this.doneHandler = doneHandler;
    checkDone();
    return this;
  }

  /**
   * Checks whether the handler should be called.
   */
  private void checkDone() {
    if (doneHandler != null) {
      if (cause != null) {
        doneHandler.handle(new DefaultFutureResult<T>(cause));
      } else {
        if (count == required) {
          doneHandler.handle(new DefaultFutureResult<T>((T) null));
        }
      }
    }
  }

}
