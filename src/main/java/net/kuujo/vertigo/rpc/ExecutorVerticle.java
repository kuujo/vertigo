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

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Future;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Verticle;

import net.kuujo.vertigo.Vertigo;
import net.kuujo.vertigo.context.InstanceContext;
import net.kuujo.vertigo.message.JsonMessage;

/**
 * A Java executor verticle.
 *
 * @author Jordan Halterman
 */
public abstract class ExecutorVerticle extends Verticle {
  private Vertigo vertigo;
  protected PollingExecutor executor;
  protected InstanceContext context;

  private Handler<JsonMessage> resultHandler = new Handler<JsonMessage>() {
    @Override
    public void handle(JsonMessage message) {
      handleResult(message);
    }
  };

  private Handler<String> failHandler = new Handler<String>() {
    @Override
    public void handle(String messageId) {
      handleFailure(messageId);
    }
  };

  @Override
  public void start(final Future<Void> future) {
    vertigo = new Vertigo(this);
    executor = vertigo.createPollingExecutor().resultHandler(resultHandler).failHandler(failHandler);
    context = executor.getContext();
    executor.start(new Handler<AsyncResult<PollingExecutor>>() {
      @Override
      public void handle(AsyncResult<PollingExecutor> result) {
        if (result.failed()) {
          future.setFailure(result.cause());
        }
        else {
          ExecutorVerticle.super.start(future);
        }
      }
    });
  }

  /**
   * Called when the executor is prepared to emit the next message.
   */
  protected abstract void nextMessage();

  /**
   * Executes the network by emitting a new message.
   *
   * @param data
   *   The message body.
   * @return
   *   The unique message identifier.
   */
  protected String execute(JsonObject data) {
    return executor.execute(data);
  }

  /**
   * Executes the network by emitting a new message.
   *
   * @param data
   *   The message body.
   * @param tag
   *   A tag to apply to the message.
   * @return
   *   The unique message identifier.
   */
  protected String execute(JsonObject data, String tag) {
    return executor.execute(data, tag);
  }

  /**
   * Called when an execution result is received.
   *
   * @param message
   *   The execution result.
   */
  protected abstract void handleResult(JsonMessage message);

  /**
   * Called when an execution failure is received.
   *
   * @param id
   *   The failed message identifier.
   */
  protected abstract void handleFailure(String id);

}
