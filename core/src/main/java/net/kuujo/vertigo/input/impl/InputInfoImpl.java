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

package net.kuujo.vertigo.input.impl;

import net.kuujo.vertigo.component.PartitionInfo;
import net.kuujo.vertigo.impl.BaseTypeInfoImpl;
import net.kuujo.vertigo.input.InputInfo;
import net.kuujo.vertigo.input.port.InputPortInfo;
import net.kuujo.vertigo.util.Args;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Input info implementation.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class InputInfoImpl extends BaseTypeInfoImpl<InputInfo> implements InputInfo {
  private PartitionInfo partition;
  private Map<String, InputPortInfo> ports = new HashMap<>();

  @Override
  public PartitionInfo partition() {
    return partition;
  }

  @Override
  public Collection<InputPortInfo> ports() {
    return ports.values();
  }

  @Override
  public InputPortInfo port(String name) {
    return ports.get(name);
  }

  /**
   * Input info builder.
   */
  public static class Builder implements InputInfo.Builder {
    private final InputInfoImpl input;

    public Builder() {
      input = new InputInfoImpl();
    }

    public Builder(InputInfoImpl input) {
      this.input = input;
    }

    @Override
    public Builder addPort(InputPortInfo port) {
      Args.checkNotNull(port, "port cannot be null");
      input.ports.put(port.name(), port);
      return this;
    }

    @Override
    public Builder removePort(InputPortInfo port) {
      Args.checkNotNull(port, "port cannot be null");
      input.ports.remove(port.name());
      return this;
    }

    @Override
    public Builder setPorts(InputPortInfo... ports) {
      input.ports.clear();
      for (InputPortInfo port : ports) {
        input.ports.put(port.name(), port);
      }
      return this;
    }

    @Override
    public Builder setPorts(Collection<InputPortInfo> ports) {
      Args.checkNotNull(ports, "ports cannot be null");
      input.ports.clear();
      for (InputPortInfo port : ports) {
        input.ports.put(port.name(), port);
      }
      return this;
    }

    @Override
    public Builder clearPorts() {
      input.ports.clear();
      return this;
    }

    @Override
    public Builder setPartition(PartitionInfo partition) {
      Args.checkNotNull(partition, "partition cannot be null");
      input.partition = partition;
      return this;
    }

    @Override
    public InputInfoImpl build() {
      return input;
    }
  }

}
