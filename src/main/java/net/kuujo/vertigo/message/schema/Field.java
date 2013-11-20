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
package net.kuujo.vertigo.message.schema;

/**
 * An object field.
 *
 * @author Jordan Halterman
 */
public final class Field {
  private String name;
  private Class<?> type;
  private JsonSchema schema;

  public Field(String name) {
    this.name = name;
  }

  public Field(String name, Class<?> type) {
    this.name = name;
    this.type = type;
  }

  public Field(String name, JsonSchema schema) {
    this.name = name;
    this.schema = schema;
  }

  public Field(String name, Class<?> type, JsonSchema schema) {
    this.name = name;
    this.type = type;
    this.schema = schema;
  }

  /**
   * Gets the field name.
   *
   * @return
   *   The field name.
   */
  public String getName() {
    return name;
  }

  /**
   * Sets the field name.
   *
   * @param name
   *   The field name.
   * @return
   *   The called field instance.
   */
  public Field setName(String name) {
    this.name = name;
    return this;
  }

  /**
   * Gets the field type.
   *
   * @return
   *   The field's expected data type.
   */
  public Class<?> getType() {
    return type;
  }

  /**
   * Sets the field type.
   *
   * @param dataType
   *   The expected field data type.
   * @return
   *   The called field instance.
   */
  public Field setType(Class<?> dataType) {
    type = dataType;
    return this;
  }

  /**
   * Checks whether the field has a type.
   *
   * @return
   *   Indicates whether the field has a type.
   */
  public boolean hasType() {
    return type != null;
  }

  /**
   * Gets the field schema.
   *
   * @return
   *   The field schema.
   */
  public JsonSchema getSchema() {
    return schema;
  }

  /**
   * Sets the field schema.
   *
   * @param schema
   *   The field schema.
   * @return
   *   The called field instance.
   */
  public Field setSchema(JsonSchema schema) {
    this.schema = schema;
    return this;
  }

  /**
   * Checks whether the field has a schema.
   *
   * @return
   *   Indicates whether the field has a schema.
   */
  public boolean hasSchema() {
    return schema != null;
  }

}
