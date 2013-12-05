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

import net.kuujo.vertigo.context.InstanceContext;
import net.kuujo.vertigo.feeder.BasicFeeder;
import net.kuujo.vertigo.feeder.Feeder;
import net.kuujo.vertigo.message.MessageId;
import net.kuujo.vertigo.runtime.FailureException;
import net.kuujo.vertigo.runtime.TimeoutException;

/**
 * A feeder verticle implementation.
 *
 * @author Jordan Halterman
 */
public abstract class FeederVerticle extends VertigoVerticle<Feeder> {
  protected Feeder feeder;

  @Override
  protected Feeder createComponent(InstanceContext context) {
    return new BasicFeeder(vertx, container, context);
  }

  @Override
  protected void start(Feeder feeder) {
    this.feeder = feeder;
    feeder.feedHandler(new Handler<Feeder>() {
      @Override
      public void handle(Feeder feeder) {
        nextMessage();
      }
    });
  }

  private Handler<AsyncResult<MessageId>> wrapHandler(final Handler<AsyncResult<MessageId>> handler) {
    return new Handler<AsyncResult<MessageId>>() {
      @Override
      public void handle(AsyncResult<MessageId> result) {
        if (result.succeeded()) {
          handleAck(result.result());
        }
        else {
          if (result.cause() instanceof FailureException) {
            handleFailure(result.result());
          }
          else if (result.cause() instanceof TimeoutException) {
            handleTimeout(result.result());
          }
        }
        if (handler != null) {
          handler.handle(result);
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
   * Emits a new message.
   *
   * @param data
   *   The message body.
   * @return
   *   The emitted message identifier.
   */
  public MessageId emit(JsonObject data) {
    return feeder.emit(data, wrapHandler(null));
  }

  /**
   * Emits a new message with an ack handler.
   *
   * The asynchronous result provided to the ack handler is a special implementation
   * that will *always* contain the emitted message ID regardless of whether
   * processing succeeded or failed. If the message is successfully processed,
   * the result will be successful. If the message processing times out then the
   * cause of the result failure will be a {@link TimeoutException}. If the message
   * or one of its descendants is explicitly failed by a worker, the cause of the
   * result failure will be a {@link FailureException}.
   *
   * @param data
   *   The message body.
   * @param ackHandler
   *   An asynchronous result handler to be called once the message has been
   *   fully processed, failed, or timed out.
   * @return
   *   The emitted message identifier.
   */
  public MessageId emit(JsonObject data, Handler<AsyncResult<MessageId>> ackHandler) {
    return feeder.emit(data, wrapHandler(ackHandler));
  }

  /**
   * Called when a message has been successfully processed.
   *
   * @param messageId
   *   The identifier of the message that was successfully processed.
   */
  protected void handleAck(MessageId messageId) {
  }

  /**
   * Called when a message has been explicitly failed by a worker.
   *
   * @param messageId
   *   The identifier of the message that was failed.
   */
  protected void handleFailure(MessageId messageId) {
  }

  /**
   * Called when a message times out.
   *
   * @param messageId
   *   The identifier of the message that timed out.
   */
  protected void handleTimeout(MessageId messageId) {
  }

}
