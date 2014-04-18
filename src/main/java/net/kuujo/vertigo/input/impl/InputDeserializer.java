/*
 * Copyright 2014 the original author or authors.
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
package net.kuujo.vertigo.input.impl;

import java.util.HashMap;
import java.util.Map;

import net.kuujo.vertigo.util.serializer.Serializer;
import net.kuujo.vertigo.util.serializer.SerializerFactory;

import org.vertx.java.core.json.JsonObject;

/**
 * Handles deserialization of input objects.
 *
 * @author Jordan Halterman
 */
class InputDeserializer {
  private final Map<String, Class<?>> classes = new HashMap<>();
  private final Map<Class<?>, Serializer> serializers = new HashMap<>();

  /**
   * Deserializes an input message.
   *
   * @param message The message to deserialize.
   * @return The message value.
   */
  public Object deserialize(JsonObject message) {
    boolean serialized = message.getBoolean("serialized", false);
    if (!serialized) {
      return message.getValue("value");
    }

    String className = message.getString("class");
    if (className == null) {
      return null;
    }

    Class<?> clazz = classes.get(className);
    if (clazz == null) {
      ClassLoader loader = Thread.currentThread().getContextClassLoader();
      try {
        clazz = loader.loadClass(className);
      } catch (Exception e) {
        throw new IllegalArgumentException("Error instantiating serializer factory.");
      }
    }

    Serializer serializer = serializers.get(clazz);
    if (serializer == null) {
      serializer = SerializerFactory.getSerializer(clazz);
      serializers.put(clazz, serializer);
    }

    String value = message.getString("value");
    if (value == null) {
      return null;
    }
    return serializer.deserializeString(value, clazz);
  }

}
