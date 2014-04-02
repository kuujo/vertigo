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
package net.kuujo.vertigo.network;

import java.util.Collection;
import java.util.List;

import net.kuujo.vertigo.network.impl.DefaultNetworkConfig;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Network configuration.
 *
 * @author Jordan Halterman
 */
@JsonTypeInfo(
  use=JsonTypeInfo.Id.CLASS,
  include=JsonTypeInfo.As.PROPERTY,
  property="type",
  defaultImpl=DefaultNetworkConfig.class
)
public interface NetworkConfig extends Config, ComponentConfigurable, ConnectionConfigurable {

  /**
   * Returns the network name.
   * 
   * This is the event bus address at which the network's coordinator will register a
   * handler for components to connect to once deployed.
   * 
   * @return The network name.
   */
  String getName();

  /**
   * Gets a list of network components.
   * 
   * @return A list of network components.
   */
  List<ComponentConfig<?>> getComponents();

  /**
   * Gets a component by name.
   * 
   * @param name The component name.
   * @return The component configuration.
   * @throws IllegalArgumentException If the given component address does not exist within
   *           the network.
   */
  <T extends ComponentConfig<T>> T getComponent(String name);

  /**
   * Returns a boolean indicating whether the network has a component.
   *
   * @param name The component name.
   * @return Indicates whether the component exists in the network.
   */
  boolean hasComponent(String name);

  /**
   * Returns a collection of network connections.
   *
   * @return A collection of connections in the network.
   */
  Collection<ConnectionConfig> getConnections();

}
