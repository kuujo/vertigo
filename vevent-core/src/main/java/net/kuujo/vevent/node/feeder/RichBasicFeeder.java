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
package net.kuujo.vevent.node.feeder;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonObject;

/**
 * A rich feeder.
 *
 * @author Jordan Halterman
 *
 * @param <T> The feeder type
 */
public interface RichBasicFeeder<T extends RichBasicFeeder<?>> extends BasicFeeder<T> {

  /**
   * Sets the default ack timeout.
   *
   * @param timeout
   *   The default ack timeout. If a message is not acked or failed within
   *   the timeout period, it will be automatically failed.
   * @return
   *   The called feeder instance.
   */
  public T setDefaultTimeout(long timeout);

  /**
   * Feeds data to the network, providing an ack handler.
   *
   * @param data
   *   The data to feed.
   * @param ackHandler
   *   An asynchronous result handler to be invoked with the ack/fail result.
   * @return
   *   The called feeder instance.
   */
  public T feed(JsonObject data, Handler<AsyncResult<Void>> ackHandler);

  /**
   * Feeds data to the network, providing an ack handler.
   *
   * @param data
   *   The data to feed.
   * @param timeout
   *   The maximum time allowed to wait for an ack or fail. Note that this
   *   timeout overrides the default ack timeout.
   * @param ackHandler
   *   An asynchronous result handler to be invoked with the ack/fail result.
   * @return
   *   The called feeder instance.
   */
  public T feed(JsonObject data, long timeout, Handler<AsyncResult<Void>> ackHandler);

  /**
   * Feeds data to the network, providing an ack handler.
   *
   * @param data
   *   The data to feed.
   * @param tag
   *   A tag to apply to the data.
   * @param ackHandler
   *   An asynchronous result handler to be invoked with the ack/fail result.
   * @return
   *   The called feeder instance.
   */
  public T feed(JsonObject data, String tag, Handler<AsyncResult<Void>> ackHandler);

  /**
   * Feeds data to the network, providing an ack handler.
   *
   * @param data
   *   The data to feed.
   * @param tag
   *   A tag to apply to the data.
   * @param timeout
   *   The maximum time allowed to wait for an ack or fail. Note that this
   *   timeout overrides the default ack timeout.
   * @param ackHandler
   *   An asynchronous result handler to be invoked with the ack/fail result.
   * @return
   *   The called feeder instance.
   */
  public T feed(JsonObject data, String tag, long timeout, Handler<AsyncResult<Void>> ackHandler);

}
