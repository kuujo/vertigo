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

import net.kuujo.vevent.definition.NodeDefinition;

/**
 * Static seed methods.
 *
 * @author Jordan Halterman
 */
public final class Nodes {

  /**
   * Creates a new seed definition.
   *
   * @return
   *   A new seed definition.
   */
  public static NodeDefinition createDefinition() {
    return new NodeDefinition();
  }

  /**
   * Creates a new seed definition.
   *
   * @param name
   *   The definition name.
   * @return
   *   A new seed definition.
   */
  public static NodeDefinition createDefinition(String name) {
    return new NodeDefinition().setName(name);
  }

  /**
   * Creates a new seed definition.
   *
   * @param name
   *   The definition name.
   * @param main
   *   The seed main.
   * @return
   *   A new seed definition.
   */
  public static NodeDefinition createDefinition(String name, String main) {
    return new NodeDefinition().setName(name).setMain(main);
  }

  /**
   * Creates a new seed definition.
   *
   * @param name
   *   The definition name.
   * @param main
   *   The seed main.
   * @param workers
   *   The number of seed workers.
   * @return
   *   A new seed definition.
   */
  public static NodeDefinition createDefinition(String name, String main, int workers) {
    return new NodeDefinition().setName(name).setMain(main).setWorkers(workers);
  }

}
