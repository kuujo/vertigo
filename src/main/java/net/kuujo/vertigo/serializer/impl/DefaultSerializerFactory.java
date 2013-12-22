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

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import net.kuujo.vertigo.serializer.Serializable;
import net.kuujo.vertigo.serializer.Serializer;
import net.kuujo.vertigo.serializer.SerializerFactory;

/**
 * A default serializer factory implementation.
 *
 * @author Jordan Halterman
 */
public class DefaultSerializerFactory extends SerializerFactory {
  private final Map<Class<?>, Class<?>> serializers = new HashMap<>();

  /**
   * Adds a type-specific serializer.
   *
   * The serializer must have a one-argument constructor that accepts the
   * serializable type, as is provided in the abstract {@link Serializer} class.
   * By default, this method can be accessed via the singleton factory instance:<p>
   *
   * <pre>
   * SerializerFactory.getInstance().addSerializer(SomeClass.class, SomeClassSerializer.class);
   * </pre>
   *
   * @param type
   *   The serializable type.
   * @param serializer
   *   The serializer class to add.
   * @return
   *   The serializer factory.
   */
  public <T1 extends Serializable, T2 extends Serializer<T1>> DefaultSerializerFactory addSerializer(Class<T1> type, Class<T2> serializer) {
    serializers.put(type, serializer);
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
  public <T1 extends Serializable, T2 extends Serializer<T1>> DefaultSerializerFactory removeSerializer(Class<T1> type, Class<T2> serializer) {
    if (serializers.containsKey(type) && serializers.get(type).equals(serializer)) {
      serializers.remove(type);
    }
    return this;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T extends Serializable> Serializer<T> createSerializer(Class<T> type) {
    if (serializers.containsKey(type)) {
      try {
        return (Serializer<T>) serializers.get(type).getDeclaredConstructor(new Class<?>[]{type.getClass()}).newInstance(type);
      }
      catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
          | NoSuchMethodException | SecurityException e) {
        return new DefaultSerializer<T>(type);
      }
    }
    return new DefaultSerializer<T>(type);
  }

}
