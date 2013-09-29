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
package net.kuujo.vitis.java.feeder;

import net.kuujo.vitis.context.WorkerContext;
import net.kuujo.vitis.messaging.JsonMessage;
import net.kuujo.vitis.node.feeder.ReliableEmbeddedFeeder;

import org.vertx.java.core.Handler;
import org.vertx.java.platform.Verticle;

/**
 * A reliable embedded feeder verticle.
 *
 * @author Jordan Halterman
 */
public abstract class ReliableEmbeddedFeederVerticle extends Verticle {

  private ReliableEmbeddedFeeder feeder;

  protected boolean autoRetry = true;

  protected int retryAttempts = -1;

  @Override
  public void start() {
    feeder = new ReliableEmbeddedFeeder(vertx, container, new WorkerContext(container.config()));
    feeder.autoRetry(autoRetry);
    feeder.retryAttempts(retryAttempts);

    feeder.feedHandler(new Handler<ReliableEmbeddedFeeder>() {
      @Override
      public void handle(ReliableEmbeddedFeeder feeder) {
        feed(feeder);
      }
    });

    feeder.ackHandler(new Handler<JsonMessage>() {
      @Override
      public void handle(JsonMessage message) {
        ack(message);
      }
    });

    feeder.failHandler(new Handler<JsonMessage>() {
      @Override
      public void handle(JsonMessage message) {
        fail(message);
      }
    });
    feeder.start();
  }

  /**
   * Feeds a single set of data.
   *
   * @param feeder
   *   The feeder with which to feed data.
   */
  protected abstract void feed(ReliableEmbeddedFeeder feeder);

  /**
   * Called when a message has been acked.
   *
   * @param message
   *   The message that was acked.
   */
  protected abstract void ack(JsonMessage message);

  /**
   * Called when a message has been failed.
   *
   * @param message
   *   The message that was failed.
   */
  protected abstract void fail(JsonMessage message);

}
