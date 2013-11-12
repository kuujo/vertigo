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

  @Override
  public T start(Handler<AsyncResult<T>> doneHandler) {
    output.ackHandler(ackHandler);
    output.failHandler(failHandler);
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
  protected String doFeed(final JsonObject data, final String tag, final int attempts,
      final Handler<String> ackHandler, final Handler<String> failHandler) {
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
            if (autoRetry && (retryAttempts == -1 || attempts < retryAttempts)) {
              doFeed(data, tag, attempts+1, ackHandler, failHandler);
            }
            else if (failHandler != null) {
              failHandler.handle(id);
            }
          }
        });
    return id;
  }

  private static class InternalQueue {
    private Map<String, HandlerHolder> handlers = new HashMap<String, HandlerHolder>();
    private long maxSize = 1000;

    private static class HandlerHolder {
      private final Handler<String> ackHandler;
      private final Handler<String> failHandler;
      public HandlerHolder(Handler<String> ackHandler, Handler<String> failHandler) {
        this.ackHandler = ackHandler;
        this.failHandler = failHandler;
      }
    }

    private int size() {
      return handlers.size();
    }

    private boolean full() {
      return size() >= maxSize;
    }

    private void enqueue(String id, Handler<String> ackHandler, Handler<String> failHandler) {
      handlers.put(id, new HandlerHolder(ackHandler, failHandler));
    }

    private void ack(String id) {
      HandlerHolder holder = handlers.remove(id);
      if (holder != null) {
        holder.ackHandler.handle(id);
      }
    }

    private void fail(String id) {
      HandlerHolder holder = handlers.remove(id);
      if (holder != null) {
        holder.failHandler.handle(id);
      }
    }
  }

}
