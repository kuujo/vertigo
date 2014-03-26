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

import net.kuujo.vertigo.input.grouping.Grouping;
import net.kuujo.vertigo.network.impl.DefaultNetworkConfig;

import org.vertx.java.core.json.JsonObject;

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
public interface NetworkConfig extends Config {

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
   * Adds a component to the network.
   * 
   * @param component The component to add.
   * @return The added component configuration.
   */
  @SuppressWarnings("rawtypes")
  <T extends ComponentConfig> T addComponent(T component);

  /**
   * Adds a module to the network.
   * 
   * @param name The component name. This will be used as the basis for internal
   *          component addresses.
   * @param moduleName The module name.
   * @return The new module configuration.
   * @throws IllegalArgumentException If the module name is not a valid module identifier.
   */
  <T extends ComponentConfig<T>> T addComponent(String name, String moduleOrMain);

  /**
   * Adds a module to the network.
   * 
   * @param name The component name. This will be used as the basis for internal
   *          component addresses.
   * @param moduleName The module name.
   * @param config The module configuration. This configuration will be made
   *          available as the verticle configuration within deployed module instances.
   * @return The new module configuration.
   * @throws IllegalArgumentException If the module name is not a valid module identifier.
   */
  <T extends ComponentConfig<T>> T addComponent(String name, String moduleOrMain, JsonObject config);

  /**
   * Adds a module to the network.
   * 
   * @param name The component name. This will be used as the basis for internal
   *          component addresses.
   * @param moduleName The module name.
   * @param numInstances The number of module instances. If multiple instances are
   *          defined, groupings will be used to determine how messages are distributed
   *          between multiple component instances.
   * @return The new module configuration.
   * @throws IllegalArgumentException If the module name is not a valid module identifier.
   */
  <T extends ComponentConfig<T>> T addComponent(String name, String moduleOrMain, int instances);

  /**
   * Adds a module or verticle component to the network.
   * 
   * @param name The component name. This will be used as the basis for internal
   *          component addresses.
   * @param moduleOrMain The component module name or verticle main.
   * @param config The component configuration. This configuration will be made
   *          available as the verticle configuration within deployed module instances.
   * @param instances The number of component instances. If multiple instances are
   *          defined, groupings will be used to determine how messages are distributed
   *          between multiple component instances.
   * @return The new module configuration.
   */
  <T extends ComponentConfig<T>> T addComponent(String name, String moduleOrMain, JsonObject config, int instances);

  /**
   * Returns a boolean indicating whether the network has a component.
   *
   * @param name The component name.
   * @return Indicates whether the component exists in the network.
   */
  boolean hasComponent(String name);

  /**
   * Removes a component from the network.
   *
   * @param component The component to remove.
   * @return The removed component configuration.
   */
  <T extends ComponentConfig<T>> T removeComponent(T component);

  /**
   * Removes a component from the network.
   *
   * @param name The component name.
   * @return The removed component configuration.
   */
  <T extends ComponentConfig<T>> T removeComponent(String name);

  /**
   * Adds a module to the network.
   * 
   * @param module The module to add.
   * @return The added module component configuration.
   */
  ModuleConfig addModule(ModuleConfig module);

  /**
   * Adds a module to the network.
   * 
   * @param name The component name. This will be used as the basis for internal
   *          component addresses.
   * @param moduleName The module name.
   * @return The new module configuration.
   * @throws IllegalArgumentException If the module name is not a valid module identifier.
   */
  ModuleConfig addModule(String name, String moduleName);

  /**
   * Adds a module to the network.
   * 
   * @param name The component name. This will be used as the basis for internal
   *          component addresses.
   * @param moduleName The module name.
   * @param config The module configuration. This configuration will be made
   *          available as the verticle configuration within deployed module instances.
   * @return The new module configuration.
   * @throws IllegalArgumentException If the module name is not a valid module identifier.
   */
  ModuleConfig addModule(String name, String moduleName, JsonObject config);

  /**
   * Adds a module to the network.
   * 
   * @param name The component name. This will be used as the basis for internal
   *          component addresses.
   * @param moduleName The module name.
   * @param numInstances The number of module instances. If multiple instances are
   *          defined, groupings will be used to determine how messages are distributed
   *          between multiple component instances.
   * @return The new module configuration.
   * @throws IllegalArgumentException If the module name is not a valid module identifier.
   */
  ModuleConfig addModule(String name, String moduleName, int numInstances);

  /**
   * Adds a module to the network.
   * 
   * @param name The component name. This will be used as the basis for internal
   *          component addresses.
   * @param moduleName The module name.
   * @param config The module configuration. This configuration will be made
   *          available as the verticle configuration within deployed module instances.
   * @param numInstances The number of module instances. If multiple instances are
   *          defined, groupings will be used to determine how messages are distributed
   *          between multiple component instances.
   * @return The new module configuration.
   * @throws IllegalArgumentException If the module name is not a valid module identifier.
   */
  ModuleConfig addModule(String name, String moduleName, JsonObject config, int numInstances);

  /**
   * Removes a module from the network.
   *
   * @param module The module component.
   * @return The removed module configuration.
   */
  ModuleConfig removeModule(ModuleConfig module);

  /**
   * Removes a module from the network.
   *
   * @param name The module component name.
   * @return The removed module configuration.
   */
  ModuleConfig removeModule(String name);

  /**
   * Adds a verticle to the network.
   * 
   * @param verticle The verticle to add.
   * @return The added verticle component configuration.
   */
  VerticleConfig addVerticle(VerticleConfig verticle);

  /**
   * Adds a verticle to the network.
   * 
   * @param name The component name. This will be used as the basis for internal
   *          component addresses.
   * @param main The verticle main.
   * @return The new verticle configuration.
   */
  VerticleConfig addVerticle(String name, String main);

  /**
   * Adds a verticle to the network.
   * 
   * @param name The component name. This will be used as the basis for internal
   *          component addresses.
   * @param main The verticle main.
   * @param config The verticle configuration. This configuration will be made
   *          available as the verticle configuration within deployed module instances.
   * @return The new verticle configuration.
   */
  VerticleConfig addVerticle(String name, String main, JsonObject config);

  /**
   * Adds a verticle to the network.
   * 
   * @param name The component name. This will be used as the basis for internal
   *          component addresses.
   * @param main The verticle main.
   * @param numInstances The number of verticle instances. If multiple instances are
   *          defined, groupings will be used to determine how messages are distributed
   *          between multiple component instances.
   * @return The new verticle configuration.
   */
  VerticleConfig addVerticle(String name, String main, int numInstances);

  /**
   * Adds a verticle to the network.
   * 
   * @param name The component name. This will be used as the basis for internal
   *          component addresses.
   * @param main The verticle main.
   * @param config The verticle configuration. This configuration will be made
   *          available as the verticle configuration within deployed module instances.
   * @param numInstances The number of verticle instances. If multiple instances are
   *          defined, groupings will be used to determine how messages are distributed
   *          between multiple component instances.
   * @return The new verticle configuration.
   */
  VerticleConfig addVerticle(String name, String main, JsonObject config, int numInstances);

  /**
   * Removes a verticle configuration from the network.
   *
   * @param verticle The verticle component.
   * @return The removed verticle configuration.
   */
  VerticleConfig removeVerticle(VerticleConfig verticle);

  /**
   * Removes a verticle configuration from the network.
   *
   * @param name The verticle component name.
   * @return The removed verticle configuration.
   */
  VerticleConfig removeVerticle(String name);

  /**
   * Creates a connection between two components.
   *
   * @param connection The new connection.
   * @return The connection instance.
   */
  ConnectionConfig createConnection(ConnectionConfig connection);

  /**
   * Creates a connection between two components.
   *
   * @param source The source component.
   * @param target The target component.
   * @return A new connection instance.
   */
  ConnectionConfig createConnection(String source, String target);

  /**
   * Creates a connection between two components.
   *
   * @param source The source component.
   * @param target The target component.
   * @param grouping The connection grouping.
   * @return A new connection instance.
   */
  ConnectionConfig createConnection(String source, String target, Grouping grouping);

  /**
   * Creates a connection between two components.
   *
   * @param source The source component.
   * @param out The source output port.
   * @param target The target component.
   * @param in The target output port.
   * @return A new connection instance.
   */
  ConnectionConfig createConnection(String source, String out, String target, String in);

  /**
   * Creates a connection between two components.
   *
   * @param source The source component.
   * @param out The source output port.
   * @param target The target component.
   * @param in The target output port.
   * @param grouping The connection grouping.
   * @return A new connection instance.
   */
  ConnectionConfig createConnection(String source, String out, String target, String in, Grouping grouping);

  /**
   * Destroys a connection between two components.
   *
   * @param connection The connection to destroy.
   * @return The network configuration.
   */
  NetworkConfig destroyConnection(ConnectionConfig connection);

  /**
   * Destroys a connection between two components.
   *
   * @param source The source component name and port.
   * @param target The target component name and port.
   * @return The network configuration.
   */
  NetworkConfig destroyConnection(String source, String target);

  /**
   * Destroys a connection between two components.
   *
   * @param source The source component name.
   * @param out The source component out port.
   * @param target The target component name.
   * @param in The target component in port.
   * @return The network configuration.
   */
  NetworkConfig destroyConnection(String source, String out, String target, String in);

  /**
   * Returns a collection of network connections.
   *
   * @return A collection of connections in the network.
   */
  Collection<ConnectionConfig> getConnections();

}
