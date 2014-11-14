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
package net.kuujo.vertigo.io;

import net.kuujo.vertigo.TypeConfig;
import net.kuujo.vertigo.component.ComponentConfig;
import net.kuujo.vertigo.io.port.PortConfig;

import java.util.Collection;

/**
 * Input/output collector info.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public interface CollectorConfig<T extends CollectorConfig<T, U>, U extends PortConfig<U>> extends TypeConfig {

  /**
   * Returns the parent component.
   *
   * @return The parent component.
   */
  ComponentConfig getComponent();

  /**
   * Sets the parent component.
   *
   * @param component The parent component.
   * @return The collector info.
   */
  T setComponent(ComponentConfig component);

  /**
   * Returns the set of ports.
   *
   * @return A collection of ports.
   */
  Collection<U> getPorts();

  /**
   * Sets the input ports.
   *
   * @param ports A collection of input ports.
   * @return The input info.
   */
  T setPorts(Collection<U> ports);

  /**
   * Returns info for a named port.
   *
   * @param name The port name.
   * @return The port info.
   */
  U getPort(String name);

  /**
   * Sets info for a named port.
   *
   * @param name The port name.
   * @param type The port type class.
   * @return The port info.
   */
  T setPort(String name, Class<?> type);

  /**
   * Adds a named port.
   *
   * @param name The port name.
   * @return The port info.
   */
  U addPort(String name);

  /**
   * Adds a named port.
   *
   * @param name The port name.
   * @param type The port type class.
   * @return The port info.
   */
  U addPort(String name, Class<?> type);

  /**
   * Removes a named port.
   *
   * @param name The port name.
   * @return The collector info.
   */
  T removePort(String name);

}
