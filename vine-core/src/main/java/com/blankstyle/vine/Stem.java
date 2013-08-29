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
package com.blankstyle.vine;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.platform.Container;

import com.blankstyle.vine.context.WorkerContext;

/**
 * A vine stem.
 *
 * @author Jordan Halterman
 */
public interface Stem {

  /**
   * Gets the stem address.
   */
  public String getAddress();

  /**
   * Sets the root vertx instance.
   *
   * @param vertx
   *   A vertx instance.
   * @return
   *   The called stem instance.
   */
  public Stem setVertx(Vertx vertx);

  /**
   * Gets the stem vertx instance.
   *
   * @return
   *   The stem vertx instance.
   */
  public Vertx getVertx();

  /**
   * Sets the stem container instance.
   *
   * @param container
   *   A container instance.
   * @return
   *   The called root instance.
   */
  public Stem setContainer(Container container);

  /**
   * Gets the stem container instance.
   *
   * @return
   *   The stem container instance.
   */
  public Container getContainer();

  /**
   * Assigns a worker to the stem.
   *
   * @param context
   *   The worker context.
   * @param doneHandler
   *   A handler to be invoked once the worker is assigned.
   */
  public void assign(WorkerContext context, Handler<AsyncResult<Void>> doneHandler);

  /**
   * Assigns a worker to the stem.
   *
   * @param context
   *   The worker context.
   * @param timeout
   *   An assignment timeout.
   * @param doneHandler
   *   A handler to be invoked once the worker is assigned.
   */
  public void assign(WorkerContext context, long timeout, Handler<AsyncResult<Void>> doneHandler);

  /**
   * Releases a worker from the stem.
   *
   * @param context
   *   The worker context.
   * @param doneHandler
   *   A handler to be invoked once the worker is released.
   */
  public void release(WorkerContext context, Handler<AsyncResult<Void>> doneHandler);

  /**
   * Releases a worker from the stem.
   *
   * @param context
   *   The worker context.
   * @param timeout
   *   A release timeout.
   * @param doneHandler
   *   A handler to be invoked once the worker is released.
   */
  public void release(WorkerContext context, long timeout, Handler<AsyncResult<Void>> doneHandler);

}
