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
package net.kuujo.vitis.node.executor;

import org.vertx.java.core.Handler;

/**
 * A stream executor.
 *
 * @author Jordan Halterman
 */
public interface StreamExecutor extends Executor<StreamExecutor> {

  /**
   * Sets a connect handler on the executor.
   *
   * @param handler
   *   A handler to be invoked when the executor is connected.
   * @return
   *   The called executor instance.
   */
  public StreamExecutor connectHandler(Handler<StreamExecutor> handler);

  /**
   * Sets a full handler on the executor.
   *
   * @param handler
   *   A handler to be invoked when the execution queue is full.
   * @return
   *   The called executor instance.
   */
  public StreamExecutor fullHandler(Handler<Void> handler);

  /**
   * Sets a drain handler on the executor.
   *
   * @param handler
   *   A handler to be invoked when a full execution queue is emptied.
   * @return
   *   The called executor instance.
   */
  public StreamExecutor drainHandler(Handler<Void> handler);

}
