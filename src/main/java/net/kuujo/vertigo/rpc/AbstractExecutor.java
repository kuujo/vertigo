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

import java.util.HashMap;
import java.util.Map;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Container;

import net.kuujo.vertigo.component.ComponentBase;
import net.kuujo.vertigo.context.InstanceContext;
import net.kuujo.vertigo.message.JsonMessage;

/**
 * An abstract executor implementation.
 *
 * @author Jordan Halterman
 *
 * @param <T> The executor type
 */
public abstract class AbstractExecutor<T extends Executor<T>> extends ComponentBase<T> implements Executor<T> {
  protected InternalQueue queue;

  protected AbstractExecutor(Vertx vertx, Container container, InstanceContext context) {
    super(vertx, container, context);
    queue = new InternalQueue(vertx);
  }

  private Handler<String> ackHandler = new Handler<String>() {
    @Override
    public void handle(String id) {
      queue.ack(id);
    }
  };

  private Handler<String> failHandler = new Handler<String>() {
    @Override
    public void handle(String id) {
      queue.fail(id);
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

  /**
   * Executes a feed.
   */
  protected String doExecute(final JsonObject data, final String tag, Handler<JsonMessage> resultHandler, Handler<String> failHandler) {
    final String id;
    if (tag != null) {
      id = output.emit(data, tag);
    }
    else {
      id = output.emit(data);
    }
    queue.enqueue(id, resultHandler, failHandler);
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
      private final Handler<String> failHandler;
      private boolean acked;
      private JsonMessage result;

      public HandlerHolder(Long timer, Handler<JsonMessage> resultHandler, Handler<String> failHandler) {
        this.timer = timer;
        this.resultHandler = resultHandler;
        this.failHandler = failHandler;
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
    private void enqueue(final String id, Handler<JsonMessage> resultHandler, Handler<String> failHandler) {
      long timerId = vertx.setTimer(replyTimeout, new Handler<Long>() {
        @Override
        public void handle(Long timerId) {
          HandlerHolder holder = handlers.get(id);
          if (holder != null) {
            handlers.remove(id).failHandler.handle(id);
          }
        }
      });
      handlers.put(id, new HandlerHolder(timerId, resultHandler, failHandler));
    }

    /**
     * Acks an item in the queue.
     */
    private void ack(String id) {
      HandlerHolder holder = handlers.get(id);
      if (holder != null) {
        holder.acked = true;
        if (holder.result != null) {
          vertx.cancelTimer(holder.timer);
          handlers.remove(id).resultHandler.handle(holder.result);
        }
      }
    }

    /**
     * Fails an item in the queue.
     */
    private void fail(String id) {
      HandlerHolder holder = handlers.remove(id);
      if (holder != null) {
        vertx.cancelTimer(holder.timer);
        holder.failHandler.handle(id);
      }
    }

    /**
     * Sets the result of an item in the queue.
     */
    private void result(JsonMessage message) {
      HandlerHolder holder = handlers.get(message.ancestor());
      if (holder != null) {
        holder.result = message;
        if (holder.acked) {
          vertx.cancelTimer(holder.timer);
          handlers.remove(message.ancestor()).resultHandler.handle(message);
        }
      }
    }
  }

}
