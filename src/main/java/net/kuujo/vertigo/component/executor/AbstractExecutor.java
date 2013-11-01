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
import net.kuujo.vertigo.context.InstanceContext;
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

  protected AbstractExecutor(Vertx vertx, Container container, InstanceContext context) {
    super(vertx, container, context);
    queue = new BasicExecuteQueue(vertx);
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
      queue.receive(message);
    }
  };

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
    output.ackHandler(ackHandler);
    output.failHandler(failHandler);
    input.messageHandler(messageHandler);

    final Future<T> future = new DefaultFutureResult<T>().setHandler(doneHandler);
    setup(new Handler<AsyncResult<Void>>() {
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
    return (T) this;
  }

  @Override
  @SuppressWarnings("unchecked")
  public T setReplyTimeout(long timeout) {
    queue.setReplyTimeout(timeout);
    return (T) this;
  }

  @Override
  public long getReplyTimeout() {
    return queue.getReplyTimeout();
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

  /**
   * Executes a feed.
   */
  @SuppressWarnings("unchecked")
  protected T doExecute(final JsonObject data, final String tag, Handler<AsyncResult<JsonMessage>> handler) {
    final String id;
    if (tag != null) {
      id = output.emit(data, tag);
    }
    else {
      id = output.emit(data);
    }
    queue.enqueue(id, handler);
    return (T) this;
  }

}
