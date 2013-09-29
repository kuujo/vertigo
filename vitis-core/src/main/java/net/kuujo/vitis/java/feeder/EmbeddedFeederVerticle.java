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
import net.kuujo.vitis.node.feeder.BasicEmbeddedFeeder;
import net.kuujo.vitis.node.feeder.EmbeddedFeeder;

import org.vertx.java.core.Handler;
import org.vertx.java.platform.Verticle;

/**
 * A basic embedded feeder verticle.
 *
 * @author Jordan Halterman
 */
public abstract class EmbeddedFeederVerticle extends Verticle {

  private BasicEmbeddedFeeder feeder;

  @Override
  public void start() {
    feeder = new BasicEmbeddedFeeder(vertx, container, new WorkerContext(container.config()));
    feeder.feedHandler(new Handler<EmbeddedFeeder>() {
      @Override
      public void handle(EmbeddedFeeder feeder) {
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
   *   The feeder to which to feed data.
   */
  protected abstract void feed(EmbeddedFeeder feeder);

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
