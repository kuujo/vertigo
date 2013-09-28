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
package net.kuujo.vitis.node.feeder;

import net.kuujo.vitis.context.WorkerContext;
import net.kuujo.vitis.messaging.DefaultJsonMessage;
import net.kuujo.vitis.messaging.JsonMessage;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Future;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.impl.DefaultFutureResult;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Container;

/**
 * A rich stream feeder implementation.
 *
 * @author Jordan Halterman
 */
public class RichStreamFeeder extends AbstractFeeder implements StreamFeeder<RichStreamFeeder>, RichFeeder<RichStreamFeeder> {

  private boolean started;

  private boolean paused;

  private Handler<Void> drainHandler;

  public RichStreamFeeder(Vertx vertx, Container container, WorkerContext context) {
    super(vertx, container, context);
  }

  @Override
  public void start() {
    super.start();
    vertx.setTimer(1000, new Handler<Long>() {
      @Override
      public void handle(Long timerID) {
        started = true;
      }
    });
  }

  @Override
  public RichStreamFeeder setDefaultTimeout(long timeout) {
    queue.setDefaultTimeout(timeout);
    return this;
  }

  @Override
  public RichStreamFeeder setFeedQueueMaxSize(long maxSize) {
    queue.setMaxQueueSize(maxSize);
    return this;
  }

  @Override
  public long getFeedQueueMaxSize() {
    return queue.getMaxQueueSize();
  }

  @Override
  public boolean feedQueueFull() {
    return !started || paused;
  }

  @Override
  public RichStreamFeeder feed(JsonObject data) {
    JsonMessage message = DefaultJsonMessage.create(data);
    queue.enqueue(message.id(), createEmptyAckHandler());
    output.emit(message);
    return this;
  }

  @Override
  public RichStreamFeeder feed(JsonObject data, String tag) {
    JsonMessage message = DefaultJsonMessage.create(data, tag);
    queue.enqueue(message.id(), createEmptyAckHandler());
    output.emit(message);
    return this;
  }

  @Override
  public RichStreamFeeder feed(JsonObject data, Handler<AsyncResult<Void>> ackHandler) {
    JsonMessage message = DefaultJsonMessage.create(data);
    queue.enqueue(message.id(), createResultAckHandler(message, ackHandler));
    output.emit(message);
    return this;
  }

  @Override
  public RichStreamFeeder feed(JsonObject data, long timeout, Handler<AsyncResult<Void>> ackHandler) {
    JsonMessage message = DefaultJsonMessage.create(data);
    queue.enqueue(message.id(), timeout, createResultAckHandler(message, ackHandler));
    output.emit(message);
    return this;
  }

  @Override
  public RichStreamFeeder feed(JsonObject data, String tag, Handler<AsyncResult<Void>> ackHandler) {
    JsonMessage message = DefaultJsonMessage.create(data, tag);
    queue.enqueue(message.id(), createResultAckHandler(message, ackHandler));
    output.emit(message);
    return this;
  }

  @Override
  public RichStreamFeeder feed(JsonObject data, String tag, long timeout, Handler<AsyncResult<Void>> ackHandler) {
    JsonMessage message = DefaultJsonMessage.create(data, tag);
    queue.enqueue(message.id(), timeout, createResultAckHandler(message, ackHandler));
    output.emit(message);
    return this;
  }

  /**
   * Creates an empty ack handler for checking queue status.
   */
  private Handler<AsyncResult<Void>> createEmptyAckHandler() {
    return new Handler<AsyncResult<Void>>() {
      @Override
      public void handle(AsyncResult<Void> result) {
        checkPause();
      }
    };
  }

  /**
   * Creates an authentic ack handler, checking queue status prior to invoking the handler.
   */
  private Handler<AsyncResult<Void>> createResultAckHandler(final JsonMessage message, Handler<AsyncResult<Void>> handler) {
    final Future<Void> future = new DefaultFutureResult<Void>().setHandler(handler);
    return new Handler<AsyncResult<Void>>() {
      @Override
      public void handle(AsyncResult<Void> result) {
        checkPause();
        if (result.failed()) {
          future.setFailure(result.cause());
        }
        else {
          future.setResult(result.result());
        }
      }
    };
  }

  /**
   * Checks the current stream pause status.
   */
  private void checkPause() {
    if (paused) {
      if (queue.size() < queue.getMaxQueueSize() * .75) {
        paused = false;
        if (drainHandler != null) {
          drainHandler.handle(null);
        }
      }
    }
    else {
      if (queue.size() >= queue.getMaxQueueSize()) {
        paused = true;
      }
    }
  }

  @Override
  public RichStreamFeeder drainHandler(Handler<Void> handler) {
    this.drainHandler = handler;
    return this;
  }

}
