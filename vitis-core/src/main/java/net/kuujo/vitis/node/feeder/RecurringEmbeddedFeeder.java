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
package net.kuujo.vitis.node.feeder;

import net.kuujo.vitis.context.WorkerContext;
import net.kuujo.vitis.messaging.DefaultJsonMessage;
import net.kuujo.vitis.messaging.JsonMessage;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Container;

/**
 * A recurring embedded feeder implementation.
 *
 * @author Jordan Halterman
 */
public class RecurringEmbeddedFeeder extends AbstractFeeder implements EmbeddedFeeder<RecurringEmbeddedFeeder>, RecurringFeeder<RecurringEmbeddedFeeder> {

  protected Handler<RecurringEmbeddedFeeder> feedHandler;

  protected Handler<JsonMessage> ackHandler;

  protected Handler<JsonMessage> failHandler;

  protected boolean autoRetry = true;

  protected int retryAttempts = -1;

  private boolean fed;

  private long feedDelay = 100;

  protected RecurringEmbeddedFeeder(Vertx vertx, Container container, WorkerContext context) {
    super(vertx, container, context);
  }

  @Override
  public void start() {
    super.start();
    vertx.setTimer(1000, new Handler<Long>() {
      @Override
      public void handle(Long timerID) {
        recursiveFeed();
      }
    });
  }

  @Override
  public RecurringEmbeddedFeeder setFeedDelay(long delay) {
    feedDelay = delay;
    return this;
  }

  @Override
  public long getFeedDelay() {
    return feedDelay;
  }

  /**
   * Schedules a feed.
   */
  private void scheduleFeed() {
    vertx.setTimer(feedDelay, new Handler<Long>() {
      @Override
      public void handle(Long timerID) {
        recursiveFeed();
      }
    });
  }

  /**
   * Recursively invokes the feed handler.
   * If the feed handler is invoked and no messages are fed from the handler,
   * a timer is set to restart the feed in the future.
   */
  private void recursiveFeed() {
    fed = true;
    while (fed && !queue.full()) {
      fed = false;
      doFeed();
    }
    scheduleFeed();
  }

  /**
   * Invokes the feed handler.
   */
  private void doFeed() {
    if (feedHandler != null) {
      feedHandler.handle(this);
    }
  }

  @Override
  public RecurringEmbeddedFeeder setFeedQueueMaxSize(long maxSize) {
    queue.setMaxQueueSize(maxSize);
    return this;
  }

  @Override
  public long getFeedQueueMaxSize() {
    return queue.getMaxQueueSize();
  }

  @Override
  public boolean feedQueueFull() {
    return queue.full();
  }

  @Override
  public RecurringEmbeddedFeeder autoRetry(boolean retry) {
    autoRetry = retry;
    return this;
  }

  @Override
  public boolean autoRetry() {
    return autoRetry;
  }

  @Override
  public RecurringEmbeddedFeeder retryAttempts(int attempts) {
    retryAttempts = attempts;
    return this;
  }

  @Override
  public int retryAttempts() {
    return retryAttempts;
  }

  /**
   * Executes a feed.
   */
  private void doFeed(final JsonObject data, final String tag, final int attempts) {
    final JsonMessage message = DefaultJsonMessage.create(data, tag);
    queue.enqueue(message.id(), new Handler<AsyncResult<Void>>() {
      @Override
      public void handle(AsyncResult<Void> result) {
        if (result.succeeded()) {
          if (ackHandler != null) {
            ackHandler.handle(message);
          }
        }
        else if (autoRetry) {
          if (retryAttempts == -1 || attempts < retryAttempts) {
            doFeed(data, tag, attempts+1);
          }
          else if (failHandler != null) {
            failHandler.handle(message);
          }
        }
        else {
          if (failHandler != null) {
            failHandler.handle(message);
          }
        }
      }
    });
    output.emit(message);
  }

  @Override
  public RecurringEmbeddedFeeder feed(JsonObject data) {
    doFeed(data, null, 0);
    return this;
  }

  @Override
  public RecurringEmbeddedFeeder feed(JsonObject data, String tag) {
    doFeed(data, tag, 0);
    return this;
  }

  @Override
  public RecurringEmbeddedFeeder feedHandler(Handler<RecurringEmbeddedFeeder> handler) {
    this.feedHandler = handler;
    return this;
  }

  @Override
  public RecurringEmbeddedFeeder ackHandler(Handler<JsonMessage> ackHandler) {
    this.ackHandler = ackHandler;
    return this;
  }

  @Override
  public RecurringEmbeddedFeeder failHandler(Handler<JsonMessage> failHandler) {
    this.failHandler = failHandler;
    return this;
  }

}
