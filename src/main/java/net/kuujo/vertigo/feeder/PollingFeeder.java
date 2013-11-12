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

import org.vertx.java.core.Handler;

/**
 * A polling feeder.
 *
 * The polling feeder reads input from code by periodically polling a handler
 * for new data. If the feed handler fails to emit new data when polled, the
 * feeder will reschedule the next feed for a period in the near future.
 *
 * @author Jordan Halterman
 */
public interface PollingFeeder extends Feeder<PollingFeeder> {

  /**
   * Sets the feed delay.
   *
   * The feed delay indicates the interval at which the feeder will attempt to
   * poll the feed handler for new data.
   *
   * @param delay
   *   The empty feed delay.
   * @return
   *   The called feeder instance.
   */
  PollingFeeder setFeedDelay(long delay);

  /**
   * Gets the feed delay.
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
  PollingFeeder feedHandler(Handler<PollingFeeder> handler);

  /**
   * Sets an ack handler on the feeder.
   *
   * @param ackHandler
   *   A handler to be invoked when a message is acked.
   * @return
   *   The called feeder instance.
   */
  PollingFeeder ackHandler(Handler<String> ackHandler);

  /**
   * Sets a fail handler on the feeder.
   *
   * @param failHandler
   *   A handler to be invoked when a message is failed.
   * @return
   *   The called feeder instance.
   */
  PollingFeeder failHandler(Handler<String> failHandler);

}
