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
package com.blankstyle.vine;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;

/**
 * An abstract vine.
 *
 * @author Jordan Halterman
 */
public interface Vine {

  /**
   * Returns a feeder to the vine.
   *
   * @return
   *   A feeder to the vine.
   */
  public Feeder feeder();

  /**
   * Shuts down the vine.
   */
  public void shutdown();

  /**
   * Shuts down the vine.
   *
   * @param doneHandler
   *   An asynchronous handler to be invoked once the shutdown is complete.
   */
  public void shutdown(Handler<AsyncResult<Void>> doneHandler);

}
