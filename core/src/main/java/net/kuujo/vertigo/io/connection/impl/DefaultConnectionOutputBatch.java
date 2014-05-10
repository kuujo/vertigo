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
package net.kuujo.vertigo.io.connection.impl;

import java.util.UUID;

import net.kuujo.vertigo.io.connection.ConnectionOutputBatch;
import net.kuujo.vertigo.io.connection.OutputConnection;
import net.kuujo.vertigo.io.group.OutputGroup;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.VoidHandler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

/**
 * Default connection output batch implementation.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class DefaultConnectionOutputBatch implements ConnectionOutputBatch {
  private final String id;
  private final DefaultOutputConnection connection;
  private Handler<ConnectionOutputBatch> startHandler;
  private Handler<Void> endHandler;
  private int children;
  private boolean started;
  private boolean ended;
  private boolean closed;

  public DefaultConnectionOutputBatch(String id, DefaultOutputConnection connection) {
    this.id = id;
    this.connection = connection;
  }

  @Override
  public String id() {
    return id;
  }

  @Override
  public String address() {
    return connection.address();
  }

  @Override
  public int size() {
    return connection.size();
  }

  @Override
  public OutputConnection open() {
    connection.open(null);
    return this;
  }

  @Override
  public OutputConnection open(Handler<AsyncResult<Void>> doneHandler) {
    connection.open(doneHandler);
    return this;
  }

  @Override
  public void close() {
    connection.close(null);
  }

  @Override
  public void close(Handler<AsyncResult<Void>> doneHandler) {
    connection.close(doneHandler);
  }

  @Override
  public Vertx vertx() {
    return connection.vertx();
  }

  @Override
  public OutputConnection setSendQueueMaxSize(int maxSize) {
    connection.setSendQueueMaxSize(maxSize);
    return this;
  }

  @Override
  public int getSendQueueMaxSize() {
    return connection.getSendQueueMaxSize();
  }

  @Override
  public boolean sendQueueFull() {
    return connection.sendQueueFull();
  }

  @Override
  public OutputConnection drainHandler(Handler<Void> handler) {
    connection.drainHandler(handler);
    return this;
  }

  /**
   * Checks whether the batch is complete.
   */
  private void checkEnd() {
    if (ended && !closed && children == 0) {
      closed = true;
      connection.doBatchEnd(id);
      if (endHandler != null) {
        endHandler.handle((Void) null);
      }
    }
  }

  void endHandler(Handler<Void> handler) {
    this.endHandler = handler;
    if (closed) {
      endHandler.handle((Void) null);
    }
  }

  /**
   * Starts the output batch.
   */
  void start(Handler<ConnectionOutputBatch> startHandler) {
    connection.doBatchStart(id);
    this.startHandler = startHandler;
  }

  /**
   * Called when the output connection receives a ready message from the
   * group at the other side of the connection. The ready message will be
   * sent once a message handler has been registered on the target group.
   */
  void handleStart() {
    if (!started && startHandler != null) {
      startHandler.handle(this);
      started = true;
    }
  }

  @Override
  public OutputConnection send(Object message) {
    if (!ended) {
      connection.doBatchSend(id, message);
    }
    return this;
  }

  @Override
  public OutputConnection send(String message) {
    if (!ended) {
      connection.doBatchSend(id, message);
    }
    return this;
  }

  @Override
  public OutputConnection send(Short message) {
    if (!ended) {
      connection.doBatchSend(id, message);
    }
    return this;
  }

  @Override
  public OutputConnection send(Integer message) {
    if (!ended) {
      connection.doBatchSend(id, message);
    }
    return this;
  }

  @Override
  public OutputConnection send(Long message) {
    if (!ended) {
      connection.doBatchSend(id, message);
    }
    return this;
  }

  @Override
  public OutputConnection send(Float message) {
    if (!ended) {
      connection.doBatchSend(id, message);
    }
    return this;
  }

  @Override
  public OutputConnection send(Double message) {
    if (!ended) {
      connection.doBatchSend(id, message);
    }
    return this;
  }

  @Override
  public OutputConnection send(Boolean message) {
    if (!ended) {
      connection.doBatchSend(id, message);
    }
    return this;
  }

  @Override
  public OutputConnection send(Byte message) {
    if (!ended) {
      connection.doBatchSend(id, message);
    }
    return this;
  }

  @Override
  public OutputConnection send(byte[] message) {
    if (!ended) {
      connection.doBatchSend(id, message);
    }
    return this;
  }

  @Override
  public OutputConnection send(Character message) {
    if (!ended) {
      connection.doBatchSend(id, message);
    }
    return this;
  }

  @Override
  public OutputConnection send(Buffer message) {
    if (!ended) {
      connection.doBatchSend(id, message);
    }
    return this;
  }

  @Override
  public OutputConnection send(JsonArray message) {
    if (!ended) {
      connection.doBatchSend(id, message);
    }
    return this;
  }

  @Override
  public OutputConnection send(JsonObject message) {
    if (!ended) {
      connection.doBatchSend(id, message);
    }
    return this;
  }

  @Override
  public OutputConnection batch(String id, Handler<ConnectionOutputBatch> handler) {
    throw new UnsupportedOperationException("Cannot batch a batch.");
  }

  @Override
  public OutputConnection group(Handler<OutputGroup> handler) {
    return group(UUID.randomUUID().toString(), handler);
  }

  @Override
  public OutputConnection group(String name, Handler<OutputGroup> handler) {
    DefaultConnectionOutputGroup group = connection.group(name, id, handler);
    children++;
    group.endHandler(new VoidHandler() {
      @Override
      protected void handle() {
        children--;
        checkEnd();
      }
    });
    return this;
  }

  @Override
  public void end() {
    ended = true;
    checkEnd();
  }

}
