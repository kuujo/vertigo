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

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.json.JsonObject;
import net.kuujo.vertigo.builder.NetworkBuilder;
import net.kuujo.vertigo.builder.impl.NetworkBuilderImpl;
import net.kuujo.vertigo.component.ComponentInfo;
import net.kuujo.vertigo.component.impl.ComponentInfoImpl;
import net.kuujo.vertigo.io.connection.ConnectionInfo;
import net.kuujo.vertigo.io.port.InputPortInfo;
import net.kuujo.vertigo.io.port.OutputPortInfo;
import net.kuujo.vertigo.network.impl.NetworkImpl;
import net.kuujo.vertigo.util.Configs;

import java.util.Collection;

/**
 * Vertigo network.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
@VertxGen
public interface Network {

  /**
   * <code>name</code> is a string indicating the unique network name. This is the
   * address at which the network will monitor network components. This field is required.
   */
  static final String NETWORK_NAME = "name";

  /**
   * <code>components</code> is an object defining network component configurations. Each
   * item in the object must be keyed by the unique component address, with each item
   * being an object containing the component configuration. See the
   * {@link net.kuujo.vertigo.component.ComponentInfo} interface for configuration options.
   */
  static final String NETWORK_COMPONENTS = "components";

  /**
   * <code>connections</code> is an array defining network connection configurations. Each
   * item in the array must be an object defining a <code>source</code> and <code>target</code>
   * configuration.
   */
  static final String NETWORK_CONNECTIONS = "connections";

  /**
   * Constructs a new network object.
   *
   * @return A new uniquely identified network configuration.
   */
  static Network network() {
    return new NetworkImpl();
  }

  /**
   * Constructs a network object from file-based configuration.
   *
   * @param network The network configuration name.
   * @return The constructed network object.
   */
  static Network network(String network) {
    return new NetworkImpl(Configs.load(network, new JsonObject().put("vertigo", new JsonObject().put("network", network))));
  }

  /**
   * Constructs a network object from a JSON configuration.
   *
   * @param network The JSON network configuration.
   * @return The constructed network object.
   */
  static Network network(JsonObject network) {
    return new NetworkImpl(network);
  }

  /**
   * Constructs a component object from file-based configuration.
   *
   * @param component The component configuration name.
   * @return The constructed component configuration.
   */
  static ComponentInfo component(String component) {
    return new ComponentInfoImpl(Configs.load(component, new JsonObject().put("vertigo", new JsonObject().put("component", component))));
  }

  /**
   * Constructs a component object from a JSON configuration.
   *
   * @param component The JSON component configuration.
   * @return The constructed component configuration.
   */
  static ComponentInfo component(JsonObject component) {
    return new ComponentInfoImpl(component);
  }

  /**
   * Returns a new network builder.
   *
   * @return A new network builder.
   */
  static NetworkBuilder builder() {
    return new NetworkBuilderImpl();
  }

  /**
   * Returns a new network builder.
   *
   * @param name The unique network name.
   * @return The network builder.
   */
  static NetworkBuilder builder(String name) {
    return new NetworkBuilderImpl(name);
  }

  /**
   * Returns the unique network name.
   *
   * @return The unique network name.
   */
  String getName();

  /**
   * Sets the unique network name.
   *
   * @param name The unique network name.
   * @return The network configuration.
   */
  @Fluent
  Network setName(String name);

  /**
   * Gets a list of network components.
   *
   * @return A list of network components.
   */
  Collection<ComponentInfo> getComponents();

  /**
   * Gets a component by name.
   *
   * @param name The component name.
   * @return The component info.
   * @throws IllegalArgumentException If the given component name does not exist within
   *           the network.
   */
  ComponentInfo getComponent(String name);

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
   * @param name The component name.
   * @return The component info.
   */
  ComponentInfo addComponent(String name);

  /**
   * Adds a component to the network.
   *
   * @param component The component info.
   * @return The component info.
   */
  ComponentInfo addComponent(ComponentInfo component);

  /**
   * Removes a component from the network.
   *
   * @param name The component name.
   * @return The component info.
   */
  ComponentInfo removeComponent(String name);

  /**
   * Removes a component from the network.
   *
   * @param component The component info.
   * @return The component info.
   */
  ComponentInfo removeComponent(ComponentInfo component);

  /**
   * Returns a collection of network connections.
   *
   * @return A collection of connections in the network.
   */
  Collection<ConnectionInfo> getConnections();

  /**
   * Creates a connection between two components.
   *
   * @param connection The new connection options.
   * @return The connection info.
   */
  ConnectionInfo createConnection(ConnectionInfo connection);

  /**
   * Creates a connection between two components.
   *
   * @param output The source component's output port.
   * @param input The target component's input port.
   * @return The connection info.
   */
  ConnectionInfo createConnection(OutputPortInfo output, InputPortInfo input);

  /**
   * Destroys a connection between two components.
   *
   * @param connection The connection to destroy.
   * @return The connection info.
   */
  ConnectionInfo destroyConnection(ConnectionInfo connection);

}
