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

import net.kuujo.vertigo.TypeInfo;
import net.kuujo.vertigo.io.port.PortInfo;

import java.util.Collection;

/**
 * Input/output collector info.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public interface CollectorInfo<T extends PortInfo<T>> extends TypeInfo {

  /**
   * Returns the set of ports.
   *
   * @return A collection of ports.
   */
  Collection<T> getPorts();

  /**
   * Sets the input ports.
   *
   * @param ports A collection of input ports.
   * @return The input info.
   */
  CollectorInfo setPorts(Collection<T> ports);

  /**
   * Returns info for a named port.
   *
   * @param name The port name.
   * @return The port info.
   */
  T getPort(String name);

  /**
   * Sets info for a named port.
   *
   * @param name The port name.
   * @param type The port type class.
   * @return The port info.
   */
  CollectorInfo setPort(String name, Class<?> type);

}
