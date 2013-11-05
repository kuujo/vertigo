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
package net.kuujo.vertigo.output.selector;

import java.util.List;

import net.kuujo.vertigo.message.JsonMessage;
import net.kuujo.vertigo.output.Connection;

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
public class FieldsSelector implements Selector {
  private String[] fieldNames;

  public FieldsSelector() {
  }

  public FieldsSelector(String... fieldNames) {
    this.fieldNames = fieldNames;
  }

  @Override
  public List<Connection> select(JsonMessage message, List<Connection> connections) {
    JsonObject body = message.body();
    String hash = "";
    for (String fieldName : fieldNames) {
      Object value = body.getValue(fieldName);
      if (value != null) {
        hash += value.hashCode();
      }
    }
    int index = Integer.valueOf(hash) % connections.size();
    return connections.subList(index, index+1);
  }

  @Override
  public JsonObject getState() {
    return new JsonObject().putArray("fields", new JsonArray(fieldNames));
  }

  @Override
  public void setState(JsonObject state) {
    JsonArray fields = state.getArray("fields");
    if (fields == null) {
      fields = new JsonArray();
    }
    fieldNames = new String[fields.size()];
    for (int i = 0; i < fields.size(); i++) {
      fieldNames[i] = fields.get(i);
    }
  }

}
