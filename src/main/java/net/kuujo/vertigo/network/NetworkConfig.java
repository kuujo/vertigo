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
   * Enables acking on the network.
   * 
   * When acking is enabled, network auditors will track message trees throughout the
   * network and notify messages sources once messages have completed processing.
   * 
   * @return The network configuration.
   */
  NetworkConfig enableAcking();

  /**
   * Disables acking on the network.
   * 
   * When acking is disabled, messages will not be tracked through networks. This
   * essentially meands that all messages will be assumed to have been successfully
   * processed. Disable acking at your own risk.
   * 
   * @return The network configuration.
   */
  NetworkConfig disableAcking();

  /**
   * Sets acking on the network.
   * 
   * @param enabled Whether acking is enabled for the network.
   * @return The network configuration.
   */
  NetworkConfig setAckingEnabled(boolean enabled);

  /**
   * Returns a boolean indicating whether acking is enabled.
   * 
   * @return Indicates whether acking is enabled for the network.
   */
  boolean isAckingEnabled();

  /**
   * Returns the number of network auditors.
   * 
   * @return The number of network auditors.
   */
  int getNumAuditors();

  /**
   * Sets the number of network auditors.
   * 
   * This is the number of auditor verticle instances that will be used to track messages
   * throughout a network. The Vertigo message tracking algorithm is designed to be
   * extremely memory efficient, so it's unlikely that memory will be an issue. However,
   * if performance of your network is an issue (particularly in larger networks) you may
   * need to increase the number of network auditors.
   * 
   * @param numAuditors The number of network auditors.
   * @return The network configuration.
   */
  NetworkConfig setNumAuditors(int numAuditors);

  /**
   * Enables message timeouts for the network.
   * 
   * @return The network configuration.
   */
  NetworkConfig enableMessageTimeouts();

  /**
   * Disables message timeouts for the network.
   * 
   * @return The network configuration.
   */
  NetworkConfig disableMessageTimeouts();

  /**
   * Sets whether message timeouts are enabled for the network.
   * 
   * @param isEnabled Indicates whether to enable message timeouts.
   * @return The network configuration.
   */
  NetworkConfig setMessageTimeoutsEnabled(boolean isEnabled);

  /**
   * Returns a boolean indicating whether message timeouts are enabled for the network.
   * 
   * @return Indicates whether message timeouts are enabled.
   */
  boolean isMessageTimeoutsEnabled();

  /**
   * Sets the network message timeout.
   * 
   * This indicates the maximum amount of time an auditor will hold message information in
   * memory before considering it to be timed out.
   * 
   * @param timeout A message timeout in milliseconds.
   * @return The network configuration.
   */
  NetworkConfig setMessageTimeout(long timeout);

  /**
   * Gets the network message timeout.
   * 
   * @return The message timeout for the network in milliseconds. Defaults to 30000
   */
  long getMessageTimeout();

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
