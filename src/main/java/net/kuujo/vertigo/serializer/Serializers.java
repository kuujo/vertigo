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
package net.kuujo.vertigo.serializer;

import java.util.HashMap;
import java.util.Map;

/**
 * Static helper methods for serialization.
 *
 * @author Jordan Halterman
 */
public class Serializers {
  private static final String SERIALIZER_FACTORY_CLASS_NAME = "net.kuujo.vertigo.serializer-factory";
  private static final String DEFAULT_SERIALIZER_NAME = "default";
  private static SerializerFactory factory;
  private static Map<String, Serializer> instances = new HashMap<String, Serializer>();

  static {
    initialize();
  }

  /**
   * Initializes the current serializer factory.
   */
  public static void initialize() {
    String className = DefaultSerializerFactory.class.getName();
    try {
      className = System.getProperty(SERIALIZER_FACTORY_CLASS_NAME);
    }
    catch (Exception e) {
    }

    if (className != null) {
      ClassLoader loader = Thread.currentThread().getContextClassLoader();
      try {
        Class<?> clazz = loader.loadClass(className);
        Serializers.factory = (SerializerFactory) clazz.newInstance();
      }
      catch (Exception e) {
        throw new IllegalArgumentException("Error instantiating serializer factory.");
      }
    }
    else {
      Serializers.factory = new DefaultSerializerFactory();
    }
  }

  /**
   * Gets the default serializer instance.
   *
   * @return
   *   The default serializer instance.
   */
  public static Serializer getDefault() {
    return getSerializer(DEFAULT_SERIALIZER_NAME);
  }

  /**
   * Gets the current serializer.
   *
   * @return
   *   An instance of the current serializer.
   */
  public static Serializer getSerializer(String name) {
    if (!instances.containsKey(name)) {
      instances.put(name, factory.createSerializer());
    }
    return instances.get(name);
  }

}
