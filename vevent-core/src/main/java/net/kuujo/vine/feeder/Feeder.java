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
package net.kuujo.vine.feeder;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonObject;

/**
 * A vine feeder.
 *
 * @author Jordan Halterman
 */
public interface Feeder {

  /**
   * Sets the feed queue max size.
   *
   * @param maxSize
   *   The feed queue max size.
   * @return
   *   The called feeder instance.
   */
  public Feeder setFeedQueueMaxSize(long maxSize);

  /**
   * Indicates whether the feeder queue is full.
   *
   * @return
   *   A boolean indicating whether the feeder queue is full.
   */
  public boolean feedQueueFull();

  /**
   * Feeds data to the vine.
   *
   * @param data
   *   The data to feed.
   * @return
   *   The called feeder instance.
   */
  public Feeder feed(JsonObject data);

  /**
   * Feeds data to the vine, providing a completion handler.
   *
   * @param data
   *   The data to feed.
   * @param doneHandler
   *   An asynchronous handler to be invoked once processing is complete (or fails).
   * @return
   *   The called feeder instance.
   */
  public Feeder feed(JsonObject data, Handler<AsyncResult<Void>> doneHandler);

  /**
   * Feeds data to the vine, providing a completion handler.
   *
   * @param data
   *   The data to feed.
   * @param timeout
   *   A data processing timeout.
   * @param doneHandler
   *   An asynchronous handler to be invoked once processing is complete (or fails).
   * @return
   *   The called feeder instance.
   */
  public Feeder feed(JsonObject data, long timeout, Handler<AsyncResult<Void>> doneHandler);

  /**
   * Sets a drain handler on the feeder.
   *
   * @param handler
   *   The drain handler.
   * @return
   *   The called feeder instance.
   */
  public Feeder drainHandler(Handler<Void> handler);

}
