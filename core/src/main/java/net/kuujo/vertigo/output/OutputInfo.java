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
package net.kuujo.vertigo.output;

import net.kuujo.vertigo.TypeInfo;
import net.kuujo.vertigo.component.PartitionInfo;
import net.kuujo.vertigo.output.impl.OutputInfoImpl;
import net.kuujo.vertigo.output.port.OutputPortInfo;

import java.util.Collection;

/**
 * Output context is a wrapper around output port information for
 * a single component partition.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public interface OutputInfo extends TypeInfo<OutputInfo> {

  /**
   * Returns a new output info builder.
   *
   * @return A new output info builder.
   */
  static Builder builder() {
    return new OutputInfoImpl.Builder();
  }

  /**
   * Returns a new output info builder.
   *
   * @param output An existing output info object to wrap.
   * @return An output info builder wrapper.
   */
  static Builder builder(OutputInfo output) {
    return new OutputInfoImpl.Builder((OutputInfoImpl) output);
  }

  /**
   * Returns the parent partition context.
   *
   * @return The parent partition context.
   */
  PartitionInfo partition();

  /**
   * Returns the output's port contexts.
   *
   * @return A collection of output port contexts.
   */
  Collection<OutputPortInfo> ports();

  /**
   * Returns the output port context for a given port.
   *
   * @param name The name of the port to return.
   * @return The output port context.
   */
  OutputPortInfo port(String name);

  /**
   * Output info builder.
   */
  public static interface Builder extends TypeInfo.Builder<OutputInfo> {

    /**
     * Adds an output port.
     *
     * @param port The output port info.
     * @return The output info builder.
     */
    Builder addPort(OutputPortInfo port);

    /**
     * Removes an output port.
     *
     * @param port The output port info.
     * @return The output info builder.
     */
    Builder removePort(OutputPortInfo port);

    /**
     * Sets all output ports.
     *
     * @param ports A collection of output port info.
     * @return The output info builder.
     */
    Builder setPorts(OutputPortInfo... ports);

    /**
     * Sets all output ports.
     *
     * @param ports A collection of output port info.
     * @return The output info builder.
     */
    Builder setPorts(Collection<OutputPortInfo> ports);

    /**
     * Clears all output ports.
     *
     * @return The output info builder.
     */
    Builder clearPorts();

    /**
     * Sets the parent partition info.
     *
     * @param partition The parent partition info.
     * @return The output info builder.
     */
    Builder setPartition(PartitionInfo partition);
  }

}
