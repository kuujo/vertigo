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

import net.kuujo.vevent.definition.ComponentDefinition;

/**
 * Static node methods.
 *
 * @author Jordan Halterman
 */
public final class Nodes {

  /**
   * Creates a new node definition.
   *
   * @return
   *   A new node definition.
   */
  public static ComponentDefinition createDefinition() {
    return new ComponentDefinition();
  }

  /**
   * Creates a new node definition.
   *
   * @param name
   *   The definition name.
   * @return
   *   A new node definition.
   */
  public static ComponentDefinition createDefinition(String name) {
    return new ComponentDefinition().setName(name);
  }

  /**
   * Creates a new node definition.
   *
   * @param name
   *   The definition name.
   * @param main
   *   The node main.
   * @return
   *   A new node definition.
   */
  public static ComponentDefinition createDefinition(String name, String main) {
    return new ComponentDefinition().setName(name).setMain(main);
  }

  /**
   * Creates a new node definition.
   *
   * @param name
   *   The definition name.
   * @param main
   *   The node main.
   * @param workers
   *   The number of node workers.
   * @return
   *   A new node definition.
   */
  public static ComponentDefinition createDefinition(String name, String main, int workers) {
    return new ComponentDefinition().setName(name).setMain(main).setWorkers(workers);
  }

}
