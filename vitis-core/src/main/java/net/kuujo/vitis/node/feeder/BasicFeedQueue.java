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

import java.util.HashMap;
import java.util.Map;

import net.kuujo.vitis.node.FailureException;
import net.kuujo.vitis.node.TimeoutException;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Future;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.impl.DefaultFutureResult;

/**
 * A default feed queue implementation.
 *
 * @author Jordan Halterman
 */
public class BasicFeedQueue implements FeedQueue {

  private Vertx vertx;

  private Map<String, FutureResult> futures = new HashMap<String, FutureResult>();

  protected static final long DEFAULT_TIMEOUT = 30000;

  private long timeout = DEFAULT_TIMEOUT;

  private long maxQueueSize = 1000;

  public BasicFeedQueue(Vertx vertx) {
    this.vertx = vertx;
  }

  @Override
  public FeedQueue timeout(long timeout) {
    this.timeout = timeout;
    return this;
  }

  @Override
  public long timeout() {
    return timeout;
  }

  @Override
  public FeedQueue maxQueueSize(long maxSize) {
    maxQueueSize = maxSize;
    return this;
  }

  @Override
  public long maxQueueSize() {
    return maxQueueSize;
  }

  @Override
  public long size() {
    return futures.size();
  }

  @Override
  public boolean full() {
    return size() >= maxQueueSize;
  }

  @Override
  public FeedQueue enqueue(final String id, Handler<AsyncResult<Void>> ackHandler) {
    FutureResult result = new FutureResult(new DefaultFutureResult<Void>().setHandler(ackHandler), vertx);
    futures.put(id, result);
    result.start(timeout, new Handler<FutureResult>() {
      @Override
      public void handle(FutureResult result) {
        if (futures.containsKey(id)) {
          futures.remove(id).fail(new TimeoutException("Processing timed out."));
        }
      }
    });
    return this;
  }

  @Override
  public void ack(String id) {
    if (futures.containsKey(id)) {
      futures.remove(id).set();
    }
  }

  @Override
  public void fail(String id) {
    if (futures.containsKey(id)) {
      futures.remove(id).fail(new FailureException("Processing failed."));
    }
  }

  /**
   * Manages timers for future ack/fail results.
   */
  private static class FutureResult {
    private Future<Void> future;
    private Vertx vertx;
    private long timerId;

    public FutureResult(Future<Void> future, Vertx vertx) {
      this.future = future;
      this.vertx = vertx;
    }

    /**
     * Starts the future timer with the given timeout.
     */
    public FutureResult start(long timeout, final Handler<FutureResult> handler) {
      timerId = vertx.setTimer(timeout, new Handler<Long>() {
        @Override
        public void handle(Long timerId) {
          handler.handle(FutureResult.this);
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
      cancel();
      future.setResult(null);
    }

    /**
     * Sets the future failure.
     */
    public void fail(Throwable e) {
      cancel();
      future.setFailure(e);
    }
  }

}
