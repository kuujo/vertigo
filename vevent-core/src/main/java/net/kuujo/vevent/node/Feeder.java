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
package net.kuujo.vevent.node;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonObject;

/**
 * A network feeder.
 *
 * @author Jordan Halterman
 */
public interface Feeder {

  /**
   * Sets the maximum feed queue size.
   *
   * @param maxQueueSize
   *   The maximum feed queue size.
   * @return
   *   The called feeder instance.
   */
  public Feeder setFeedQueueMaxSize(long maxQueueSize);

  /**
   * Starts the feeder.
   */
  public void start();

  /**
   * Feeds data to the network.
   *
   * @param data
   *   The data to feed.
   * @return
   *   The called feeder instance.
   */
  public Feeder feed(JsonObject data);

  /**
   * Feeds data to the network.
   *
   * @param data
   *   The data to feed.
   * @param doneHandler
   *   A handler to be invoked once processing is complete.
   * @return
   *   The called feeder instance.
   */
  public Feeder feed(JsonObject data, Handler<AsyncResult<Void>> doneHandler);

  /**
   * Feeds data to the network.
   *
   * @param data
   *   The data to feed.
   * @param timeout
   *   A processing timeout.
   * @param doneHandler
   *   A handler to be invoked once processing is complete.
   * @return
   *   The called feeder instance.
   */
  public Feeder feed(JsonObject data, long tiemout, Handler<AsyncResult<Void>> doneHandler);

  /**
   * Feeds data to the network.
   *
   * @param data
   *   The data to feed.
   * @param tag
   *   A tag to apply to fed data.
   * @return
   *   The called feeder instance.
   */
  public Feeder feed(JsonObject data, String tag);

  /**
   * Feeds data to the network.
   *
   * @param data
   *   The data to feed.
   * @param tag
   *   A tag to apply to fed data.
   * @param doneHandler
   *   A handler to be invoked once processing is complete.
   * @return
   *   The called feeder instance.
   */
  public Feeder feed(JsonObject data, String tag, Handler<AsyncResult<Void>> doneHandler);

  /**
   * Feeds data to the network.
   *
   * @param data
   *   The data to feed.
   * @param tag
   *   A tag to apply to fed data.
   * @param timeout
   *   A processing timeout.
   * @param doneHandler
   *   A handler to be invoked once processing is complete.
   * @return
   *   The called feeder instance.
   */
  public Feeder feed(JsonObject data, String tag, long tiemout, Handler<AsyncResult<Void>> doneHandler);

}
