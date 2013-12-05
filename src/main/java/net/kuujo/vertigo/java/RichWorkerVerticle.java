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
package net.kuujo.vertigo.java;

import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonObject;

import net.kuujo.vertigo.component.ComponentFactory;
import net.kuujo.vertigo.component.DefaultComponentFactory;
import net.kuujo.vertigo.context.InstanceContext;
import net.kuujo.vertigo.message.JsonMessage;
import net.kuujo.vertigo.message.MessageId;
import net.kuujo.vertigo.worker.Worker;

/**
 * A rich worker verticle implementation.
 *
 * @author Jordan Halterman
 */
public abstract class RichWorkerVerticle extends ComponentVerticle<Worker> {
  protected Worker worker;

  @Override
  protected Worker createComponent(InstanceContext<Worker> context) {
    ComponentFactory componentFactory = new DefaultComponentFactory(vertx, container);
    return componentFactory.createWorker(context);
  }

  @Override
  protected void start(final Worker worker) {
    this.worker = worker;
    worker.messageHandler(new Handler<JsonMessage>() {
      @Override
      public void handle(JsonMessage message) {
        handleMessage(message);
      }
    });
  }

  /**
   * Called when a new message is received by the worker.
   *
   * @param message
   *   The message that was received.
   */
  protected abstract void handleMessage(JsonMessage message);

  /**
   * Emits data from the worker.
   *
   * @param data
   *   The data to emit.
   * @return
   *   The emitted message identifier.
   */
  public MessageId emit(JsonObject data) {
    return worker.emit(data);
  }

  /**
   * Emits data as the child of a message.
   *
   * @param data
   *   The data to emit.
   * @param parent
   *   The parent message.
   * @return
   *   The emitted message identifier.
   */
  public MessageId emit(JsonObject data, JsonMessage parent) {
    return worker.emit(data, parent);
  }

  /**
   * Emits a message as a child of itself.
   *
   * @param message
   *   The message to emit.
   * @return
   *   The emitted message identifier.
   */
  public MessageId emit(JsonMessage message) {
    return worker.emit(message);
  }

  /**
   * Emits data from the worker.
   *
   * @param stream
   *   The stream to which to emit the data.
   * @param data
   *   The data to emit.
   * @return
   *   The emitted message identifier.
   */
  public MessageId emit(String stream, JsonObject data) {
    return worker.emit(stream, data);
  }

  /**
   * Emits data as the child of a message.
   *
   * @param stream
   *   The stream to which to emit the data.
   * @param data
   *   The data to emit.
   * @param parent
   *   The parent message.
   * @return
   *   The emitted message identifier.
   */
  public MessageId emit(String stream, JsonObject data, JsonMessage parent) {
    return worker.emit(stream, data, parent);
  }

  /**
   * Emits a message as a child of itself.
   *
   * @param stream
   *   The stream to which to emit the data.
   * @param message
   *   The message to emit.
   * @return
   *   The emitted message identifier.
   */
  public MessageId emit(String stream, JsonMessage message) {
    return worker.emit(stream, message);
  }

  /**
   * Acks a message.
   *
   * @param message
   *   The message to ack.
   */
  public void ack(JsonMessage message) {
    worker.ack(message);
  }

  /**
   * Fails a message.
   *
   * @param message
   *   The message to fail.
   */
  public void fail(JsonMessage message) {
    worker.fail(message);
  }

}
