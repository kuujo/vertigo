/*
 * Copyright 2014 the original author or authors.
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
package net.kuujo.vertigo.output.impl;

import java.util.List;

import net.kuujo.vertigo.annotations.Factory;
import net.kuujo.vertigo.cluster.VertigoCluster;
import net.kuujo.vertigo.context.OutputConnectionContext;
import net.kuujo.vertigo.data.AsyncQueue;
import net.kuujo.vertigo.eventbus.AdaptiveEventBus;
import net.kuujo.vertigo.eventbus.impl.WrappedAdaptiveEventBus;
import net.kuujo.vertigo.message.JsonMessage;
import net.kuujo.vertigo.output.OutputConnection;
import net.kuujo.vertigo.util.CountingCompletionHandler;
import net.kuujo.vertigo.util.Factories;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.impl.DefaultFutureResult;

/**
 * Ordered output connection for at-least-once processing.
 *
 * @author Jordan Halterman
 */
public class OrderedAtLeastOnceOutputConnection extends AtLeastOnceOutputConnection {
  private final AdaptiveEventBus eventBus;
  private final AsyncQueue<String> messages;
  private int currentQueueSize;
  private boolean processing;

  @Factory
  public static OrderedAtLeastOnceOutputConnection factory(Vertx vertx, OutputConnectionContext context, VertigoCluster cluster) {
    return new OrderedAtLeastOnceOutputConnection(vertx, context, cluster);
  }

  private OrderedAtLeastOnceOutputConnection(Vertx vertx, OutputConnectionContext context, VertigoCluster cluster) {
    super(vertx, context, cluster);
    this.eventBus = new WrappedAdaptiveEventBus(vertx);
    this.messages = ((VertigoCluster) Factories.createObject(null, vertx, context)).getQueue(context.address());
  }

  @Override
  public boolean sendQueueFull() {
    return currentQueueSize >= maxQueueSize;
  }

  @Override
  public OutputConnection open(final Handler<AsyncResult<Void>> doneHandler) {
    messages.size(new Handler<AsyncResult<Integer>>() {
      @Override
      public void handle(AsyncResult<Integer> result) {
        if (result.failed()) {
          new DefaultFutureResult<Void>(result.cause()).setHandler(doneHandler);
        } else {
          currentQueueSize = result.result() != null ? result.result() : 0;
          new DefaultFutureResult<Void>((Void) null).setHandler(doneHandler);
        }
      }
    });
    return this;
  }

  @Override
  public String send(JsonMessage message) {
    return send(message, (Handler<AsyncResult<Void>>) null);
  }

  @Override
  public String send(final JsonMessage message, final Handler<AsyncResult<Void>> doneHandler) {
    checkFull();
    messages.add(serializer.serializeToString(message), new Handler<AsyncResult<Boolean>>() {
      @Override
      public void handle(AsyncResult<Boolean> result) {
        if (result.failed()) {
          new DefaultFutureResult<Void>(result.cause()).setHandler(doneHandler);
        } else {
          currentQueueSize++;
          checkPause();
          new DefaultFutureResult<Void>((Void) null).setHandler(doneHandler);
          checkMessages();
        }
      }
    });
    return message.id();
  }

  /**
   * Sends a message asynchronously.
   */
  private void sendAsync(final JsonMessage message) {
    final List<String> addresses = selector.select(message, targets);
    if (!addresses.isEmpty()) {
      final CountingCompletionHandler<Void> counter = new CountingCompletionHandler<Void>(addresses.size());
      counter.setHandler(new Handler<AsyncResult<Void>>() {
        @Override
        public void handle(AsyncResult<Void> result) {
          if (result.failed()) {
            sendAsync(message);
          } else {
            messages.remove(new Handler<AsyncResult<String>>() {
              @Override
              public void handle(AsyncResult<String> result) {
                if (result.succeeded()) {
                  currentQueueSize--;
                  processing = false;
                  checkPause();
                  checkMessages();
                }
              }
            });
          }
        }
      });

      for (String address : addresses) {
        final JsonMessage child = createCopy(message);
        eventBus.sendWithAdaptiveTimeout(address, serializer.serializeToString(child), 5, new Handler<AsyncResult<Message<Boolean>>>() {
          @Override
          public void handle(AsyncResult<Message<Boolean>> result) {
            if (result.failed()) {
              counter.fail(result.cause());
            } else if (result.result().body()) {
              counter.succeed();
            } else {
              counter.fail(null);
            }
          }
        });
      }
    }
  }

  /**
   * Checks for messages to send.
   */
  private void checkMessages() {
    if (processing) return;
    messages.peek(new Handler<AsyncResult<String>>() {
      @Override
      public void handle(AsyncResult<String> result) {
        if (result.succeeded() && result.result() != null) {
          processing = true;
          sendAsync(serializer.deserializeString(result.result(), JsonMessage.class));
        }
      }
    });
  }

}
