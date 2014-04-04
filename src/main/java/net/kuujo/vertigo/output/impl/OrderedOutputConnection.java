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

import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;

import net.kuujo.vertigo.annotations.Factory;
import net.kuujo.vertigo.cluster.VertigoCluster;
import net.kuujo.vertigo.context.OutputConnectionContext;
import net.kuujo.vertigo.message.JsonMessage;
import net.kuujo.vertigo.message.impl.ReliableJsonMessage;
import net.kuujo.vertigo.util.CountingCompletionHandler;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.impl.DefaultFutureResult;

/**
 * Basic output connection implementation.
 *
 * The basic connection is essentially a basic at-most-once output connection.
 *
 * @author Jordan Halterman
 */
public class OrderedOutputConnection extends BaseOutputConnection {
  private final Queue<JsonMessage> queue = new ArrayDeque<>();
  private boolean processing;

  @Factory
  public static OrderedOutputConnection factory(Vertx vertx, OutputConnectionContext context, VertigoCluster cluster) {
    return new OrderedOutputConnection(vertx, context, cluster);
  }

  public OrderedOutputConnection(Vertx vertx, OutputConnectionContext context, VertigoCluster cluster) {
    super(vertx, context, cluster);
  }

  @Override
  public boolean sendQueueFull() {
    return queue.size() >= maxQueueSize;
  }

  @Override
  public String send(JsonMessage message) {
    return doSend(message, null);
  }

  @Override
  public String send(JsonMessage message, Handler<AsyncResult<Void>> doneHandler) {
    return doSend(message, doneHandler);
  }

  @Override
  public String send(JsonMessage message, ReliableJsonMessage parent) {
    return doSend(message, null);
  }

  @Override
  public String send(JsonMessage message, ReliableJsonMessage parent, final Handler<AsyncResult<Void>> doneHandler) {
    return doSend(message, doneHandler);
  }

  /**
   * Sends a message on the connection.
   */
  private String doSend(final JsonMessage message, final Handler<AsyncResult<Void>> doneHandler) {
    checkOpen();
    if (processing) {
      checkFull();
      queue.add(message);
      checkPause();
      new DefaultFutureResult<Void>((Void) null).setHandler(doneHandler);
    } else {
      processing = true;
      sendAsync(message);
      new DefaultFutureResult<Void>((Void) null).setHandler(doneHandler);
    }
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
            processing = false;
            checkMessages();
          }
        }
      });

      processing = true;
      for (String address : addresses) {
        final JsonMessage child = createCopy(message);
        // TODO Use adaptive timeouts for message replies.
        vertx.eventBus().sendWithTimeout(address, serializer.serializeToString(child), 30000, new Handler<AsyncResult<Message<Void>>>() {
          @Override
          public void handle(AsyncResult<Message<Void>> result) {
            if (result.failed()) {
              counter.fail(result.cause());
            } else {
              counter.succeed();
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
    final JsonMessage message = queue.poll();
    if (message != null) {
      processing = true;
      sendAsync(message);
      checkPause();
    }
  }

}
