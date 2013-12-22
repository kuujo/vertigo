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

import org.vertx.java.core.json.JsonObject;

import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.kuujo.vertigo.serializer.DeserializationException;
import net.kuujo.vertigo.serializer.Serializable;
import net.kuujo.vertigo.serializer.SerializationException;
import net.kuujo.vertigo.serializer.Serializer;

/**
 * A default serializer implementation.
 *
 * @author Jordan Halterman
 *
 * @param <T> The serializable type.
 */
public class DefaultSerializer<T extends Serializable> extends Serializer<T> {
  private final ObjectMapper mapper;

  public DefaultSerializer(Class<T> type) {
    this(type, new InclusiveAnnotationIntrospector());
  }

  public DefaultSerializer(Class<T> type, AnnotationIntrospector introspector) {
    super(type);
    mapper = new ObjectMapper();
    mapper.setAnnotationIntrospector(introspector);
  }

  @Override
  public JsonObject serialize(T object) {
    try {
      return new JsonObject(mapper.writeValueAsString(object));
    }
    catch (Exception e) {
      throw new SerializationException(e.getMessage());
    }
  }

  @Override
  public T deserialize(JsonObject json) {
    try {
      return mapper.readValue(json.encode(), type);
    }
    catch (Exception e) {
      throw new DeserializationException(e.getMessage());
    }
  }

}
