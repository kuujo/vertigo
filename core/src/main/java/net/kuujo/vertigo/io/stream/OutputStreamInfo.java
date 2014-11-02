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
package net.kuujo.vertigo.io.stream;

import io.vertx.codegen.annotations.VertxGen;
import net.kuujo.vertigo.TypeInfo;
import net.kuujo.vertigo.io.connection.OutputConnectionInfo;
import net.kuujo.vertigo.io.port.OutputPortInfo;
import net.kuujo.vertigo.io.partitioner.Partitioner;
import net.kuujo.vertigo.io.stream.impl.OutputStreamInfoImpl;

import java.util.Collection;
import java.util.List;

/**
 * The output stream context represents a set of output connections
 * from one component partition to all partitions of another component.
 * The context contains information about how to dispatch messages
 * between the group of target component partitions.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
@VertxGen
public interface OutputStreamInfo extends TypeInfo<OutputStreamInfo> {

  /**
   * Returns a new output stream info builder.
   *
   * @return A new output stream info builder.
   */
  static Builder builder() {
    return new OutputStreamInfoImpl.Builder();
  }

  /**
   * Returns a new output stream info builder.
   *
   * @param stream An existing output stream info object to wrap.
   * @return An output stream builder wrapper.
   */
  static Builder builder(OutputStreamInfo stream) {
    return new OutputStreamInfoImpl.Builder((OutputStreamInfoImpl) stream);
  }

  /**
   * Returns the parent output port context.
   *
   * @return The parent port context.
   */
  OutputPortInfo port();

  /**
   * Returns the stream connection partitioner.
   *
   * @return The stream connection partitioner.
   */
  Partitioner partitioner();

  /**
   * Returns a list of output connections.
   *
   * @return A list of output connections.
   */
  List<OutputConnectionInfo> connections();

  /**
   * Output stream info builder.
   */
  public static interface Builder extends TypeInfo.Builder<OutputStreamInfo> {

    /**
     * Adds a connection to the stream.
     *
     * @param connection The output connection info to add.
     * @return The output stream info builder.
     */
    Builder addConnection(OutputConnectionInfo connection);

    /**
     * Removes a connection from the stream.
     *
     * @param connection The output connection info to remove.
     * @return The output stream info builder.
     */
    Builder removeConnection(OutputConnectionInfo connection);

    /**
     * Sets all connections on the stream.
     *
     * @param connections A collection of output connection info to add.
     * @return The output stream info builder.
     */
    Builder setConnections(OutputConnectionInfo... connections);

    /**
     * Sets all connections on the stream.
     *
     * @param connections A collection of output connection info to add.
     * @return The output stream info builder.
     */
    Builder setConnections(Collection<OutputConnectionInfo> connections);

    /**
     * Sets the parent output port.
     *
     * @param port The output port info.
     * @return The output stream info builder.
     */
    Builder setPort(OutputPortInfo port);
  }

}
