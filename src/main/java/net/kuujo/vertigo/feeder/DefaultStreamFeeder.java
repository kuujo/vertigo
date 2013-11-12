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
import net.kuujo.vertigo.runtime.FailureException;

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

  public DefaultStreamFeeder(Vertx vertx, Container container, InstanceContext context) {
    super(vertx, container, context);
  }

  private Handler<String> ackFailHandler = new Handler<String>() {
    @Override
    public void handle(String messageId) {
      checkPause();
    }
  };

  private Handler<String> createAckHandler(final Future<Void> future) {
    return new Handler<String>() {
      @Override
      public void handle(String messageId) {
        checkPause();
        future.setResult(null);
      }
    };
  }

  private Handler<String> createFailHandler(final Future<Void> future) {
    return new Handler<String>() {
      @Override
      public void handle(String messageId) {
        checkPause();
        future.setFailure(new FailureException("Processing failed."));
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
  public String emit(JsonObject data) {
    String id = doFeed(data, null, 0, ackFailHandler, ackFailHandler);
    checkPause();
    return id;
  }

  @Override
  public String emit(JsonObject data, String tag) {
    String id = doFeed(data, tag, 0, ackFailHandler, ackFailHandler);
    checkPause();
    return id;
  }

  @Override
  public String emit(JsonObject data, Handler<AsyncResult<Void>> ackHandler) {
    Future<Void> future = new DefaultFutureResult<Void>().setHandler(ackHandler);
    String id = doFeed(data, null, 0, createAckHandler(future), createFailHandler(future));
    checkPause();
    return id;
  }

  @Override
  public String emit(JsonObject data, String tag, Handler<AsyncResult<Void>> ackHandler) {
    Future<Void> future = new DefaultFutureResult<Void>().setHandler(ackHandler);
    String id = doFeed(data, tag, 0, createAckHandler(future), createFailHandler(future));
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
