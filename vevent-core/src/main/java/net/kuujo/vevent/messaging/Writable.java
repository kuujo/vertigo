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
package net.kuujo.vevent.messaging;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;

/**
 * A writeable interface.
 *
 * @author Jordan Halterman
 */
public interface Writable<T extends Writable<T>> {

  /**
   * Writes a message to the channel.
   *
   * @param message
   *   The message to write to the channel.
   * @return
   *   The called writable instance.
   */
  public T write(JsonMessage message);

  /**
   * Writes a message to the channel.
   *
   * @param message
   *   The message to write to the channel.
   * @param doneHandler
   *   An asynchronous result handler to be invoked once the message has been received.
   * @return
   *   The called writable instance.
   */
  public T write(JsonMessage message, Handler<AsyncResult<Boolean>> doneHandler);

  /**
   * Writes a message to the channel.
   *
   * @param message
   *   The message to write to the channel.
   * @param timeout
   *   A message timeout.
   * @param doneHandler
   *   An asynchronous result handler to be invoked once the message has been received.
   * @return
   *   The called writable instance.
   */
  public T write(JsonMessage message, long timeout, Handler<AsyncResult<Boolean>> doneHandler);

  /**
   * Writes a message to the channel.
   *
   * @param message
   *   The message to write to the channel.
   * @param timeout
   *   A message timeout.
   * @param retry
   *   Indicates whether to retry sending the message if sending times out.
   * @param doneHandler
   *   An asynchronous result handler to be invoked once the message has been received.
   * @return
   *   The called writable instance.
   */
  public T write(JsonMessage message, long timeout, boolean retry, Handler<AsyncResult<Boolean>> doneHandler);

  /**
   * Writes a message to the channel.
   *
   * @param message
   *   The message to write to the channel.
   * @param timeout
   *   A message timeout.
   * @param retry
   *   Indicates whether to retry sending the message if sending times out.
   * @param attempts
   *   Indicates the number of times to retry if retries are enabled.
   * @param doneHandler
   *   An asynchronous result handler to be invoked once the message has been received.
   * @return
   *   The called writable instance.
   */
  public T write(JsonMessage message, long timeout, boolean retry, int attempts, Handler<AsyncResult<Boolean>> doneHandler);

}
