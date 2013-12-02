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
import net.kuujo.vertigo.VertigoException;
import net.kuujo.vertigo.annotations.Config;
import net.kuujo.vertigo.annotations.Schema;
import net.kuujo.vertigo.context.InstanceContext;
import net.kuujo.vertigo.message.JsonMessage;
import net.kuujo.vertigo.message.MessageId;
import net.kuujo.vertigo.message.schema.Field;
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
  protected Worker worker;
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
    worker = vertigo.createWorker().messageHandler(messageHandler);
    context = worker.getContext();

    try {
      checkConfig();
      declareSchema();
    }
    catch (VertigoException e) {
      future.setFailure(e);
      return;
    }

    worker.start(new Handler<AsyncResult<Worker>>() {
      @Override
      public void handle(AsyncResult<Worker> result) {
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
   * Checks the worker configuration.
   */
  private void checkConfig() {
    JsonObject config = container.config();
    Config configInfo = getClass().getAnnotation(Config.class);
    if (configInfo != null) {
      for (Config.Field field : configInfo.value()) {
        Object value = config.getValue(field.name());
        if (value != null) {
          if (!field.type().isAssignableFrom(value.getClass())) {
            throw new VertigoException("Invalid component configuration.");
          }
        }
        else {
          if (field.required()) {
            throw new VertigoException("Invalid component configuration.");
          }
        }
      }
    }
  }

  /**
   * Declares the worker schema according to annotations.
   */
  private void declareSchema() {
    Schema schemaInfo = getClass().getAnnotation(Schema.class);
    if (schemaInfo != null) {
      MessageSchema schema = new MessageSchema();
      for (Schema.Field field : schemaInfo.value()) {
        schema.addField(new Field(field.name(), field.type()).setRequired(field.required()));
      }
      worker.declareSchema(schema);
    }
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
   * Emits a message as a child of itself.
   *
   * @param message
   *   The message to emit.
   * @return
   *   The new child message's unique identifier.
   */
  protected MessageId emit(JsonMessage message) {
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
