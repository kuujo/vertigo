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

import net.kuujo.vertigo.component.ComponentBase;
import net.kuujo.vertigo.context.InstanceContext;

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
public abstract class AbstractFeeder<T extends Feeder<T>> extends ComponentBase<T> implements Feeder<T> {
  protected InternalQueue queue = new InternalQueue();
  protected boolean autoRetry;
  protected int retryAttempts = -1;

  protected AbstractFeeder(Vertx vertx, Container container, InstanceContext context) {
    super(vertx, container, context);
  }

  private final Handler<String> internalAckHandler = new Handler<String>() {
    @Override
    public void handle(String id) {
      queue.ack(id);
    }
  };

  private final Handler<String> internalFailHandler = new Handler<String>() {
    @Override
    public void handle(String id) {
      queue.fail(id);
    }
  };

  private final Handler<String> internalTimeoutHandler = new Handler<String>() {
    @Override
    public void handle(String id) {
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
  protected String doFeed(final JsonObject data, final String tag,
      final Handler<String> ackHandler, final Handler<String> failHandler, final Handler<String> timeoutHandler) {
    return doFeed(data, tag, 0, ackHandler, failHandler, timeoutHandler);
  }

  /**
   * Executes a feed.
   */
  protected String doFeed(final JsonObject data, final String tag, final int attempts,
      final Handler<String> ackHandler, final Handler<String> failHandler, final Handler<String> timeoutHandler) {
    final String id;
    if (tag != null) {
      id = output.emit(data, tag);
    }
    else {
      id = output.emit(data);
    }

    queue.enqueue(id,
        new Handler<String>() {
          @Override
          public void handle(String messageId) {
            if (ackHandler != null) {
              ackHandler.handle(id);
            }
          }
        },
        new Handler<String>() {
          @Override
          public void handle(String messageId) {
            if (failHandler != null) {
              failHandler.handle(id);
            }
          }
        },
        new Handler<String>() {
          @Override
          public void handle(String messageId) {
            if (autoRetry && (retryAttempts == -1 || attempts < retryAttempts)) {
              doFeed(data, tag, attempts+1, ackHandler, failHandler, timeoutHandler);
            }
            else if (timeoutHandler != null) {
              timeoutHandler.handle(id);
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
      private final Handler<String> ackHandler;
      private final Handler<String> failHandler;
      private final Handler<String> timeoutHandler;
      public HandlerHolder(Handler<String> ackHandler, Handler<String> failHandler, Handler<String> timeoutHandler) {
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
    private void enqueue(String id, Handler<String> ackHandler, Handler<String> failHandler, Handler<String> timeoutHandler) {
      handlers.put(id, new HandlerHolder(ackHandler, failHandler, timeoutHandler));
    }

    /**
     * Acks an item in the queue. The item will be removed and its ack handler called.
     */
    private void ack(String id) {
      HandlerHolder holder = handlers.remove(id);
      if (holder != null) {
        holder.ackHandler.handle(id);
      }
    }

    /**
     * Fails an item in the queue. The item will be removed and its fail handler called.
     */
    private void fail(String id) {
      HandlerHolder holder = handlers.remove(id);
      if (holder != null) {
        holder.failHandler.handle(id);
      }
    }

    /**
     * Times out an item in the queue. The item will be removed and its timeout handler called.
     */
    private void timeout(String id) {
      HandlerHolder holder = handlers.remove(id);
      if (holder != null) {
        holder.timeoutHandler.handle(id);
      }
    }
  }

}
