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
 * A reliable stream feeder implementation.
 *
 * @author Jordan Halterman
 */
public class ReliableStreamFeeder extends AbstractFeeder implements StreamFeeder<ReliableStreamFeeder>, ReliableFeeder<ReliableStreamFeeder> {

  private boolean started;

  private boolean paused;

  protected boolean autoRetry = true;

  protected int retryAttempts = -1;

  private Handler<Void> drainHandler;

  public ReliableStreamFeeder(Vertx vertx, Container container, WorkerContext context) {
    super(vertx, container, context);
  }

  @Override
  public void start() {
    super.start();
    vertx.setTimer(1000, new Handler<Long>() {
      @Override
      public void handle(Long timerID) {
        started = true;
      }
    });
  }

  @Override
  public ReliableStreamFeeder autoRetry(boolean retry) {
    autoRetry = retry;
    return this;
  }

  @Override
  public boolean autoRetry() {
    return autoRetry;
  }

  @Override
  public ReliableStreamFeeder retryAttempts(int attempts) {
    retryAttempts = attempts;
    return this;
  }

  @Override
  public int retryAttempts() {
    return retryAttempts;
  }

  @Override
  public ReliableStreamFeeder setFeedQueueMaxSize(long maxSize) {
    queue.setMaxQueueSize(maxSize);
    return this;
  }

  @Override
  public long getFeedQueueMaxSize() {
    return queue.getMaxQueueSize();
  }

  @Override
  public boolean feedQueueFull() {
    return !started || paused;
  }

  /**
   * Executes a feed.
   */
  private void doFeed(final JsonObject data, final String tag, final int attempts) {
    final JsonMessage message = DefaultJsonMessage.create(data, tag);
    queue.enqueue(message.id(), new Handler<AsyncResult<Void>>() {
      @Override
      public void handle(AsyncResult<Void> result) {
        checkPause();
        if (result.failed() && autoRetry && (retryAttempts == -1 || attempts < retryAttempts)) {
          doFeed(data, tag, attempts+1);
        }
      }
    });
    output.emit(message);
  }

  @Override
  public ReliableStreamFeeder feed(JsonObject data) {
    doFeed(data, null, 0);
    return this;
  }

  @Override
  public ReliableStreamFeeder feed(JsonObject data, String tag) {
    doFeed(data, tag, 0);
    return this;
  }

  /**
   * Checks the current stream pause status.
   */
  private void checkPause() {
    if (paused) {
      if (queue.size() < queue.getMaxQueueSize() * .75) {
        paused = false;
        if (drainHandler != null) {
          drainHandler.handle(null);
        }
      }
    }
    else {
      if (queue.size() >= queue.getMaxQueueSize()) {
        paused = true;
      }
    }
  }

  @Override
  public ReliableStreamFeeder drainHandler(Handler<Void> handler) {
    this.drainHandler = handler;
    return this;
  }

}
