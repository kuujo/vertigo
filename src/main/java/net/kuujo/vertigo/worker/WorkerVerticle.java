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
package net.kuujo.vertigo.worker;

import net.kuujo.vertigo.Vertigo;
import net.kuujo.vertigo.context.InstanceContext;
import net.kuujo.vertigo.message.JsonMessage;
import net.kuujo.vertigo.message.MessageId;
import net.kuujo.vertigo.message.schema.MessageSchema;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Future;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Verticle;

/**
 * A Java worker verticle.
 *
 * @author Jordan Halterman
 */
public abstract class WorkerVerticle extends Verticle {
  private Vertigo vertigo;
  protected BasicWorker worker;
  protected InstanceContext context;

  private Handler<JsonMessage> messageHandler = new Handler<JsonMessage>() {
    @Override
    public void handle(JsonMessage message) {
      handleMessage(message);
    }
  };

  @Override
  public void start(final Future<Void> future) {
    vertigo = new Vertigo(this);
    worker = vertigo.createBasicWorker().messageHandler(messageHandler);
    context = worker.getContext();

    MessageSchema schema = declareSchema();
    if (schema != null) {
      worker.declareSchema(schema);
    }

    worker.start(new Handler<AsyncResult<BasicWorker>>() {
      @Override
      public void handle(AsyncResult<BasicWorker> result) {
        if (result.failed()) {
          future.setFailure(result.cause());
        }
        else {
          WorkerVerticle.super.start(future);
        }
      }
    });
  }

  /**
   * Declares a worker input schema.
   *
   * Override this method to provide a required input schema.
   *
   * @return
   *   An input message schema.
   */
  protected MessageSchema declareSchema() {
    return null;
  }

  /**
   * Called when the worker receives a new message.
   *
   * @param message
   *   The received message.
   */
  protected abstract void handleMessage(JsonMessage message);

  /**
   * Emits a new message from the worker.
   *
   * @param data
   *   The message body.
   * @return
   *   The unique message identifier.
   */
  protected MessageId emit(JsonObject data) {
    return worker.emit(data);
  }

  /**
   * Emits a new message from the worker.
   *
   * @param data
   *   The message body.
   * @param tag
   *   A tag to apply to the message.
   * @return
   *   The unique message identifier.
   */
  protected MessageId emit(JsonObject data, String tag) {
    return worker.emit(data, tag);
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
  protected MessageId emit(JsonObject data, JsonMessage parent) {
    return worker.emit(data, parent);
  }

  /**
   * Emits a child message from the worker.
   *
   * @param data
   *   The message body.
   * @param tag
   *   A tag to apply to the message.
   * @param parent
   *   The message parent.
   * @return
   *   The unique message identifier.
   */
  protected MessageId emit(JsonObject data, String tag, JsonMessage parent) {
    return worker.emit(data, tag, parent);
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
