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

import net.kuujo.vevent.definition.NetworkDefinition;

import org.vertx.java.core.json.JsonObject;

/**
 * Static vine methods.
 *
 * @author Jordan Halterman
 */
public class Networks {

  /**
   * Creates a new vine definition.
   *
   * @return
   *   A vine definition.
   */
  public static NetworkDefinition createDefinition() {
    return new NetworkDefinition();
  }

  /**
   * Creates a new vine definition.
   *
   * @param address
   *   The vine address.
   * @return
   *   A vine definition.
   */
  public static NetworkDefinition createDefinition(String address) {
    return new NetworkDefinition(new JsonObject().putString("address", address));
  }

}
