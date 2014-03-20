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
import net.kuujo.vertigo.network.FailureException;
import net.kuujo.vertigo.network.TimeoutException;

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
 */
public interface Feeder extends Component<Feeder> {

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
  Feeder setFeedQueueMaxSize(long maxSize);

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
  long getFeedQueueMaxSize();

  /**
   * Indicates whether the feed queue is full.
   *
   * Depending on the feeder implementation, this method may be used to check
   * whether the feed queue is full prior to feeding additional data to a network.
   *
   * @return
   *   A boolean indicating whether the feed queue is full.
   */
  boolean feedQueueFull();

  /**
   * Sets the feeder auto-retry option.
   *
   * If this option is enabled, the feeder will automatically retry sending
   * timed out messages. Note that this means that asynchronous ack handlers
   * will not be called since the feeder will automatically retry sending those
   * timed out messages. Failed messages will still trigger the ack handler.
   *
   * @param retry
   *   Indicates whether to automatically retry emitting timed out data.
   * @return
   *   The called feeder instance.
   */
  Feeder setAutoRetry(boolean retry);

  /**
   * Gets the feeder auto-retry option.
   *
   * If this option is enabled, the feeder will automatically retry sending
   * timed out messages.
   *
   * @return
   *   Indicates whether the feeder with automatically retry emitting timed out data.
   */
  boolean isAutoRetry();

  /**
   * Sets the number of automatic retry attempts for a single timed out message.
   *
   * @param attempts
   *   The number of retry attempts allowed. If attempts is -1 then an infinite
   *   number of retry attempts will be allowed.
   * @return
   *   The called feeder instance.
   */
  Feeder setAutoRetryAttempts(int attempts);

  /**
   * Gets the number of automatic retry attempts.
   *
   * @return
   *   Indicates the number of retry attempts allowed for the feeder.
   */
  int getAutoRetryAttempts();

  /**
   * Sets the feed interval.
   *
   * The feed delay indicates the interval at which the feeder will attempt to
   * poll the feed handler for new data.
   *
   * @param delay
   *   The empty feed delay.
   * @return
   *   The called feeder instance.
   */
  Feeder setFeedDelay(long delay);

  /**
   * Gets the feed interval.
   *
   * The feed delay indicates the interval at which the feeder will attempt to
   * poll the feed handler for new data.
   *
   * @return
   *   The empty feed delay.
   */
  long getFeedDelay();

  /**
   * Sets a feed handler.
   *
   * The feed handler will be periodically polled for new data. Each time the
   * feed handler is polled only a single message should be emitted. This allows
   * the feeder to maintain control over the flow of data. If the feed handler
   * is called but fails to feed any new messages to the network, the feeder
   * will reschedule the next call to the handler for a period in the near future.
   *
   * @param handler
   *   A handler to be invoked for feeding data to the network.
   * @return
   *   The called feeder instance.
   */
  Feeder feedHandler(Handler<Feeder> handler);

  /**
   * Sets a drain handler on the feeder.
   *
   * The drain handler will be called when the feed queue is available to
   * receive new messages.
   *
   * @param handler
   *   A handler to be invoked when a full feed queue is emptied.
   * @return
   *   The called feeder instance.
   */
  Feeder drainHandler(Handler<Void> handler);

  /**
   * Emits a message to the default stream.
   *
   * @param body
   *   The message body.
   * @return
   *   The emitted message identifier.
   */
  String emit(JsonObject body);

  /**
   * Emits a message to the default stream with an ack handler.
   *
   * @param body
   *   The message body.
   * @param ackHandler
   *   An asynchronous result handler to be called once the emitted message
   *   has been fully processed. This {@link AsyncResult} implementation is a
   *   special implementation that will *always* contain the message ID
   *   regardless of whether the message was successfully processed or not. If
   *   the message was not successfully processed, the cause of the failure will
   *   be either a {@link FailureException} or a {@link TimeoutException}. If
   *   auto retries are enabled on the feeder then the ack handler will not
   *   be called for timed out messages (unless the maximum number of automatic
   *   retries has been reached).
   * @return
   *   The emitted message identifier.
   */
  String emit(JsonObject body, Handler<AsyncResult<String>> ackHandler);

  /**
   * Emits a message to a non-default stream.
   *
   * @param stream
   *   The stream to which to emit the data.
   * @param body
   *   The message body.
   * @return
   *   The emitted message identifier.
   */
  String emit(String stream, JsonObject body);

  /**
   * Emits a message to a non-default stream with an ack handler.
   *
   * @param stream
   *   The stream to which to emit the data.
   * @param body
   *   The message body.
   * @param ackHandler
   *   An asynchronous result handler to be called once the emitted message
   *   has been fully processed. This {@link AsyncResult} implementation is a
   *   special implementation that will *always* contain the message ID
   *   regardless of whether the message was successfully processed or not. If
   *   the message was not successfully processed, the cause of the failure will
   *   be either a {@link FailureException} or a {@link TimeoutException}. If
   *   auto retries are enabled on the feeder then the ack handler will not
   *   be called for timed out messages (unless the maximum number of automatic
   *   retries has been reached).
   * @return
   *   The emitted message identifier.
   */
  String emit(String stream, JsonObject body, Handler<AsyncResult<String>> ackHandler);

}
