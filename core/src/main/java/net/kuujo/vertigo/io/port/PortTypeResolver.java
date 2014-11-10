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
package net.kuujo.vertigo.io.port;

import io.vertx.core.ServiceHelper;

/**
 * Port type resolver.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public interface PortTypeResolver {

  /**
   * Resolves a port type string.
   *
   * @param type The port type string to resolve.
   * @return The port type class.
   */
  static Class<?> resolveType(String type) {
    return resolver.resolve(type);
  }

  /**
   * Resolves a port type string.
   *
   * @param type The port type string to resolve.
   * @return The port type class.
   */
  Class<?> resolve(String type);

  static PortTypeResolver resolver = ServiceHelper.loadFactory(PortTypeResolver.class);

}
