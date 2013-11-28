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

import org.vertx.java.core.json.JsonObject;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * A default serializer implementation.
 *
 * This serializer uses an internal {@link ObjectMapper} to serialize and
 * deserialize objects to and from Json. By default, the serializer will automatically
 * recognize any fields of the class being serialized, but getters, setters,
 * and creators must be explicitly noted. See the Jackson Json serializer
 * documentation for more information.
 *
 * @author Jordan Halterman
 */
public class DefaultSerializer implements Serializer {
  private final ObjectMapper mapper;

  public DefaultSerializer() {
    mapper = new ObjectMapper();
    mapper.setVisibilityChecker(mapper.getVisibilityChecker()
        .with(JsonAutoDetect.Visibility.NONE)
        .withFieldVisibility(JsonAutoDetect.Visibility.ANY));
  }

  @Override
  public JsonObject serialize(Object object) throws SerializationException {
    try {
      return new JsonObject(mapper.writeValueAsString(object));
    }
    catch (Exception e) {
      throw new SerializationException(e);
    }
  }

  @Override
  public <T> T deserialize(JsonObject json, Class<T> type) throws SerializationException {
    try {
      return (T) mapper.readValue(json.encode(), type);
    }
    catch (Exception e) {
      throw new SerializationException(e);
    }
  }

}
