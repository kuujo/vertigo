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
package net.kuujo.vertigo.node.feeder;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;

/**
 * A feed queue.
 *
 * @author Jordan Halterman
 */
public interface FeedQueue {

  /**
   * Sets the maximum feed queue size.
   *
   * @param maxSize
   *   A feed queue size.
   * @return
   *   The called feed queue instance.
   */
  public FeedQueue maxQueueSize(long maxSize);

  /**
   * Gets the maximum feed queue size.
   *
   * @return
   *   The maximum allowed feed queue size.
   */
  public long maxQueueSize();

  /**
   * Indicates the current feed queue size.
   *
   * @return
   *   The current feed queue size.
   */
  public long size();

  /**
   * Indicates whether the feed queue is full.
   *
   * @return
   *   A boolean indicating whether the feed queue is full.
   */
  public boolean full();

  /**
   * Adds a unique ID to the queue.
   *
   * @param id
   *   A unique message ID.
   * @param ackHandler
   *   A handler to be invoked once the message is acked or fails or the message
   *   times out.
   * @return
   *   The called feed queue instance.
   */
  public FeedQueue enqueue(String id, Handler<AsyncResult<Void>> ackHandler);

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

}
