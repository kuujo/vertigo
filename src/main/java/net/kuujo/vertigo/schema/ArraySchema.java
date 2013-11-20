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
package net.kuujo.vertigo.schema;

import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonElement;

/**
 * A JSON array schema.
 *
 * @author Jordan Halterman
 */
public class ArraySchema implements JsonSchema {
  private Class<?> type;
  private JsonSchema schema;

  public ArraySchema() {
  }

  public ArraySchema(Class<?> type) {
    this.type = type;
  }

  public ArraySchema(JsonSchema schema) {
    this.schema = schema;
  }

  public ArraySchema(Class<?> type, JsonSchema schema) {
    this.type = type;
    this.schema = schema;
  }

  /**
   * Sets the array type.
   *
   * @param dataType
   *   The expected array data type.
   * @return
   *   The called schema instance.
   */
  public ArraySchema setElementType(Class<?> dataType) {
    this.type = dataType;
    return this;
  }

  /**
   * Gets the array type.
   *
   * @return
   *   The expected array data type.
   */
  public Class<?> getElementType() {
    return type;
  }

  /**
   * Checks whether the array schema has an element type.
   *
   * @return
   *   Indicates whether the schema has an element type.
   */
  public boolean hasElementType() {
    return type != null;
  }

  /**
   * Sets the array elements schema.
   *
   * @param schema
   *   The array elements schema.
   * @return
   *   The called schema instance.
   */
  public ArraySchema setElementSchema(JsonSchema schema) {
    this.schema = schema;
    return this;
  }

  /**
   * Checks whether the array schema has an element schema.
   *
   * @return
   *   Indicates whether the schema has an element schema.
   */
  public boolean hasElementSchema() {
    return schema != null;
  }

  /**
   * Gets the array elements schema.
   *
   * @return
   *   The array elements schema.
   */
  public JsonSchema getElementSchema() {
    return schema;
  }

  @Override
  public JsonValidator getValidator() {
    return new Validator(this);
  }

  /**
   * An array schema validator.
   *
   * @author Jordan Halterman
   */
  public static class Validator implements JsonValidator {
    private final ArraySchema schema;

    public Validator(ArraySchema schema) {
      this.schema = schema;
    }

    @Override
    public boolean validate(JsonElement json) {
      if (!json.isArray()) {
        return false;
      }

      if (schema.hasElementType() || schema.hasElementSchema()) {
        JsonArray jsonArray = json.asArray();
        for (Object value : jsonArray) {
          // If the field has a type then check the value type against the field type.
          // If the field has a schema then check that the value is a JsonElement
          // and then validate the value against the schema.
          if ((schema.hasElementSchema() && !schema.getElementType().isAssignableFrom(value.getClass()))
              || (schema.hasElementSchema() && (!(value instanceof JsonElement)
                  || !schema.getElementSchema().getValidator().validate((JsonElement) value)))) {
            return false;
          }
        }
      }
      return true;
    }

  }

}
