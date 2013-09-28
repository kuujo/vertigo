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
package net.kuujo.vitis.node.feeder;

import java.util.HashMap;
import java.util.Map;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Future;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.impl.DefaultFutureResult;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Container;

import net.kuujo.vitis.context.WorkerContext;
import net.kuujo.vitis.messaging.JsonMessage;
import net.kuujo.vitis.node.FailureException;
import net.kuujo.vitis.node.NodeBase;
import net.kuujo.vitis.node.TimeoutException;

/**
 * An abstract feeder.
 *
 * @author Jordan Halterman
 */
abstract class AbstractFeeder extends NodeBase {

  protected static final long DEFAULT_TIMEOUT = 30000;

  protected Map<String, FutureResult> futures = new HashMap<>();

  protected AbstractFeeder(Vertx vertx, Container container, WorkerContext context) {
    super(vertx, container, context);
  }

  @Override
  protected void doAck(String id) {
    if (futures.containsKey(id)) {
      futures.get(id).set();
    }
  }

  @Override
  protected void doFail(String id) {
    if (futures.containsKey(id)) {
      futures.get(id).fail(new FailureException("Processing failed."));
    }
  }

  /**
   * Creates and stores a message future.
   */
  protected void createFuture(JsonMessage message, long timeout, Handler<AsyncResult<Void>> handler) {
    eventBus.send(authAddress, new JsonObject().putString("action", "create").putString("id", message.id()));
    FutureResult future = new FutureResult(message.id(), new DefaultFutureResult<Void>().setHandler(handler));
    future.start(timeout);
  }

  /**
   * Manages timers for future ack/fail results.
   */
  protected class FutureResult {
    private String id;
    private Future<Void> future;
    private long timerId;

    public FutureResult(String id, Future<Void> future) {
      this.id = id;
      this.future = future;
    }

    /**
     * Starts the future timer with the given timeout.
     */
    public FutureResult start(long timeout) {
      futures.put(id, this);
      timerId = vertx.setTimer(timeout, new Handler<Long>() {
        @Override
        public void handle(Long event) {
          futures.remove(id);
          future.setFailure(new TimeoutException("Feed timed out."));
        }
      });
      return this;
    }

    /**
     * Cancels the future timer.
     */
    public FutureResult cancel() {
      if (timerId > 0) {
        vertx.cancelTimer(timerId);
      }
      return this;
    }

    /**
     * Sets the future result, indicating success.
     */
    public void set() {
      cancel();
      futures.remove(id);
      future.setResult(null);
    }

    /**
     * Sets the future failure.
     */
    public void fail(Throwable e) {
      cancel();
      futures.remove(id);
      future.setFailure(e);
    }
  }

}
