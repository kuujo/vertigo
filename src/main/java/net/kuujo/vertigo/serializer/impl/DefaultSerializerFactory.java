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
package net.kuujo.vertigo.serializer.impl;

import java.util.HashMap;
import java.util.Map;

import net.kuujo.vertigo.serializer.Serializer;
import net.kuujo.vertigo.serializer.SerializerFactory;

/**
 * A default serializer factory implementation.
 *
 * @author Jordan Halterman
 */
public class DefaultSerializerFactory extends SerializerFactory {
  private final Map<String, Class<?>> serializers = new HashMap<>();

  /**
   * Adds a type-specific serializer.
   *
   * @param type
   *   The serializable type.
   * @param serializer
   *   The serializer class to add.
   * @return
   *   The serializer factory.
   */
  public <T extends Serializer> DefaultSerializerFactory addSerializer(Class<?> type, Class<T> serializer) {
    return addSerializer(type.getCanonicalName(), serializer);
  }

  /**
   * Adds a named serializer.
   *
   * @param name
   *   The serializer name.
   * @param serializer
   *   The serializer class to add.
   * @return
   *   The serializer factory.
   */
  public <T extends Serializer> DefaultSerializerFactory addSerializer(String name, Class<T> serializer) {
    serializers.put(name, serializer);
    return this;
  }

  /**
   * Removes a type-specific serializer.
   *
   * @param type
   *   The serializable type.
   * @param serializer
   *   The serializer class to remove.
   * @return
   *   The serializer factory.
   */
  public <T extends Serializer> DefaultSerializerFactory removeSerializer(Class<?> type, Class<T> serializer) {
    return removeSerializer(type.getCanonicalName(), serializer);
  }

  /**
   * Removes a named serializer.
   *
   * @param name
   *   The serializer name.
   * @param serializer
   *   The serializer class to remove.
   * @return
   *   The serializer factory.
   */
  public <T extends Serializer> DefaultSerializerFactory removeSerializer(String name, Class<T> serializer) {
    if (serializers.containsKey(name) && serializers.get(name).equals(serializer)) {
      serializers.remove(name);
    }
    return this;
  }

  @Override
  public Serializer createSerializer(String name) {
    if (serializers.containsKey(name)) {
      try {
        return (Serializer) serializers.get(name).newInstance();
      }
      catch (InstantiationException | IllegalAccessException | IllegalArgumentException | SecurityException e) {
        return new DefaultSerializer();
      }
    }
    return new DefaultSerializer();
  }

}
