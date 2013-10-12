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

import net.kuujo.vertigo.grouping.AllGrouping;
import net.kuujo.vertigo.grouping.FieldsGrouping;
import net.kuujo.vertigo.grouping.RandomGrouping;
import net.kuujo.vertigo.grouping.RoundGrouping;

/**
 * Static helper for creating grouping definitions.
 *
 * @author Jordan Halterman
 */
public final class Groupings {

  /**
   * Creates a random grouping definition.
   *
   * @return
   *   A random grouping definition.
   */
  public static RandomGrouping random() {
    return new RandomGrouping();
  }

  /**
   * Creates a round-robin grouping definition.
   *
   * @return
   *   A round robin grouping definition.
   */
  public static RoundGrouping round() {
    return new RoundGrouping();
  }

  /**
   * Creates a fields-based grouping definition.
   *
   * @return
   *   A fields-based grouping definition.
   */
  public static FieldsGrouping fields() {
    return new FieldsGrouping();
  }

  /**
   * Creates a fields-based grouping definition.
   *
   * @param field
   *   The field name.
   * @return
   *   A fields-based grouping definition.
   */
  public static FieldsGrouping fields(String field) {
    return new FieldsGrouping(field);
  }

  /**
   * Creates an all grouping definition.
   *
   * @return
   *   A grouping definition for dispatching messages to all connected workers.
   */
  public static AllGrouping all() {
    return new AllGrouping();
  }

}
