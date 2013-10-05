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
package net.kuujo.vertigo.grouping;

import net.kuujo.vertigo.definition.GroupingDefinition;
import net.kuujo.vertigo.dispatcher.FieldsDispatcher;

/**
 * A fields-based grouping.
 *
 * This grouping dispatches messages to workers in a consistent fashion
 * using the FieldsDispatcher dispatcher.
 *
 * @author Jordan Halterman
 */
public class FieldsGrouping extends GroupingDefinition {

  public FieldsGrouping() {
    super();
    definition.putString("dispatcher", FieldsDispatcher.class.getName());
  }

  public FieldsGrouping(String field) {
    this();
    definition.putString("field", field);
  }

  /**
   * Sets the grouping field.
   *
   * @param field
   *   A field on which to group.
   * @return
   *   The called grouping definition.
   */
  public FieldsGrouping setField(String field) {
    definition.putString("field", field);
    return this;
  }

  /**
   * Gets the grouping field.
   *
   * @return
   *   The field on which the grouping groups.
   */
  public String getField() {
    return definition.getString("field");
  }

}
