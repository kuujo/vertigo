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
package net.kuujo.vertigo.feeder;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonObject;

import net.kuujo.vertigo.component.Component;

/**
 * A message feeder.
 *
 * Feeders are tasked with feeding messages from a data source to a network.
 * When data is emitted from a feeder, the data will be transformed into a
 * message and tagged with a unique identifier. This identifier is used to track
 * the message and its descendents throughout the network. Once the message and
 * all of its descendants have been fully processed (acked), the feeder may
 * optionally be notified via asynchronous result handlers.
 *
 * @author Jordan Halterman
 *
 * @param <T> The feeder type
 */
public interface Feeder<T extends Feeder<T>> extends Component {

  /**
   * Starts the feeder.
   *
   * @return
   *   The called feeder instance.
   */
  T start();

  /**
   * Starts the feeder.
   *
   * @param doneHandler
   *   An asyncronous result handler to be invoked once the feeder is started.
   * @return
   *   The called feeder instance.
   */
  T start(Handler<AsyncResult<T>> doneHandler);

  /**
   * Sets the maximum feed queue size.
   *
   * The feeder uses an underlying queue to track which messages have been emitted
   * from the component but not yet acked. This indicates how many messages may
   * reside in the queue (in memory) at any given time.
   *
   * @param maxSize
   *   The maximum queue size allowed for the feeder.
   * @return
   *   The called feeder instance.
   */
  T setMaxQueueSize(long maxSize);

  /**
   * Gets the maximum feed queue size.
   *
   * The feeder uses an underlying queue to track which messages have been emitted
   * from the component but not yet acked. This indicates how many messages may
   * reside in the queue (in memory) at any given time.
   *
   * @return
   *   The maximum queue size allowed for the feeder.
   */
  long getMaxQueueSize();

  /**
   * Indicates whether the feed queue is full.
   *
   * Depending on the feeder implementation, this method may be used to check
   * whether the feed queue is full prior to feeding additional data to a network.
   *
   * @return
   *   A boolean indicating whether the feed queue is full.
   */
  boolean queueFull();

  /**
   * Sets the feeder auto-retry option.
   *
   * If this option is enabled, the feeder will automatically retry sending
   * failed or timed out messages.
   *
   * @param retry
   *   Indicates whether to automatically retry emitting failed data.
   * @return
   *   The called feeder instance.
   */
  T setAutoRetry(boolean retry);

  /**
   * Gets the feeder auto-retry option.
   *
   * If this option is enabled, the feeder will automatically retry sending
   * failed or timed out messages.
   *
   * @return
   *   Indicates whether the feeder with automatically retry emitting failed data.
   */
  boolean isAutoRetry();

  /**
   * Sets the number of automatic retry attempts for a single failed message.
   *
   * @param attempts
   *   The number of retry attempts allowed. If attempts is -1 then an infinite
   *   number of retry attempts will be allowed.
   * @return
   *   The called feeder instance.
   */
  T setRetryAttempts(int attempts);

  /**
   * Gets the number of automatic retry attempts.
   *
   * @return
   *   Indicates the number of retry attempts allowed for the feeder.
   */
  int getRetryAttempts();

  /**
   * Feeds data through the feeder.
   *
   * @param data
   *   The data to feed.
   * @return
   *   The called feeder instance.
   */
  T feed(JsonObject data);

  /**
   * Feeds data through the feeder.
   *
   * @param data
   *   The data to feed.
   * @param tag
   *   A tag to apply to the data.
   * @return
   *   The called feeder instance.
   */
  T feed(JsonObject data, String tag);

  /**
   * Feeds data to the network with an ack handler.
   *
   * @param data
   *   The data to feed.
   * @param ackHandler
   *   An asynchronous result handler to be invoke with the ack result.
   * @return
   *   The called feeder instance.
   */
  T feed(JsonObject data, Handler<AsyncResult<Void>> ackHandler);

  /**
   * Feeds data to the network with an ack handler.
   *
   * @param data
   *   The data to feed.
   * @param tag
   *   A tag to apply to the data.
   * @param ackHandler
   *   An asynchronous result handler to be invoke with the ack result.
   * @return
   *   The called feeder instance.
   */
  T feed(JsonObject data, String tag, Handler<AsyncResult<Void>> ackHandler);

}
