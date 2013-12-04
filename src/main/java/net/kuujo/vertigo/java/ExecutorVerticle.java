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
import net.kuujo.vertigo.message.JsonMessage;
import net.kuujo.vertigo.message.MessageId;
import net.kuujo.vertigo.rpc.BasicExecutor;
import net.kuujo.vertigo.rpc.Executor;

/**
 * An executor verticle implementation.
 *
 * @author Jordan Halterman
 */
public abstract class ExecutorVerticle extends VertigoVerticle<Executor> {
  protected Executor executor;

  @Override
  protected Executor createComponent(InstanceContext context) {
    return new BasicExecutor(vertx, container, context);
  }

  @Override
  protected void start(Executor executor) {
    this.executor = executor;
    executor.executeHandler(new Handler<Executor>() {
      @Override
      public void handle(Executor executor) {
        nextMessage();
      }
    });
  }

  /**
   * Called when the executor is requesting the next message.
   *
   * Override this method to perform polling-based executions. The executor will automatically
   * call this method any time the execute queue is prepared to accept new messages.
   */
  protected void nextMessage() {
  }

  /**
   * Performs an execution.
   *
   * @param args
   *   The execution arguments.
   * @param resultHandler
   *   An asynchronous handler to be called with the execution result.
   * @return
   *   The execution's unique message identifier.
   */
  public MessageId execute(JsonObject args, Handler<AsyncResult<JsonMessage>> resultHandler) {
    return executor.execute(args, resultHandler);
  }

  /**
   * Performs an execution.
   *
   * The asynchronous result supplied to the result handler is a special
   * {@link AsyncResult} implementation that *always* provides a {@link JsonMessage}
   * instance. If the execution fails or times out, the {@link JsonMessage} that is
   * provided will be the original executing message.
   *
   * @param args
   *   The execution arguments.
   * @param tag
   *   A tag to apply to the execution.
   * @param resultHandler
   *   An asynchronous handler to be called with the execution result.
   * @return
   *   The execution's unique message identifier.
   */
  public MessageId execute(JsonObject args, String tag, Handler<AsyncResult<JsonMessage>> resultHandler) {
    return executor.execute(args, tag, resultHandler);
  }

}
