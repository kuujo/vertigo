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

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Future;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.impl.DefaultFutureResult;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Container;

/**
 * A default polling feeder implementation.
 *
 * @author Jordan Halterman
 */
public class DefaultPollingFeeder extends AbstractFeeder<PollingFeeder> implements PollingFeeder {

  private Handler<PollingFeeder> feedHandler;

  private long feedDelay = 500;

  private boolean fed;

  public DefaultPollingFeeder(Vertx vertx, Container container, WorkerContext context) {
    super(vertx, container, context);
  }

  @Override
  public PollingFeeder feedDelay(long delay) {
    feedDelay = delay;
    return this;
  }

  @Override
  public long feedDelay() {
    return feedDelay;
  }

  @Override
  public PollingFeeder feedHandler(Handler<PollingFeeder> handler) {
    feedHandler = handler;
    return this;
  }

  @Override
  public PollingFeeder start() {
    return super.start(new Handler<AsyncResult<PollingFeeder>>() {
      @Override
      public void handle(AsyncResult<PollingFeeder> result) {
        if (result.succeeded()) {
          recursiveFeed();
        }
      }
    });
  }

  @Override
  public PollingFeeder start(Handler<AsyncResult<PollingFeeder>> doneHandler) {
    final Future<PollingFeeder> future = new DefaultFutureResult<PollingFeeder>().setHandler(doneHandler);
    return super.start(new Handler<AsyncResult<PollingFeeder>>() {
      @Override
      public void handle(AsyncResult<PollingFeeder> result) {
        if (result.failed()) {
          future.setFailure(result.cause());
        }
        else {
          recursiveFeed();
          future.setResult(result.result());
        }
      }
    });
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
  public PollingFeeder feed(JsonObject data) {
    fed = true;
    doFeed(data, null, 0, null);
    return this;
  }

  @Override
  public PollingFeeder feed(JsonObject data, String tag) {
    fed = true;
    doFeed(data, tag, 0, null);
    return this;
  }

  @Override
  public PollingFeeder feed(JsonObject data, Handler<AsyncResult<Void>> ackHandler) {
    fed = true;
    doFeed(data, null, 0, new DefaultFutureResult<Void>().setHandler(ackHandler));
    return this;
  }

  @Override
  public PollingFeeder feed(JsonObject data, String tag, Handler<AsyncResult<Void>> ackHandler) {
    fed = true;
    doFeed(data, tag, 0, new DefaultFutureResult<Void>().setHandler(ackHandler));
    return this;
  }

}
