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
package net.kuujo.vertigo.output.condition;

import net.kuujo.vertigo.message.JsonMessage;

import org.vertx.java.core.json.JsonObject;

/**
 * A field/value condition.
 *
 * @author Jordan Halterman
 */
public class FieldCondition implements Condition {
  private String fieldName;
  private Object value;

  public FieldCondition() {
  }

  public FieldCondition(String fieldName, Object value) {
    this.fieldName = fieldName;
    this.value = value;
  }

  @Override
  public JsonObject getState() {
    return new JsonObject().putString("field", fieldName).putValue("value", value);
  }

  @Override
  public void setState(JsonObject state) {
    fieldName = state.getString("field");
    value = state.getValue("value");
  }

  @Override
  public boolean isValid(JsonMessage message) {
    JsonObject body = message.body();
    if (body != null) {
      Object fieldValue = body.getValue(fieldName);
      if (fieldValue != null) {
        return fieldValue.equals(value);
      }
    }
    return false;
  }

}
