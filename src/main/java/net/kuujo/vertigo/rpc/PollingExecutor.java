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

import net.kuujo.vertigo.message.JsonMessage;

import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonObject;

/**
 * A polling executor.
 *
 * The polling executor reads input from code by periodically polling a handler
 * for new data. If the execute handler fails to emit new data when polled, the
 * executor will reschedule the next execution for a period in the near future.
 *
 * @author Jordan Halterman
 */
public interface PollingExecutor extends Executor<PollingExecutor> {

  /**
   * Sets the execute delay.
   *
   * The execute delay indicates the interval at which the executor will attempt to
   * poll the execute handler for new data.
   *
   * @param delay
   *   The empty execute delay.
   * @return
   *   The called executor instance.
   */
  PollingExecutor setExecuteDelay(long delay);

  /**
   * Gets the execute delay.
   *
   * The execute delay indicates the interval at which the executor will attempt to
   * poll the execute handler for new data.
   *
   * @return
   *   The empty executor delay.
   */
  long getExecuteDelay();

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
  PollingExecutor executeHandler(Handler<PollingExecutor> handler);

  /**
   * Sets a result handler on the executor.
   *
   * @param resultHandler
   *   A handler to be invoked when a result is received.
   * @return
   *   The called executor instance.
   */
  PollingExecutor resultHandler(Handler<JsonMessage> resultHandler);

  /**
   * Sets a fail handler on the executor.
   *
   * @param failHandler
   *   A handler to be invoked when a failure is received.
   * @return
   *   The called executor instance.
   */
  PollingExecutor failHandler(Handler<String> failHandler);

  /**
   * Executes the network.
   *
   * @param args
   *   Execution arguments.
   * @return
   *   The emitted message correlation identifier.
   */
  String execute(JsonObject args);

  /**
   * Executes the network.
   *
   * @param args
   *   Execution arguments.
   * @param tag
   *   A tag to apply to the arguments.
   * @return
   *   The emitted message correlation identifier.
   */
  String execute(JsonObject args, String tag);

}
