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

import java.util.ArrayList;
import java.util.List;

import net.kuujo.vertigo.context.OutputConnectionContext;
import net.kuujo.vertigo.message.JsonMessage;
import net.kuujo.vertigo.message.MessageId;
import net.kuujo.vertigo.output.OutputConnection;
import net.kuujo.vertigo.output.selector.Selector;
import net.kuujo.vertigo.util.serializer.Serializer;
import net.kuujo.vertigo.util.serializer.SerializerFactory;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.impl.DefaultFutureResult;

/**
 * Default output connection implementation.
 *
 * @author Jordan Halterman
 */
public class DefaultOutputConnection implements OutputConnection {
  private static final Serializer serializer = SerializerFactory.getSerializer(JsonMessage.class);
  private final Vertx vertx;
  private final EventBus eventBus;
  private final OutputConnectionContext context;
  private final String address;
  private final List<String> ports;
  private final Selector selector;
  private boolean open;

  public DefaultOutputConnection(Vertx vertx, OutputConnectionContext context) {
    this.vertx = vertx;
    this.eventBus = vertx.eventBus();
    this.context = context;
    this.address = context.address();
    this.ports = context.ports();
    this.selector = context.grouping().createSelector();
  }

  @Override
  public OutputConnectionContext context() {
    return context;
  }

  @Override
  public List<String> ports() {
    return ports;
  }

  @Override
  public List<MessageId> send(JsonMessage message) {
    final List<MessageId> messageIds = new ArrayList<>();
    if (!open) return messageIds;
    for (String address : selector.select(message, ports)) {
      JsonMessage child = message.copy(new StringBuilder()
        .append(this.address)
        .append(":")
        .append(OutputCounter.incrementAndGet())
        .toString());
      eventBus.send(address, serializer.serializeToString(child));
      messageIds.add(child.messageId());
    }
    return messageIds;
  }

  @Override
  public OutputConnection open() {
    return open(null);
  }

  @Override
  public OutputConnection open(final Handler<AsyncResult<Void>> doneHandler) {
    open = true;
    vertx.runOnContext(new Handler<Void>() {
      @Override
      public void handle(Void _) {
        new DefaultFutureResult<Void>((Void) null).setHandler(doneHandler);
      }
    });
    return this;
  }

  @Override
  public void close() {
    close(null);
  }

  @Override
  public void close(final Handler<AsyncResult<Void>> doneHandler) {
    open = false;
    vertx.runOnContext(new Handler<Void>() {
      @Override
      public void handle(Void _) {
        new DefaultFutureResult<Void>((Void) null).setHandler(doneHandler);
      }
    });
  }

}
