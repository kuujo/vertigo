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

import net.kuujo.vertigo.serializer.impl.DefaultSerializerFactory;

import org.vertx.java.core.json.JsonObject;

/**
 * A base serializer.<p>
 *
 * Serializers in Vertigo are type specific. This means that different serializers
 * may be provided for different types of objects. To register type-specific
 * serializers see the {@link DefaultSerializerFactory}.
 *
 * @author Jordan Halterman
 */
public abstract class Serializer<T extends Serializable> {
  protected final Class<T> type;

  public Serializer(Class<T> type) {
    this.type = type;
  }

  /**
   * Gets the serializer type.
   *
   * @return
   *   The serializable type.
   */
  public Class<T> type() {
    return type;
  }

  /**
   * Serializes an object to Json. If an error occurs during serialization, a
   * {@link SerializationException} will be thrown.
   *
   * @param object
   *   The object to serialize.
   * @return
   *   A Json representation of the serializable object.
   * @throws SerializationException
   *   If an error occurs during serialization.
   */
  public abstract JsonObject serialize(T object);

  /**
   * Deserializes an object from Json. If an error occurs during deserialization, a
   * {@link DeserializationException} will be thrown.
   *
   * @param json
   *   A Json representation of the serializable object.
   * @return
   *   The deserialized object.
   * @throws DeserializationException
   *   If an error occurs during deserialization.
   */
  public abstract T deserialize(JsonObject json);

  @Deprecated
  public T deserialize(JsonObject json, Class<T> type) {
    return deserialize(json);
  }

}
