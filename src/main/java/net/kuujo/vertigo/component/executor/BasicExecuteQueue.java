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
package net.kuujo.vertigo.component.executor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.kuujo.vertigo.component.FailureException;
import net.kuujo.vertigo.messaging.JsonMessage;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Future;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.impl.DefaultFutureResult;

/**
 * A basic execute queue implementation.
 *
 * @author Jordan Halterman
 */
public class BasicExecuteQueue implements ExecuteQueue {
  private Vertx vertx;
  private Map<String, QueueItem> queueItems = new HashMap<String, QueueItem>();
  private Map<Long, Timer> timers = new HashMap<>();
  private long replyTimeout = 15000;
  private long maxQueueSize = 1000;

  public BasicExecuteQueue(Vertx vertx) {
    this.vertx = vertx;
  }

  @Override
  public ExecuteQueue setReplyTimeout(long timeout) {
    replyTimeout = timeout;
    return this;
  }

  @Override
  public long getReplyTimeout() {
    return replyTimeout;
  }

  @Override
  public ExecuteQueue setMaxQueueSize(long maxSize) {
    maxQueueSize = maxSize;
    return this;
  }

  @Override
  public long getMaxQueueSize() {
    return maxQueueSize;
  }

  @Override
  public long size() {
    return queueItems.size();
  }

  @Override
  public boolean full() {
    return size() >= maxQueueSize;
  }

  @Override
  public ExecuteQueue enqueue(final String id, Handler<AsyncResult<JsonMessage>> resultHandler) {
    Future<JsonMessage> future = new DefaultFutureResult<JsonMessage>().setHandler(resultHandler);
    queueItems.put(id, new QueueItem(id, future, getCurrentTimer()));
    return this;
  }

  @Override
  public void ack(String id) {
    if (queueItems.containsKey(id)) {
      queueItems.get(id).ack();
    }
  }

  @Override
  public void fail(String id) {
    if (queueItems.containsKey(id)) {
      queueItems.get(id).fail();
    }
  }

  @Override
  public void receive(JsonMessage message) {
    String id = message.ancestor();
    if (queueItems.containsKey(id)) {
      queueItems.get(id).result(message);
    }
  }

  /**
   * An expire timer.
   */
  private class Timer {
    private final long endTime;
    private final Set<String> ids = new HashSet<String>();

    private Timer(long endTime) {
      this.endTime = endTime;
    }

    private Timer start() {
      vertx.setTimer(replyTimeout, new Handler<Long>() {
        @Override
        public void handle(Long timerId) {
          timers.remove(endTime);
          for (String id : ids) {
            if (queueItems.containsKey(id)) {
              queueItems.get(id).expire();
            }
          }
        }
      });
      timers.put(endTime, this);
      return this;
    }

  }

  /**
   * Gets the current timer.
   *
   * Timers are grouped by rounding the expire time to the nearest .1 second.
   * Message timeouts should be lengthy, and this ensures that a timer
   * is not created for every single message emitted from a feeder.
   */
  private Timer getCurrentTimer() {
    long end = Math.round((System.currentTimeMillis() + replyTimeout) / 100) * 100;
    if (timers.containsKey(end)) {
      return timers.get(end);
    }
    else {
      return new Timer(end).start();
    }
  }

  /**
   * A single queue item.
   */
  private class QueueItem {
    private final String id;
    private final Future<JsonMessage> future;
    private boolean acked;
    private boolean ack;
    private JsonMessage result;
    private Timer timer;
    private boolean complete;

    private QueueItem(String id, Future<JsonMessage> future, Timer timer) {
      this.id = id;
      this.future = future;
      this.timer = timer;
      timer.ids.add(id);
    }

    /**
     * Acks the queue item.
     */
    private void ack() {
      acked = true; ack = true;
      checkResult();
    }

    /**
     * Fails the queue item.
     */
    private void fail() {
      acked = true; ack = false;
      checkResult();
    }

    /**
     * Sets the item result.
     */
    private void result(JsonMessage result) {
      this.result = result;
      checkResult();
    }

    /**
     * Expires the item.
     */
    private void expire() {
      if (!complete && (!acked || result == null)) {
        future.setFailure(new FailureException("Reply timed out."));
        complete = true;
        queueItems.remove(id);
        timer.ids.remove(id);
      }
    }

    /**
     * Checks the current result status.
     */
    private void checkResult() {
      if (!complete && acked) {
        if (!ack) {
          future.setFailure(new FailureException("A failure occurred."));
          complete = true;
          queueItems.remove(id);
          timer.ids.remove(id);
        }
        else if (result != null) {
          future.setResult(result);
          complete = true;
          queueItems.remove(id);
          timer.ids.remove(id);
        }
      }
    }
  }

}
