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
 * An object serializer.
 *
 * @author Jordan Halterman
 */
public class Serializer {

  private Serializer() {
  }

  /**
   * Serializes an object.
   *
   * @param serializable
   *   The serializable object.
   * @return
   *   Serialized object information.
   */
  public static JsonObject serialize(Serializable serializable) {
    JsonObject serialized = new JsonObject();
    serialized.putString("class", serializable.getClass().getName());
    serialized.putObject("state", serializable.getState());
    return serialized;
  }

  /**
   * Unserializes a serialized object.
   *
   * @param serialized
   *   The serialized object information.
   * @return
   *   An unserialized object.
   * @throws SerializationException
   *   If the object class cannot be found.
   */
  @SuppressWarnings("unchecked")
  public static <T extends Serializable> T unserialize(JsonObject serialized) throws SerializationException {
    String className = serialized.getString("class");
    if (className == null) {
      throw new SerializationException("Invalid serialization info. No class name found.");
    }

    JsonObject state = serialized.getObject("state");
    if (state == null) {
      throw new SerializationException("Invalid serialization info. No object state found.");
    }

    try {
      T obj = (T) Class.forName(className).newInstance();
      obj.setState(state);
      return obj;
    }
    catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
      throw new SerializationException(e);
    }
  }

}
