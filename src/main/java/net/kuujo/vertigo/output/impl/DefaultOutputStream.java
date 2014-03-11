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

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.impl.DefaultFutureResult;

import net.kuujo.vertigo.context.ConnectionContext;
import net.kuujo.vertigo.context.OutputStreamContext;
import net.kuujo.vertigo.message.JsonMessage;
import net.kuujo.vertigo.message.MessageId;
import net.kuujo.vertigo.output.OutputConnection;
import net.kuujo.vertigo.output.OutputStream;
import net.kuujo.vertigo.output.selector.Selector;

/**
 * Default output stream implementation.
 *
 * @author Jordan Halterman
 */
public class DefaultOutputStream implements OutputStream {
  private final Vertx vertx;
  private final OutputStreamContext context;
  private List<OutputConnection> connections;
  private Selector selector;

  public DefaultOutputStream(Vertx vertx, OutputStreamContext context) {
    this.vertx = vertx;
    this.context = context;
  }

  @Override
  public OutputStreamContext context() {
    return context;
  }

  @Override
  public List<MessageId> emit(JsonMessage message) {
    List<MessageId> messageIds = new ArrayList<>();
    for (OutputConnection connection : selector.select(message, connections)) {
      messageIds.add(connection.write(message.copy()));
    }
    return messageIds;
  }

  @Override
  public OutputStream start() {
    return start(null);
  }

  @Override
  public OutputStream start(final Handler<AsyncResult<Void>> doneHandler) {
    selector = context.grouping().createSelector();
    connections = new ArrayList<>();
    for (ConnectionContext connection : context.connections()) {
      connections.add(new DefaultOutputConnection(connection.address(), vertx));
    }
    vertx.runOnContext(new Handler<Void>() {
      @Override
      public void handle(Void _) {
        new DefaultFutureResult<Void>((Void) null).setHandler(doneHandler);
      }
    });
    return this;
  }

  @Override
  public void stop() {
    stop(null);
  }

  @Override
  public void stop(final Handler<AsyncResult<Void>> doneHandler) {
    connections.clear();
    vertx.runOnContext(new Handler<Void>() {
      @Override
      public void handle(Void _) {
        new DefaultFutureResult<Void>((Void) null).setHandler(doneHandler);
      }
    });
  }

}
