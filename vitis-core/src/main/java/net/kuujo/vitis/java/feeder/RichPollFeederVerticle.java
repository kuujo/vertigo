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
import net.kuujo.vitis.node.feeder.RichPollFeeder;

import org.vertx.java.core.Handler;
import org.vertx.java.platform.Verticle;

/**
 * A rich poll feeder verticle.
 *
 * @author Jordan Halterman
 */
public abstract class RichPollFeederVerticle extends Verticle {

  private RichPollFeeder feeder;

  @Override
  public void start() {
    feeder = new RichPollFeeder(vertx, container, new WorkerContext(container.config()));
    feeder.feedHandler(new Handler<RichPollFeeder>() {
      @Override
      public void handle(RichPollFeeder feeder) {
        feed(feeder);
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
  protected abstract void feed(RichPollFeeder feeder);

}
