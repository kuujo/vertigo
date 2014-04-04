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
package net.kuujo.vertigo.network;

import net.kuujo.vertigo.util.serializer.JsonSerializable;

import org.vertx.java.core.json.JsonObject;

/**
 * Base configuration.
 *
 * @author Jordan Halterman
 */
public interface Config<T extends Config<T>> extends JsonSerializable {

  /**
   * Sets arbitrary configuration options.
   *
   * @param options A json object of configuration options.
   * @return The configuration instance.
   */
  T setOptions(JsonObject options);

  /**
   * Returns arbitrary configuration options.
   *
   * @return A json object of configuration options.
   */
  JsonObject getOptions();

  /**
   * Sets an arbitrary configuration option.
   *
   * @param option The option name.
   * @param value The option value.
   * @return The configuration instance.
   */
  T setOption(String option, Object value);

  /**
   * Returns an arbitrary configuration option.
   *
   * @param option The option name.
   * @return The configured option value.
   */
  @SuppressWarnings("hiding")
  <T> T getOption(String option);

  /**
   * Returns an arbitrary configuration option.
   *
   * @param option The option name.
   * @param defaultValue The default option value.
   * @return The configured option value.
   */
  @SuppressWarnings("hiding")
  <T> T getOption(String option, T defaultValue);

}
