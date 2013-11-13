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
package net.kuujo.vertigo.rpc;

import net.kuujo.vertigo.context.InstanceContext;
import net.kuujo.vertigo.message.JsonMessage;

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
  private Handler<JsonMessage> resultHandler;
  private Handler<String> failHandler;
  private static final long DEFAULT_EXECUTE_DELAY = 10;
  private long executeDelay = DEFAULT_EXECUTE_DELAY;
  private boolean executed;

  private Handler<JsonMessage> internalResultHandler = new Handler<JsonMessage>() {
    @Override
    public void handle(JsonMessage result) {
      if (resultHandler != null) {
        resultHandler.handle(result);
      }
    }
  };

  private Handler<String> internalFailHandler = new Handler<String>() {
    @Override
    public void handle(String messageId) {
      if (failHandler != null) {
        failHandler.handle(messageId);
      }
    }
  };

  public DefaultPollingExecutor(Vertx vertx, Container container, InstanceContext context) {
    super(vertx, container, context);
  }

  @Override
  public PollingExecutor start() {
    return super.start(new Handler<AsyncResult<PollingExecutor>>() {
      @Override
      public void handle(AsyncResult<PollingExecutor> result) {
        if (result.succeeded()) {
          recursiveExecute();
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
          future.setResult(result.result());
          recursiveExecute();
        }
      }
    });
  }

  /**
   * Recursively invokes the execute handler.
   * If the feed handler is invoked and no messages are executed from the handler,
   * a timer is set to restart the execution in the future.
   */
  private void recursiveExecute() {
    if (executeHandler != null) {
      executed = true;
      while (executed && !queueFull()) {
        executed = false;
        executeHandler.handle(this);
      }
    }

    vertx.setTimer(executeDelay, new Handler<Long>() {
      @Override
      public void handle(Long timerID) {
        recursiveExecute();
      }
    });
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
  public PollingExecutor resultHandler(Handler<JsonMessage> resultHandler) {
    this.resultHandler = resultHandler;
    return this;
  }

  @Override
  public PollingExecutor failHandler(Handler<String> failHandler) {
    this.failHandler = failHandler;
    return this;
  }

  @Override
  public String execute(JsonObject args) {
    executed = true;
    return doExecute(args, null, internalResultHandler, internalFailHandler);
  }

  @Override
  public String execute(JsonObject args, String tag) {
    executed = true;
    return doExecute(args, tag, internalResultHandler, internalFailHandler);
  }

}
