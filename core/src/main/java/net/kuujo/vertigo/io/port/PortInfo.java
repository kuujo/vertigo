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

/**
 * Port info.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public interface PortInfo<T extends PortInfo<T>> {

  /**
   * Returns the port name.
   *
   * @return The port name.
   */
  String getName();

  /**
   * Sets the port name.
   *
   * @param name The port name.
   * @return The port info.
   */
  T setName(String name);

  /**
   * Returns the port type.
   *
   * @return The port type.
   */
  Class<?> getType();

  /**
   * Sets the port type.
   *
   * @param type The port type.
   * @return The port info.
   */
  T setType(Class<?> type);

}
