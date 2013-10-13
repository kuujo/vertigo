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
package net.kuujo.vertigo.component.worker;

import net.kuujo.vertigo.component.Component;
import net.kuujo.vertigo.messaging.JsonMessage;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonObject;

/**
 * A worker component.
 *
 * @author Jordan Halterman
 */
public interface Worker extends Component {

  /**
   * Starts the worker.
   *
   * @return
   *   The called worker instance.
   */
  public Worker start();

  /**
   * Starts the worker.
   *
   * @param doneHandler
   *   An asyncronous result handler to be invoked once the worker is started.
   * @return
   *   The called worker instance.
   */
  public Worker start(Handler<AsyncResult<Worker>> doneHandler);

  /**
   * Sets a worker data handler.
   *
   * @param handler
   *   A json data handler.
   * @return 
   *   The called worker instance.
   */
  public Worker messageHandler(Handler<JsonMessage> handler);

  /**
   * Emits data from the worker.
   *
   * @param data
   *   The data to emit.
   * @return 
   *   The called worker instance.
   */
  public Worker emit(JsonObject data);

  /**
   * Emits data from the worker with a tag.
   *
   * @param data
   *   The data to emit.
   * @param tag
   *   A tag to apply to the message.
   * @return 
   *   The called worker instance.
   */
  public Worker emit(JsonObject data, String tag);

  /**
   * Emits child data from the worker.
   *
   * @param data
   *   The data to emit.
   * @param parent
   *   The parent message.
   * @return 
   *   The called worker instance.
   */
  public Worker emit(JsonObject data, JsonMessage parent);

  /**
   * Emits child data from the worker with a tag.
   *
   * @param data
   *   The data to emit.
   * @param tag
   *   A tag to apply to the message.
   * @param parent
   *   The parent message.
   * @return 
   *   The called worker instance.
   */
  public Worker emit(JsonObject data, String tag, JsonMessage parent);

  /**
   * Acknowledges processing of a message.
   *
   * @param message
   *   The message to ack.
   */
  public void ack(JsonMessage message);

  /**
   * Fails processing of a message.
   *
   * @param message
   *   The message to fail.
   */
  public void fail(JsonMessage message);

  /**
   * Fails processing of a message with a fail message.
   *
   * @param message
   *   The message to fail.
   * @param failureMessage
   *   A fail message to sent to the data source.
   */
  public void fail(JsonMessage message, String failureMessage);

}
