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

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.kuujo.vertigo.message.JsonMessage;
import net.kuujo.vertigo.output.Connection;

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
  private Set<String> fields;

  public FieldsSelector() {
  }

  public FieldsSelector(String... fieldNames) {
    this.fields = new HashSet<>();
    for (String fieldName : fieldNames) {
      this.fields.add(fieldName);
    }
  }

  public FieldsSelector(Set<String> fieldNames) {
    this.fields = fieldNames;
  }

  @Override
  public List<Connection> select(JsonMessage message, List<Connection> connections) {
    JsonObject body = message.body();
    Map<String, Object> fields = new HashMap<>(this.fields.size() + 1);
    for (String fieldName : this.fields) {
      Object value = body.getValue(fieldName);
      fields.put(fieldName, value);
    }
    int index = Math.abs(fields.hashCode() % connections.size());
    return connections.subList(index, index+1);
  }

}
