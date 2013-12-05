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

import net.kuujo.vertigo.context.InstanceContext;
import net.kuujo.vertigo.message.JsonMessage;
import net.kuujo.vertigo.message.MessageId;
import net.kuujo.vertigo.worker.BasicWorker;
import net.kuujo.vertigo.worker.Worker;

/**
 * A Java worker verticle.
 *
 * @author Jordan Halterman
 */
public abstract class WorkerVerticle extends VertigoVerticle<Worker> {
  protected Worker worker;

  @Override
  protected Worker createComponent(InstanceContext context) {
    return new BasicWorker(vertx, container, context);
  }

  @Override
  protected void start(Worker worker) {
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
   * Emits a new message.
   *
   * @param data
   *   The message body.
   * @return
   *   The emitted message identifier.
   */
  public MessageId emit(JsonObject data) {
    return worker.emit(data);
  }

  /**
   * Emits a child message from the worker.
   *
   * @param data
   *   The message body.
   * @param parent
   *   The message parent.
   * @return
   *   The unique message identifier.
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
   *   The new child message's unique identifier.
   */
  public MessageId emit(JsonMessage message) {
    return worker.emit(message);
  }

  /**
   * Acks a message.
   *
   * @param message
   *   The message to ack.
   */
  protected void ack(JsonMessage message) {
    worker.ack(message);
  }

  /**
   * Fails a message.
   *
   * @param message
   *   The message to fail.
   */
  protected void fail(JsonMessage message) {
    worker.fail(message);
  }

}
