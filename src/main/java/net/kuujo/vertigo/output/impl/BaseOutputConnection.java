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
import net.kuujo.vertigo.network.ConnectionException;
import net.kuujo.vertigo.output.OutputConnection;
import net.kuujo.vertigo.output.selector.MessageSelector;
import net.kuujo.vertigo.util.Observer;
import net.kuujo.vertigo.util.serializer.Serializer;
import net.kuujo.vertigo.util.serializer.SerializerFactory;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.impl.DefaultFutureResult;

/**
 * Base class for output connections.
 *
 * @author Jordan Halterman
 */
public abstract class BaseOutputConnection implements OutputConnection, Observer<OutputConnectionContext> {
  protected static final Serializer serializer = SerializerFactory.getSerializer(JsonMessage.class);
  protected final Vertx vertx;
  protected final OutputConnectionContext context;
  protected final VertigoCluster cluster;
  protected final MessageSelector selector;
  protected final List<String> targets;
  protected int maxQueueSize;
  protected Handler<Void> fullHandler;
  protected Handler<Void> drainHandler;
  protected boolean open;
  protected boolean paused;

  protected BaseOutputConnection(Vertx vertx, OutputConnectionContext context, VertigoCluster cluster) {
    this.vertx = vertx;
    this.context = context;
    this.cluster = cluster;
    this.selector = context.grouping().createSelector();
    this.targets = context.targets();
    context.registerObserver(this);
  }

  @Override
  public OutputConnectionContext context() {
    return context;
  }

  @Override
  public void update(OutputConnectionContext update) {
    for (String address : update.targets()) {
      if (!targets.contains(address)) {
        targets.add(address);
      }
    }
    for (String address : targets) {
      if (!update.targets().contains(address)) {
        targets.remove(address);
      }
    }
  }

  @Override
  public OutputConnection open() {
    return open(null);
  }

  @Override
  public OutputConnection open(Handler<AsyncResult<Void>> doneHandler) {
    open = true;
    new DefaultFutureResult<Void>((Void) null).setHandler(doneHandler);
    return this;
  }

  @Override
  public OutputConnection setSendQueueMaxSize(int maxSize) {
    this.maxQueueSize = maxSize;
    return this;
  }

  @Override
  public boolean sendQueueFull() {
    return false;
  }

  @Override
  public OutputConnection fullHandler(Handler<Void> handler) {
    this.fullHandler = handler;
    return this;
  }

  @Override
  public OutputConnection drainHandler(Handler<Void> handler) {
    this.drainHandler = handler;
    return this;
  }

  /**
   * Creates a copy of a message.
   */
  protected JsonMessage createCopy(JsonMessage message) {
    return message.copy(createUniqueId());
  }

  /**
   * Creates a unique message ID.
   */
  private String createUniqueId() {
    return new StringBuilder()
      .append(context.address())
      .append(":")
      .append(OutputCounter.incrementAndGet())
      .toString();
  }

  /**
   * Checks whether the connection is open. If the connection isn't open then
   * an exception is thrown.
   */
  protected void checkOpen() {
    if (!open) throw new ConnectionException("Output connection is closed.");
  }

  /**
   * Checks whether the drain handler needs to be triggered.
   */
  protected void checkPause() {
    if (paused && !sendQueueFull()) {
      paused = false;
      if (drainHandler != null) {
        drainHandler.handle((Void) null);
      }
    } else if (!paused && sendQueueFull()) {
      paused = true;
      if (fullHandler != null) {
        fullHandler.handle((Void) null);
      }
    }
  }

  /**
   * Checks if the send queue is full.
   */
  protected void checkFull() {
    if (sendQueueFull()) throw new ConnectionException("Send queue full.");
  }

  @Override
  public void close() {
    close(null);
  }

  @Override
  public void close(Handler<AsyncResult<Void>> doneHandler) {
    open = false;
    new DefaultFutureResult<Void>((Void) null).setHandler(doneHandler);
  }

  @Override
  public String toString() {
    return context.address();
  }

  @Override
  public boolean equals(Object object) {
    return getClass().isAssignableFrom(object.getClass()) && ((OutputConnection) object).context().address().equals(context.address());
  }

}
