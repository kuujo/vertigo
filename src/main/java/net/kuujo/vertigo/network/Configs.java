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

import net.kuujo.vertigo.network.impl.DefaultNetworkConfig;
import net.kuujo.vertigo.util.serializer.Serializer;
import net.kuujo.vertigo.util.serializer.SerializerFactory;

import org.vertx.java.core.json.JsonObject;

/**
 * Configuration helper methods.
 *
 * @author Jordan Halterman
 */
public final class Configs {
  private static final Serializer serializer = SerializerFactory.getSerializer(Config.class);

  /**
   * Creates a network configuration from json.
   *
   * @param config A json network configuration.
   * @return A network configuration.
   */
  public static NetworkConfig createNetwork(JsonObject config) {
    return serializer.deserializeObject(config, NetworkConfig.class);
  }

  /**
   * Creates a component configuration from json.
   *
   * @param config A json component configuration.
   * @return A component configuration.
   */
  @SuppressWarnings("unchecked")
  public static <T extends ComponentConfig<T>> T createComponent(JsonObject config) {
    return (T) serializer.deserializeObject(config, ComponentConfig.class);
  }

  /**
   * Creates a connection configuration from json.
   *
   * @param config A json connection configuration.
   * @return A connection configuration.
   */
  public static ConnectionConfig createConnection(JsonObject config) {
    return serializer.deserializeObject(config, ConnectionConfig.class);
  }

  /**
   * Merges two network configurations into a single configuraiton.
   *
   * @param base The base configuration.
   * @param merge The configuration to merge.
   * @return The combined configuration.
   */
  public static NetworkConfig mergeNetworks(NetworkConfig base, NetworkConfig merge) {
    if (!base.getName().equals(merge.getName())) {
      throw new IllegalArgumentException("Cannot merge networks of different names.");
    }

    NetworkConfig network = new DefaultNetworkConfig(base.getName());

    for (ComponentConfig<?> component : merge.getComponents()) {
      ComponentConfig<?> existing = base.getComponent(component.getName());
      if (existing == null) {
        network.addComponent(component);
      } else {
        network.addComponent(existing);
      }
    }

    for (ConnectionConfig connection : merge.getConnections()) {
      ConnectionConfig exists = null;
      for (ConnectionConfig existing : base.getConnections()) {
        if (existing.equals(connection)) {
          exists = existing;
          break;
        }
      }
      if (exists != null) {
        network.createConnection(exists);
      } else {
        network.createConnection(connection);
      }
    }
    return network;
  }

  /**
   * Unmerges one network configuration from another.
   *
   * @param base The base network configuration.
   * @param unmerge The configuration to extract.
   * @return The cleaned configuration.
   */
  public static NetworkConfig unmergeNetworks(NetworkConfig base, NetworkConfig unmerge) {
    if (!base.getName().equals(unmerge.getName())) {
      throw new IllegalArgumentException("Cannot merge networks of different names.");
    }

    NetworkConfig network = new DefaultNetworkConfig(base.getName());

    for (ComponentConfig<?> component : base.getComponents()) {
      if (!unmerge.hasComponent(component.getName())) {
        network.addComponent(component);
      }
    }

    for (ConnectionConfig connection : base.getConnections()) {
      boolean exists = false;
      for (ConnectionConfig existing : unmerge.getConnections()) {
        if (existing.equals(connection)) {
          exists = true;
          break;
        }
      }
      if (!exists) {
        network.createConnection(connection);
      }
    }
    return network;
  }

}
