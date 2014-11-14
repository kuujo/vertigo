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

/**
 * Configuration resolver.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public interface ConfigResolver<T> {

  /**
   * Returns the resolver weight.
   *
   * @return The resolver weight.
   */
  default int weight() {
    return 0;
  }

  /**
   * Resolves the configuration for the given object.
   *
   * @param value The value for which to resolve the configuration.
   * @return The resolved configuration.
   */
  T resolve(T value);

}
