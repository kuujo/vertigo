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

import net.kuujo.vertigo.impl.BaseContextImpl;
import net.kuujo.vertigo.io.connection.OutputConnectionContext;
import net.kuujo.vertigo.io.partition.Partitioner;
import net.kuujo.vertigo.io.port.OutputPortContext;
import net.kuujo.vertigo.io.stream.OutputStreamContext;
import net.kuujo.vertigo.util.Args;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Output stream context implementation.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class OutputStreamContextImpl extends BaseContextImpl<OutputStreamContext> implements OutputStreamContext {
  private OutputPortContext port;
  private Partitioner partitioner;
  private List<OutputConnectionContext> connections = new ArrayList<>();

  @Override
  public OutputPortContext port() {
    return port;
  }

  @Override
  public Partitioner partitioner() {
    return partitioner;
  }

  @Override
  public List<OutputConnectionContext> connections() {
    return connections;
  }

  /**
   * Output stream context builder.
   */
  public static class Builder implements OutputStreamContext.Builder {
    private final OutputStreamContextImpl stream;

    public Builder() {
      stream = new OutputStreamContextImpl();
    }

    public Builder(OutputStreamContextImpl stream) {
      this.stream = stream;
    }

    @Override
    public Builder setId(String id) {
      Args.checkNotNull(id, "id cannot be null");
      stream.id = id;
      return this;
    }

    @Override
    public Builder addConnection(OutputConnectionContext connection) {
      Args.checkNotNull(connection, "connection cannot be null");
      stream.connections.add(connection);
      return this;
    }

    @Override
    public Builder removeConnection(OutputConnectionContext connection) {
      Args.checkNotNull(connection, "connection cannot be null");
      stream.connections.remove(connection);
      return this;
    }

    @Override
    public Builder setConnections(OutputConnectionContext... connections) {
      stream.connections = new ArrayList<>(Arrays.asList(connections));
      return this;
    }

    @Override
    public Builder setConnections(Collection<OutputConnectionContext> connections) {
      Args.checkNotNull(connections, "connections cannot be null");
      stream.connections = new ArrayList<>(connections);
      return this;
    }

    @Override
    public Builder setPort(OutputPortContext port) {
      Args.checkNotNull(port, "port cannot be null");
      stream.port = port;
      return this;
    }

    @Override
    public OutputStreamContext.Builder setPartitioner(Partitioner partitioner) {
      stream.partitioner = Args.checkNotNull(partitioner, "partitioner cannot be null");
      return this;
    }

    @Override
    public OutputStreamContextImpl build() {
      return stream;
    }
  }

}
