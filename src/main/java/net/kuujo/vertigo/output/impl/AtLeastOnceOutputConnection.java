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

import net.kuujo.vertigo.cluster.VertigoCluster;
import net.kuujo.vertigo.cluster.data.AsyncMap;
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
 * Output connection for at-least-once processing.
 *
 * @author Jordan Halterman
 */
public class AtLeastOnceOutputConnection extends BaseOutputConnection {
  private final AsyncMap<String, String> messages;
  private int currentQueueSize;

  public AtLeastOnceOutputConnection(Vertx vertx, OutputConnectionContext context, VertigoCluster cluster) {
    super(vertx, context, cluster);
    this.messages = cluster.getMap(context.address());
  }

  @Override
  public boolean sendQueueFull() {
    return currentQueueSize >= maxQueueSize;
  }

  @Override
  public String send(JsonMessage message) {
    return send(message, (Handler<AsyncResult<Void>>) null);
  }

  @Override
  public String send(final JsonMessage message, final Handler<AsyncResult<Void>> doneHandler) {
    checkFull();
    messages.put(message.id(), serializer.serializeToString(message), new Handler<AsyncResult<String>>() {
      @Override
      public void handle(AsyncResult<String> result) {
        if (result.failed()) {
          new DefaultFutureResult<Void>(result.cause()).setHandler(doneHandler);
        } else {
          currentQueueSize++;
          checkPause();
          new DefaultFutureResult<Void>((Void) null).setHandler(doneHandler);
          doSendNew(message);
        }
      }
    });
    return message.id();
  }

  /**
   * Sends a new message.
   */
  private void doSendNew(final JsonMessage message) {
    final List<String> addresses = selector.select(message, targets);
    final CountingCompletionHandler<Void> counter = new CountingCompletionHandler<>(addresses.size());
    counter.setHandler(new Handler<AsyncResult<Void>>() {
      @Override
      public void handle(AsyncResult<Void> result) {
        if (result.failed()) {
          doSendNew(message);
        } else {
          messages.remove(message.id(), new Handler<AsyncResult<String>>() {
            @Override
            public void handle(AsyncResult<String> result) {
              if (result.succeeded()) {
                currentQueueSize--;
                checkPause();
              }
            }
          });
        }
      }
    });

    for (final String address : addresses) {
      final JsonMessage child = createCopy(message);
      vertx.eventBus().sendWithTimeout(address, serializer.serializeToString(child), 30000, new Handler<AsyncResult<Message<Boolean>>>() {
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

  @Override
  public String send(JsonMessage message, ReliableJsonMessage parent) {
    return send(message, parent, null);
  }

  @Override
  public String send(final JsonMessage message, final ReliableJsonMessage parent, Handler<AsyncResult<Void>> doneHandler) {
    final List<String> addresses = selector.select(message, targets);
    for (final String address : addresses) {
      final JsonMessage child = createCopy(message);
      parent.anchor(child);
      vertx.eventBus().sendWithTimeout(address, serializer.serializeToString(child), 30000, new Handler<AsyncResult<Message<Boolean>>>() {
        @Override
        public void handle(AsyncResult<Message<Boolean>> result) {
          if (result.failed()) {
            parent.timeout();
          } else if (result.result().body()) {
            parent.ack();
          } else {
            parent.timeout();
          }
        }
      });
    }
    return message.id();
  }

}
