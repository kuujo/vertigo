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
package net.kuujo.vertigo.feeder;

import java.util.HashMap;
import java.util.Map;

import net.kuujo.vertigo.component.BaseComponent;
import net.kuujo.vertigo.context.InstanceContext;
import net.kuujo.vertigo.message.MessageId;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Container;

/**
 * An abstract feeder implementation.
 *
 * @author Jordan Halterman
 */
public abstract class AbstractFeeder<T extends Feeder<T>> extends BaseComponent<T> implements Feeder<T> {
  protected InternalQueue queue = new InternalQueue();
  protected boolean autoRetry;
  protected int retryAttempts = -1;

  protected AbstractFeeder(Vertx vertx, Container container, InstanceContext context) {
    super(vertx, container, context);
  }

  private final Handler<MessageId> internalAckHandler = new Handler<MessageId>() {
    @Override
    public void handle(MessageId id) {
      queue.ack(id);
    }
  };

  private final Handler<MessageId> internalFailHandler = new Handler<MessageId>() {
    @Override
    public void handle(MessageId id) {
      queue.fail(id);
    }
  };

  private final Handler<MessageId> internalTimeoutHandler = new Handler<MessageId>() {
    @Override
    public void handle(MessageId id) {
      queue.timeout(id);
    }
  };

  @Override
  public T start(Handler<AsyncResult<T>> doneHandler) {
    output.ackHandler(internalAckHandler);
    output.failHandler(internalFailHandler);
    output.timeoutHandler(internalTimeoutHandler);
    return super.start(doneHandler);
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
   * Executes a feed.
   */
  protected MessageId doFeed(final JsonObject data, final String tag,
      final Handler<MessageId> ackHandler, final Handler<MessageId> failHandler, final Handler<MessageId> timeoutHandler) {
    return doFeed(data, tag, 0, ackHandler, failHandler, timeoutHandler);
  }

  /**
   * Executes a feed.
   */
  protected MessageId doFeed(final JsonObject data, final String tag, final int attempts,
      final Handler<MessageId> ackHandler, final Handler<MessageId> failHandler, final Handler<MessageId> timeoutHandler) {
    final MessageId id;
    if (tag != null) {
      id = output.emit(data, tag);
    }
    else {
      id = output.emit(data);
    }

    queue.enqueue(id,
        new Handler<MessageId>() {
          @Override
          public void handle(MessageId messageId) {
            if (ackHandler != null) {
              ackHandler.handle(messageId);
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
              doFeed(data, tag, attempts+1, ackHandler, failHandler, timeoutHandler);
            }
            else if (timeoutHandler != null) {
              timeoutHandler.handle(messageId);
            }
          }
        });
    return id;
  }

  /**
   * An internal feeder queue.
   */
  private static class InternalQueue {
    private final Map<String, HandlerHolder> handlers = new HashMap<String, HandlerHolder>();
    private long maxSize = 1000;

    /**
     * A holder for ack/fail handlers.
     */
    private static class HandlerHolder {
      private final Handler<MessageId> ackHandler;
      private final Handler<MessageId> failHandler;
      private final Handler<MessageId> timeoutHandler;
      public HandlerHolder(Handler<MessageId> ackHandler, Handler<MessageId> failHandler, Handler<MessageId> timeoutHandler) {
        this.ackHandler = ackHandler;
        this.failHandler = failHandler;
        this.timeoutHandler = timeoutHandler;
      }
    }

    /**
     * Returns the queue size.
     */
    private int size() {
      return handlers.size();
    }

    /**
     * Returns a boolean indicating whether the queue is full.
     */
    private boolean full() {
      return size() >= maxSize;
    }

    /**
     * Enqueues a new item. The item is enqueued with an ack and fail handler.
     */
    private void enqueue(MessageId id, Handler<MessageId> ackHandler, Handler<MessageId> failHandler, Handler<MessageId> timeoutHandler) {
      handlers.put(id.correlationId(), new HandlerHolder(ackHandler, failHandler, timeoutHandler));
    }

    /**
     * Acks an item in the queue. The item will be removed and its ack handler called.
     */
    private void ack(MessageId id) {
      HandlerHolder holder = handlers.remove(id.correlationId());
      if (holder != null) {
        holder.ackHandler.handle(id);
      }
    }

    /**
     * Fails an item in the queue. The item will be removed and its fail handler called.
     */
    private void fail(MessageId id) {
      HandlerHolder holder = handlers.remove(id.correlationId());
      if (holder != null) {
        holder.failHandler.handle(id);
      }
    }

    /**
     * Times out an item in the queue. The item will be removed and its timeout handler called.
     */
    private void timeout(MessageId id) {
      HandlerHolder holder = handlers.remove(id.correlationId());
      if (holder != null) {
        holder.timeoutHandler.handle(id);
      }
    }
  }

}
