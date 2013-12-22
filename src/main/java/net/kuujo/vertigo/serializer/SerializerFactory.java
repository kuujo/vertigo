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

import net.kuujo.vertigo.serializer.impl.DefaultSerializerFactory;

/**
 * A serializer factory.<p>
 *
 * This class is the primary interface to object serialization in Vertigo. Use
 * this class to load type-specific serializers as follows:<p>
 *
 * <pre>
 * JsonObject json = SerializerFactory.getSerializer(MyClass.class).serialize(myClassObj);
 * </pre>
 * <p>
 *
 * In order to serialize an object with the default serializer, classes must
 * implement the {@link Serializable} interface. In most cases, the default serializer
 * will automatically serialize all primitives, collections, and {@link Serializable}
 * properties within the serializable object. For more advanced serialization, look
 * at the Jackson annotations documentation.
 *
 * @author Jordan Halterman
 */
public abstract class SerializerFactory {
  private static final String SERIALIZER_FACTORY_CLASS_NAME = "net.kuujo.vertigo.serializer-factory";
  private static SerializerFactory instance;
  @SuppressWarnings("rawtypes")
  private static Map<Class, Serializer> serializers = new HashMap<>();

  /**
   * Gets a singleton serializer factory instance.
   *
   * @return
   *   The current serializer factory instance.
   */
  public static SerializerFactory getInstance() {
    if (instance == null) {
      String className = DefaultSerializerFactory.class.getName();
      try {
        className = System.getProperty(SERIALIZER_FACTORY_CLASS_NAME);
      }
      catch (Exception e) {}

      if (className != null) {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        try {
          Class<?> clazz = loader.loadClass(className);
          instance = (SerializerFactory) clazz.newInstance();
        }
        catch (Exception e) {
          throw new IllegalArgumentException("Error instantiating serializer factory.");
        }
      }
      else {
        instance = new DefaultSerializerFactory();
      }
    }
    return instance;
  }

  /**
   * Gets a serializer instance.
   *
   * @param type
   *   The serializer type.
   * @return
   *   A serializer instance.
   */
  @SuppressWarnings("unchecked")
  public static <T extends Serializable> Serializer<T> getSerializer(Class<T> type) {
    if (!serializers.containsKey(type)) {
      serializers.put(type, getInstance().createSerializer(type));
    }
    return serializers.get(type);
  }

  /**
   * Creates a serializer.
   *
   * @param type
   *   The serializer type.
   * @return
   *   A serializer instance.
   */
  public abstract <T extends Serializable> Serializer<T> createSerializer(Class<T> type);

}
