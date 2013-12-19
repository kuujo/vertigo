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

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonObject;

import net.kuujo.vertigo.annotations.FeederOptions;
import net.kuujo.vertigo.component.ComponentFactory;
import net.kuujo.vertigo.component.impl.DefaultComponentFactory;
import net.kuujo.vertigo.context.InstanceContext;
import net.kuujo.vertigo.feeder.Feeder;
import net.kuujo.vertigo.message.MessageId;
import net.kuujo.vertigo.runtime.FailureException;
import net.kuujo.vertigo.runtime.TimeoutException;

/**
 * A rich feeder verticle implementation.
 *
 * This verticle implementation provides additional helpers for feeding messages
 * to Vertigo networks.<p>
 *
 * To configure the internal feeder, use the <code>@FeederOptions</code> annotation.<p>
 *
 * @author Jordan Halterman
 */
public abstract class RichFeederVerticle extends ComponentVerticle<Feeder> {
  protected Feeder feeder;

  @Override
  protected Feeder createComponent(InstanceContext<Feeder> context) {
    ComponentFactory componentFactory = new DefaultComponentFactory(vertx, container);
    return componentFactory.createFeeder(context);
  }

  @Override
  protected void start(Feeder feeder) {
    this.feeder = setupFeeder(feeder);
    feeder.feedHandler(new Handler<Feeder>() {
      @Override
      public void handle(Feeder feeder) {
        nextMessage();
      }
    });
  }

  /**
   * Sets up the feeder according to feeder options.
   */
  private Feeder setupFeeder(Feeder feeder) {
    FeederOptions options = getClass().getAnnotation(FeederOptions.class);
    if (options != null) {
      feeder.setFeedQueueMaxSize(options.feedQueueMaxSize());
      feeder.setAutoRetry(options.autoRetry());
      feeder.setAutoRetryAttempts(options.autoRetryAttempts());
      feeder.setFeedInterval(options.feedInterval());
    }
    return feeder;
  }

  private Handler<AsyncResult<MessageId>> wrapHandler(final Handler<AsyncResult<MessageId>> ackHandler) {
    return new Handler<AsyncResult<MessageId>>() {
      @Override
      public void handle(AsyncResult<MessageId> result) {
        if (result.failed()) {
          if (result.cause() instanceof FailureException) {
            handleFailure(result.result(), (FailureException) result.cause());
          }
          else if (result.cause() instanceof TimeoutException) {
            handleTimeout(result.result(), (TimeoutException) result.cause());
          }
        }
        else {
          handleAck(result.result());
        }
        if (ackHandler != null) {
          ackHandler.handle(result);
        }
      }
    };
  }

  /**
   * Called when the feeder is requesting the next message.
   *
   * Override this method to perform polling-based feeding. The feeder will automatically
   * call this method any time the feed queue is prepared to accept new messages.
   */
  protected void nextMessage() {
  }

  /**
   * Emits data from the feeder.
   *
   * @param data
   *   The data to emit.
   * @return
   *   The emitted message identifier.
   */
  public MessageId emit(JsonObject data) {
    return feeder.emit(data, wrapHandler(null));
  }

  /**
   * Emits data from the feeder.
   *
   * @param data
   *   The data to emit.
   * @param ackHandler
   *   An asynchronous handler to be called once the message is acked, failed,
   *   or times out.
   * @return
   *   The emitted message identifier.
   */
  public MessageId emit(JsonObject data, Handler<AsyncResult<MessageId>> ackHandler) {
    return feeder.emit(data, wrapHandler(ackHandler));
  }

  /**
   * Emits data from the feeder.
   *
   * @param stream
   *   The stream to which to emit the data.
   * @param data
   *   The data to emit.
   * @return
   *   The emitted message identifier.
   */
  public MessageId emit(String stream, JsonObject data) {
    return feeder.emit(stream, data, wrapHandler(null));
  }

  /**
   * Emits data from the feeder.
   *
   * @param stream
   *   The stream to which to emit the data.
   * @param data
   *   The data to emit.
   * @param ackHandler
   *   An asynchronous handler to be called once the message is acked, failed,
   *   or times out.
   * @return
   *   The emitted message identifier.
   */
  public MessageId emit(String stream, JsonObject data, Handler<AsyncResult<MessageId>> ackHandler) {
    return feeder.emit(stream, data, wrapHandler(ackHandler));
  }

  /**
   * Called when a message is successfully processed.
   *
   * Override this method to provide custom handling for message acks.
   *
   * @param messageId
   *   The processed message identifier.
   */
  protected void handleAck(MessageId messageId) {
  }

  /**
   * Called when a message is explicitly failed.
   *
   * Override this method to provide custom handling for message failures.
   *
   * @param messageId
   *   The failed message identifier.
   */
  protected void handleFailure(MessageId messageId, FailureException cause) {
  }

  /**
   * Called when a message times out.
   *
   * Override this method to provide custom handling for message timeouts.
   *
   * @param messageId
   *   The timed out message identifier.
   */
  protected void handleTimeout(MessageId messageId, TimeoutException cause) {
  }

}
