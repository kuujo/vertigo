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
package net.kuujo.vertigo.io.selector;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.kuujo.vertigo.io.connection.Connection;
import net.kuujo.vertigo.util.serialization.SerializerFactory;

import org.vertx.java.core.json.JsonObject;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;

/**
 * Custom selector wrapper.<p>
 *
 * This class is used only internally by Vertigo to handle custom connection
 * selectors. To implement a custom selector, simply extend the base
 * {@link Selector} class.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class CustomSelector implements Selector {
  private Map<String, Object> properties = new HashMap<>();
  private boolean initialized;
  private Selector selector;

  public CustomSelector() {
  }

  public CustomSelector(Selector selector) {
    this.selector = selector;
  }

  @JsonAnyGetter
  private Map<String, Object> getProperties() {
    if (selector != null) {
      properties = SerializerFactory.getSerializer(selector.getClass()).serializeToObject(selector).toMap();
    }
    return properties;
  }

  @JsonAnySetter
  private void setProperty(String name, Object value) {
    properties.put(name, value);
  }

  private void init() {
    if (!initialized) {
      String className = (String) properties.get("selector");
      if (className != null) {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        try {
          Class<?> clazz = loader.loadClass(className);
          selector = (Selector) SerializerFactory.getSerializer(clazz).deserializeObject(new JsonObject(properties), clazz);
        } catch (Exception e) {
          throw new IllegalArgumentException("Error instantiating serializer factory.");
        }
      } else {
        throw new IllegalStateException("Not a valid custom serializer.");
      }
    }
  }

  @Override
  @SuppressWarnings("rawtypes")
  public <T extends Connection> List<T> select(Object message, List<T> connections) {
    init();
    return selector.select(message, connections);
  }

}
