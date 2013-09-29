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

import org.vertx.java.platform.Verticle;

import net.kuujo.vitis.context.WorkerContext;
import net.kuujo.vitis.node.feeder.BasicStreamFeeder;
import net.kuujo.vitis.node.feeder.StreamFeeder;

/**
 * A basic stream feeder verticle.
 *
 * @author Jordan Halterman
 */
public abstract class StreamFeederVerticle extends Verticle {

  BasicStreamFeeder feeder;

  @Override
  public void start() {
    feeder = new BasicStreamFeeder(vertx, container, new WorkerContext(container.config()));
    feeder.start();
    feed(feeder);
  }

  /**
   * Begins feeding data.
   *
   * @param feeder
   *   The feeder with which to feed data.
   */
  protected abstract void feed(StreamFeeder feeder);

}
