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
package net.kuujo.vertigo.input;

import net.kuujo.vertigo.messaging.JsonMessage;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;

/**
 * An input collector.
 *
 * @author Jordan Halterman
 */
public interface InputCollector {

  /**
   * Sets a message handler on the collector.
   *
   * @param handler
   *   The message handler.
   * @return
   *   The called collector instance.
   */
  public InputCollector messageHandler(Handler<JsonMessage> handler);

  /**
   * Acknowledges a message.
   *
   * @param message
   *   The message to ack.
   * @return
   *   The called input collector instance.
   */
  public InputCollector ack(JsonMessage message);

  /**
   * Fails a message.
   *
   * @param message
   *   The message to fail.
   * @return
   *   The called input collector instance.
   */
  public InputCollector fail(JsonMessage message);

  /**
   * Fails a message with a failure message.
   *
   * @param message
   *   The message to fail.
   * @param failMessage
   *   A failure message.
   * @return
   *   The called input collector instance.
   */
  public InputCollector fail(JsonMessage message, String failMessage);

  /**
   * Starts the input collector.
   *
   * @return
   *   The called input collector instance.
   */
  public InputCollector start();

  /**
   * Starts the input collector.
   *
   * @param doneHandler
   *   An asynchronous handler to be invoked once the collector is started.
   * @return
   *   The called input collector instance.
   */
  public InputCollector start(Handler<AsyncResult<Void>> doneHandler);

  /**
   * Stops the input collector.
   */
  public void stop();

  /**
   * Stops the input collector.
   *
   * @param doneHandler
   *   An asynchronous handler to be invoked once the collector is stopped.
   */
  public void stop(Handler<AsyncResult<Void>> doneHandler);

}
