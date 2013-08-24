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

/**
 * A component deployer.
 *
 * @author Jordan Halterman
 *
 * @param <T> The type being deployed.
 * @param <R> The asynchronous result type.
 */
public interface Deployer<T, R> {

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
   * Deploys a component.
   *
   * @param component
   *   The component to deploy.
   */
  public void deploy(T component);

  /**
   * Deploys a component.
   *
   * @param component
   *   The component to deploy.
   * @param doneHandler
   *   A handler to invoke once deployment is complete.
   */
  public void deploy(T component, Handler<AsyncResult<R>> doneHandler);

}
