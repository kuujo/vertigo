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

import net.kuujo.vevent.context.WorkerContext;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Container;

/**
 * A basic pull feeder implementation.
 *
 * @author Jordan Halterman
 */
public class BasicPullFeeder extends BasicFeeder implements PullFeeder {

  protected Handler<Feeder> feedHandler;

  private boolean fed;

  public BasicPullFeeder(Vertx vertx, Container container, WorkerContext context) {
    super(vertx, container, context);
  }

  @Override
  public void start() {
    super.start();
    recursiveFeed();
  }

  /**
   * Calls the feeder recursively.
   */
  private void recursiveFeed() {
    if (feedHandler != null) {
      fed = true;
      while (fed) {
        fed = false;
        feedHandler.handle(this);
      }
      rescheduleFeed();
    }
  }

  /**
   * Reschedules the feeder.
   */
  private void rescheduleFeed() {
    vertx.setTimer(100, new Handler<Long>() {
      @Override
      public void handle(Long event) {
        recursiveFeed();
      }
    });
  }

  @Override
  public PullFeeder feedHandler(Handler<Feeder> handler) {
    this.feedHandler = handler;
    return this;
  }

  @Override
  public Feeder feed(JsonObject data) {
    fed = true;
    return super.feed(data);
  }

  @Override
  public Feeder feed(JsonObject data, Handler<AsyncResult<Void>> doneHandler) {
    fed = true;
    return super.feed(data, doneHandler);
  }

  @Override
  public Feeder feed(JsonObject data, long timeout, Handler<AsyncResult<Void>> doneHandler) {
    fed = true;
    return super.feed(data, timeout, doneHandler);
  }

  @Override
  public Feeder feed(JsonObject data, String tag) {
    fed = true;
    return super.feed(data, tag);
  }

  @Override
  public Feeder feed(JsonObject data, String tag, Handler<AsyncResult<Void>> doneHandler) {
    fed = true;
    return super.feed(data, tag, doneHandler);
  }

  @Override
  public Feeder feed(JsonObject data, String tag, long timeout, Handler<AsyncResult<Void>> doneHandler) {
    fed = true;
    return super.feed(data, tag, timeout, doneHandler);
  }

}
