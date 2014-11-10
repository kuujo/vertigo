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

import io.vertx.core.ServiceHelper;
import io.vertx.core.json.JsonObject;
import net.kuujo.vertigo.spi.ConfigFormat;

/**
 * Configuration utility.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public final class Configs {

  /**
   * Loads the global configuration.
   *
   * @return The global configuration.
   */
  public static JsonObject load() {
    return format.load();
  }

  /**
   * Loads a named configuration.
   *
   * @param config The name of the configuration to load.
   * @return The loaded configuration.
   */
  public static JsonObject load(String config) {
    return format.load(config);
  }

  /**
   * Loads a named configuration.
   *
   * @param config The name of the configuration to load.
   * @param defaults An object of default values to apply to the configuration.
   * @return The loaded configuration.
   */
  public static JsonObject load(String config, JsonObject defaults) {
    return format.load(config, defaults);
  }

  static ConfigFormat format = ServiceHelper.loadFactory(ConfigFormat.class);

}
