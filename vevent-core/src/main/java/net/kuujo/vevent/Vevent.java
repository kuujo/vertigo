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
package net.kuujo.vevent;

import net.kuujo.vevent.context.NetworkContext;
import net.kuujo.vevent.definition.VineDefinition;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;

/**
 * Primary interface for network deployment.
 *
 * @author Jordan Halterman
 */
public interface Vevent {

  /**
   * Sets the root vertx instance.
   *
   * @param vertx
   *   A vertx instance.
   * @return
   *   The called root instance.
   */
  public Vevent setVertx(Vertx vertx);

  /**
   * Gets the root vertx instance.
   *
   * @return
   *   The root vertx instance.
   */
  public Vertx getVertx();

  /**
   * Deploys a vine definition.
   *
   * @param vine
   *   The vine definition.
   * @param handler
   *   An asynchronous result handler to be invoked with a vine feeder.
   */
  public void deploy(VineDefinition vine, Handler<AsyncResult<NetworkContext>> handler);

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
  public void deploy(VineDefinition vine, long timeout, Handler<AsyncResult<NetworkContext>> handler);

  /**
   * Shuts down the vine at the given address.
   *
   * @param context
   *   The vine context.
   */
  public void shutdown(NetworkContext context);

  /**
   * Shuts down the vine at the given address, awaiting a result.
   *
   * @param context
   *   The vine context.
   * @param doneHandler
   *   An asynchronous result handler to be invoked once the shutdown is complete.
   */
  public void shutdown(NetworkContext context, Handler<AsyncResult<Void>> doneHandler);

  /**
   * Shuts down the vine at the given address, awaiting a result.
   *
   * @param context
   *   The vine context.
   * @param timeout
   *   A shutdown timeout.
   * @param doneHandler
   *   An asynchronous result handler to be invoked once the shutdown is complete.
   */
  public void shutdown(NetworkContext context, long timeout, Handler<AsyncResult<Void>> doneHandler);

}
