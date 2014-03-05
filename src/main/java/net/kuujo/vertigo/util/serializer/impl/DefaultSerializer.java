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
package net.kuujo.vertigo.util.serializer.impl;

import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.kuujo.vertigo.util.serializer.DeserializationException;
import net.kuujo.vertigo.util.serializer.Serializable;
import net.kuujo.vertigo.util.serializer.SerializationException;
import net.kuujo.vertigo.util.serializer.Serializer;

/**
 * A default serializer implementation.
 *
 * @author Jordan Halterman
 */
public class DefaultSerializer implements Serializer {
  private final ObjectMapper mapper;

  public DefaultSerializer() {
    this(new InclusiveAnnotationIntrospector());
  }

  public DefaultSerializer(AnnotationIntrospector introspector) {
    mapper = new ObjectMapper();
    mapper.setAnnotationIntrospector(introspector);
  }

  @Override
  public <T extends Serializable> String serializeToString(T object) {
    try {
      return mapper.writeValueAsString(object);
    }
    catch (Exception e) {
      throw new SerializationException(e.getMessage());
    }
  }

  @Override
  public <T extends Serializable> T deserializeString(String json, Class<T> type) {
    try {
      return mapper.readValue(json, type);
    }
    catch (Exception e) {
      throw new SerializationException(e.getMessage());
    }
  }

  @Override
  public <T> T deserializeString(String json, TypeReference<T> type) {
    try {
      return mapper.readValue(json, type);
    }
    catch (Exception e) {
      throw new SerializationException(e.getMessage());
    }
  }

  @Override
  public <T extends Serializable> JsonObject serializeToObject(T object) {
    try {
      return new JsonObject(mapper.writeValueAsString(object));
    }
    catch (Exception e) {
      throw new SerializationException(e.getMessage());
    }
  }

  @Override
  public <T extends Serializable> T deserializeObject(JsonObject json, Class<T> type) {
    try {
      return mapper.readValue(json.encode(), type);
    }
    catch (Exception e) {
      throw new DeserializationException(e.getMessage());
    }
  }

  @Override
  public <T> T deserializeObject(JsonObject json, TypeReference<T> type) {
    try {
      return mapper.readValue(json.encode(), type);
    }
    catch (Exception e) {
      throw new DeserializationException(e.getMessage());
    }
  }

  @Override
  public <T> T deserializeObject(JsonArray json, Class<T> type) {
    try {
      return mapper.readValue(json.encode(), type);
    }
    catch (Exception e) {
      throw new DeserializationException(e.getMessage());
    }
  }

  @Override
  public <T> T deserializeObject(JsonArray json, TypeReference<T> type) {
    try {
      return mapper.readValue(json.encode(), type);
    }
    catch (Exception e) {
      throw new DeserializationException(e.getMessage());
    }
  }

}
