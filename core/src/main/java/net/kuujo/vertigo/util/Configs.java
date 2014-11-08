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

import com.typesafe.config.Config;
import com.typesafe.config.ConfigObject;
import com.typesafe.config.ConfigValue;
import com.typesafe.config.ConfigValueType;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.List;
import java.util.Map;

/**
 * Configuration utility.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public final class Configs {

  /**
   * Converts a Typesafe configuration object to {@link JsonObject}
   *
   * @param config The Typesafe configuration object to convert.
   * @return The converted configuration.
   */
  @SuppressWarnings("unchecked")
  public static JsonObject configObjectToJson(Config config) {
    JsonObject json = new JsonObject();
    for (Map.Entry<String, ConfigValue> entry : config.entrySet()) {
      ConfigValue value = entry.getValue();
      if (value.valueType().equals(ConfigValueType.OBJECT)) {
        json.put(entry.getKey(), configObjectToJson(((ConfigObject) value.unwrapped()).toConfig()));
      } else if (value.valueType().equals(ConfigValueType.LIST)) {
        json.put(entry.getKey(), configListToJson((List<? extends ConfigValue>) value.unwrapped()));
      } else  {
        json.put(entry.getKey(), value.unwrapped());
      }
    }
    return json;
  }

  /**
   * Converts a Typesafe configuration list to {@link JsonObject}
   *
   * @param configs The Typesafe configuration list to convert.
   * @return The converted configuration.
   */
  @SuppressWarnings("unchecked")
  private static JsonArray configListToJson(List<? extends ConfigValue> configs) {
    JsonArray json = new JsonArray();
    for (ConfigValue value : configs) {
      if (value.valueType().equals(ConfigValueType.OBJECT)) {
        json.add(configObjectToJson(((ConfigObject) value.unwrapped()).toConfig()));
      } else if (value.valueType().equals(ConfigValueType.LIST)) {
        json.add(configListToJson((List<? extends ConfigValue>) value.unwrapped()));
      } else {
        json.add(value.unwrapped());
      }
    }
    return json;
  }

}
