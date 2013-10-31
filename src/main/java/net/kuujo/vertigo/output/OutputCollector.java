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
  String getAddress();

  /**
   * Emits a new message to all output channels.
   *
   * @param body
   *   The message body.
   * @return
   *   The unique output message correlation identifier.
   */
  String emit(JsonObject body);

  /**
   * Emits a new message to all output channels.
   *
   * @param body
   *   The message body.
   * @param tag
   *   A tag to apply to the message.
   * @return
   *   The unique output message correlation identifier.
   */
  String emit(JsonObject data, String tag);

  /**
   * Emits a child message to all output channels.
   *
   * @param body
   *   The message body.
   * @param parent
   *   The parent message of the data.
   * @return
   *   The unique child message correlation identifier.
   */
  String emit(JsonObject data, JsonMessage parent);

  /**
   * Emits a child message to all output channels.
   *
   * @param body
   *   The message body.
   * @param tag
   *   A tag to apply to output data.
   * @param parent
   *   The parent message of the data.
   * @return
   *   The unique child message correlation identifier.
   */
  String emit(JsonObject data, String tag, JsonMessage parent);

  /**
   * Sets an ack handler on the output collector.
   *
   * @param handler
   *   A handler to be invoked when an ack message is received.
   * @return
   *   The called output collector instance.
   */
  OutputCollector ackHandler(Handler<String> handler);

  /**
   * Sets a fail handler on the output collector.
   *
   * @param handler
   *   A handler to be invoked when a fail message is received.
   * @return
   *   The called output collector instance.
   */
  OutputCollector failHandler(Handler<String> handler);

  /**
   * Starts the output collector.
   *
   * @return
   *   The called output collector instance.
   */
  OutputCollector start();

  /**
   * Starts the output collector.
   *
   * @param doneHandler
   *   An asynchronous handler to be invoked once the collector is started.
   * @return
   *   The called output collector instance.
   */
  OutputCollector start(Handler<AsyncResult<Void>> doneHandler);

  /**
   * Stops the output collector.
   */
  void stop();

  /**
   * Stops the output collector.
   *
   * @param doneHandler
   *   An asynchronous handler to be invoked once the collector is stopped.
   */
  void stop(Handler<AsyncResult<Void>> doneHandler);

}
