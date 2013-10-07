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
package net.kuujo.vertigo.component.executor;

import org.vertx.java.core.Handler;

/**
 * A polling executor.
 *
 * @author Jordan Halterman
 */
public interface PollingExecutor extends Executor<PollingExecutor> {

  /**
   * Sets the execute delay.
   *
   * @param delay
   *   The empty execute delay.
   * @return
   *   The called executor instance.
   */
  public PollingExecutor executeDelay(long delay);

  /**
   * Gets the execute delay.
   *
   * @return
   *   The empty execute delay.
   */
  public long executeDelay();

  /**
   * Sets a execute handler.
   *
   * @param handler
   *   A handler to be invoked for executing the network.
   * @return
   *   The called executor instance.
   */
  public PollingExecutor feedHandler(Handler<PollingExecutor> handler);

}
