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

package net.kuujo.vertigo.io.connection;


import net.kuujo.vertigo.TypeContext;

/**
 * Connection endpoint context.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 *
 * @param <T> The endpoint type.
 */
public interface EndpointContext<T extends EndpointContext<T>> extends TypeContext<T> {

  /**
   * Returns the endpoint component name.
   *
   * @return The endpoint component name.
   */
  String component();

  /**
   * Returns the endpoint port name.
   *
   * @return The endpoint port name.
   */
  String port();

}
