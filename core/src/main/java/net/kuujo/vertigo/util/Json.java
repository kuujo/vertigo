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
package net.kuujo.vertigo.util;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.json.JsonObject;
import net.kuujo.vertigo.VertigoException;

import java.io.IOException;

/**
 * JSON utilities.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public final class Json {
  private static final ObjectMapper mapper = new ObjectMapper();

  /**
   * JSON serializable interface.
   */
  @JsonIgnoreProperties(ignoreUnknown=true)
  @JsonInclude(JsonInclude.Include.ALWAYS)
  @JsonAutoDetect(
    creatorVisibility= JsonAutoDetect.Visibility.NONE,
    fieldVisibility=JsonAutoDetect.Visibility.ANY,
    getterVisibility=JsonAutoDetect.Visibility.NONE,
    isGetterVisibility=JsonAutoDetect.Visibility.NONE,
    setterVisibility=JsonAutoDetect.Visibility.NONE
  )
  public static interface Serializable {
  }

  /**
   * Serializes an object to JSON.
   *
   * @param object The object to serialize.
   * @param <T> The serializable type.
   * @return The serialized object.
   */
  public static <T extends Json.Serializable> JsonObject serialize(T object) {
    try {
      return new JsonObject(mapper.writeValueAsString(object));
    } catch (IOException e) {
      throw new VertigoException(e);
    }
  }

  /**
   * Deserializes an object from JSON.
   *
   * @param json The JSON from which to deserialize the object.
   * @param type The object type to deserialize.
   * @param <T> The deserialized object type.
   * @return The deserialized object.
   */
  public static <T extends Json.Serializable> T deserialize(JsonObject json, Class<T> type) {
    try {
      return mapper.readValue(json.encode(), type);
    } catch (IOException e) {
      throw new VertigoException(e);
    }
  }

}
