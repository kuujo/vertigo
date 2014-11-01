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

package net.kuujo.vertigo.io.stream.impl;

import net.kuujo.vertigo.TypeInfo;
import net.kuujo.vertigo.impl.BaseTypeInfoImpl;
import net.kuujo.vertigo.io.connection.OutputConnectionInfo;
import net.kuujo.vertigo.io.partitioner.Partitioner;
import net.kuujo.vertigo.io.port.OutputPortInfo;
import net.kuujo.vertigo.io.stream.OutputStreamInfo;
import net.kuujo.vertigo.util.Args;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Output stream info implementation.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class OutputStreamInfoImpl extends BaseTypeInfoImpl<OutputStreamInfo> implements OutputStreamInfo {
  private OutputPortInfo port;
  private Partitioner partitioner;
  private List<OutputConnectionInfo> connections = new ArrayList<>();

  @Override
  public OutputPortInfo port() {
    return port;
  }

  @Override
  public Partitioner partitioner() {
    return partitioner;
  }

  @Override
  public List<OutputConnectionInfo> connections() {
    return connections;
  }

  /**
   * Output stream info builder.
   */
  public static class Builder implements TypeInfo.Builder<OutputStreamInfo> {
    private final OutputStreamInfoImpl stream;

    public Builder() {
      stream = new OutputStreamInfoImpl();
    }

    public Builder(OutputStreamInfoImpl stream) {
      this.stream = stream;
    }

    /**
     * Adds a connection to the stream.
     *
     * @param connection The output connection info to add.
     * @return The output stream info builder.
     */
    public Builder addConnection(OutputConnectionInfo connection) {
      Args.checkNotNull(connection, "connection cannot be null");
      stream.connections.add(connection);
      return this;
    }

    /**
     * Removes a connection from the stream.
     *
     * @param connection The output connection info to remove.
     * @return The output stream info builder.
     */
    public Builder removeConnection(OutputConnectionInfo connection) {
      Args.checkNotNull(connection, "connection cannot be null");
      stream.connections.remove(connection);
      return this;
    }

    /**
     * Sets all connections on the stream.
     *
     * @param connections A collection of output connection info to add.
     * @return The output stream info builder.
     */
    public Builder setConnections(OutputConnectionInfo... connections) {
      stream.connections = new ArrayList<>(Arrays.asList(connections));
      return this;
    }

    /**
     * Sets all connections on the stream.
     *
     * @param connections A collection of output connection info to add.
     * @return The output stream info builder.
     */
    public Builder setConnections(Collection<OutputConnectionInfo> connections) {
      Args.checkNotNull(connections, "connections cannot be null");
      stream.connections = new ArrayList<>(connections);
      return this;
    }

    /**
     * Sets the parent output port.
     *
     * @param port The output port info.
     * @return The output stream info builder.
     */
    public Builder setPort(OutputPortInfo port) {
      Args.checkNotNull(port, "port cannot be null");
      stream.port = port;
      return this;
    }

    @Override
    public OutputStreamInfoImpl build() {
      return stream;
    }
  }

}
