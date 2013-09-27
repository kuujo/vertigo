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

import java.util.HashMap;
import java.util.Map;

import net.kuujo.vevent.context.WorkerContext;
import net.kuujo.vevent.messaging.DefaultJsonMessage;
import net.kuujo.vevent.messaging.JsonMessage;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Future;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.impl.DefaultFutureResult;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Container;

/**
 * A basic feeder.
 *
 * @author Jordan Halterman
 */
public class BasicFeeder extends ComponentBase implements Feeder {

  private static final long DEFAULT_TIMEOUT = 30000;

  protected long maxQueueSize = 1000;

  protected boolean paused;

  private boolean rescheduled;

  protected Handler<Feeder> feedHandler;

  private Map<String, FutureResult> futures = new HashMap<>();

  public BasicFeeder(Vertx vertx, Container container, WorkerContext context) {
    super(vertx, container, context);
  }

  @Override
  public Feeder setMaxQueueSize(long maxQueueSize) {
    this.maxQueueSize = maxQueueSize;
    return this;
  }

  @Override
  protected void doStart() {
    recursiveFeed();
  }

  @Override
  protected void doAck(String id) {
    if (futures.containsKey(id)) {
      futures.remove(id).cancel().set();
    }
  }

  @Override
  protected void doFail(String id) {
    if (futures.containsKey(id)) {
      futures.remove(id).cancel().fail(new FailureException("Processing failed."));
    }
  }

  /**
   * Recursively called the feed handler.
   */
  private void recursiveFeed() {
    rescheduled = false;
    while (!paused && !rescheduled) {
      doFeed();
    }
  }

  /**
   * Executes the feed handler.
   */
  private void doFeed() {
    if (feedHandler != null) {
      feedHandler.handle(this);
    }
    else {
      rescheduled = true;
    }
  }

  /**
   * Schedules a feed call for a future time.
   */
  protected void scheduleFeed() {
    vertx.setTimer(500, new Handler<Long>() {
      @Override
      public void handle(Long id) {
        recursiveFeed();
      }
    });
    rescheduled = true;
  }

  @Override
  public Feeder feedHandler(Handler<Feeder> handler) {
    this.feedHandler = handler;
    rescheduled = false;
    return this;
  }

  @Override
  public Feeder feed(JsonObject data) {
    JsonMessage message = DefaultJsonMessage.create(data);
    eventBus.publish(authAddress, new JsonObject().putString("action", "create").putString("id", message.id()));
    output.emit(message);
    return this;
  }

  @Override
  public Feeder feed(JsonObject data, Handler<AsyncResult<Void>> doneHandler) {
    JsonMessage message = DefaultJsonMessage.create(data);
    eventBus.publish(authAddress, new JsonObject().putString("action", "create").putString("id", message.id()));
    FutureResult result = new FutureResult(new DefaultFutureResult<Void>().setHandler(doneHandler));
    futures.put(message.id(), result);
    result.start(DEFAULT_TIMEOUT);
    output.emit(message);
    return this;
  }

  @Override
  public Feeder feed(JsonObject data, long timeout, Handler<AsyncResult<Void>> doneHandler) {
    JsonMessage message = DefaultJsonMessage.create(data);
    eventBus.publish(authAddress, new JsonObject().putString("action", "create").putString("id", message.id()));
    FutureResult result = new FutureResult(new DefaultFutureResult<Void>().setHandler(doneHandler));
    futures.put(message.id(), result);
    result.start(timeout);
    output.emit(message);
    return this;
  }

  @Override
  public Feeder feed(JsonObject data, String tag) {
    JsonMessage message = DefaultJsonMessage.create(data, tag);
    eventBus.publish(authAddress, new JsonObject().putString("action", "create").putString("id", message.id()));
    output.emit(message);
    return this;
  }

  @Override
  public Feeder feed(JsonObject data, String tag, Handler<AsyncResult<Void>> doneHandler) {
    JsonMessage message = DefaultJsonMessage.create(data, tag);
    eventBus.publish(authAddress, new JsonObject().putString("action", "create").putString("id", message.id()));
    FutureResult result = new FutureResult(new DefaultFutureResult<Void>().setHandler(doneHandler));
    futures.put(message.id(), result);
    result.start(DEFAULT_TIMEOUT);
    output.emit(message);
    return this;
  }

  @Override
  public Feeder feed(JsonObject data, String tag, long timeout, Handler<AsyncResult<Void>> doneHandler) {
    JsonMessage message = DefaultJsonMessage.create(data, tag);
    eventBus.publish(authAddress, new JsonObject().putString("action", "create").putString("id", message.id()));
    FutureResult result = new FutureResult(new DefaultFutureResult<Void>().setHandler(doneHandler));
    futures.put(message.id(), result);
    result.start(timeout);
    output.emit(message);
    return this;
  }

  /**
   * Manages timers for future ack/fail results.
   */
  private class FutureResult {
    private Future<Void> future;
    private long timerId;

    public FutureResult(Future<Void> future) {
      this.future = future;
    }

    /**
     * Starts the future timer with the given timeout.
     */
    public FutureResult start(long timeout) {
      timerId = vertx.setTimer(timeout, new Handler<Long>() {
        @Override
        public void handle(Long event) {
          future.setFailure(new TimeoutException("Feed timed out."));
        }
      });
      return this;
    }

    /**
     * Cancels the future timer.
     */
    public FutureResult cancel() {
      if (timerId > 0) {
        vertx.cancelTimer(timerId);
      }
      return this;
    }

    /**
     * Sets the future result, indicating success.
     */
    public void set() {
      future.setResult(null);
    }

    /**
     * Sets the future failure.
     */
    public void fail(Throwable e) {
      future.setFailure(e);
    }
  }

}
