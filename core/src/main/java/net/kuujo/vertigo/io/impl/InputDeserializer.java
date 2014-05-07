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
package net.kuujo.vertigo.io.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;

import net.kuujo.vertigo.util.serialization.SerializationException;

import org.vertx.java.core.json.JsonObject;

/**
 * Handles deserialization of input objects.<p>
 *
 * The deserializer deserializes {@link JsonObject} messages to any
 * type that is supported by the format. If a message was serialized
 * using the Vertigo serializer, it can deserialize the message
 * back to the original object.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class InputDeserializer {

  /**
   * Object input stream that loads the class from the current context class loader.
   */
  private class ThreadObjectInputStream extends ObjectInputStream {

    public ThreadObjectInputStream(InputStream in) throws IOException {
      super(in);
    }

    @Override
    public Class<?> resolveClass(ObjectStreamClass desc) throws ClassNotFoundException, IOException {
      try {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        return loader.loadClass(desc.getName());
      } catch (Exception e) {
      }
      return super.resolveClass(desc);
    }

  }

  /**
   * Deserializes an input message.
   *
   * @param message The message to deserialize.
   * @return The message value.
   */
  public Object deserialize(JsonObject message) {
    boolean serialized = message.getBoolean("serialized", false);
    if (!serialized) {
      return message.getValue("value");
    }

    byte[] bytes = message.getBinary("value");
    ObjectInputStream stream = null;
    try {
      stream = new ThreadObjectInputStream(new ByteArrayInputStream(bytes));
      return stream.readObject();
    } catch (ClassNotFoundException | IOException e) {
      throw new SerializationException(e.getMessage());
    } finally {
      if (stream != null) {
        try {
          stream.close();
        } catch (IOException e) {
        }
      }
    }
  }

}
