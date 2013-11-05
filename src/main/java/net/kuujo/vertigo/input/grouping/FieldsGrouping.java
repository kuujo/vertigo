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
package net.kuujo.vertigo.input.grouping;

import net.kuujo.vertigo.output.selector.FieldsSelector;
import net.kuujo.vertigo.output.selector.Selector;

import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

/**
 * A fields selector.
 *
 * The *fields* selector is a consistent-hashing based grouping. Given
 * a field on which to hash, this grouping guarantees that workers will
 * always receive messages with the same field values.
 *
 * @author Jordan Halterman
 */
public class FieldsGrouping implements Grouping {
  private JsonObject definition = new JsonObject();

  public FieldsGrouping() {
  }

  public FieldsGrouping(String... fieldNames) {
    definition.putArray("fields", new JsonArray(fieldNames));
  }

  /**
   * Sets the grouping fields.
   *
   * @param fieldNames
   *   The grouping field names.
   * @return
   *   The called grouping instance.
   */
  public FieldsGrouping setFields(String... fieldNames) {
    definition.putArray("fields", new JsonArray(fieldNames));
    return this;
  }

  /**
   * Gets the grouping fields.
   *
   * @return
   *   The grouping fields.
   */
  public String[] getFields() {
    JsonArray fieldNames = definition.getArray("fields");
    if (fieldNames == null) {
      fieldNames = new JsonArray();
    }
    String[] fields = new String[fieldNames.size()];
    for (int i = 0; i < fieldNames.size(); i++) {
      fields[i] = fieldNames.get(i);
    }
    return fields;
  }

  @Override
  public JsonObject getState() {
    return definition;
  }

  @Override
  public void setState(JsonObject state) {
    definition = state;
  }

  @Override
  public Selector createSelector() {
    return new FieldsSelector(getFields());
  }

}
