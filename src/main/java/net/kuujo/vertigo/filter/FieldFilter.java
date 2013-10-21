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
package net.kuujo.vertigo.filter;

import net.kuujo.vertigo.messaging.JsonMessage;

import org.vertx.java.core.json.JsonObject;

/**
 * A field filter.
 *
 * @author Jordan Halterman
 */
public class FieldFilter implements Filter {

  private JsonObject definition = new JsonObject();

  public FieldFilter() {
    definition = new JsonObject();
  }

  public FieldFilter(String fieldName, Object value) {
    definition.putString("field", fieldName).putValue("value", value);
  }

  /**
   * Sets the filter field.
   *
   * @param fieldName
   *   The filter field name.
   * @return
   *   The called filter instance.
   */
  public FieldFilter field(String fieldName) {
    definition.putString("field", fieldName);
    return this;
  }

  /**
   * Sets the filter value.
   *
   * @param value
   *   The filter field value.
   * @return
   *   The called filter instance.
   */
  public FieldFilter value(Object value) {
    definition.putValue("value", value);
    return this;
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
  public boolean isValid(JsonMessage message) {
    JsonObject body = message.body();
    if (body != null) {
      Object fieldValue = body.getValue(definition.getString("field"));
      if (fieldValue != null) {
        return fieldValue.equals(definition.getValue("value"));
      }
    }
    return false;
  }

}
