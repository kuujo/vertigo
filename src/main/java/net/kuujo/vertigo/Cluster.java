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
package net.kuujo.vertigo;

import net.kuujo.vertigo.context.NetworkContext;
import net.kuujo.vertigo.definition.NetworkDefinition;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;

/**
 * Primary interface for network deployment.
 *
 * @author Jordan Halterman
 */
public interface Cluster {

  /**
   * Deploys a network definition.
   *
   * @param network
   *   The network definition.
   */
  public void deploy(NetworkDefinition network);

  /**
   * Deploys a network definition.
   *
   * @param network
   *   The network definition.
   * @param doneHandler
   *   An asynchronous result handler to be invoked with a network context.
   */
  public void deploy(NetworkDefinition network, Handler<AsyncResult<NetworkContext>> doneHandler);

  /**
   * Shuts down the network at the given address.
   *
   * @param context
   *   The network context.
   */
  public void shutdown(NetworkContext context);

  /**
   * Shuts down the network at the given address, awaiting a result.
   *
   * @param context
   *   The network context.
   * @param doneHandler
   *   An asynchronous result handler to be invoked once the shutdown is complete.
   */
  public void shutdown(NetworkContext context, Handler<AsyncResult<Void>> doneHandler);

}
