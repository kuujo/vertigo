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

package net.kuujo.vertigo.io.port.impl;

import net.kuujo.vertigo.TypeInfo;
import net.kuujo.vertigo.io.InputInfo;
import net.kuujo.vertigo.io.connection.InputConnectionInfo;
import net.kuujo.vertigo.io.port.InputPortInfo;
import net.kuujo.vertigo.util.Args;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Input port info implementation.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class InputPortInfoImpl extends BasePortInfoImpl<InputPortInfo> implements InputPortInfo {
  private InputInfo input;
  private List<InputConnectionInfo> connections = new ArrayList<>();

  @Override
  public InputInfo input() {
    return input;
  }

  @Override
  public Collection<InputConnectionInfo> connections() {
    return connections;
  }

  /**
   * Input port info builder.
   */
  public static class Builder implements TypeInfo.Builder<InputPortInfo> {
    private final InputPortInfoImpl port;

    public Builder() {
      port = new InputPortInfoImpl();
    }

    public Builder(InputPortInfoImpl port) {
      this.port = port;
    }

    /**
     * Adds a connection to the port.
     *
     * @param connection The input connection info.
     * @return The input port info builder.
     */
    public Builder addConnection(InputConnectionInfo connection) {
      Args.checkNotNull(connection, "connection cannot be null");
      port.connections.add(connection);
      return this;
    }

    /**
     * Removes a connection from the port.
     *
     * @param connection The input connection info.
     * @return The input port info builder.
     */
    public Builder removeConnection(InputConnectionInfo connection) {
      Args.checkNotNull(connection, "connection cannot be null");
      port.connections.remove(connection);
      return this;
    }

    /**
     * Sets all connections on the port.
     *
     * @param connections A collection of input connection info.
     * @return The input port info builder.
     */
    public Builder setConnections(InputConnectionInfo... connections) {
      port.connections = new ArrayList<>(Arrays.asList(connections));
      return this;
    }

    /**
     * Sets all connections on the port.
     *
     * @param connections A collection of input connection info.
     * @return The input port info builder.
     */
    public Builder setConnections(Collection<InputConnectionInfo> connections) {
      Args.checkNotNull(connections, "connections cannot be null");
      port.connections = new ArrayList<>(connections);
      return this;
    }

    /**
     * Sets the parent input.
     *
     * @param input The parent input.
     * @return The input port builder.
     */
    public Builder setInput(InputInfo input) {
      Args.checkNotNull(input, "input cannot be null");
      port.input = input;
      return this;
    }

    @Override
    public InputPortInfoImpl build() {
      return port;
    }
  }

}
