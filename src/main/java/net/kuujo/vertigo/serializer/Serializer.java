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

/**
 * A Vertigo object serializer.
 *
 * @author Jordan Halterman
 */
public interface Serializer {

  /**
   * Serializes an object.
   *
   * @param object
   *   The object to serialize.
   * @return
   *   The serialized object.
   * @throws SerializationException
   *   If serialization fails.
   */
  JsonObject serialize(Serializable object) throws SerializationException;

  /**
   * Deserializes an object.
   *
   * @param serialized
   *   The serialized object.
   * @param type
   *   The deserialization type.
   * @return
   *   The deserialized object.
   * @throws SerializationException
   *   If deserialization fails.
   */
  <T extends Serializable> T deserialize(JsonObject serialized, Class<T> type) throws SerializationException;

}
