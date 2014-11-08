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

package net.kuujo.vertigo.network.impl;

import net.kuujo.vertigo.component.ComponentContext;
import net.kuujo.vertigo.impl.BaseContextImpl;
import net.kuujo.vertigo.network.Network;
import net.kuujo.vertigo.network.NetworkContext;
import net.kuujo.vertigo.util.Args;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Network context implementation.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class NetworkContextImpl extends BaseContextImpl<NetworkContext> implements NetworkContext {
  private String name;
  private String version;
  private Network config;
  private Map<String, ComponentContext> components = new HashMap<>();

  @Override
  public String name() {
    return name;
  }

  @Override
  public String version() {
    return version;
  }

  @Override
  public Network config() {
    return config;
  }

  @Override
  public Collection<ComponentContext> components() {
    return components.values();
  }

  @Override
  public boolean hasComponent(String name) {
    return components.containsKey(name);
  }

  @Override
  public ComponentContext component(String name) {
    return components.get(name);
  }

  /**
   * Network context builder.
   */
  public static class Builder implements NetworkContext.Builder {
    private final NetworkContextImpl network;

    public Builder() {
      network = new NetworkContextImpl();
    }

    public Builder(NetworkContextImpl network) {
      this.network = network;
    }

    @Override
    public NetworkContext.Builder setId(String id) {
      Args.checkNotNull(id, "id cannot be null");
      network.id = id;
      return this;
    }

    @Override
    public Builder setName(String name) {
      Args.checkNotNull(name, "name cannot be null");
      network.name = name;
      return this;
    }

    @Override
    public Builder setVersion(String version) {
      Args.checkNotNull(version, "version cannot be null");
      network.version = version;
      return this;
    }

    @Override
    public Builder setConfig(Network config) {
      Args.checkNotNull(config, "configuration cannot be null");
      network.config = config;
      return this;
    }

    @Override
    public Builder addComponent(ComponentContext component) {
      Args.checkNotNull(component, "component cannot be null");
      network.components.put(component.name(), component);
      return this;
    }

    @Override
    public Builder removeComponent(ComponentContext component) {
      Args.checkNotNull(component, "component cannot be null");
      network.components.remove(component.name());
      return this;
    }

    @Override
    public Builder setComponents(ComponentContext... components) {
      network.components.clear();
      for (ComponentContext component : components) {
        network.components.put(component.name(), component);
      }
      return this;
    }

    @Override
    public Builder setComponents(Collection<ComponentContext> components) {
      Args.checkNotNull(components, "components cannot be null");
      network.components.clear();
      for (ComponentContext component : components) {
        network.components.put(component.name(), component);
      }
      return this;
    }

    /**
     * Checks network fields.
     */
    private void checkFields() {
      Args.checkNotNull(network.name, "name cannot be null");
      Args.checkNotNull(network.version, "version cannot be null");
      Args.checkNotNull(network.config, "configuration cannot be null");
      Args.checkNotNull(network.components, "components cannot be null");
    }

    @Override
    public NetworkContext build() {
      checkFields();
      return network;
    }

  }

}
