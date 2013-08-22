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

import com.blankstyle.vine.impl.DefaultVineContext;

/**
 * An abstract vine.
 *
 * @author Jordan Halterman
 */
public abstract class Vine {

  /**
   * Creates a new vine context.
   *
   * @return
   *   A new vine context.
   */
  public static VineContext createContext() {
    return new DefaultVineContext();
  }

  /**
   * Creates a new vine context.
   *
   * @param address
   *   The context address.
   * @return
   *   A new vine context.
   */
  public static VineContext createContext(String address) {
    return new DefaultVineContext().setAddress(address);
  }

  /**
   * Deploys the vine.
   */
  public abstract void deploy();

  /**
   * Deploys the vine.
   *
   * @param doneHandler
   *   A handler to be invoked once the vine has been deployed.
   */
  public abstract void deploy(Handler<AsyncResult<Void>> doneHandler);

}
