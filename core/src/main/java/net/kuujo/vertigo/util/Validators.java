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

import net.kuujo.vertigo.spi.ConfigValidator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * Configuration validator utilities.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public final class Validators {
  private static final Map<Class, List<?>> validators = new HashMap<>();

  /**
   * Validates the configuration for the given value.
   *
   * @param value The value for which to validate the configuration.
   * @param type The validator type.
   * @param <T> The value type.
   * @param <U> The validator type.
   * @return The validated configuration value.
   * @throws net.kuujo.vertigo.network.ValidationException if a validation error occurs.
   */
  public static <T, U extends ConfigValidator<T>> T validate(T value, Class<U> type) {
    for (U validator : ServiceLoader.load(type)) {
      validator.validate(value);
    }
    return value;
  }

}
