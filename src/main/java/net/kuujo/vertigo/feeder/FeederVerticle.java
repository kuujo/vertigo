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
package net.kuujo.vertigo.feeder;

import net.kuujo.vertigo.Vertigo;
import net.kuujo.vertigo.VertigoException;
import net.kuujo.vertigo.annotations.Config;
import net.kuujo.vertigo.context.InstanceContext;
import net.kuujo.vertigo.message.MessageId;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Future;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Verticle;

/**
 * A Java feeder verticle.
 *
 * @author Jordan Halterman
 */
public abstract class FeederVerticle extends Verticle {
  private Vertigo vertigo;
  protected PollingFeeder feeder;
  protected InstanceContext context;

  private Handler<MessageId> ackHandler = new Handler<MessageId>() {
    @Override
    public void handle(MessageId messageId) {
      handleAck(messageId);
    }
  };

  private Handler<MessageId> failHandler = new Handler<MessageId>() {
    @Override
    public void handle(MessageId messageId) {
      handleFailure(messageId);
    }
  };

  private Handler<MessageId> timeoutHandler = new Handler<MessageId>() {
    @Override
    public void handle(MessageId messageId) {
      handleTimeout(messageId);
    }
  };

  @Override
  public void start(final Future<Void> future) {
    vertigo = new Vertigo(this);
    feeder = vertigo.createPollingFeeder()
        .ackHandler(ackHandler).failHandler(failHandler).timeoutHandler(timeoutHandler);
    context = feeder.getContext();

    try {
      checkConfig();
    }
    catch (VertigoException e) {
      future.setFailure(e);
      return;
    }

    feeder.start(new Handler<AsyncResult<PollingFeeder>>() {
      @Override
      public void handle(AsyncResult<PollingFeeder> result) {
        if (result.failed()) {
          future.setFailure(result.cause());
        }
        else {
          FeederVerticle.super.start(future);
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
   * Called when the feeder is prepared to emit the next message.
   */
  protected abstract void nextMessage();

  /**
   * Emits a message from the feeder.
   *
   * @param data
   *   The message body.
   * @return
   *   The unique message identifier.
   */
  protected MessageId emit(JsonObject data) {
    return feeder.emit(data);
  }

  /**
   * Emits a message from the feeder.
   *
   * @param data
   *   The message body.
   * @param tag
   *   A tag to apply to the message.
   * @return
   *   The unique message identifier.
   */
  protected MessageId emit(JsonObject data, String tag) {
    return feeder.emit(data, tag);
  }

  /**
   * Called when a message has been acked.
   *
   * @param messageId
   *   The acked message identifier.
   */
  protected abstract void handleAck(MessageId messageId);

  /**
   * Called when a message has been failed.
   *
   * @param messageId
   *   The failed message identifier.
   */
  protected abstract void handleFailure(MessageId messageId);

  /**
   * Called when a message has timed out.
   *
   * @param messageId
   *   The timed out message identifier.
   */
  protected abstract void handleTimeout(MessageId messageId);

}
