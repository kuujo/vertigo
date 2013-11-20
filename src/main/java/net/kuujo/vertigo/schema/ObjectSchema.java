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

import org.vertx.java.core.json.JsonElement;
import org.vertx.java.core.json.JsonObject;

/**
 * A JSON object schema.
 *
 * @author Jordan Halterman
 */
public class ObjectSchema implements JsonSchema {
  private final Fields fields;

  public ObjectSchema() {
    this.fields = new Fields();
  }

  public ObjectSchema(Field... fields) {
    this.fields = new Fields(fields);
  }

  public ObjectSchema(Fields fields) {
    this.fields = fields;
  }

  /**
   * Adds a field to the schema.
   *
   * @param field
   *   A schema field.
   * @return
   *   The called schema instance.
   */
  public ObjectSchema addField(Field field) {
    fields.add(field);
    return this;
  }

  /**
   * Adds a field to the schema.
   *
   * @param fieldName
   *   The field name.
   * @return
   *   The called schema instance.
   */
  public ObjectSchema addField(String fieldName) {
    fields.add(new Field(fieldName));
    return this;
  }

  /**
   * Adds a field to the schema.
   *
   * @param fieldName
   *   The field name.
   * @param dataType
   *   The field data type.
   * @return
   *   The called schema instance.
   */
  public ObjectSchema addField(String fieldName, Class<?> dataType) {
    fields.add(new Field(fieldName, dataType));
    return this;
  }

  /**
   * Adds a field schema to the schema.
   *
   * @param fieldName
   *   The field name.
   * @param schema
   *   The field schema.
   * @return
   *   The called schema instance.
   */
  public ObjectSchema addField(String fieldName, JsonSchema schema) {
    fields.add(new Field(fieldName, schema));
    return this;
  }

  /**
   * Adds a field schema to the schema.
   *
   * @param fieldName
   *   The field name.
   * @param dataType
   *   The field data type.
   * @param schema
   *   The field schema.
   * @return
   *   The called schema instance.
   */
  public ObjectSchema addField(String fieldName, Class<?> dataType, JsonSchema schema) {
    fields.add(new Field(fieldName, dataType, schema));
    return this;
  }

  /**
   * Gets fields contained in the schema.
   *
   * @return
   *   A set of fields.
   */
  public Fields getFields() {
    return fields;
  }

  @Override
  public JsonValidator getValidator() {
    return new Validator(this);
  }

  /**
   * An object schema validator.
   *
   * @author Jordan Halterman
   */
  public static class Validator implements JsonValidator {
    private final Fields fields;

    public Validator(Fields fields) {
      this.fields = fields;
    }

    public Validator(ObjectSchema schema) {
      this.fields = schema.getFields();
    }

    @Override
    public boolean validate(JsonElement json) {
      if (!json.isObject()) {
        return false;
      }

      JsonObject jsonObject = json.asObject();
      for (Field field : fields) {
        Object value = jsonObject.getValue(field.getName());
        if (value != null) {
          // If the field has a type then check the value type against the field type.
          // If the field has a schema then check that the value is a JsonElement
          // and then validate the value against the schema.
          if ((field.hasType() && !field.getType().isAssignableFrom(value.getClass()))
              || (field.hasSchema() && (!(value instanceof JsonElement) ||
                  !field.getSchema().getValidator().validate((JsonElement) value)))) {
            return false;
          }
        }
        else if (field.hasType()) {
          return false;
        }
      }
      return true;
    }

  }

}
