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
package net.kuujo.vevent.java;

import net.kuujo.vevent.context.ComponentContext;
import net.kuujo.vevent.node.Executor;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Verticle;

/**
 * An abstract executor verticle.
 *
 * @author Jordan Halterman
 */
abstract class ExecutorVerticle extends Verticle implements Handler<Executor> {

  protected ComponentContext context;

  protected Executor executor;

  /**
   * Creates a root executor instance.
   *
   * @return
   *   A new root executor instance.
   */
  abstract Executor createExecutor(ComponentContext context);

  @Override
  public void start() {
    context = new ComponentContext(container.config());
    executor = createExecutor(context);
    executor.executeHandler(this);
  }

  @Override
  public void handle(Executor executor) {
    execute();
  }

  /**
   * Primary method for executing data.
   */
  protected abstract void execute();

  /**
   * Performs an execution.
   *
   * @param args
   *   Execution arguments.
   * @param resultHandler
   *   An asynchronous result handler.
   */
  public void execute(JsonObject args, Handler<AsyncResult<JsonObject>> resultHandler) {
    executor.execute(args, resultHandler);
  }

  /**
   * Performs an execution.
   *
   * @param args
   *   Execution arguments.
   * @param tag
   *   A tag to apply to the arguments.
   * @param resultHandler
   *   An asynchronous result handler.
   */
  public void execute(JsonObject args, String tag, Handler<AsyncResult<JsonObject>> resultHandler) {
    executor.execute(args, tag, resultHandler);
  }

  /**
   * Performs an execution.
   *
   * @param args
   *   Execution arguments.
   * @param timeout
   *   An execution timeout in milliseconds.
   * @param resultHandler
   *   An asynchronous result handler.
   */
  public void execute(JsonObject args, long timeout, Handler<AsyncResult<JsonObject>> resultHandler) {
    executor.execute(args, timeout, resultHandler);
  }

  /**
   * Performs an execution.
   *
   * @param args
   *   Execution arguments.
   * @param tag
   *   A tag to apply to the arguments.
   * @param timeout
   *   An execution timeout in milliseconds.
   * @param resultHandler
   *   An asynchronous result handler.
   */
  public void execute(JsonObject args, String tag, long timeout, Handler<AsyncResult<JsonObject>> resultHandler) {
    executor.execute(args, tag, timeout, resultHandler);
  }

}
