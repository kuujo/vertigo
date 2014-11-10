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
package net.kuujo.vertigo.spi;

import io.vertx.core.json.JsonObject;

/**
 * Vertigo configuration format.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public interface ConfigFormat {

  /**
   * Loads the global configuration.
   *
   * @return The global configuration.
   */
  JsonObject load();

  /**
   * Loads a named configuration.
   *
   * @param config The configuration name.
   * @return The loaded configuration.
   */
  default JsonObject load(String config) {
    return load(config, new JsonObject());
  }

  /**
   * Loads a named configuration.
   *
   * @param config The configuration name.
   * @param defaults The configuration default values.
   * @return The loaded configuration.
   */
  JsonObject load(String config, JsonObject defaults);

}
