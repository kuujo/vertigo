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
package net.kuujo.vertigo.input.impl;

import net.kuujo.vertigo.cluster.VertigoCluster;
import net.kuujo.vertigo.context.InputConnectionContext;
import net.kuujo.vertigo.input.InputConnection;
import net.kuujo.vertigo.message.JsonMessage;
import net.kuujo.vertigo.message.impl.ReliableJsonMessage;
import net.kuujo.vertigo.util.Observer;
import net.kuujo.vertigo.util.serializer.Serializer;
import net.kuujo.vertigo.util.serializer.SerializerFactory;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.eventbus.Message;

/**
 * Base input connection.
 *
 * @author Jordan Halterman
 */
public abstract class BaseInputConnection implements InputConnection, Observer<InputConnectionContext> {
  protected static final Serializer serializer = SerializerFactory.getSerializer(JsonMessage.class);
  protected final Vertx vertx;
  protected final InputConnectionContext context;
  protected final VertigoCluster cluster;
  protected Handler<ReliableJsonMessage> messageHandler;

  private final Handler<Message<String>> internalHandler = new Handler<Message<String>>() {
    @Override
    public void handle(Message<String> message) {
      handleMessage(serializer.deserializeString(message.body(), ReliableJsonMessage.class), message);
    }
  };

  protected BaseInputConnection(Vertx vertx, InputConnectionContext context, VertigoCluster cluster) {
    this.vertx = vertx;
    this.context = context;
    this.cluster = cluster;
  }

  @Override
  public InputConnectionContext context() {
    return context;
  }

  @Override
  public void update(InputConnectionContext update) {
  }

  @Override
  public InputConnection messageHandler(Handler<ReliableJsonMessage> handler) {
    this.messageHandler = handler;
    return this;
  }

  /**
   * Handles a message.
   */
  protected void handleMessage(ReliableJsonMessage message, Message<String> sourceMessage) {
    if (messageHandler != null) {
      messageHandler.handle(message);
    }
  }

  @Override
  public InputConnection open() {
    return open(null);
  }

  @Override
  public InputConnection open(Handler<AsyncResult<Void>> doneHandler) {
    vertx.eventBus().registerHandler(context.address(), internalHandler, doneHandler);
    return this;
  }

  @Override
  public void close() {
    close(null);
  }

  @Override
  public void close(Handler<AsyncResult<Void>> doneHandler) {
    vertx.eventBus().unregisterHandler(context.address(), internalHandler, doneHandler);
  }

  @Override
  public String toString() {
    return context.address();
  }

  @Override
  public boolean equals(Object object) {
    return getClass().isAssignableFrom(object.getClass()) && ((InputConnection) object).context().address().equals(context.address());
  }

}
