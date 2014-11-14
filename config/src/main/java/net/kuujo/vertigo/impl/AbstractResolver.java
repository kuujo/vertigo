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
package net.kuujo.vertigo.impl;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigList;
import com.typesafe.config.ConfigValue;
import com.typesafe.config.ConfigValueType;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.Map;

/**
 * Base class for resolvers.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
abstract class AbstractResolver {

  /**
   * Converts a Typesafe configuration object to {@link io.vertx.core.json.JsonObject}
   *
   * @param config The Typesafe configuration object to convert.
   * @return The converted configuration.
   */
  @SuppressWarnings("unchecked")
  protected static JsonObject configObjectToJson(Config config) {
    JsonObject json = new JsonObject();
    for (Map.Entry<String, ConfigValue> entry : config.entrySet()) {
      String key = entry.getKey();
      ConfigValue value = entry.getValue();
      if (key.contains(".")) {
        key = key.substring(0, key.indexOf('.'));
        value = config.getValue(key);
      }
      if (value.valueType() == ConfigValueType.OBJECT) {
        json.put(key, configObjectToJson(config.getConfig(key)));
      } else if (value.valueType() == ConfigValueType.LIST) {
        json.put(key, configListToJson(config.getList(key)));
      } else  {
        json.put(key, value.unwrapped());
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
  protected static JsonArray configListToJson(ConfigList configs) {
    JsonArray json = new JsonArray();
    for (ConfigValue value : configs) {
      if (value.valueType() == ConfigValueType.OBJECT) {
        json.add(configObjectToJson((Config) value));
      } else if (value.valueType() == ConfigValueType.LIST) {
        json.add(configListToJson((ConfigList) value));
      } else {
        json.add(value.unwrapped());
      }
    }
    return json;
  }

}
