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
import net.kuujo.vertigo.context.OutputConnectionContext;
import net.kuujo.vertigo.message.JsonMessage;
import net.kuujo.vertigo.message.impl.ReliableJsonMessage;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.impl.DefaultFutureResult;

/**
 * Basic output connection implementation.
 *
 * The basic connection is essentially a basic at-most-once output connection.
 *
 * @author Jordan Halterman
 */
public class BasicOutputConnection extends BaseOutputConnection {

  public BasicOutputConnection(Vertx vertx, OutputConnectionContext context, VertigoCluster cluster) {
    super(vertx, context, cluster);
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
    vertx.runOnContext(new Handler<Void>() {
      @Override
      public void handle(Void _) {
        final List<String> addresses = selector.select(message, targets);
        for (String address : addresses) {
          final JsonMessage child = createCopy(message);
          vertx.eventBus().send(address, serializer.serializeToString(child));
        }
        new DefaultFutureResult<Void>((Void) null).setHandler(doneHandler);
      }
    });
    return message.id();
  }

}
