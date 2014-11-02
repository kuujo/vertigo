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
package net.kuujo.vertigo.input.port;

import io.vertx.codegen.annotations.VertxGen;
import net.kuujo.vertigo.TypeInfo;
import net.kuujo.vertigo.input.InputInfo;
import net.kuujo.vertigo.input.connection.InputConnectionInfo;
import net.kuujo.vertigo.input.port.impl.InputPortInfoImpl;

import java.util.Collection;

/**
 * Input port context represents a set of input connections for a single
 * port within a single partition of a component.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
@VertxGen
public interface InputPortInfo extends TypeInfo<InputPortInfo> {

  /**
   * Returns a new input port info builder.
   *
   * @return A new input port info builder.
   */
  static Builder builder() {
    return new InputPortInfoImpl.Builder();
  }

  /**
   * Returns a new input port info builder.
   *
   * @param port An existing input port info builder to wrap.
   * @return An input port info builder wrapper.
   */
  static Builder builder(InputPortInfo port) {
    return new InputPortInfoImpl.Builder((InputPortInfoImpl) port);
  }

  /**
   * Returns the port name.
   *
   * @return The port name.
   */
  String name();

  /**
   * Returns the parent input context.
   *
   * @return The parent input context.
   */
  InputInfo input();

  /**
   * Returns a collection of input port connections.
   *
   * @return A list of input connections.
   */
  Collection<InputConnectionInfo> connections();

  /**
   * Input port info builder.
   */
  public static interface Builder extends TypeInfo.Builder<InputPortInfo> {

    /**
     * Adds a connection to the port.
     *
     * @param connection The input connection info.
     * @return The input port info builder.
     */
    Builder addConnection(InputConnectionInfo connection);

    /**
     * Removes a connection from the port.
     *
     * @param connection The input connection info.
     * @return The input port info builder.
     */
    Builder removeConnection(InputConnectionInfo connection);

    /**
     * Sets all connections on the port.
     *
     * @param connections A collection of input connection info.
     * @return The input port info builder.
     */
    Builder setConnections(InputConnectionInfo... connections);

    /**
     * Sets all connections on the port.
     *
     * @param connections A collection of input connection info.
     * @return The input port info builder.
     */
    Builder setConnections(Collection<InputConnectionInfo> connections);

    /**
     * Sets the parent input.
     *
     * @param input The parent input.
     * @return The input port builder.
     */
    Builder setInput(InputInfo input);
  }

}
