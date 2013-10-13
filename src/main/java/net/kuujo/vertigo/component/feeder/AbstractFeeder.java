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
package net.kuujo.vertigo.component.feeder;

import net.kuujo.vertigo.component.ComponentBase;
import net.kuujo.vertigo.context.WorkerContext;
import net.kuujo.vertigo.messaging.JsonMessage;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Future;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.impl.DefaultFutureResult;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Container;

/**
 * An abstract feeder implementation.
 *
 * @author Jordan Halterman
 */
public abstract class AbstractFeeder<T extends Feeder<T>> extends ComponentBase implements Feeder<T> {

  protected FeedQueue queue;

  protected boolean autoRetry;

  protected int retryAttempts = -1;

  protected AbstractFeeder(Vertx vertx, Container container, WorkerContext context) {
    super(vertx, container, context);
    queue = new BasicFeedQueue();
  }

  @Override
  @SuppressWarnings("unchecked")
  public T start() {
    start(new Handler<AsyncResult<T>>() {
      @Override
      public void handle(AsyncResult<T> result) {
        // Do nothing.
      }
    });
    return (T) this;
  }

  @Override
  @SuppressWarnings("unchecked")
  public T start(Handler<AsyncResult<T>> doneHandler) {
    final Future<T> future = new DefaultFutureResult<T>().setHandler(doneHandler);
    setupHeartbeat(new Handler<AsyncResult<Void>>() {
      @Override
      public void handle(AsyncResult<Void> result) {
        if (result.failed()) {
          future.setFailure(result.cause());
        }
        else {
          setupOutputs(new Handler<AsyncResult<Void>>() {
            @Override
            public void handle(AsyncResult<Void> result) {
              if (result.failed()) {
                future.setFailure(result.cause());
              }
              else {
                setupInputs(new Handler<AsyncResult<Void>>() {
                  @Override
                  public void handle(AsyncResult<Void> result) {
                    if (result.failed()) {
                      future.setFailure(result.cause());
                    }
                    else {
                      ready(new Handler<AsyncResult<Void>>() {
                        @Override
                        public void handle(AsyncResult<Void> result) {
                          if (result.failed()) {
                            future.setFailure(result.cause());
                          }
                          else {
                            future.setResult((T) AbstractFeeder.this);
                          }
                        }
                      });
                    }
                  }
                });
              }
            }
          });
        }
      }
    });
    return (T) this;
  }

  @Override
  @SuppressWarnings("unchecked")
  public T setMaxQueueSize(long maxSize) {
    queue.setMaxQueueSize(maxSize);
    return (T) this;
  }

  @Override
  public long getMaxQueueSize() {
    return queue.getMaxQueueSize();
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
  @SuppressWarnings("unchecked")
  protected T doFeed(final JsonObject data, final String tag, final int attempts, final Future<Void> future) {
    final JsonMessage message = createMessage(data, tag);
    queue.enqueue(message.id(), new Handler<AsyncResult<Void>>() {
      @Override
      public void handle(AsyncResult<Void> result) {
        if (result.failed()) {
          if (autoRetry && (retryAttempts == -1 || attempts < retryAttempts)) {
            doFeed(data, tag, attempts+1, future);
          }
          else if (future != null) {
            future.setFailure(result.cause());
          }
        }
        else if (future != null) {
          future.setResult(result.result());
        }
      }
    });
    output.emit(message);
    return (T) this;
  }

  @Override
  protected void doAck(String id) {
    queue.ack(id);
  }

  @Override
  protected void doFail(String id) {
    queue.fail(id);
  }

}
