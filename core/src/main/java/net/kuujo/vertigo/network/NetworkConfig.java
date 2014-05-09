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

import net.kuujo.vertigo.Config;
import net.kuujo.vertigo.component.ComponentConfig;
import net.kuujo.vertigo.component.ModuleConfig;
import net.kuujo.vertigo.component.VerticleConfig;
import net.kuujo.vertigo.io.connection.ConnectionConfig;
import net.kuujo.vertigo.io.selector.Selector;
import net.kuujo.vertigo.network.impl.DefaultNetworkConfig;

import org.vertx.java.core.json.JsonObject;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Network configuration.<p>
 *
 * The network configuration defines a collection of components
 * (Vert.x modules and verticles) that can be connected together in
 * a meaningful way.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
@JsonTypeInfo(
  use=JsonTypeInfo.Id.CLASS,
  include=JsonTypeInfo.As.PROPERTY,
  property="type",
  defaultImpl=DefaultNetworkConfig.class
)
public interface NetworkConfig extends Config<NetworkConfig> {

  /**
   * <code>name</code> is a string indicating the unique network name. This is the
   * address at which the network will monitor network components. This field is required.
   */
  public static final String NETWORK_NAME = "name";

  /**
   * <code>components</code> is an object defining network component configurations. Each
   * item in the object must be keyed by the unique component address, with each item
   * being an object containing the component configuration. See the {@link ComponentConfig}
   * class for component configuration options.
   */
  public static final String NETWORK_COMPONENTS = "components";

  /**
   * Returns the network name.<p>
   *
   * The network's name should be unique within a given cluster.
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
   * Adds a component to the network.
   * 
   * @param component The component to add.
   * @return The added component configuration.
   */
  @SuppressWarnings("rawtypes")
  <T extends ComponentConfig> T addComponent(T component);

  /**
   * Adds a module or verticle component to the network.<p>
   *
   * The type of component that's added to the network will be determined
   * based on Vert.x module naming standards. If the module/main is a
   * valid module identifier then a module component will be added, otherwise
   * a verticle component will be added.
   * 
   * @param name The component name. This will be used as the basis for internal
   *          component addresses.
   * @param moduleOrMain The component module name or verticle main.
   * @return The new component configuration.
   * @throws IllegalArgumentException If the module name is not a valid module identifier.
   */
  <T extends ComponentConfig<T>> T addComponent(String name, String moduleOrMain);

  /**
   * Adds a module or verticle component to the network.<p>
   *
   * The type of component that's added to the network will be determined
   * based on Vert.x module naming standards. If the module/main is a
   * valid module identifier then a module component will be added, otherwise
   * a verticle component will be added.
   * 
   * @param name The component name. This will be used as the basis for internal
   *          component addresses.
   * @param moduleOrMain The component module name or verticle main.
   * @param config The component configuration. This configuration will be made
   *          available as the verticle configuration within deployed module instances.
   * @return The new component configuration.
   * @throws IllegalArgumentException If the module name is not a valid module identifier.
   */
  <T extends ComponentConfig<T>> T addComponent(String name, String moduleOrMain, JsonObject config);

  /**
   * Adds a module or verticle component to the network.<p>
   *
   * The type of component that's added to the network will be determined
   * based on Vert.x module naming standards. If the module/main is a
   * valid module identifier then a module component will be added, otherwise
   * a verticle component will be added.
   * 
   * @param name The component name. This will be used as the basis for internal
   *          component addresses.
   * @param moduleOrMain The component module name or verticle main.
   * @param instances The number of module instances. If multiple instances are
   *          defined, groupings will be used to determine how messages are distributed
   *          between multiple component instances.
   * @return The new component configuration.
   * @throws IllegalArgumentException If the module name is not a valid module identifier.
   */
  <T extends ComponentConfig<T>> T addComponent(String name, String moduleOrMain, int instances);

  /**
   * Adds a module or verticle component to the network.<p>
   *
   * The type of component that's added to the network will be determined
   * based on Vert.x module naming standards. If the module/main is a
   * valid module identifier then a module component will be added, otherwise
   * a verticle component will be added.
   * 
   * @param name The component name. This will be used as the basis for internal
   *          component addresses.
   * @param moduleOrMain The component module name or verticle main.
   * @param config The component configuration. This configuration will be made
   *          available as the verticle configuration within deployed module instances.
   * @param instances The number of component instances. If multiple instances are
   *          defined, groupings will be used to determine how messages are distributed
   *          between multiple component instances.
   * @return The new component configuration.
   */
  <T extends ComponentConfig<T>> T addComponent(String name, String moduleOrMain, JsonObject config, int instances);

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
   * @param instances The number of module instances. If multiple instances are
   *          defined, groupings will be used to determine how messages are distributed
   *          between multiple component instances.
   * @return The new module configuration.
   * @throws IllegalArgumentException If the module name is not a valid module identifier.
   */
  ModuleConfig addModule(String name, String moduleName, int instances);

  /**
   * Adds a module to the network.
   * 
   * @param name The component name. This will be used as the basis for internal
   *          component addresses.
   * @param moduleName The module name.
   * @param config The module configuration. This configuration will be made
   *          available as the verticle configuration within deployed module instances.
   * @param instances The number of module instances. If multiple instances are
   *          defined, groupings will be used to determine how messages are distributed
   *          between multiple component instances.
   * @return The new module configuration.
   * @throws IllegalArgumentException If the module name is not a valid module identifier.
   */
  ModuleConfig addModule(String name, String moduleName, JsonObject config, int instances);

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
   * @param instances The number of verticle instances. If multiple instances are
   *          defined, groupings will be used to determine how messages are distributed
   *          between multiple component instances.
   * @return The new verticle configuration.
   */
  VerticleConfig addVerticle(String name, String main, int instances);

  /**
   * Adds a verticle to the network.
   * 
   * @param name The component name. This will be used as the basis for internal
   *          component addresses.
   * @param main The verticle main.
   * @param config The verticle configuration. This configuration will be made
   *          available as the verticle configuration within deployed module instances.
   * @param instances The number of verticle instances. If multiple instances are
   *          defined, groupings will be used to determine how messages are distributed
   *          between multiple component instances.
   * @return The new verticle configuration.
   */
  VerticleConfig addVerticle(String name, String main, JsonObject config, int instances);

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
   * Returns a collection of network connections.
   *
   * @return A collection of connections in the network.
   */
  Collection<ConnectionConfig> getConnections();

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
   * @param selector The connection selector.
   * @return A new connection instance.
   */
  ConnectionConfig createConnection(String source, String target, Selector selector);

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
   * @param selector The connection selector.
   * @return A new connection instance.
   */
  ConnectionConfig createConnection(String source, String out, String target, String in, Selector selector);

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

}
