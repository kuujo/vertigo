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
import net.kuujo.vertigo.network.Input;

/**
 * The <code>fields</code> grouping is a hashing based grouping. Given a set of
 * fields on which to hash, this grouping guarantees that workers will always
 * receive messages with the same field values.<p>
 *
 * Note that users should use the <code>fieldsGrouping</code> {@link Input} method
 * rather than constructing this grouping directly.
 *
 * @author Jordan Halterman
 */
public class FieldsGrouping implements Grouping {
  private Set<String> fields;

  public FieldsGrouping() {
    fields = new HashSet<>();
  }

  public FieldsGrouping(String... fieldNames) {
    this.fields = new HashSet<>();
    for (String fieldName : fieldNames) {
      this.fields.add(fieldName);
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
    this.fields = new HashSet<>();
    for (String fieldName : fieldNames) {
      this.fields.add(fieldName);
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
    this.fields = fieldNames;
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
    this.fields.add(fieldName);
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
  public Selector createSelector() {
    return new FieldsSelector(fields);
  }

}
