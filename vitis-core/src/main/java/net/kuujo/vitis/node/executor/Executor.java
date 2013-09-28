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
package net.kuujo.vitis.node.executor;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonObject;

/**
 * A network executor.
 *
 * @author Jordan Halterman
 */
public interface Executor {

  /**
   * Starts the executor.
   */
  public void start();

  /**
   * Sets the execution queue size.
   *
   * @param maxSize
   *   The execution queue maximum size.
   * @return
   *   The called executor instance.
   */
  public Executor setWriteQueueMaxSize(long maxSize);

  /**
   * Registers a handler to be invoked for execution.
   *
   * @param handler
   *   The handler to register.
   * @return
   *   The called executor instance.
   */
  public Executor executeHandler(Handler<Executor> handler);

  /**
   * Registers a handler to be invoked when the execution queue is full.
   *
   * @param fullHandler
   *   The handler to register.
   * @return
   *   The called executor instance.
   */
  public Executor fullHandler(Handler<Executor> fullHandler);

  /**
   * Registers a handler to be invoked when a full executor is prepared to accept
   * new executions.
   *
   * @param drainHandler
   *   The handler to register.
   * @return
   *   The called executor instance.
   */
  public Executor drainHandler(Handler<Executor> drainHandler);

  /**
   * Executes the network.
   *
   * @param args
   *   Arguments to network nodes.
   * @param resultHandler
   *   An asynchronous result handler.
   * @return
   *   The called executor instance.
   */
  public Executor execute(JsonObject args, Handler<AsyncResult<JsonObject>> resultHandler);

  /**
   * Executes the network.
   *
   * @param args
   *   Arguments to network nodes.
   * @param timeout
   *   An execution timeout in milliseconds.
   * @param resultHandler
   *   An asynchronous result handler.
   * @return
   *   The called executor instance.
   */
  public Executor execute(JsonObject args, long timeout, Handler<AsyncResult<JsonObject>> resultHandler);

  /**
   * Executes the network.
   *
   * @param args
   *   Arguments to network nodes.
   * @param tag
   *   A tag to apply to arguments.
   * @param resultHandler
   *   An asynchronous result handler.
   * @return
   *   The called executor instance.
   */
  public Executor execute(JsonObject args, String tag, Handler<AsyncResult<JsonObject>> resultHandler);

  /**
   * Executes the network.
   *
   * @param args
   *   Arguments to network nodes.
   * @param tag
   *   A tag to apply to arguments.
   * @param timeout
   *   An execution timeout in milliseconds.
   * @param resultHandler
   *   An asynchronous result handler.
   * @return
   *   The called executor instance.
   */
  public Executor execute(JsonObject args, String tag, long timeout, Handler<AsyncResult<JsonObject>> resultHandler);

}
