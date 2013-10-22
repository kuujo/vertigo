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
package net.kuujo.vertigo.output;

import net.kuujo.vertigo.messaging.JsonMessage;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonObject;

/**
 * A component output collector.
 *
 * @author Jordan Halterman
 */
public interface OutputCollector {

  /**
   * Returns the output address.
   *
   * @return
   *   The output address.
   */
  public String getAddress();

  /**
   * Emits a message to all output channels.
   *
   * @param data
   *   The data to emit.
   * @return
   *   The called output collector instance.
   */
  public OutputCollector emit(JsonObject data);

  /**
   * Emits a message to all output channels.
   *
   * @param data
   *   The data to emit.
   * @param tag
   *   A tag to apply to output data.
   * @return
   *   The called output collector instance.
   */
  public OutputCollector emit(JsonObject data, String tag);

  /**
   * Emits a message to all output channels.
   *
   * @param data
   *   The data to emit.
   * @param parent
   *   The parent message of the data.
   * @return
   *   The called output collector instance.
   */
  public OutputCollector emit(JsonObject data, JsonMessage parent);

  /**
   * Emits a message to all output channels.
   *
   * @param data
   *   The data to emit.
   * @param tag
   *   A tag to apply to output data.
   * @param parent
   *   The parent message of the data.
   * @return
   *   The called output collector instance.
   */
  public OutputCollector emit(JsonObject data, String tag, JsonMessage parent);

  /**
   * Starts the output collector.
   */
  public void start();

  /**
   * Starts the output collector.
   *
   * @param doneHandler
   *   An asynchronous handler to be invoked once the collector is started.
   */
  public void start(Handler<AsyncResult<Void>> doneHandler);

  /**
   * Stops the output collector.
   */
  public void stop();

  /**
   * Stops the output collector.
   *
   * @param doneHandler
   *   An asynchronous handler to be invoked once the collector is stopped.
   */
  public void stop(Handler<AsyncResult<Void>> doneHandler);

}
