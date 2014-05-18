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

import java.util.concurrent.ExecutorService;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.impl.VertxInternal;
import org.vertx.java.core.spi.Action;

/**
 * Manages access to the internal Vert.x context.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class ContextManager {
  private final VertxInternal vertx;

  public ContextManager(Vertx vertx) {
    if (!(vertx instanceof VertxInternal)) {
      throw new IllegalArgumentException("ContextManager requires a VertxInternal instance");
    }
    this.vertx = (VertxInternal) vertx;
  }

  /**
   * Returns the underlying Vert.x executor service.
   *
   * @return The Vert.x executor service.
   */
  public ExecutorService getExecutorService() {
    return vertx.getBackgroundPool();
  }

  /**
   * Runs a runnable on a background thread.
   *
   * @param runnable The runnable to run.
   * @return The context manager.
   */
  public ContextManager run(Runnable runnable) {
    vertx.startInBackground(runnable, false);
    return this;
  }

  /**
   * Runs a runnable on a background thread.
   *
   * @param runnable The runnable to run.
   * @param multiThreaded Indicates whether the runnable is multi-threaded.
   * @return The context manager.
   */
  public ContextManager run(Runnable runnable, boolean multiThreaded) {
    vertx.startInBackground(runnable, multiThreaded);
    return this;
  }

  /**
   * Executes a blocking action on a background thread.
   *
   * @param action The action to execute.
   * @param resultHandler A handler to be called with the action result.
   * @return The context manager.
   */
  public <T> ContextManager execute(Action<T> action, Handler<AsyncResult<T>> resultHandler) {
    vertx.executeBlocking(action, resultHandler);
    return this;
  }

}
