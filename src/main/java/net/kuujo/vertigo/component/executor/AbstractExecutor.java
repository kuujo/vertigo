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

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Future;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.impl.DefaultFutureResult;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Container;

import net.kuujo.vertigo.component.ComponentBase;
import net.kuujo.vertigo.context.WorkerContext;
import net.kuujo.vertigo.messaging.JsonMessage;

/**
 * An abstract executor implementation.
 *
 * @author Jordan Halterman
 *
 * @param <T> The executor type
 */
public abstract class AbstractExecutor<T extends Executor<T>> extends ComponentBase implements Executor<T> {

  protected ExecuteQueue queue;

  protected AbstractExecutor(Vertx vertx, Container container, WorkerContext context) {
    super(vertx, container, context);
    queue = new BasicExecuteQueue(vertx);
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
                            future.setResult((T) AbstractExecutor.this);
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
  public T replyTimeout(long timeout) {
    queue.replyTimeout(timeout);
    return (T) this;
  }

  @Override
  public long replyTimeout() {
    return queue.replyTimeout();
  }

  @Override
  @SuppressWarnings("unchecked")
  public T maxQueueSize(long maxSize) {
    queue.maxQueueSize(maxSize);
    return (T) this;
  }

  @Override
  public long maxQueueSize() {
    return queue.maxQueueSize();
  }

  @Override
  public boolean queueFull() {
    return queue.full();
  }

  /**
   * Executes a feed.
   */
  @SuppressWarnings("unchecked")
  protected T doExecute(final JsonObject data, final String tag, Handler<AsyncResult<JsonMessage>> handler) {
    JsonMessage message = createMessage(data, tag);
    queue.enqueue(message.id(), handler);
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

  @Override
  protected void doReceive(JsonMessage message) {
    output.ack(message);
    queue.receive(message);
  }

}
