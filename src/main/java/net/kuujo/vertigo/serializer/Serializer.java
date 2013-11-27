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

import org.vertx.java.core.json.JsonObject;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

/**
 * An object serializer.
 *
 * This serializer uses an internal {@link ObjectMapper} to serialize and
 * deserialize objects to and from Json. By default, the serializer will automatically
 * recognize any fields of the class being serialized, but getters, setters,
 * and creators must be explicitly noted. See the Jackson Json serializer
 * documentation for more information.
 *
 * @author Jordan Halterman
 */
public final class Serializer {
  private static final String DEFAULT_SERIALIZER = "default";
  private static final Map<String, Serializer> instances = new HashMap<>();

  /**
   * Gets a default serializer instance.
   *
   * @return
   *   A default serializer instance.
   */
  public static Serializer getInstance() {
    return getInstance(DEFAULT_SERIALIZER);
  }

  /**
   * Gets a serializer instance.
   *
   * @param name
   *   The instance name.
   * @return
   *   A serializer instance.
   */
  public static Serializer getInstance(String name) {
    if (!instances.containsKey(name)) {
      instances.put(name, new Serializer(name));
    }
    return instances.get(name);
  }

  private final ObjectMapper mapper = new ObjectMapper();
  private final SimpleModule module;

  private Serializer(String name) {
    module = new SimpleModule(name);
    mapper.registerModule(module);
    configureMapper();
  }

  /**
   * Sets auto-detect visibility to none for the mapper.
   */
  private void configureMapper() {
    mapper.setVisibilityChecker(mapper.getVisibilityChecker()
        .with(JsonAutoDetect.Visibility.NONE)
        .withFieldVisibility(JsonAutoDetect.Visibility.ANY));
  }

  /**
   * Gets the internal Jackson object mapper.
   *
   * @return
   *   The internal object mapper.
   */
  public ObjectMapper getMapper() {
    return mapper;
  }

  /**
   * Adds a type-specific serializer to the serializer.
   *
   * @param serializer
   *   The serializer to register.
   * @return
   *   The called serializer instance.
   */
  public <T> Serializer addSerializer(JsonSerializer<T> serializer) {
    module.addSerializer(serializer);
    return this;
  }

  /**
   * Adds a type-specific deserializer to the serializer.
   *
   * @param serializer
   *   The serializer to register.
   * @return
   *   The called serializer instance.
   */
  public <T> Serializer addDeserializer(Class<T> type, JsonDeserializer<T> deserializer) {
    module.addDeserializer(type, deserializer);
    return this;
  }

  /**
   * Serializes an object.
   *
   * @param object
   *   The object to serialize.
   * @return
   *   The serialized object.
   * @throws SerializationException
   *   If the object cannot be serialized.
   */
  public JsonObject serialize(Object object) throws SerializationException {
    try {
      return new JsonObject(mapper.writeValueAsString(object));
    }
    catch (Exception e) {
      throw new SerializationException(e);
    }
  }

  /**
   * Deserializes an object.
   *
   * @param json
   *   The serialized object.
   * @return
   *   The deserialized object.
   * @throws SerializationException
   *   If the object cannot be deserialized.
   */
  @SuppressWarnings("unchecked")
  public <T> T deserialize(JsonObject json, Class<?> type) throws SerializationException {
    try {
      return (T) mapper.readValue(json.encode(), type);
    }
    catch (Exception e) {
      throw new SerializationException(e);
    }
  }

}
