/*
 * Copyright 2014 the original author or authors.
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
package net.kuujo.vertigo.io;

import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;

/**
 * Provides an interface for feeding outputs using a handler that will
 * be called whenever the given output is prepared to accept a single message.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 *
 * @param <T> The output type.
 */
public class Feeder<T extends Output<T>> {
  private static final long DEFAULT_FEED_DELAY = 10L;
  private final T output;
  private final Vertx vertx;
  private Handler<T> feedHandler;
  private boolean fed;
  private long feedDelay = DEFAULT_FEED_DELAY;
  private long feedTimer;

  private final Handler<Void> feedRunner = new Handler<Void>() {
    @Override
    public void handle(Void _) {
      int before = output.size();
      feedHandler.handle(output);
      int after = output.size();
      fed = after > before;
      doFeed();
    }
  };

  private final Handler<Long> recursiveRunner = new Handler<Long>() {
    @Override
    public void handle(Long timerID) {
      doFeed();
    }
  };

  /**
   * Creates a new feeder.
   *
   * @param output The output to which to feed messages.
   * @return A new feeder.
   */
  public static <T extends Output<T>> Feeder<T> createFeeder(T output) {
    return new Feeder<T>(output);
  }

  public Feeder(T output) {
    this.output = output;
    this.vertx = output.vertx();
  }

  /**
   * Sets a feed handler on the feeder.
   *
   * @param handler A handler to be called each time a message should be
   *                fed to the feeder.
   * @return
   */
  public Feeder<T> feedHandler(Handler<T> handler) {
    this.feedHandler = handler;
    return this;
  }

  /**
   * Sets the period the feeder will wait before calling the feed handler again
   * if the feed handler is called but no messages are produced.
   *
   * @param feedDelay The feed delay in milliseconds.
   * @return The feeder.
   */
  public Feeder<T> setFeedDelay(long feedDelay) {
    this.feedDelay = feedDelay;
    return this;
  }

  /**
   * Returns the period the feeder will wait after a failed feed handler call.
   *
   * @return The feed delay. Defaults to <code>10</code> milliseconds.
   */
  public long getFeedDelay() {
    return feedDelay;
  }

  /**
   * Starts the feeder.
   */
  public void start() {
    doFeed();
  }

  /**
   * Feeds the next message.
   */
  private void doFeed() {
    if (fed && !output.sendQueueFull()) {
      fed = false;
      vertx.runOnContext(feedRunner);
    } else {
      feedTimer = vertx.setTimer(feedDelay, recursiveRunner);
    }
  }

  /**
   * Stops the feeder.
   */
  public void stop() {
    if (feedTimer > 0) {
      vertx.cancelTimer(feedTimer);
    }
  }

}
