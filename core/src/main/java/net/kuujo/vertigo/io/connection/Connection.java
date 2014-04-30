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

import net.kuujo.vertigo.io.Closeable;
import net.kuujo.vertigo.io.Openable;

/**
 * Connection between two instances of two components.<p>
 *
 * When a connection is created between two components, each component
 * instance will create a single connection for each instance of the
 * component to which it is connected. Connections are inherently
 * uni-directional, so each connection has an input and an output side.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 *
 * @param <T> The connection type.
 */
public interface Connection<T extends Connection<T>> extends Openable<T>, Closeable<T> {

  /**
   * Returns the connection address.
   *
   * @return The connection address.
   */
  String address();

  /**
   * Returns the current connection size.
   *
   * @return The current connection size.
   */
  int size();

}
