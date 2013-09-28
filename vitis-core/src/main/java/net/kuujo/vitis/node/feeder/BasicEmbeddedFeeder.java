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
 * A basic embedded feeder implementation.
 *
 * @author Jordan Halterman
 */
@SuppressWarnings("rawtypes")
public class BasicEmbeddedFeeder extends AbstractFeeder implements EmbeddedFeeder<EmbeddedFeeder> {

  private Handler<EmbeddedFeeder> feedHandler;

  private Handler<JsonMessage> ackHandler;

  private Handler<JsonMessage> failHandler;

  private boolean fed;

  private long feedDelay = 100;

  protected BasicEmbeddedFeeder(Vertx vertx, Container container, WorkerContext context) {
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
  public EmbeddedFeeder setFeedDelay(long delay) {
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
  public EmbeddedFeeder setFeedQueueMaxSize(long maxSize) {
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

  private Handler<AsyncResult<Void>> createAckHandler(final JsonMessage message) {
    return new Handler<AsyncResult<Void>>() {
      @Override
      public void handle(AsyncResult<Void> result) {
        if (result.failed()) {
          if (failHandler != null) {
            failHandler.handle(message);
          }
        }
        else {
          if (ackHandler != null) {
            ackHandler.handle(message);
          }
        }
      }
    };
  }

  @Override
  public EmbeddedFeeder feed(JsonObject data) {
    JsonMessage message = DefaultJsonMessage.create(data);
    queue.enqueue(message.id(), createAckHandler(message));
    output.emit(message);
    return this;
  }

  @Override
  public EmbeddedFeeder feed(JsonObject data, String tag) {
    JsonMessage message = DefaultJsonMessage.create(data, tag);
    queue.enqueue(message.id(), createAckHandler(message));
    output.emit(message);
    return this;
  }

  @Override
  public EmbeddedFeeder feedHandler(Handler<EmbeddedFeeder> handler) {
    this.feedHandler = handler;
    return this;
  }

  @Override
  public EmbeddedFeeder ackHandler(Handler<JsonMessage> ackHandler) {
    this.ackHandler = ackHandler;
    return this;
  }

  @Override
  public EmbeddedFeeder failHandler(Handler<JsonMessage> failHandler) {
    this.failHandler = failHandler;
    return this;
  }

}
