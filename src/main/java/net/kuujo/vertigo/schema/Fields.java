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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * A set of object fields.
 *
 * @author Jordan Halterman
 */
public final class Fields implements Iterable<Field> {
  private final Map<String, Field> fields = new HashMap<>();

  public Fields() {
  }

  public Fields(String... fieldNames) {
    for (String fieldName : fieldNames) {
      add(new Field(fieldName));
    }
  }

  public Fields(Set<String> fieldNames) {
    for (String fieldName : fieldNames) {
      add(new Field(fieldName));
    }
  }

  public Fields(Field... fields) {
    for (Field field : fields) {
      add(field);
    }
  }

  /**
   * Adds a field to the fields collection.
   *
   * @param field
   *   The field to add.
   * @return
   *   The called fields collection.
   */
  public Fields add(Field field) {
    if (!fields.containsKey(field.getName())) {
      fields.put(field.getName(), field);
    }
    return this;
  }

  @Override
  public Iterator<Field> iterator() {
    return fields.values().iterator();
  }

}
