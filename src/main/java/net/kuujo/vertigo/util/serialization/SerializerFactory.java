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
    Class<?> serializable = findSerializableType(type);
    Serializer serializer = serializers.get(serializable);
    if (serializer == null) {
      serializer = getInstance().createSerializer(serializable);
      serializers.put(serializable, serializer);
    }
    return serializer;
  }

  /**
   * Iterates over the class hierarchy searching for the base serializable type.
   */
  private static Class<?> findSerializableType(Class<?> type) {
    if (serializableTypes.containsKey(type)) {
      return serializableTypes.get(type);
    }

    Class<?> current = type;
    while (current != null && current != Object.class) {
      Class<?> serializable = findSerializableInterface(current);
      if (serializable != null) {
        serializableTypes.put(type, serializable);
        return serializable;
      }
      current = current.getSuperclass();
    }
    throw new SerializationException("Invalid serializable type.");
  }

  /**
   * Recursively iterates over the interface hierarchy to find the base serializable
   * interface. This is the interface that initially extends the Serializable interface
   * and it used to group serializers together.
   */
  private static Class<?> findSerializableInterface(Class<?> type) {
    Class<?>[] interfaces = type.getInterfaces();
    if (interfaces.length > 0) {
      for (Class<?> iface : interfaces) {
        if (iface == JsonSerializable.class) {
          return type;
        }
      }

      for (Class<?> iface : interfaces) {
        Class<?> serializable = findSerializableType(iface);
        if (serializable != null) {
          return serializable;
        }
      }
      return null;
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
