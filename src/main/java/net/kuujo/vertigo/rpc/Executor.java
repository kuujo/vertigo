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

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonObject;

import net.kuujo.vertigo.component.Component;
import net.kuujo.vertigo.message.JsonMessage;
import net.kuujo.vertigo.message.MessageId;

/**
 * A network executor.
 *
 * Executors may be used to execute portions of networks as remote procedures.
 * Executors work by exploiting circular connections between components. Thus,
 * when using an executor the network must be setup in a specific manner.
 *
 * @author Jordan Halterman
 */
public interface Executor extends Component<Executor> {

  /**
   * Sets the execution reply timeout.
   *
   * @param timeout
   *   An execution reply timeout.
   * @return
   *   The called executor instance.
   */
  Executor setReplyTimeout(long timeout);

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
   * Use the {@link setExecuteQueueMaxSize(long) setExecuteQueueMaxSize} method.
   *
   * @param maxSize
   *   The maximum queue size allowed for the executor.
   * @return
   *   The called executor instance.
   */
  @Deprecated
  Executor setMaxQueueSize(long maxSize);

  /**
   * Gets the maximum execution queue size.
   *
   * Use the {@link getExecuteQueueMaxSize() getExecuteQueueMaxSize} method.
   *
   * @return
   *   The maximum queue size allowed for the executor.
   */
  @Deprecated
  long getMaxQueueSize();

  /**
   * Sets the maximum execution queue size.
   *
   * @param maxSize
   *   The maximum queue size allowed for the executor.
   * @return
   *   The called executor instance.
   */
  Executor setExecuteQueueMaxSize(long maxSize);

  /**
   * Gets the maximum execution queue size.
   *
   * @return
   *   The maximum queue size allowed for the executor.
   */
  long getExecuteQueueMaxSize();

  /**
   * Indicates whether the execution queue is full.
   *
   * Use the {@link executeQueueFull() executeQueueFull} method.
   *
   * @return
   *   A boolean indicating whether the execution queue is full.
   */
  @Deprecated
  boolean queueFull();

  /**
   * Indicates whether the execution queue is full.
   *
   * @return
   *   A boolean indicating whether the execution queue is full.
   */
  boolean executeQueueFull();

  /**
   * Sets the executor auto-retry option.
   *
   * If this option is enabled, the executor will automatically retry sending
   * timed out messages.
   *
   * @param retry
   *   Indicates whether to automatically executor emitting timed out data.
   * @return
   *   The called executor instance.
   */
  Executor setAutoRetry(boolean retry);

  /**
   * Gets the executor auto-retry option.
   *
   * If this option is enabled, the executor will automatically retry sending
   * timed out messages.
   *
   * @return
   *   Indicates whether the executor with automatically retry emitting timed out data.
   */
  boolean isAutoRetry();

  /**
   * Sets the number of automatic retry attempts for a single timed out message.
   *
   * Use the {@link setAutoRetryAttempts(int) setAutoRetryAttempts} method.
   *
   * @param attempts
   *   The number of retry attempts allowed. If attempts is -1 then an infinite
   *   number of retry attempts will be allowed.
   * @return
   *   The called executor instance.
   */
  @Deprecated
  Executor setRetryAttempts(int attempts);

  /**
   * Gets the number of automatic retry attempts.
   *
   * Use the {@link getAutoRetryAttempts() getAutoRetryAttempts} method.
   *
   * @return
   *   Indicates the number of retry attempts allowed for the executor.
   */
  @Deprecated
  int getRetryAttempts();

  /**
   * Sets the number of automatic retry attempts for a single timed out message.
   *
   * @param attempts
   *   The number of retry attempts allowed. If attempts is -1 then an infinite
   *   number of retry attempts will be allowed.
   * @return
   *   The called executor instance.
   */
  Executor setAutoRetryAttempts(int attempts);

  /**
   * Gets the number of automatic retry attempts.
   *
   * @return
   *   Indicates the number of retry attempts allowed for the executor.
   */
  int getAutoRetryAttempts();

  /**
   * Sets the execute delay.
   *
   * Use the {@link setExecuteInterval(long) setExecuteInterval} method.
   *
   * @param delay
   *   The empty execute delay.
   * @return
   *   The called executor instance.
   */
  @Deprecated
  Executor setExecuteDelay(long delay);

  /**
   * Gets the execute delay.
   *
   * Use the {@link getExecuteInterval() getExecuteInterval} method.
   *
   * @return
   *   The empty executor delay.
   */
  @Deprecated
  long getExecuteDelay();

  /**
   * Sets the execute delay.
   *
   * The execute interval indicates the interval at which the executor will attempt to
   * poll the execute handler for new data.
   *
   * @param interval
   *   The empty execute interval.
   * @return
   *   The called executor instance.
   */
  Executor setExecuteInterval(long interval);

  /**
   * Gets the execute interval.
   *
   * The execute delay indicates the interval at which the executor will attempt to
   * poll the execute handler for new data.
   *
   * @return
   *   The empty execute interval.
   */
  long getExecuteInterval();

  /**
   * Sets an execute handler.
   *
   * The execute handler will be periodically polled for new data. Each time the
   * execute handler is polled only a single message should be emitted. This allows
   * the executor to maintain control over the flow of data. If the execute handler
   * is called but fails to emit any new messages to the network, the executor
   * will reschedule the next call to the handler for a period in the near future.
   *
   * @param handler
   *   A handler to be invoked for executing the network.
   * @return
   *   The called executor instance.
   */
  Executor executeHandler(Handler<Executor> handler);

  /**
   * Sets a drain handler on the executor.
   *
   * The drain handler will be called when the execute queue is available to
   * receive new messages.
   *
   * @param handler
   *   A handler to be invoked when a full execute queue is emptied.
   * @return
   *   The called executor instance.
   */
  Executor drainHandler(Handler<Void> handler);

  /**
   * Executes the network.
   *
   * @param args
   *   Execution arguments.
   * @param resultHandler
   *   An asynchronous result handler to be invoke with the execution result.
   * @return
   *   The emitted message correlation identifier.
   */
  MessageId execute(JsonObject args, Handler<AsyncResult<JsonMessage>> resultHandler);

}
