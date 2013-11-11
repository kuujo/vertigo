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
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Container;

/**
 * A default stream executor implementation.
 *
 * @author Jordan Halterman
 */
public class DefaultStreamExecutor extends AbstractExecutor<StreamExecutor> implements StreamExecutor {
  private Handler<Void> fullHandler;
  private Handler<Void> drainHandler;
  private boolean paused;

  public DefaultStreamExecutor(Vertx vertx, Container container, InstanceContext context) {
    super(vertx, container, context);
  }

  @Override
  public boolean queueFull() {
    return paused;
  }

  @Override
  public StreamExecutor fullHandler(Handler<Void> handler) {
    fullHandler = handler;
    return this;
  }

  @Override
  public StreamExecutor drainHandler(Handler<Void> handler) {
    drainHandler = handler;
    return this;
  }

  @Override
  public String execute(JsonObject args, Handler<AsyncResult<JsonMessage>> resultHandler) {
    String id = doExecute(args, null, resultHandler);
    checkPause();
    return id;
  }

  @Override
  public String execute(JsonObject args, String tag, Handler<AsyncResult<JsonMessage>> resultHandler) {
    String id = doExecute(args, tag, resultHandler);
    checkPause();
    return id;
  }

  /**
   * Checks the current stream pause status.
   */
  private void checkPause() {
    if (paused) {
      if (queue.size() < queue.getMaxQueueSize() * .75) {
        paused = false;
        if (drainHandler != null) {
          drainHandler.handle(null);
        }
      }
    }
    else {
      if (queue.size() >= queue.getMaxQueueSize()) {
        paused = true;
        if (fullHandler != null) {
          fullHandler.handle(null);
        }
      }
    }
  }

}
