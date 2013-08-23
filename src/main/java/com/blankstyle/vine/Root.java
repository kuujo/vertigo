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
import org.vertx.java.core.Vertx;
import org.vertx.java.platform.Container;

import com.blankstyle.vine.context.VineContext;

public interface Root {

  /**
   * Sets the root vertx instance.
   *
   * @param vertx
   *   A vertx instance.
   */
  public void setVertx(Vertx vertx);

  /**
   * Gets the root vertx instance.
   *
   * @return
   *   A vertx instance.
   */
  public Vertx getVertx();

  /**
   * Sets the root container instance.
   *
   * @param container
   *   A container instance.
   */
  public void setContainer(Container container);

  /**
   * Gets the root container instance.
   *
   * @return
   *   A container instance.
   */
  public Container getContainer();

  /**
   * Deploys a vine.
   *
   * @param context
   *   The vine context.
   * @param feedHandler
   *   A handler to invoke with a feeder to the vine.
   */
  public void deploy(VineContext context, Handler<AsyncResult<Feeder>> feedHandler);

  /**
   * Deploys a vine.
   *
   * @param name
   *   The vine name.
   * @param context
   *   The vine context.
   * @param feedHandler
   *   A handler to invoke with a feeder to the vine.
   */
  public void deploy(String name, VineContext context, Handler<AsyncResult<Feeder>> feedHandler);

}
