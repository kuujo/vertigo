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

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import io.vertx.core.json.JsonObject;
import net.kuujo.vertigo.builder.NetworkBuilder;
import net.kuujo.vertigo.builder.impl.NetworkBuilderImpl;
import net.kuujo.vertigo.component.ComponentDefinition;
import net.kuujo.vertigo.io.connection.ConnectionDefinition;
import net.kuujo.vertigo.io.connection.SourceDefinition;
import net.kuujo.vertigo.io.connection.TargetDefinition;
import net.kuujo.vertigo.network.impl.NetworkImpl;
import net.kuujo.vertigo.util.Configs;
import net.kuujo.vertigo.util.Json;

import java.util.Collection;

/**
 * Vertigo network.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public interface Network extends Json.Serializable {

  /**
   * <code>name</code> is a string indicating the unique network name. This is the
   * address at which the network will monitor network components. This field is required.
   */
  static final String NETWORK_NAME = "name";

  /**
   * <code>components</code> is an object defining network component configurations. Each
   * item in the object must be keyed by the unique component address, with each item
   * being an object containing the component configuration. See the
   * {@link net.kuujo.vertigo.component.ComponentDefinition} interface for configuration options.
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
   * @param configName The network configuration file name.
   * @return The constructed network object.
   */
  static Network network(String configName) {
    Config config = ConfigFactory.load(Thread.currentThread().getContextClassLoader(), configName);
    return network(Configs.configObjectToJson(config));
  }

  /**
   * Constructs a network object from a JSON configuration.
   *
   * @param network The JSON network definition.
   * @return The constructed network object.
   */
  static Network network(JsonObject network) {
    return Json.deserialize(network, NetworkImpl.class);
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
   * @param id The unique network ID.
   * @return The network builder.
   */
  static NetworkBuilder builder(String id) {
    return new NetworkBuilderImpl(id);
  }

  /**
   * Returns the unique network ID.
   *
   * @return The unique network ID.
   */
  String getId();

  /**
   * Sets the unique network ID.
   *
   * @param id The unique network ID.
   * @return The network configuration.
   */
  Network setId(String id);

  /**
   * Gets a list of network components.
   *
   * @return A list of network components.
   */
  Collection<ComponentDefinition> getComponents();

  /**
   * Gets a component by name.
   *
   * @param id The component id.
   * @return The component definition.
   * @throws IllegalArgumentException If the given component id does not exist within
   *           the network.
   */
  ComponentDefinition getComponent(String id);

  /**
   * Returns a boolean indicating whether the network has a component.
   *
   * @param id The component id.
   * @return Indicates whether the component exists in the network.
   */
  boolean hasComponent(String id);

  /**
   * Adds a component to the network.
   *
   * @param id The component id.
   * @return The component definition.
   */
  ComponentDefinition addComponent(String id);

  /**
   * Adds a component to the network.
   *
   * @param component The component definition.
   * @return The component definition.
   */
  ComponentDefinition addComponent(ComponentDefinition component);

  /**
   * Removes a component from the network.
   *
   * @param name The component name.
   * @return The component definition.
   */
  ComponentDefinition removeComponent(String name);

  /**
   * Removes a component from the network.
   *
   * @param component The component definition.
   * @return The component definition.
   */
  ComponentDefinition removeComponent(ComponentDefinition component);

  /**
   * Returns a collection of network connections.
   *
   * @return A collection of connections in the network.
   */
  Collection<ConnectionDefinition> getConnections();

  /**
   * Creates a connection between two components.
   *
   * @param connection The new connection options.
   * @return The connection definition.
   */
  ConnectionDefinition createConnection(ConnectionDefinition connection);

  /**
   * Creates a connection between two components.
   *
   * @param source The source connection options.
   * @param target The target connection options.
   * @return The connection definition.
   */
  ConnectionDefinition createConnection(SourceDefinition source, TargetDefinition target);

  /**
   * Destroys a connection between two components.
   *
   * @param connection The connection to destroy.
   * @return The connection definition.
   */
  ConnectionDefinition destroyConnection(ConnectionDefinition connection);

}
