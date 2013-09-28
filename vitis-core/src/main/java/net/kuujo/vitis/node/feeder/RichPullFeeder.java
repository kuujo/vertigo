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
 * A rich pull feeder implementation.
 *
 * @author Jordan Halterman
 */
public class RichPullFeeder extends AbstractFeeder implements PullFeeder<RichPullFeeder>, RichFeeder<RichPullFeeder> {

  protected Handler<RichPullFeeder> feedHandler;

  private boolean fed;

  private long feedDelay = 100;

  public RichPullFeeder(Vertx vertx, Container container, WorkerContext context) {
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
  public RichPullFeeder setDefaultTimeout(long timeout) {
    queue.setDefaultTimeout(timeout);
    return this;
  }

  @Override
  public RichPullFeeder setFeedDelay(long delay) {
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
  public RichPullFeeder setFeedQueueMaxSize(long maxSize) {
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
  public RichPullFeeder feed(JsonObject data) {
    output.emit(DefaultJsonMessage.create(data));
    fed = true;
    return this;
  }

  @Override
  public RichPullFeeder feed(JsonObject data, String tag) {
    output.emit(DefaultJsonMessage.create(data, tag));
    fed = true;
    return this;
  }

  @Override
  public RichPullFeeder feed(JsonObject data, Handler<AsyncResult<Void>> ackHandler) {
    JsonMessage message = DefaultJsonMessage.create(data);
    queue.enqueue(message.id(), ackHandler);
    output.emit(message);
    return this;
  }

  @Override
  public RichPullFeeder feed(JsonObject data, long timeout, Handler<AsyncResult<Void>> ackHandler) {
    JsonMessage message = DefaultJsonMessage.create(data);
    queue.enqueue(message.id(), timeout, ackHandler);
    output.emit(message);
    return this;
  }

  @Override
  public RichPullFeeder feed(JsonObject data, String tag, Handler<AsyncResult<Void>> ackHandler) {
    JsonMessage message = DefaultJsonMessage.create(data, tag);
    queue.enqueue(message.id(), ackHandler);
    output.emit(message);
    return this;
  }

  @Override
  public RichPullFeeder feed(JsonObject data, String tag, long timeout, Handler<AsyncResult<Void>> ackHandler) {
    JsonMessage message = DefaultJsonMessage.create(data, tag);
    queue.enqueue(message.id(), timeout, ackHandler);
    output.emit(message);
    return this;
  }

  @Override
  public RichPullFeeder feedHandler(Handler<RichPullFeeder> handler) {
    this.feedHandler = handler;
    return this;
  }

}
