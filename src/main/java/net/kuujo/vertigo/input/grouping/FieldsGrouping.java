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

import java.util.HashSet;
import java.util.Set;

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
  private Set<String> fields;

  public FieldsGrouping() {
    fields = new HashSet<>();
  }

  public FieldsGrouping(String... fieldNames) {
    fields = new HashSet<>();
    for (String fieldName : fieldNames) {
      fields.add(fieldName);
    }
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
    fields = new HashSet<>();
    for (String fieldName : fieldNames) {
      fields.add(fieldName);
    }
    return this;
  }

  /**
   * Sets the grouping fields.
   *
   * @param fieldNames
   *   The grouping field names.
   * @return
   *   The called grouping instance.
   */
  public FieldsGrouping setFields(Set<String> fieldNames) {
    fields = fieldNames;
    return this;
  }

  /**
   * Adds a field to the grouping.
   *
   * @param fieldName
   *   The field name.
   * @return
   *   The called grouping instance.
   */
  public FieldsGrouping addField(String fieldName) {
    fields.add(fieldName);
    return this;
  }

  /**
   * Gets the grouping fields.
   *
   * @return
   *   The grouping fields.
   */
  public Set<String> getFields() {
    return fields;
  }

  @Override
  public JsonObject getState() {
    JsonArray fieldsArray = new JsonArray();
    for (String fieldName : fields) {
      fieldsArray.add(fieldName);
    }
    return new JsonObject().putArray("fields", fieldsArray);
  }

  @Override
  public void setState(JsonObject state) {
    fields = new HashSet<>();
    JsonArray fieldsArray = state.getArray("fields");
    if (fieldsArray != null) {
      for (Object fieldName : fieldsArray) {
        fields.add((String) fieldName);
      }
    }
  }

  @Override
  public Selector createSelector() {
    return new FieldsSelector(getFields());
  }

}
