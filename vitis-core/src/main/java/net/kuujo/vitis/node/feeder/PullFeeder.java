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

import org.vertx.java.core.Handler;

/**
 * A pull feeder.
 *
 * @author Jordan Halterman
 *
 * @param <T> The feeder type
 */
public interface PullFeeder<T extends PullFeeder<?>> extends BasicFeeder<T> {

  /**
   * Sets a feed handler.
   *
   * @param handler
   *   A handler to be invoked for feeding data to the network.
   * @return
   *   The called feeder instance.
   */
  public T feedHandler(Handler<T> handler);

}
