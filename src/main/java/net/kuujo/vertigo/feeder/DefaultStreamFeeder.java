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

import net.kuujo.vertigo.context.InstanceContext;
import net.kuujo.vertigo.message.MessageId;
import net.kuujo.vertigo.runtime.FailureException;
import net.kuujo.vertigo.runtime.TimeoutException;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Future;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.impl.DefaultFutureResult;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Container;

/**
 * A default stream feeder implementation.
 *
 * @author Jordan Halterman
 */
public class DefaultStreamFeeder extends AbstractFeeder<StreamFeeder> implements StreamFeeder {
  private Handler<Void> drainHandler;
  private boolean paused;

  private static final FailureException FAILURE_EXCEPTION = new FailureException("Processing failed.");
  static { FAILURE_EXCEPTION.setStackTrace(new StackTraceElement[0]); }

  private static final TimeoutException TIMEOUT_EXCEPTION = new TimeoutException("Processing timed out.");
  static { TIMEOUT_EXCEPTION.setStackTrace(new StackTraceElement[0]); }

  public DefaultStreamFeeder(Vertx vertx, Container container, InstanceContext context) {
    super(vertx, container, context);
  }

  private Handler<MessageId> ackFailTimeoutHandler = new Handler<MessageId>() {
    @Override
    public void handle(MessageId messageId) {
      checkPause();
    }
  };

  private Handler<MessageId> createAckHandler(final Future<Void> future) {
    return new Handler<MessageId>() {
      @Override
      public void handle(MessageId messageId) {
        checkPause();
        future.setResult(null);
      }
    };
  }

  private Handler<MessageId> createFailHandler(final Future<Void> future) {
    return new Handler<MessageId>() {
      @Override
      public void handle(MessageId messageId) {
        checkPause();
        future.setFailure(FAILURE_EXCEPTION);
      }
    };
  }

  private Handler<MessageId> createTimeoutHandler(final Future<Void> future) {
    return new Handler<MessageId>() {
      @Override
      public void handle(MessageId messageId) {
        checkPause();
        future.setFailure(TIMEOUT_EXCEPTION);
      }
    };
  }

  @Override
  public boolean queueFull() {
    return paused;
  }

  @Override
  public StreamFeeder drainHandler(Handler<Void> handler) {
    drainHandler = handler;
    return this;
  }

  @Override
  public MessageId emit(JsonObject data) {
    MessageId id = doFeed(data, null, ackFailTimeoutHandler, ackFailTimeoutHandler, ackFailTimeoutHandler);
    checkPause();
    return id;
  }

  @Override
  public MessageId emit(JsonObject data, String tag) {
    MessageId id = doFeed(data, tag, ackFailTimeoutHandler, ackFailTimeoutHandler, ackFailTimeoutHandler);
    checkPause();
    return id;
  }

  @Override
  public MessageId emit(JsonObject data, Handler<AsyncResult<Void>> ackHandler) {
    Future<Void> future = new DefaultFutureResult<Void>().setHandler(ackHandler);
    MessageId id = doFeed(data, null, createAckHandler(future), createFailHandler(future), createTimeoutHandler(future));
    checkPause();
    return id;
  }

  @Override
  public MessageId emit(JsonObject data, String tag, Handler<AsyncResult<Void>> ackHandler) {
    Future<Void> future = new DefaultFutureResult<Void>().setHandler(ackHandler);
    MessageId id = doFeed(data, tag, createAckHandler(future), createFailHandler(future), createTimeoutHandler(future));
    checkPause();
    return id;
  }

  /**
   * Checks the current stream pause status.
   */
  private void checkPause() {
    if (paused) {
      if (!queueFull()) {
        paused = false;
        if (drainHandler != null) {
          drainHandler.handle(null);
        }
      }
    }
    else if (queueFull()) {
      paused = true;
    }
  }

}
