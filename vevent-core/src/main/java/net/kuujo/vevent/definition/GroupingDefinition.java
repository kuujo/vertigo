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
package net.kuujo.vevent.definition;

import net.kuujo.vevent.Serializeable;

import org.vertx.java.core.json.JsonObject;

/**
 * A grouping definition.
 *
 * @author Jordan Halterman
 */
public class GroupingDefinition implements Serializeable<JsonObject> {

  protected JsonObject definition;

  public GroupingDefinition() {
    this.definition = new JsonObject();
  }

  public GroupingDefinition(JsonObject definition) {
    this.definition = definition;
  }

  /**
   * Sets a grouping option.
   *
   * @param option
   *   The option to set.
   * @param value
   *   The option value.
   * @return
   *   The called grouping definition.
   */
  public GroupingDefinition setOption(String option, String value) {
    definition.putString(option, value);
    return this;
  }

  /**
   * Gets a grouping option.
   *
   * @param option
   *   The option to get.
   * @return
   *   The option value.
   */
  public String getOption(String option) {
    return definition.getString(option);
  }

  /**
   * Gets a grouping option.
   *
   * @param option
   *   The option to get.
   * @param defaultValue
   *   A default value for the option.
   * @return
   *   The option value.
   */
  public String getOption(String option, String defaultValue) {
    return definition.getString(option, defaultValue);
  }

  @Override
  public JsonObject serialize() {
    return definition;
  }

}
