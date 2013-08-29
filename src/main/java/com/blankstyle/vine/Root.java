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

import com.blankstyle.vine.definition.MalformedDefinitionException;
import com.blankstyle.vine.definition.VineDefinition;

/**
 * A vine root.
 *
 * @author Jordan Halterman
 */
public interface Root {

  /**
   * Sets the root address.
   *
   * @param address
   *   The root address.
   * @return
   *   The called root instance.
   */
  public Root setAddress(String address);

  /**
   * Gets the root address.
   *
   * @return
   *   The root address.
   */
  public String getAddress();

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
   * @param doneHandler
   *   An asyncronous result handler to be invoked with a feeder to
   *   the vine.
   */
  public void deploy(VineDefinition vine, Handler<AsyncResult<Feeder>> doneHandler) throws MalformedDefinitionException;

}
