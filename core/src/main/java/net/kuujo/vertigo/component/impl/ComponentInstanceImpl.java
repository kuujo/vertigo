/*
 * Copyright 2014 the original author or authors.
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
package net.kuujo.vertigo.component.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.eventbus.ReplyFailure;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.impl.LoggerFactory;
import net.kuujo.vertigo.component.ComponentContext;
import net.kuujo.vertigo.component.ComponentInstance;
import net.kuujo.vertigo.io.InputCollector;
import net.kuujo.vertigo.io.OutputCollector;
import net.kuujo.vertigo.io.impl.InputCollectorImpl;
import net.kuujo.vertigo.io.impl.OutputCollectorImpl;
import net.kuujo.vertigo.util.CountingCompletionHandler;

/**
 * Component partition implementation.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class ComponentInstanceImpl implements ComponentInstance, Handler<Message<Object>> {
  private static final String ACTION_HEADER = "action";
  private static final String MESSAGE_ACTION = "message";
  private static final String ACK_ACTION = "ack";
  private static final String FAIL_ACTION = "fail";
  private static final String PAUSE_ACTION = "pause";
  private static final String RESUME_ACTION = "resume";
  private final Vertx vertx;
  private final ComponentContext context;
  private final InputCollectorImpl input;
  private final OutputCollectorImpl output;
  private final Logger logger;
  private JsonObject state;
  private Handler<JsonObject> checkpointHandler;
  private Handler<JsonObject> recoverHandler;
  private MessageConsumer<Object> consumer;

  public ComponentInstanceImpl(Vertx vertx, ComponentContext context) {
    this.vertx = vertx;
    this.context = context;
    this.input = new InputCollectorImpl(vertx, context.input());
    this.output = new OutputCollectorImpl(vertx, context.output());
    this.logger = LoggerFactory.getLogger(String.format("%s-%s", ComponentInstance.class.getName(), context.address()));
  }

  @Override
  public Vertx vertx() {
    return vertx;
  }

  @Override
  public ComponentContext context() {
    return context;
  }

  @Override
  public InputCollector input() {
    return input;
  }

  @Override
  public OutputCollector output() {
    return output;
  }

  @Override
  public Logger logger() {
    return logger;
  }

  @Override
  public void handle(Message<Object> message) {
    String action = message.headers().get(ACTION_HEADER);
    if (action == null) {
      input.handle(message);
    } else {
      switch (action) {
        case MESSAGE_ACTION:
          input.handle(message);
          break;
        case ACK_ACTION:
        case FAIL_ACTION:
        case PAUSE_ACTION:
        case RESUME_ACTION:
          output.handle(message);
          break;
        default:
          message.fail(ReplyFailure.RECIPIENT_FAILURE.toInt(), String.format("Invalid action %s", action));
      }
    }
  }

  @Override
  public JsonObject state() {
    return state;
  }

  @Override
  public ComponentInstance checkpoint(Handler<JsonObject> handler) {
    checkpointHandler = handler;
    return this;
  }

  @Override
  public ComponentInstance recover(Handler<JsonObject> handler) {
    recoverHandler = handler;
    return this;
  }

  @Override
  public ComponentInstance start() {
    return start(null);
  }

  @Override
  public ComponentInstance start(Handler<AsyncResult<Void>> doneHandler) {
    if (consumer == null) {
      consumer = vertx.eventBus().consumer(context.address());
      consumer.handler(this);
      consumer.completionHandler(doneHandler);
    } else {
      Future.<Void>completedFuture().setHandler(doneHandler);
    }
    return this;
  }

  @Override
  public void stop() {
    stop(null);
  }

  @Override
  public void stop(Handler<AsyncResult<Void>> doneHandler) {
    if (consumer != null) {
      consumer.unregister(doneHandler);
    } else {
      Future.<Void>completedFuture().setHandler(doneHandler);
    }
  }

}
