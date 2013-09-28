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

import org.vertx.java.core.json.JsonObject;

/**
 * A basic feeder.
 *
 * @author Jordan Halterman
 *
 * @param <T> The feeder type
 */
public interface BasicFeeder<T extends BasicFeeder<?>> extends Feeder {

  /**
   * Sets the internal feed queue maximum size.
   *
   * @param maxSize
   *   The maximum queue size.
   * @return
   *   The called feeder instance.
   */
  public T setFeedQueueMaxSize(long maxSize);

  /**
   * Feeds data to the network.
   *
   * @param data
   *   The data to feed.
   * @return
   *   The called feeder instance.
   */
  public T feed(JsonObject data);

  /**
   * Feeds data to the network.
   *
   * @param data
   *   The data to feed.
   * @param tag
   *   A tag to apply to the data.
   * @return
   *   The called feeder instance.
   */
  public T feed(JsonObject data, String tag);

}
