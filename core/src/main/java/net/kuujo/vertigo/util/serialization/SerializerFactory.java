/*
 * Copyright 2013-2014 the original author or authors.
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
package net.kuujo.vertigo.util.serialization;

import java.util.HashMap;
import java.util.Map;

import net.kuujo.vertigo.util.serialization.impl.JacksonSerializerFactory;

/**
 * Json serializer factory.
 * <p>
 * 
 * This class is the primary interface to object serialization in Vertigo. Use this class
 * to load type-specific serializers as follows:
 * <p>
 * 
 * <pre>
 * JsonObject json = SerializerFactory.getSerializer(MyClass.class).serialize(myClassObj);
 * </pre>
 * <p>
 *
 * Note that when loading a serializer for a class, the factory will load the serializer
 * for the first class that implements {@link JsonSerializable} in the class hierarchy.
 * If no class in the hierarchy implements {@link JsonSerializable} then the default
 * serializer will be used.<p>
 * 
 * In order to serialize an object with the default serializer, classes must implement the
 * {@link JsonSerializable} interface. In most cases, the default serializer will
 * automatically serialize all primitives, collections, and {@link JsonSerializable}
 * properties within the serializable object. For more advanced serialization, look at the
 * Jackson annotations documentation.
 * 
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public abstract class SerializerFactory {
  private static final String SERIALIZER_FACTORY_CLASS_NAME = "net.kuujo.vertigo.serializer-factory";
  private static SerializerFactory instance;
  private static Map<Class<?>, Class<?>> serializableTypes = new HashMap<>();
  private static Map<Class<?>, Serializer> serializers = new HashMap<>();

  /**
   * Gets a singleton serializer factory instance.
   * 
   * @return The current serializer factory instance.
   */
  private static SerializerFactory getInstance() {
    if (instance == null) {
      String className = JacksonSerializerFactory.class.getName();
      try {
        className = System.getProperty(SERIALIZER_FACTORY_CLASS_NAME);
      } catch (Exception e) {
      }

      if (className != null) {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        try {
          Class<?> clazz = loader.loadClass(className);
          instance = (SerializerFactory) clazz.newInstance();
        } catch (Exception e) {
          throw new IllegalArgumentException("Error instantiating serializer factory.");
        }
      } else {
        instance = new JacksonSerializerFactory();
      }
    }
    return instance;
  }

  /**
   * Gets a serializer instance.
   * 
   * @param type The serializer type.
   * @return A serializer instance.
   */
  public static Serializer getSerializer(Class<?> type) {
    Class<?> serializable = lookupSerializableType(type);
    Serializer serializer = serializers.get(serializable);
    if (serializer == null) {
      serializer = getInstance().createSerializer(serializable);
      serializers.put(serializable, serializer);
    }
    return serializer;
  }

  /**
   * Looks up the serializable type for the given type.
   */
  private static Class<?> lookupSerializableType(Class<?> type) {
    Class<?> serializableType = serializableTypes.get(type);
    if (serializableType != null) {
      return serializableType;
    }
    serializableType = findSerializableType(type);
    if (serializableType != null) {
      serializableTypes.put(type, serializableType);
      return serializableType;
    }
    return type;
  }

  /**
   * Iterates over the class hierarchy searching for the base serializable type.
   */
  private static Class<?> findSerializableType(Class<?> type) {
    while (type != null && type != Object.class) {
      for (Class<?> iface : type.getInterfaces()) {
        if (iface == JsonSerializable.class) {
          return type;
        }
        Class<?> serializable = findSerializableType(iface);
        if (serializable != null) {
          return serializable;
        }
      }
      type = type.getSuperclass();
    }
    return null;
  }

  /**
   * Creates a new serializer instance.
   * 
   * @param type The serializer class.
   * @return A new serializer instance.
   */
  public abstract Serializer createSerializer(Class<?> clazz);

}
