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
package net.kuujo.vertigo.context;

import java.util.List;

import net.kuujo.vertigo.context.impl.DefaultNetworkContext;
import net.kuujo.vertigo.network.NetworkConfig;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * A network context which contains information regarding the complete structure of a
 * deployed network. Network contexts are immutable as they are constructed after a
 * network is deployed.
 * 
 * @author Jordan Halterman
 */
@JsonTypeInfo(
  use=JsonTypeInfo.Id.CLASS,
  include=JsonTypeInfo.As.PROPERTY,
  property="class",
  defaultImpl=DefaultNetworkContext.class
)
public interface NetworkContext extends Context<NetworkContext> {

  /**
   * Returns the network name.
   *
   * @return The network name.
   */
  String name();

  /**
   * Returns the network configuration.
   *
   * @return The network configuration.
   */
  NetworkConfig config();

  /**
   * Returns the network status address.
   *
   * @return The network status address.
   */
  String status();

  /**
   * Returns a list of network component contexts.
   * 
   * @return A list of network component contexts.
   */
  List<ComponentContext<?>> components();

  /**
   * Returns a boolean indicating whether the component exists.
   *
   * @param name The name of the component to check.
   * @return Indicates whether a component with that name exists.
   */
  boolean hasComponent(String name);

  /**
   * Returns a component context by name.
   * 
   * @param name The component name.
   * @return A component context.
   * @throws IllegalArgumentException If a component does not exist at the given name.
   */
  <T extends ComponentContext<T>> T component(String name);

}
