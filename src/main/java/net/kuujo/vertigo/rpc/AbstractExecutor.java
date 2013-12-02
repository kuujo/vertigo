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
package net.kuujo.vertigo.rpc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Container;

import net.kuujo.vertigo.component.BaseComponent;
import net.kuujo.vertigo.context.InstanceContext;
import net.kuujo.vertigo.message.JsonMessage;
import net.kuujo.vertigo.message.MessageId;

/**
 * An abstract executor implementation.
 *
 * @author Jordan Halterman
 *
 * @param <T> The executor type
 */
public abstract class AbstractExecutor<T extends Executor<T>> extends BaseComponent<T> implements Executor<T> {
  protected InternalQueue queue;
  private boolean autoRetry;
  private int retryAttempts = -1;

  protected AbstractExecutor(Vertx vertx, Container container, InstanceContext context) {
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
  public T start(Handler<AsyncResult<T>> doneHandler) {
    output.ackHandler(ackHandler);
    output.failHandler(failHandler);
    output.timeoutHandler(timeoutHandler);
    input.messageHandler(messageHandler);
    return super.start(doneHandler);
  }

  @Override
  @SuppressWarnings("unchecked")
  public T setReplyTimeout(long timeout) {
    queue.replyTimeout = timeout;
    return (T) this;
  }

  @Override
  public long getReplyTimeout() {
    return queue.replyTimeout;
  }

  @Override
  @SuppressWarnings("unchecked")
  public T setMaxQueueSize(long maxSize) {
    queue.maxSize = maxSize;
    return (T) this;
  }

  @Override
  public long getMaxQueueSize() {
    return queue.maxSize;
  }

  @Override
  public boolean queueFull() {
    return queue.full();
  }

  @Override
  @SuppressWarnings("unchecked")
  public T setAutoRetry(boolean retry) {
    autoRetry = retry;
    return (T) this;
  }

  @Override
  public boolean isAutoRetry() {
    return autoRetry;
  }

  @Override
  @SuppressWarnings("unchecked")
  public T setRetryAttempts(int attempts) {
    retryAttempts = attempts;
    return (T) this;
  }

  @Override
  public int getRetryAttempts() {
    return retryAttempts;
  }

  /**
   * Executes an execution.
   */
  protected MessageId doExecute(final JsonObject data, final String tag,
      final Handler<JsonMessage> resultHandler, final Handler<MessageId> failHandler, final Handler<MessageId> timeoutHandler) {
    return doExecute(data, tag, 0, resultHandler, failHandler, timeoutHandler);
  }

  /**
   * Executes an execution.
   */
  protected MessageId doExecute(final JsonObject data, final String tag, final int attempts,
        final Handler<JsonMessage> resultHandler, final Handler<MessageId> failHandler, final Handler<MessageId> timeoutHandler) {
    final MessageId id;
    if (tag != null) {
      id = output.emit(data, tag);
    }
    else {
      id = output.emit(data);
    }

    queue.enqueue(id,
        new Handler<JsonMessage>() {
          @Override
          public void handle(JsonMessage message) {
            if (resultHandler != null) {
              resultHandler.handle(message);
            }
          }
        },
        new Handler<MessageId>() {
          @Override
          public void handle(MessageId messageId) {
            if (failHandler != null) {
              failHandler.handle(messageId);
            }
          }
        },
        new Handler<MessageId>() {
          @Override
          public void handle(MessageId messageId) {
            if (autoRetry && (retryAttempts == -1 || attempts < retryAttempts)) {
              doExecute(data, tag, attempts+1, resultHandler, failHandler, timeoutHandler);
            }
            else if (timeoutHandler != null) {
              timeoutHandler.handle(messageId);
            }
          }
        });
    return id;
  }

  /**
   * An internal execute queue.
   */
  private static class InternalQueue {
    private final Vertx vertx;
    private final Map<String, HandlerHolder> handlers = new HashMap<String, HandlerHolder>();
    private long replyTimeout = 30000;
    private long maxSize = 1000;

    private InternalQueue(Vertx vertx) {
      this.vertx = vertx;
    }

    /**
     * Holds execute queue handlers.
     */
    private static class HandlerHolder {
      private final Long timer;
      private final Handler<JsonMessage> resultHandler;
      private final Handler<MessageId> failHandler;
      private final Handler<MessageId> timeoutHandler;
      private final List<JsonMessage> results = new ArrayList<JsonMessage>();

      public HandlerHolder(Long timer, Handler<JsonMessage> resultHandler,
          Handler<MessageId> failHandler, Handler<MessageId> timeoutHandler) {
        this.timer = timer;
        this.resultHandler = resultHandler;
        this.failHandler = failHandler;
        this.timeoutHandler = timeoutHandler;
      }
    }

    /**
     * Returns the execute queue size.
     */
    private final int size() {
      return handlers.size();
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
    private void enqueue(final MessageId id, Handler<JsonMessage> resultHandler,
        Handler<MessageId> failHandler, Handler<MessageId> timeoutHandler) {
      long timerId = vertx.setTimer(replyTimeout, new Handler<Long>() {
        @Override
        public void handle(Long timerId) {
          HandlerHolder holder = handlers.get(id.correlationId());
          if (holder != null) {
            handlers.remove(id.correlationId()).timeoutHandler.handle(id);
          }
        }
      });
      handlers.put(id.correlationId(), new HandlerHolder(timerId, resultHandler, failHandler, timeoutHandler));
    }

    /**
     * Acks an item in the queue.
     */
    private void ack(MessageId id) {
      HandlerHolder holder = handlers.remove(id.correlationId());
      if (holder != null) {
        vertx.cancelTimer(holder.timer);
        for (JsonMessage result : holder.results) {
          holder.resultHandler.handle(result);
        }
      }
    }

    /**
     * Fails an item in the queue.
     */
    private void fail(MessageId id) {
      HandlerHolder holder = handlers.remove(id.correlationId());
      if (holder != null) {
        vertx.cancelTimer(holder.timer);
        holder.failHandler.handle(id);
      }
    }

    /**
     * Times out an item in the queue.
     */
    private void timeout(MessageId id) {
      HandlerHolder holder = handlers.remove(id.correlationId());
      if (holder != null) {
        vertx.cancelTimer(holder.timer);
        holder.timeoutHandler.handle(id);
      }
    }

    /**
     * Sets the result of an item in the queue.
     */
    private void result(JsonMessage message) {
      HandlerHolder holder = handlers.get(message.messageId().root());
      if (holder != null) {
        holder.results.add(message);
      }
    }
  }

}
