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
package net.kuujo.vertigo.component.executor;

import net.kuujo.vertigo.context.ComponentContext;
import net.kuujo.vertigo.messaging.JsonMessage;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Future;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.impl.DefaultFutureResult;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Container;

/**
 * A default polling executor implementation.
 *
 * @author Jordan Halterman
 */
public class DefaultPollingExecutor extends AbstractExecutor<PollingExecutor> implements PollingExecutor {
  private Handler<PollingExecutor> executeHandler;
  private long executeDelay = 100;
  private boolean executed;

  public DefaultPollingExecutor(Vertx vertx, Container container, ComponentContext context) {
    super(vertx, container, context);
  }

  @Override
  public PollingExecutor start() {
    return super.start(new Handler<AsyncResult<PollingExecutor>>() {
      @Override
      public void handle(AsyncResult<PollingExecutor> result) {
        if (result.succeeded()) {
          recursiveFeed();
        }
      }
    });
  }

  @Override
  public PollingExecutor start(Handler<AsyncResult<PollingExecutor>> doneHandler) {
    final Future<PollingExecutor> future = new DefaultFutureResult<PollingExecutor>().setHandler(doneHandler);
    return super.start(new Handler<AsyncResult<PollingExecutor>>() {
      @Override
      public void handle(AsyncResult<PollingExecutor> result) {
        if (result.failed()) {
          future.setFailure(result.cause());
        }
        else {
          recursiveFeed();
          future.setResult(result.result());
        }
      }
    });
  }

  /**
   * Schedules a feed.
   */
  private void scheduleFeed() {
    vertx.setTimer(executeDelay, new Handler<Long>() {
      @Override
      public void handle(Long timerID) {
        recursiveFeed();
      }
    });
  }

  /**
   * Recursively invokes the feed handler.
   * If the feed handler is invoked and no messages are fed from the handler,
   * a timer is set to restart the feed in the future.
   */
  private void recursiveFeed() {
    executed = true;
    while (executed && !queue.full()) {
      executed = false;
      doFeed();
    }
    scheduleFeed();
  }

  /**
   * Invokes the feed handler.
   */
  private void doFeed() {
    if (executeHandler != null) {
      executeHandler.handle(this);
    }
  }

  @Override
  public PollingExecutor setExecuteDelay(long delay) {
    executeDelay = delay;
    return this;
  }

  @Override
  public long getExecuteDelay() {
    return executeDelay;
  }

  @Override
  public PollingExecutor executeHandler(Handler<PollingExecutor> handler) {
    executeHandler = handler;
    return this;
  }

  @Override
  public PollingExecutor execute(JsonObject args, Handler<AsyncResult<JsonMessage>> resultHandler) {
    executed = true;
    doExecute(args, null, resultHandler);
    return this;
  }

  @Override
  public PollingExecutor execute(JsonObject args, String tag, Handler<AsyncResult<JsonMessage>> resultHandler) {
    executed = true;
    doExecute(args, tag, resultHandler);
    return this;
  }

}
