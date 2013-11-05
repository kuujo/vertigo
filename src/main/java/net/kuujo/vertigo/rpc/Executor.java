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
package net.kuujo.vertigo.rpc;

import net.kuujo.vertigo.component.Component;
import net.kuujo.vertigo.message.JsonMessage;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonObject;

/**
 * A network executor.
 *
 * Executors may be used to execute portions of networks as remote procedures.
 * Executors work by exploiting circular connections between components. Thus,
 * when using an executor the network must be setup in a specific manner.
 *
 * @author Jordan Halterman
 */
public interface Executor<T extends Executor<T>> extends Component {

  /**
   * Starts the executor.
   *
   * @return
   *   The called executor instance.
   */
  T start();

  /**
   * Starts the executor.
   *
   * @param doneHandler
   *   An asyncronous result handler to be invoked once the executor is started.
   * @return
   *   The called executor instance.
   */
  T start(Handler<AsyncResult<T>> doneHandler);

  /**
   * Sets the execution reply timeout.
   *
   * @param timeout
   *   An execution reply timeout.
   * @return
   *   The called executor instance.
   */
  T setReplyTimeout(long timeout);

  /**
   * Gets the execution reply timeout.
   *
   * @return
   *  An execution reply timeout.
   */
  long getReplyTimeout();

  /**
   * Sets the maximum execution queue size.
   *
   * @param maxSize
   *   The maximum queue size allowed for the executor.
   * @return
   *   The called executor instance.
   */
  T setMaxQueueSize(long maxSize);

  /**
   * Gets the maximum execution queue size.
   *
   * @return
   *   The maximum queue size allowed for the executor.
   */
  long getMaxQueueSize();

  /**
   * Indicates whether the execution queue is full.
   *
   * @return
   *   A boolean indicating whether the execution queue is full.
   */
  boolean queueFull();

  /**
   * Executes the network.
   *
   * @param args
   *   Execution arguments.
   * @param resultHandler
   *   An asynchronous result handler to be invoke with the execution result.
   * @return
   *   The called executor instance.
   */
  T execute(JsonObject args, Handler<AsyncResult<JsonMessage>> resultHandler);

  /**
   * Executes the network.
   *
   * @param args
   *   Execution arguments.
   * @param tag
   *   A tag to apply to the arguments.
   * @param resultHandler
   *   An asynchronous result handler to be invoke with the execution result.
   * @return
   *   The called executor instance.
   */
  T execute(JsonObject args, String tag, Handler<AsyncResult<JsonMessage>> resultHandler);

}
