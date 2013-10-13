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
package net.kuujo.vertigo.component.executor;

import net.kuujo.vertigo.messaging.JsonMessage;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;

/**
 * An executor queue.
 *
 * @author Jordan Halterman
 */
public interface ExecuteQueue {

  /**
   * Sets the queue reply timeout.
   *
   * @param timeout
   *   The reply timeout.
   * @return
   *   The called executor queue instance.
   */
  public ExecuteQueue setReplyTimeout(long timeout);

  /**
   * Gets the queue reply timeout.
   *
   * @return
   *   The queue reply timeout.
   */
  public long getReplyTimeout();

  /**
   * Sets the maximum executor queue size.
   *
   * @param maxSize
   *   A executor queue size.
   * @return
   *   The called executor queue instance.
   */
  public ExecuteQueue setMaxQueueSize(long maxSize);

  /**
   * Gets the maximum executor queue size.
   *
   * @return
   *   The maximum allowed executor queue size.
   */
  public long getMaxQueueSize();

  /**
   * Indicates the current executor queue size.
   *
   * @return
   *   The current feed executor size.
   */
  public long size();

  /**
   * Indicates whether the executor queue is full.
   *
   * @return
   *   A boolean indicating whether the executor queue is full.
   */
  public boolean full();

  /**
   * Adds a unique ID to the queue.
   *
   * @param id
   *   A unique message ID.
   * @param resultHandler
   *   A handler to be invoked once the response is received.
   * @return
   *   The called executor queue instance.
   */
  public ExecuteQueue enqueue(String id, Handler<AsyncResult<JsonMessage>> resultHandler);

  /**
   * Acks a message in the queue.
   *
   * @param id
   *   The unique message ID.
   */
  public void ack(String id);

  /**
   * Fails a message in the queue.
   *
   * @param id
   *   The unique message ID.
   */
  public void fail(String id);

  /**
   * Fails a message in the queue with a failure message.
   *
   * @param id
   *   The unique message ID.
   * @param message
   *   A failure message.
   */
  public void fail(String id, String message);

  /**
   * Receives a response.
   *
   * @param message
   *   The response message.
   */
  public void receive(JsonMessage message);

}
