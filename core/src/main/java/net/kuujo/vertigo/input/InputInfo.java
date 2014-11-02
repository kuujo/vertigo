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
package net.kuujo.vertigo.input;

import net.kuujo.vertigo.TypeInfo;
import net.kuujo.vertigo.component.PartitionInfo;
import net.kuujo.vertigo.input.impl.InputInfoImpl;
import net.kuujo.vertigo.input.port.InputPortInfo;

import java.util.Collection;

/**
 * Input context is a wrapper around input port information for
 * a single component partition.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public interface InputInfo extends TypeInfo<InputInfo> {

  /**
   * Returns a new input info builder.
   *
   * @return A new input info builder.
   */
  static Builder builder() {
    return new InputInfoImpl.Builder();
  }

  /**
   * Returns a new input info builder.
   *
   * @param input An existing input info object to wrap.
   * @return An input info builder wrapper.
   */
  static Builder builder(InputInfo input) {
    return new InputInfoImpl.Builder((InputInfoImpl) input);
  }

  /**
   * Returns the parent partition context.
   *
   * @return The parent partition context.
   */
  PartitionInfo partition();

  /**
   * Returns the input's port contexts.
   *
   * @return A collection of input port contexts.
   */
  Collection<InputPortInfo> ports();

  /**
   * Returns the input port context for a given port.
   *
   * @param name The name of the port to return.
   * @return The input port context.
   */
  InputPortInfo port(String name);

  /**
   * Input info builder.
   */
  public static interface Builder extends TypeInfo.Builder<InputInfo> {

    /**
     * Adds an input port.
     *
     * @param port The input port info.
     * @return The input info builder.
     */
    Builder addPort(InputPortInfo port);

    /**
     * Removes an input port.
     *
     * @param port The input port info.
     * @return The input info builder.
     */
    Builder removePort(InputPortInfo port);

    /**
     * Sets all input ports.
     *
     * @param ports A collection of input port info.
     * @return The input info builder.
     */
    Builder setPorts(InputPortInfo... ports);

    /**
     * Sets all input ports.
     *
     * @param ports A collection of input port info.
     * @return The input info builder.
     */
    Builder setPorts(Collection<InputPortInfo> ports);

    /**
     * Clears all input ports.
     *
     * @return The input info builder.
     */
    Builder clearPorts();

    /**
     * Sets the parent partition info.
     *
     * @param partition The parent partition info.
     * @return The input info builder.
     */
    Builder setPartition(PartitionInfo partition);
  }

}
