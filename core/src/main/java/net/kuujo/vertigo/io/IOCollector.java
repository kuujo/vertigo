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

import net.kuujo.vertigo.io.port.Port;

/**
 * Input/output collector.<p>
 *
 * Collectors are simple interfaces to collections of ports.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 *
 * @param <T> The collector type.
 */
public interface IOCollector<T extends IOCollector<T>> extends Openable<T>, Closeable<T> {

  /**
   * Returns a named port.
   *
   * @param name The port name.
   * @return A named port.
   */
  @SuppressWarnings("rawtypes")
  Port port(String name);

}
