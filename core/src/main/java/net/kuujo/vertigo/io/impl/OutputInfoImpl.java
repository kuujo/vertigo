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

package net.kuujo.vertigo.io.impl;

import net.kuujo.vertigo.component.PartitionInfo;
import net.kuujo.vertigo.io.OutputInfo;
import net.kuujo.vertigo.io.port.OutputPortInfo;
import net.kuujo.vertigo.util.Args;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Output info implementation.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class OutputInfoImpl extends BaseIOInfoImpl<OutputInfo> implements OutputInfo {
  private Map<String, OutputPortInfo> ports = new HashMap<>();

  @Override
  public Collection<OutputPortInfo> ports() {
    return ports.values();
  }

  @Override
  public OutputPortInfo port(String name) {
    return ports.get(name);
  }

  /**
   * Output info builder.
   */
  public static class Builder implements OutputInfo.Builder {
    private final OutputInfoImpl output;

    public Builder() {
      output = new OutputInfoImpl();
    }

    public Builder(OutputInfoImpl input) {
      this.output = input;
    }

    @Override
    public Builder addPort(OutputPortInfo port) {
      Args.checkNotNull(port, "port cannot be null");
      output.ports.put(port.name(), port);
      return this;
    }

    @Override
    public Builder removePort(OutputPortInfo port) {
      Args.checkNotNull(port, "port cannot be null");
      output.ports.remove(port.name());
      return this;
    }

    @Override
    public Builder setPorts(OutputPortInfo... ports) {
      output.ports.clear();
      for (OutputPortInfo port : ports) {
        output.ports.put(port.name(), port);
      }
      return this;
    }

    @Override
    public Builder setPorts(Collection<OutputPortInfo> ports) {
      Args.checkNotNull(ports, "ports cannot be null");
      output.ports.clear();
      for (OutputPortInfo port : ports) {
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
    public OutputInfoImpl build() {
      return output;
    }
  }

}
