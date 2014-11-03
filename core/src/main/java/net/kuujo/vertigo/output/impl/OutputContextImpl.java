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

package net.kuujo.vertigo.output.impl;

import net.kuujo.vertigo.component.PartitionInfo;
import net.kuujo.vertigo.impl.BaseContextImpl;
import net.kuujo.vertigo.output.OutputContext;
import net.kuujo.vertigo.output.port.OutputPortContext;
import net.kuujo.vertigo.util.Args;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Output info implementation.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class OutputContextImpl extends BaseContextImpl<OutputContext> implements OutputContext {
  private PartitionInfo partition;
  private Map<String, OutputPortContext> ports = new HashMap<>();

  @Override
  public PartitionInfo partition() {
    return partition;
  }

  @Override
  public Collection<OutputPortContext> ports() {
    return ports.values();
  }

  @Override
  public OutputPortContext port(String name) {
    return ports.get(name);
  }

  /**
   * Output info builder.
   */
  public static class Builder implements OutputContext.Builder {
    private final OutputContextImpl output;

    public Builder() {
      output = new OutputContextImpl();
    }

    public Builder(OutputContextImpl input) {
      this.output = input;
    }

    @Override
    public Builder addPort(OutputPortContext port) {
      Args.checkNotNull(port, "port cannot be null");
      output.ports.put(port.name(), port);
      return this;
    }

    @Override
    public Builder removePort(OutputPortContext port) {
      Args.checkNotNull(port, "port cannot be null");
      output.ports.remove(port.name());
      return this;
    }

    @Override
    public Builder setPorts(OutputPortContext... ports) {
      output.ports.clear();
      for (OutputPortContext port : ports) {
        output.ports.put(port.name(), port);
      }
      return this;
    }

    @Override
    public Builder setPorts(Collection<OutputPortContext> ports) {
      Args.checkNotNull(ports, "ports cannot be null");
      output.ports.clear();
      for (OutputPortContext port : ports) {
        output.ports.put(port.name(), port);
      }
      return this;
    }

    @Override
    public Builder clearPorts() {
      output.ports.clear();
      return this;
    }

    @Override
    public Builder setPartition(PartitionInfo partition) {
      Args.checkNotNull(partition, "partition cannot be null");
      output.partition = partition;
      return this;
    }

    @Override
    public OutputContextImpl build() {
      return output;
    }
  }

}
