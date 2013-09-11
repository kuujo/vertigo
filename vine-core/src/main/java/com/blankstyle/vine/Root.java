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

import com.blankstyle.vine.definition.VineDefinition;

/**
 * A vine root.
 *
 * @author Jordan Halterman
 */
public interface Root {

  /**
   * Sets the root vertx instance.
   *
   * @param vertx
   *   A vertx instance.
   * @return
   *   The called root instance.
   */
  public Root setVertx(Vertx vertx);

  /**
   * Gets the root vertx instance.
   *
   * @return
   *   The root vertx instance.
   */
  public Vertx getVertx();

  /**
   * Sets the root container instance.
   *
   * @param container
   *   A container instance.
   * @return
   *   The called root instance.
   */
  public Root setContainer(Container container);

  /**
   * Gets the root container instance.
   *
   * @return
   *   The root container instance.
   */
  public Container getContainer();

  /**
   * Deploys a vine definition.
   *
   * @param vine
   *   The vine definition.
   * @param handler
   *   An asynchronous result handler to be invoked with a vine feeder.
   */
  public void deploy(VineDefinition vine, Handler<AsyncResult<Feeder>> handler);

  /**
   * Deploys a vine definition with a timeout.
   *
   * @param vine
   *   The vine definition.
   * @param timeout
   *   The deploy timeout.
   * @param handler
   *   An asynchronous result handler to be invoked with a vine feeder.
   */
  public void deploy(VineDefinition vine, long timeout, Handler<AsyncResult<Feeder>> handler);

  /**
   * Shuts down the vine at the given address.
   *
   * @param address
   *   The vine address.
   */
  public void shutdown(String address);

  /**
   * Shuts down the vine at the given address, awaiting a result.
   *
   * @param address
   *   The vine address.
   * @param doneHandler
   *   An asynchronous result handler to be invoked once the shutdown is complete.
   */
  public void shutdown(String address, Handler<AsyncResult<Void>> doneHandler);

  /**
   * Shuts down the vine at the given address, awaiting a result.
   *
   * @param address
   *   The vine address.
   * @param timeout
   *   A shutdown timeout.
   * @param doneHandler
   *   An asynchronous result handler to be invoked once the shutdown is complete.
   */
  public void shutdown(String address, long timeout, Handler<AsyncResult<Void>> doneHandler);

}
