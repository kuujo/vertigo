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
import net.kuujo.vitis.node.feeder.BasicEmbeddedStreamFeeder;
import net.kuujo.vitis.node.feeder.EmbeddedStreamFeeder;

import org.vertx.java.core.Handler;
import org.vertx.java.platform.Verticle;

/**
 * A basic embedded stream feeder verticle.
 *
 * @author Jordan Halterman
 */
public abstract class EmbeddedStreamFeederVerticle extends Verticle {

  private BasicEmbeddedStreamFeeder feeder;

  @Override
  public void start() {
    feeder = new BasicEmbeddedStreamFeeder(vertx, container, new WorkerContext(container.config()));
    feeder.feedHandler(new Handler<EmbeddedStreamFeeder>() {
      @Override
      public void handle(EmbeddedStreamFeeder feeder) {
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

    feeder.fullHandler(new Handler<Void>() {
      @Override
      public void handle(Void event) {
        full();
      }
    });

    feeder.drainHandler(new Handler<Void>() {
      @Override
      public void handle(Void event) {
        drain();
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
  protected abstract void feed(EmbeddedStreamFeeder feeder);

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

  /**
   * Called when the feed queue is full.
   */
  protected abstract void full();

  /**
   * Called when the feed queue is prepared to accept messages.
   */
  protected abstract void drain();

}
