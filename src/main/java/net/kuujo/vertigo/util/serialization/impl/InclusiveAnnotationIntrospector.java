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
package net.kuujo.vertigo.util.serialization.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.kuujo.vertigo.util.serialization.JsonSerializable;

import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;

/**
 * An annotation introspector that automatically includes primitives and their
 * wrappers into all serializations. Additionally, any clas that implements the
 * {@link JsonSerializable} interface will be automatically included for serialization.
 * This allows all Vertigo serializables to be automatically serialized by the
 * default serializer. 
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class InclusiveAnnotationIntrospector extends JacksonAnnotationIntrospector {
  private static final long serialVersionUID = 1L;

  @SuppressWarnings("serial")
  private static final Set<Class<?>> primitiveTypes = new HashSet<Class<?>>() {{
    add(Class.class);
    add(String.class);
    add(String[].class);
    add(Boolean.class);
    add(Boolean[].class);
    add(boolean.class);
    add(boolean[].class);
    add(Character.class);
    add(Character[].class);
    add(char.class);
    add(char[].class);
    add(Byte.class);
    add(Byte[].class);
    add(byte.class);
    add(byte[].class);
    add(Short.class);
    add(Short[].class);
    add(short.class);
    add(short[].class);
    add(Integer.class);
    add(Integer[].class);
    add(int.class);
    add(int[].class);
    add(Long.class);
    add(Long[].class);
    add(long.class);
    add(long[].class);
    add(Float.class);
    add(Float[].class);
    add(float.class);
    add(float[].class);
    add(Double.class);
    add(Double[].class);
    add(double.class);
    add(double[].class);
    add(Void.class);
    add(Void[].class);
    add(void.class);
  }};

  @SuppressWarnings("serial")
  private final Set<Class<?>> serializableTypes = new HashSet<Class<?>>() {{
    add(Map.class);
    add(Map[].class);
    add(Collection.class);
    add(Collection[].class);
  }};

  private final Map<Class<?>, Boolean> cache = new HashMap<>();

  /**
   * Indicates whether the given type is a "serializable" type.
   */
  private boolean isSerializableType(Class<?> type) {
    Boolean serializable = cache.get(type);
    if (serializable != null) {
      return serializable;
    }

    if (type == Object.class) {
      return true;
    }

    if (primitiveTypes.contains(type)) {
      cache.put(type, true);
      return true;
    }

    if (JsonSerializable.class.isAssignableFrom(type)) {
      cache.put(type, true);
      return true;
    }

    for (Class<?> clazz : serializableTypes) {
      if (clazz.isAssignableFrom(type)) {
        cache.put(type, true);
        return true;
      }
    }
    cache.put(type, false);
    return false;
  }

  @Override
  public Boolean isIgnorableType(AnnotatedClass ac) {
    Boolean ignorable = super.isIgnorableType(ac);
    if (ignorable != null && ignorable) {
      return true;
    }
    return !isSerializableType(ac.getRawType());
  }

  @Override
  public boolean hasIgnoreMarker(AnnotatedMember member) {
    Boolean ignore = super.hasIgnoreMarker(member);
    if (ignore != null && ignore) {
      return true;
    }
    return !isSerializableType(member.getDeclaringClass());
  }

}
