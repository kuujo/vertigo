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
 * An embedded stream feeder.
 *
 * @author Jordan Halterman
 *
 * @param <T> The feeder type
 */
public interface EmbeddedStreamFeeder<T extends EmbeddedStreamFeeder<?>> extends EmbeddedFeeder<T>, StreamFeeder<T> {

  /**
   * Sets a full handler on the feeder.
   *
   * @param fullHandler
   *   A handler to be invoked when the feeder is full.
   * @return
   *   The called feeder instance.
   */
  public T fullHandler(Handler<Void> fullHandler);

}
