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

import net.kuujo.vertigo.definition.NetworkDefinition;

import org.vertx.java.core.json.JsonObject;

/**
 * Static network methods.
 *
 * @author Jordan Halterman
 */
public class Networks {

  /**
   * Creates a new network definition.
   *
   * @return
   *   A network definition.
   */
  public static NetworkDefinition createNetwork() {
    return new NetworkDefinition();
  }

  /**
   * Creates a new network definition.
   *
   * @param address
   *   The network address.
   * @return
   *   A network definition.
   */
  public static NetworkDefinition createNetwork(String address) {
    return new NetworkDefinition(new JsonObject().putString("address", address));
  }

}
