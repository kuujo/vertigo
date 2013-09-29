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

/**
 * A reliable feeder.
 *
 * @author Jordan Halterman
 *
 * @param <T> The feeder type
 */
public interface ReliableFeeder<T extends ReliableFeeder<?>> extends BasicFeeder<T> {

  /**
   * Sets the feeder auto-retry option.
   *
   * @param retry
   *   Indicates whether to automatically retry emitting failed data.
   * @return
   *   The called feeder instance.
   */
  public T autoRetry(boolean retry);

  /**
   * Gets the feeder auto-retry option.
   *
   * @return
   *   Indicates whether the feeder with automatically retry emitting failed data.
   */
  public boolean autoRetry();

  /**
   * Sets the number of automatic retry attempts for a single failed message.
   *
   * @param attempts
   *   The number of retry attempts allowed. If attempts is -1 then an infinite
   *   number of retry attempts will be allowed.
   * @return
   *   The called feeder instance.
   */
  public T retryAttempts(int attempts);

  /**
   * Gets the number of automatic retry attempts.
   *
   * @return
   *   Indicates the number of retry attempts allowed for the feeder.
   */
  public int retryAttempts();

}
