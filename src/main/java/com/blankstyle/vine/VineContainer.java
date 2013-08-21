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

import org.vertx.java.core.Handler;
import org.vertx.java.platform.Container;

/**
 * A Vine container.
 *
 * @author Jordan Halterman
 */
public interface VineContainer extends Container {

  /**
   * Deploys a vine.
   *
   * @param handler
   *   A handler to invoke with the deployed vine instance.
   */
  public void deployVine(Handler<Feeder> handler);

  /**
   * Undeploys a vine.
   *
   * @param doneHandler
   *   A handler to invoke when the vine is undeployed.
   */
  public void undeployVine(Handler<Void> doneHandler);

}
