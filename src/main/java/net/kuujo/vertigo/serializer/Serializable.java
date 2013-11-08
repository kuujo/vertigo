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
 * A serializable object.
 *
 * @author Jordan Halterman
 */
public interface Serializable {

  /**
   * Gets the object state.
   *
   * @return
   *   A JSON object representation of the serializable object's internal state.
   */
  JsonObject getState();

  /**
   * Sets the object state.
   *
   * @param state
   *   A JSON object representation of the serializable object's internal state.
   * @throws SerializationException
   *   If the given state is not applicable.
   */
  void setState(JsonObject state) throws SerializationException;

}
