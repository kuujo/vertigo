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
package net.kuujo.vevent.node;

import net.kuujo.vevent.context.WorkerContext;
import net.kuujo.vevent.messaging.JsonMessage;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.platform.Container;

/**
 * A basic stream feeder implementation.
 *
 * @author Jordan Halterman
 */
public class BasicStreamFeeder extends BasicFeeder implements StreamFeeder {

  protected boolean paused;

  protected Handler<Void> drainHandler;

  public BasicStreamFeeder(Vertx vertx, Container container, WorkerContext context) {
    super(vertx, container, context);
  }

  @Override
  public void start() {
    super.start();
    paused = true;
    vertx.setTimer(1000, new Handler<Long>() {
      @Override
      public void handle(Long timerID) {
        paused = false;
      }
    });
  }

  @Override
  protected void createFuture(JsonMessage message, long timeout, Handler<AsyncResult<Void>> handler) {
    super.createFuture(message, timeout, handler);
    checkPause();
  }

  @Override
  protected void doAck(String id) {
    super.doAck(id);
    checkPause();
  }

  @Override
  protected void doFail(String id) {
    super.doFail(id);
    checkPause();
  }

  @Override
  public boolean feedQueueFull() {
    return paused;
  }

  @Override
  public StreamFeeder drainHandler(Handler<Void> handler) {
    this.drainHandler = handler;
    return this;
  }

  /**
   * Pauses the stream.
   */
  private void pause() {
    paused = true;
  }

  /**
   * Unpauses the stream.
   */
  private void unpause() {
    paused = false;
    if (drainHandler != null) {
      drainHandler.handle(null);
    }
  }

  /**
   * Checks the pause status of the stream.
   */
  private void checkPause() {
    if (paused) {
      if (futures.size() < maxQueueSize / 2) {
        unpause();
      }
    }
    else {
      if (futures.size() >= maxQueueSize) {
        pause();
      }
    }
  }

}
