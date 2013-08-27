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
package com.blankstyle.vine.eventbus.vine;

import java.util.HashMap;
import java.util.Map;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Future;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.impl.DefaultFutureResult;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;

import com.blankstyle.vine.context.JsonVineContext;
import com.blankstyle.vine.context.VineContext;
import com.blankstyle.vine.eventbus.Action;
import com.blankstyle.vine.eventbus.Argument;
import com.blankstyle.vine.eventbus.ArgumentsDefinition;
import com.blankstyle.vine.eventbus.AsynchronousAction;
import com.blankstyle.vine.eventbus.CommandDispatcher;
import com.blankstyle.vine.eventbus.DefaultCommandDispatcher;
import com.blankstyle.vine.eventbus.JsonCommand;
import com.blankstyle.vine.eventbus.ReliableBusVerticle;
import com.blankstyle.vine.eventbus.ReliableEventBus;

/**
 * A vine verticle.
 *
 * The Vine verticle is the primary entry point for communicating with a vine.
 * It listens for messages on a unique address, dispatches incoming messaages to
 * the first worker, and tracks messages for completion. If a message fails to
 * be processed through the vine in a sufficient amount of time the message will
 * be resent down the vine.
 *
 * Note that the vine does *not* monitor the status of worker processes. The root
 * (in local mode) or the stem (in remote mode) will monitor the status of worker
 * processes and start or stop them as necessary.
 *
 * @author Jordan Halterman
 */
public class VineVerticle extends ReliableBusVerticle implements Handler<Message<JsonObject>> {

  private VineContext context;

  private Logger log;

  private CommandDispatcher dispatcher = new DefaultCommandDispatcher() {{
    registerAction(Start.NAME, Start.class);
    registerAction(Finish.NAME, Finish.class);
  }};

  /**
   * The message process time expiration.
   */
  private long messageExpiration;

  /**
   * The current message correlation ID.
   */
  private long currentID;

  /**
   * A map of correlation IDs to message objects.
   */
  private Map<Long, VineMessage> inProcess = new HashMap<Long, VineMessage>();

  @Override
  protected void start(ReliableEventBus eventBus) {
    log = container.logger();
    context = new JsonVineContext(container.config());
    dispatcher.setVertx(vertx);
    dispatcher.setEventBus(vertx.eventBus());
    dispatcher.setContext(context);
    messageExpiration = context.getDefinition().getMessageExpiration();
    eventBus.registerHandler(context.getAddress(), this);
  }

  @Override
  public void handle(final Message<JsonObject> message) {
    dispatcher.dispatch(new JsonCommand(message.body()), new Handler<AsyncResult<Object>>() {
      @Override
      public void handle(AsyncResult<Object> result) {
        if (result.succeeded()) {
          message.reply(result.result());
        }
        else {
          message.reply(result.cause());
        }
      }
    });
  }

  /**
   * Returns the next message correlation ID.
   *
   * @return
   *   A correlation ID.
   */
  private long nextCorrelationID() {
    return ++currentID;
  }

  /**
   * Tags a message in preparation for processing.
   *
   * @param message
   *   The message to tag.
   * @param resultHandler
   *   A handler to be invoked with the vine result.
   * @return
   *   The tagged message.
   */
  private JsonObject tagMessage(JsonObject message, final Handler<AsyncResult<Object>> resultHandler) {
    Future<Object> future = new DefaultFutureResult<Object>().setHandler(resultHandler);

    final VineMessage vineMessage = new VineMessage(message);
    vineMessage.setCorrelationID(nextCorrelationID());
    vineMessage.setFutureResult(future);

    // Add the VineMessage to the correlation identifier map.
    inProcess.put(vineMessage.getCorrelationID(), vineMessage);

    // Set a timer that will be triggered if the message isn't processed before
    // the indicated time. If this is the case then the message will be re-sent.
    // Once the message completes, this timer will be cancelled.
    vineMessage.setTimerID(vertx.setTimer(messageExpiration, new Handler<Long>() {
      @Override
      public void handle(Long event) {
        inProcess.remove(vineMessage.getCorrelationID());
        dispatchMessage(vineMessage.getMessage(), resultHandler);
      }
    }));
    return message;
  }

  /**
   * Dispatches a message.
   *
   * @param message
   *   The message to dispatch.
   */
  private void dispatchMessage(JsonObject message, Handler<AsyncResult<Object>> resultHandler) {
    message = tagMessage(message, resultHandler);
  }

  /**
   * Receives a completed message.
   *
   * @param message
   *   The completed message.
   */
  private void receiveMessage(JsonObject message, Handler<AsyncResult<Object>> resultHandler) {
    long correlationID = message.getLong("correlation_id");
    if (correlationID == 0) {
      log.warn("Invalid correlation identifier found.");
    }
    else {
      // Get the VineMessage from the correlation identifier map.
      VineMessage vineMessage = inProcess.get(correlationID);
      inProcess.remove(correlationID);

      // Cancel the re-send timer.
      vertx.cancelTimer(vineMessage.getTimerID());

      // Invoke the VineMessage future with the message result.
      vineMessage.getFutureResult().setResult(message);
    }
    // Invoke the eventbus result handler to indicate we received the message.
    new DefaultFutureResult<Object>().setHandler(resultHandler).setResult(null);
  }

  /**
   * A Vine message.
   */
  private class VineMessage {
    private long timerID;
    private Future<Object> futureResult;
    private JsonObject message;

    public VineMessage(JsonObject message) {
      this.message = message;
    }

    public VineMessage setCorrelationID(long correlationID) {
      message.putNumber("correlation_id", correlationID);
      return this;
    }

    public long getCorrelationID() {
      return message.getLong("correlation_id");
    }

    public VineMessage setTimerID(long timerID) {
      this.timerID = timerID;
      return this;
    }

    public long getTimerID() {
      return timerID;
    }

    public VineMessage setFutureResult(Future<Object> futureResult) {
      this.futureResult = futureResult;
      return this;
    }

    public Future<Object> getFutureResult() {
      return futureResult;
    }

    public JsonObject getMessage() {
      return message;
    }
  }

  /**
   * A vine verticle process action.
   *
   * @author Jordan Halterman
   */
  public class Start extends Action<VineContext> implements AsynchronousAction<Object> {

    public static final String NAME = "process";

    private ArgumentsDefinition args = new ArgumentsDefinition() {{
      addArgument(new Argument<JsonObject>() {
        @Override
        public String name() {
          return "message";
        }
        @Override
        public boolean isValid(JsonObject value) {
          return value instanceof JsonObject;
        }
      });
    }};

    public Start(Vertx vertx, ReliableEventBus eventBus, VineContext context) {
      super(vertx, eventBus, context);
    }

    @Override
    public ArgumentsDefinition getArgumentsDefinition() {
      return args;
    }

    @Override
    public void execute(Object[] args, Handler<AsyncResult<Object>> resultHandler) {
      dispatchMessage((JsonObject) args[0], resultHandler);
    }

  }

  /**
   * A vine verticle process action.
   *
   * @author Jordan Halterman
   */
  public class Finish extends Action<VineContext> implements AsynchronousAction<Void> {

    public static final String NAME = "complete";

    private ArgumentsDefinition args = new ArgumentsDefinition() {{
      addArgument(new Argument<JsonObject>() {
        @Override
        public String name() {
          return "message";
        }
        @Override
        public boolean isValid(JsonObject value) {
          return value instanceof JsonObject;
        }
      });
    }};

    public Finish(Vertx vertx, ReliableEventBus eventBus, VineContext context) {
      super(vertx, eventBus, context);
    }

    @Override
    public ArgumentsDefinition getArgumentsDefinition() {
      return args;
    }

    @Override
    public void execute(Object[] args, Handler<AsyncResult<Object>> resultHandler) {
      receiveMessage((JsonObject) args[0], resultHandler);
    }

  }

}
