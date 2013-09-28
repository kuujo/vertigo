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
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Container;

/**
 * A basic stream feeder implementation.
 *
 * @author Jordan Halterman
 */
@SuppressWarnings("rawtypes")
public class BasicStreamFeeder extends AbstractFeeder implements StreamFeeder<StreamFeeder> {

  private boolean paused;

  private Handler<Void> drainHandler;

  protected BasicStreamFeeder(Vertx vertx, Container container, WorkerContext context) {
    super(vertx, container, context);
  }

  @Override
  public StreamFeeder setFeedQueueMaxSize(long maxSize) {
    queue.setMaxQueueSize(maxSize);
    return this;
  }

  @Override
  public long getFeedQueueMaxSize() {
    return queue.getMaxQueueSize();
  }

  @Override
  public boolean feedQueueFull() {
    return paused;
  }

  @Override
  public StreamFeeder feed(JsonObject data) {
    JsonMessage message = DefaultJsonMessage.create(data);
    queue.enqueue(message.id(), createAckHandler(message));
    output.emit(message);
    return this;
  }

  @Override
  public StreamFeeder feed(JsonObject data, String tag) {
    JsonMessage message = DefaultJsonMessage.create(data, tag);
    queue.enqueue(message.id(), createAckHandler(message));
    output.emit(message);
    return this;
  }

  /**
   * Creates a message ack handler.
   */
  private Handler<AsyncResult<Void>> createAckHandler(final JsonMessage message) {
    return new Handler<AsyncResult<Void>>() {
      @Override
      public void handle(AsyncResult<Void> result) {
        checkPause();
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
  public StreamFeeder drainHandler(Handler<Void> handler) {
    this.drainHandler = handler;
    return this;
  }

}
