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
package net.kuujo.vitis.feeder;

import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;

import net.kuujo.vitis.context.VineContext;
import net.kuujo.vitis.eventbus.TimeoutException;
import net.kuujo.vitis.messaging.DefaultJsonMessage;
import net.kuujo.vitis.messaging.JsonMessage;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.impl.DefaultFutureResult;
import org.vertx.java.core.json.JsonObject;

/**
 * A basic executor implementation.
 *
 * @author Jordan Halterman
 */
public class ReliableExecutor extends AbstractExecutor implements Executor {

  private Vertx vertx;

  private String uniqueId;

  private long messageCount;

  private long feedQueueSize = 10000;

  private boolean queueFull;

  private Handler<Void> drainHandler;

  private Map<String, Handler<AsyncResult<JsonObject>>> awaitingResponse = new HashMap<String, Handler<AsyncResult<JsonObject>>>();

  private Map<String, ArrayList<JsonObject>> responses = new HashMap<String, ArrayList<JsonObject>>();

  public ReliableExecutor(VineContext context, Vertx vertx) {
    super(context, vertx);
    this.vertx = vertx;
    createUniqueId();
    init();
  }

  public ReliableExecutor(VineContext context, Vertx vertx, EventBus eventBus) {
    super(context, vertx, eventBus);
    this.vertx = vertx;
    createUniqueId();
    init();
  }

  private void createUniqueId() {
    uniqueId = UUID.randomUUID().toString();
  }

  @Override
  public Executor setFeedQueueMaxSize(long maxSize) {
    feedQueueSize = maxSize;
    return this;
  }

  @Override
  public boolean feedQueueFull() {
    return false;
  }

  /**
   * Returns the next unique ID for the executor.
   */
  private String nextId() {
    return String.format("%s.%d", uniqueId, ++messageCount);
  }

  @Override
  public Executor execute(final JsonObject args, final Handler<AsyncResult<JsonObject>> resultHandler) {
    return execute(args, 0, resultHandler);
  }

  @Override
  public Executor execute(JsonObject args, long timeout, Handler<AsyncResult<JsonObject>> resultHandler) {
    new RecursiveExecutor(args, timeout, resultHandler).execute();
    return this;
  }

  private class RecursiveExecutor {
    private JsonObject args;
    private Handler<AsyncResult<JsonObject>> resultHandler;
    private String currentId;
    private long timeout;
    private long timerId;
    private boolean timedOut;

    public RecursiveExecutor(JsonObject args, long timeout, Handler<AsyncResult<JsonObject>> resultHandler) {
      this.args = args;
      this.timeout = timeout;
      this.resultHandler = resultHandler;
    }

    public void execute() {
      // If there are old responses in the response list then remove them.
      if (currentId != null && responses.containsKey(currentId)) {
        responses.remove(currentId);
      }

      if (timedOut) {
        return;
      }
      if (timeout > 0 && timerId == 0) {
        createTimer();
      }

      final String currentId = nextId();
      JsonMessage message = DefaultJsonMessage.create(currentId, args);

      // Add the result handler to the awaitingResponse map. Note that if the
      // execution times out, the result handler will be removed from this map
      // and thus the handler will not be able to be executed again.
      awaitingResponse.put(currentId, new Handler<AsyncResult<JsonObject>>() {
        @Override
        public void handle(AsyncResult<JsonObject> result) {
          if (!timedOut && result.succeeded()) {
            // Store responses in a list until they are acked. Once the message
            // tree is completed, the responses will be sent to the resultHandler.
            ArrayList<JsonObject> responseList;
            if (responses.containsKey(currentId)) {
              responseList = responses.get(currentId);
            }
            else {
              responseList = new ArrayList<JsonObject>();
              responses.put(currentId, responseList);
            }

            JsonObject response = result.result();
            if (response != null) {
              responseList.add(result.result());
            }
          }
        }
      });

      checkQueue();

      // Once all messages in the tree have been processed, remove the future from
      // awaiting response.
      output.emit(message, new Handler<AsyncResult<Boolean>>() {
        @Override
        public void handle(AsyncResult<Boolean> result) {
          awaitingResponse.remove(currentId);
          checkQueue();
          if (result.failed()) {
            new DefaultFutureResult<JsonObject>().setHandler(resultHandler).setFailure(result.cause());
          }
          else if (!result.result()) {
            if (!timedOut) {
              execute();
            }
          }
          // If responses have been stored for this execution, invoke the result
          // handler with each response.
          else if (responses.containsKey(currentId)) {
            cancelTimer();
            ArrayList<JsonObject> responseList = responses.get(currentId);
            for (JsonObject response : responseList) {
              new DefaultFutureResult<JsonObject>().setHandler(resultHandler).setResult(response);
            }
            responses.remove(currentId);
          }
          // Otherwise, invoke the result handler with an empty response.
          else {
            new DefaultFutureResult<JsonObject>().setHandler(resultHandler).setResult(null);
          }
        }
      });
    }

    /**
     * Creates a new timeout timer.
     */
    private void createTimer() {
      if (timeout > 0) {
        timerId = vertx.setTimer(timeout, new Handler<Long>() {
          @Override
          public void handle(Long timerId) {
            if (!timedOut) {
              timedOut = true;
              awaitingResponse.remove(currentId);
              new DefaultFutureResult<JsonObject>().setHandler(resultHandler).setFailure(new TimeoutException("Execution timed out."));
            }
          }
        });
      }
    }

    /**
     * Cancels the current timeout timer.
     */
    private void cancelTimer() {
      if (timerId > 0) {
        vertx.cancelTimer(timerId);
      }
    }
  }

  @Override
  public Executor drainHandler(Handler<Void> handler) {
    this.drainHandler = handler;
    return this;
  }

  /**
   * Checks the feed queue status and pauses or unpauses the feeder
   * as necessary.
   */
  private void checkQueue() {
    if (!queueFull) {
      if (awaitingResponse.size() >= feedQueueSize) {
        pause();
      }
    }
    else {
      if (awaitingResponse.size() < feedQueueSize / 2) {
        unpause();
      }
    }
  }

  /**
   * Pauses the feeder.
   */
  private void pause() {
    queueFull = true;
  }

  /**
   * Unpauses the feeder.
   */
  private void unpause() {
    queueFull = false;
    if (drainHandler != null) {
      drainHandler.handle(null);
    }
  }

  @Override
  protected void outputReceived(Message<JsonObject> message) {
    JsonObject body = message.body();
    if (body == null) {
      return;
    }

    JsonMessage jsonMessage = new DefaultJsonMessage(body);
    String id = jsonMessage.tree();
    if (id == null) {
      return;
    }
    else if (awaitingResponse.containsKey(id)) {
      JsonObject messageBody = jsonMessage.body();
      if (messageBody != null) {
        new DefaultFutureResult<JsonObject>().setHandler(awaitingResponse.get(id)).setResult(messageBody);
      }
    }
  }

}
