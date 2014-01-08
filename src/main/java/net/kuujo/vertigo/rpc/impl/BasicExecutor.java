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
package net.kuujo.vertigo.rpc.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Future;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.impl.DefaultFutureResult;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Container;

import net.kuujo.vertigo.component.impl.AbstractComponent;
import net.kuujo.vertigo.context.InstanceContext;
import net.kuujo.vertigo.message.JsonMessage;
import net.kuujo.vertigo.message.MessageId;
import net.kuujo.vertigo.rpc.Executor;
import net.kuujo.vertigo.runtime.FailureException;
import net.kuujo.vertigo.runtime.TimeoutException;

/**
 * An abstract executor implementation.
 *
 * @author Jordan Halterman
 */
public class BasicExecutor extends AbstractComponent<Executor> implements Executor {

  /**
   * Constant indicating unlimited automatic retry attempts.
   */
  public static final int AUTO_RETRY_ATTEMPTS_UNLIMITED = -1;

  private static final long DEFAULT_FEED_INTERVAL = 10;
  private Handler<Executor> executeHandler;
  private Handler<Void> drainHandler;
  protected InternalQueue queue;
  private boolean autoRetry;
  private int retryAttempts = AUTO_RETRY_ATTEMPTS_UNLIMITED;
  private long executeInterval = DEFAULT_FEED_INTERVAL;
  private boolean started;
  private boolean paused;
  private boolean executed;
  private long executeTimer;

  public BasicExecutor(Vertx vertx, Container container, InstanceContext<Executor> context) {
    super(vertx, container, context);
    queue = new InternalQueue(vertx);
  }

  private Handler<MessageId> ackHandler = new Handler<MessageId>() {
    @Override
    public void handle(MessageId messageId) {
      queue.ack(messageId);
    }
  };

  private Handler<MessageId> failHandler = new Handler<MessageId>() {
    @Override
    public void handle(MessageId messageId) {
      queue.fail(messageId);
    }
  };

  private Handler<MessageId> timeoutHandler = new Handler<MessageId>() {
    @Override
    public void handle(MessageId messageId) {
      queue.timeout(messageId);
    }
  };

  private Handler<JsonMessage> messageHandler = new Handler<JsonMessage>() {
    @Override
    public void handle(JsonMessage message) {
      input.ack(message);
      queue.result(message);
    }
  };

  @Override
  public Executor start(Handler<AsyncResult<Executor>> doneHandler) {
    output.ackHandler(ackHandler);
    output.failHandler(failHandler);
    output.timeoutHandler(timeoutHandler);
    input.messageHandler(messageHandler);
    final Future<Executor> future = new DefaultFutureResult<Executor>().setHandler(doneHandler);
    return super.start(new Handler<AsyncResult<Executor>>() {
      @Override
      public void handle(AsyncResult<Executor> result) {
        if (result.failed()) {
          future.setFailure(result.cause());
        }
        else {
          future.setResult(result.result());
          started = true;
          recursiveExecute();
        }
      }
    });
  }

  /**
   * Recursively invokes the execute handler.
   * If the execute handler is invoked and no messages are executed from the handler,
   * a timer is set to restart the execution in the future.
   */
  private void recursiveExecute() {
    if (executeHandler != null) {
      executed = true;
      while (executed && !executeQueueFull()) {
        executed = false;
        executeHandler.handle(this);
      }

      executeTimer = vertx.setTimer(executeInterval, new Handler<Long>() {
        @Override
        public void handle(Long timerID) {
          recursiveExecute();
        }
      });
    }
    else if (executeTimer > 0) {
      vertx.cancelTimer(executeTimer);
      executeTimer = 0;
    }
  }

  @Override
  public Executor setReplyTimeout(long timeout) {
    return setResultTimeout(timeout);
  }

  @Override
  public long getReplyTimeout() {
    return getResultTimeout();
  }

  @Override
  public Executor setResultTimeout(long timeout) {
    queue.replyTimeout = timeout;
    return this;
  }

  @Override
  public long getResultTimeout() {
    return queue.replyTimeout;
  }

  @Override
  public Executor setMaxQueueSize(long maxSize) {
    return setExecuteQueueMaxSize(maxSize);
  }

  @Override
  public long getMaxQueueSize() {
    return getExecuteQueueMaxSize();
  }

  @Override
  public Executor setExecuteQueueMaxSize(long maxSize) {
    queue.maxSize = maxSize;
    return this;
  }

  @Override
  public long getExecuteQueueMaxSize() {
    return queue.maxSize;
  }

  @Override
  public boolean queueFull() {
    return executeQueueFull();
  }

  @Override
  public boolean executeQueueFull() {
    return queue.full();
  }

  @Override
  public Executor setAutoRetry(boolean retry) {
    autoRetry = retry;
    return this;
  }

  @Override
  public boolean isAutoRetry() {
    return autoRetry;
  }

  @Override
  public Executor setRetryAttempts(int attempts) {
    return setAutoRetryAttempts(attempts);
  }

  @Override
  public int getRetryAttempts() {
    return getAutoRetryAttempts();
  }

  @Override
  public Executor setAutoRetryAttempts(int attempts) {
    retryAttempts = attempts;
    return this;
  }

  @Override
  public int getAutoRetryAttempts() {
    return retryAttempts;
  }

  @Override
  public Executor setExecuteDelay(long delay) {
    return setExecuteInterval(delay);
  }

  @Override
  public long getExecuteDelay() {
    return getExecuteInterval();
  }

  @Override
  public Executor setExecuteInterval(long interval) {
    executeInterval = interval;
    return this;
  }

  @Override
  public long getExecuteInterval() {
    return executeInterval;
  }

  @Override
  public Executor executeHandler(Handler<Executor> executeHandler) {
    this.executeHandler = executeHandler;
    if (started) {
      recursiveExecute();
    }
    return this;
  }

  @Override
  public Executor drainHandler(Handler<Void> drainHandler) {
    this.drainHandler = drainHandler;
    return this;
  }

  @Override
  public MessageId execute(JsonObject args, Handler<AsyncResult<JsonMessage>> resultHandler) {
    return doExecute(null, args, 0, resultHandler);
  }

  @Override
  public MessageId execute(String stream, JsonObject args, Handler<AsyncResult<JsonMessage>> resultHandler) {
    return doExecute(stream, args, 0, resultHandler);
  }

  /**
   * Performs an execution.
   */
  protected MessageId doExecute(JsonObject args, Handler<AsyncResult<JsonMessage>> resultHandler) {
    return doExecute(null, args, 0, resultHandler);
  }

  /**
   * Performs an execution.
   */
  protected MessageId doExecute(String stream, JsonObject args, Handler<AsyncResult<JsonMessage>> resultHandler) {
    return doExecute(stream, args, 0, resultHandler);
  }

  /**
   * Performs an execution.
   */
  private MessageId doExecute(final String stream, final JsonObject args, final int attempts, final Handler<AsyncResult<JsonMessage>> resultHandler) {
    final MessageId id = stream != null ? output.emitTo(stream, args) : output.emit(args);
    queue.enqueue(id, new Handler<AsyncResult<JsonMessage>>() {
      @Override
      public void handle(AsyncResult<JsonMessage> result) {
        if (autoRetry && (retryAttempts == AUTO_RETRY_ATTEMPTS_UNLIMITED || attempts < retryAttempts)
            && result.failed() && result.cause() instanceof TimeoutException) {
          doExecute(stream, args, attempts+1, resultHandler);
        }
        else {
          resultHandler.handle(result);
        }
      }
    });
    executed = true; checkPause();
    return id;
  }

  /**
   * Checks the current pause status.
   */
  private void checkPause() {
    if (paused) {
      if (!executeQueueFull()) {
        paused = false;
        if (drainHandler != null) {
          drainHandler.handle(null);
        }
      }
    }
    else if (executeQueueFull()) {
      paused = true;
    }
  }

  /**
   * An internal execute queue.
   */
  private static class InternalQueue {
    // Set up failure and timeout exceptions without stack traces. This prevents
    // us from having to create exceptions repeatedly which would otherwise result
    // in the stack trace being filled in frequently.
    private static final FailureException FAILURE_EXCEPTION = new FailureException("Processing failed.");
    static { FAILURE_EXCEPTION.setStackTrace(new StackTraceElement[0]); }

    private static final TimeoutException TIMEOUT_EXCEPTION = new TimeoutException("Processing timed out.");
    static { TIMEOUT_EXCEPTION.setStackTrace(new StackTraceElement[0]); }

    private final Vertx vertx;
    private final Map<String, FutureResult> futures = new HashMap<>();
    private long replyTimeout = 30000;
    private long maxSize = 1000;

    private InternalQueue(Vertx vertx) {
      this.vertx = vertx;
    }

    /**
     * Holds execute futures.
     */
    private static class FutureResult {
      private final long timer;
      private final Handler<AsyncResult<JsonMessage>> handler;
      private List<JsonMessage> results = new ArrayList<>();

      public FutureResult(long timer, Handler<AsyncResult<JsonMessage>> handler) {
        this.timer = timer;
        this.handler = handler;
      }
    }

    /**
     * Returns the execute queue size.
     */
    private final int size() {
      return futures.size();
    }

    /**
     * Indicates whether the execute queue is full.
     */
    private final boolean full() {
      return size() > maxSize;
    }

    /**
     * Enqueues a new item in the execute queue. When the item is acked or failed
     * by ID, or when a result is received, the appropriate handlers will be called.
     */
    private void enqueue(final MessageId id, Handler<AsyncResult<JsonMessage>> resultHandler) {
      long timerId = vertx.setTimer(replyTimeout, new Handler<Long>() {
        @Override
        public void handle(Long timerId) {
          FutureResult future = futures.get(id.correlationId());
          if (future != null) {
            new DefaultFutureResult<JsonMessage>(TIMEOUT_EXCEPTION).setHandler(futures.remove(id.correlationId()).handler);
          }
        }
      });
      futures.put(id.correlationId(), new FutureResult(timerId, resultHandler));
    }

    /**
     * Acks an item in the queue.
     */
    private void ack(MessageId id) {
      FutureResult future = futures.remove(id.correlationId());
      if (future != null) {
        vertx.cancelTimer(future.timer);
        for (JsonMessage result : future.results) {
          new DefaultFutureResult<JsonMessage>(result).setHandler(future.handler);
        }
      }
    }

    /**
     * Fails an item in the queue.
     */
    private void fail(MessageId id) {
      FutureResult future = futures.remove(id.correlationId());
      if (future != null) {
        vertx.cancelTimer(future.timer);
        new DefaultFutureResult<JsonMessage>(FAILURE_EXCEPTION).setHandler(future.handler);
      }
    }

    /**
     * Times out an item in the queue.
     */
    private void timeout(MessageId id) {
      FutureResult future = futures.remove(id.correlationId());
      if (future != null) {
        vertx.cancelTimer(future.timer);
        new DefaultFutureResult<JsonMessage>(TIMEOUT_EXCEPTION).setHandler(future.handler);
      }
    }

    /**
     * Sets the result of an item in the queue.
     */
    private void result(JsonMessage message) {
      FutureResult future = futures.get(message.messageId().root());
      if (future != null) {
        future.results.add(message);
      }
    }
  }

}
