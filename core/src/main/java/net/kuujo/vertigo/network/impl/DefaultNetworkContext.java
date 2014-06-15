/*
 * Copyright 2013-2014 the original author or authors.
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.kuujo.vertigo.component.ComponentContext;
import net.kuujo.vertigo.component.impl.DefaultComponentContext;
import net.kuujo.vertigo.component.impl.DefaultModuleContext;
import net.kuujo.vertigo.component.impl.DefaultVerticleContext;
import net.kuujo.vertigo.impl.BaseContext;
import net.kuujo.vertigo.network.NetworkConfig;
import net.kuujo.vertigo.network.NetworkContext;

/**
 * A network context which contains information regarding the complete structure of a
 * deployed network. Network contexts are immutable as they are constructed after a
 * network is deployed.
 * 
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class DefaultNetworkContext extends BaseContext<NetworkContext> implements NetworkContext {
  private String name;
  private String version;
  private NetworkConfig config;
  private String cluster;
  private String status;
  private Map<String, DefaultComponentContext<?>> components = new HashMap<>();

  private DefaultNetworkContext() {
  }

  @Override
  public String name() {
    return name;
  }

  @Override
  public String version() {
    return version;
  }

  @Override
  public NetworkConfig config() {
    return config;
  }

  @Override
  public String cluster() {
    return cluster;
  }

  @Override
  public String address() {
    return address;
  }

  @Override
  public String status() {
    return status;
  }

  @Override
  public Collection<ComponentContext<?>> components() {
    List<ComponentContext<?>> components = new ArrayList<>();
    for (DefaultComponentContext<?> component : this.components.values()) {
      components.add(component.setNetworkContext(this));
    }
    return components;
  }

  /**
   * Returns a boolean indicating whether the component exists.
   *
   * @param name The name of the component to check.
   * @return Indicates whether a component with that name exists.
   */
  public boolean hasComponent(String name) {
    return components.containsKey(name);
  }

  @Override
  @SuppressWarnings({"unchecked", "rawtypes"})
  public <T extends ComponentContext<?>> T component(String name) {
    return (T) (components.containsKey(name) ? ((DefaultComponentContext) components.get(name)).setNetworkContext(this) : null);
  }

  @Override
  @SuppressWarnings({"unchecked", "rawtypes"})
  public void notify(NetworkContext update) {
    if (update == null) {
      for (ComponentContext<?> component : components.values()) {
        component.notify(null);
      }
      components.clear();
    } else {
      Iterator<Map.Entry<String, DefaultComponentContext<?>>> iter = components.entrySet().iterator();
      while (iter.hasNext()) {
        DefaultComponentContext component = iter.next().getValue();
        ComponentContext match = null;
        for (ComponentContext c : update.components()) {
          if (component.equals(c)) {
            match = c;
            break;
          }
        }
        if (match != null) {
          component.notify(match);
        } else {
          component.notify(null);
          iter.remove();
        }
      }
  
      for (ComponentContext component : update.components()) {
        if (!components.containsKey(component.name())) {
          if (component.isModule()) {
            components.put(component.name(), DefaultModuleContext.Builder.newBuilder(component.asModule()).build().setNetworkContext(this));
          } else if (component.isVerticle()) {
            components.put(component.name(), DefaultVerticleContext.Builder.newBuilder(component.asVerticle()).build().setNetworkContext(this));
          }
        }
      }
    }
    super.notify(this);
  }

  @Override
  public String uri() {
    return String.format("%s://%s", cluster, name);
  }

  @Override
  public String toString() {
    return address();
  }

  /**
   * Network context builder.
   *
   * @author Jordan Halterman
   */
  public static class Builder extends BaseContext.Builder<Builder, DefaultNetworkContext> {

    private Builder() {
      super(new DefaultNetworkContext());
    }

    private Builder(DefaultNetworkContext context) {
      super(context);
    }

    /**
     * Returns a new context builder.
     *
     * @return A new context builder.
     */
    public static Builder newBuilder() {
      return new Builder();
    }

    /**
     * Creates a new context builder from an existing context.
     *
     * @param context The starting context.
     * @return A new context builder.
     */
    public static Builder newBuilder(NetworkContext context) {
      if (context instanceof DefaultNetworkContext) {
        return new Builder((DefaultNetworkContext) context);
      } else {
        return new Builder().setAddress(context.address())
            .setCluster(context.cluster())
            .setName(context.name())
            .setConfig(context.config())
            .setStatusAddress(context.status())
            .setVersion(context.version())
            .setComponents(context.components());
      }
    }

    /**
     * Sets the network name.
     *
     * @param name The network name.
     * @return The context builder.
     */
    public Builder setName(String name) {
      context.name = name;
      context.address = name;
      return this;
    }

    /**
     * Sets the network version.
     *
     * @param version The network version.
     * @return The context builder.
     */
    public Builder setVersion(String version) {
      context.version = version;
      return this;
    }

    /**
     * Sets the network configuration.
     *
     * @param config The network configuration.
     * @return The context builder.
     */
    public Builder setConfig(NetworkConfig config) {
      context.config = config;
      return this;
    }

    /**
     * Sets the network address.
     *
     * @param address The network address.
     * @return The context builder.
     */
    public Builder setAddress(String address) {
      context.address = address;
      return this;
    }

    /**
     * Sets the network's cluster address.
     *
     * @param cluster The address of the cluster to which the network belongs.
     * @return The context builder.
     */
    public Builder setCluster(String cluster) {
      context.cluster = cluster;
      return this;
    }

    /**
     * Sets the network status address.
     *
     * @param address The network status address.
     * @return The context builder.
     */
    public Builder setStatusAddress(String address) {
      context.status = address;
      return this;
    }

    /**
     * Set the network components.
     *
     * @param components A list of component contexts.
     * @return The context builder.
     */
    public Builder setComponents(Collection<ComponentContext<?>> components) {
      context.components = new HashMap<>();
      for (ComponentContext<?> component : components) {
        if (component.isModule()) {
          context.components.put(component.name(), DefaultModuleContext.Builder.newBuilder(component.asModule()).build().setNetworkContext(context));
        } else if (component.isVerticle()) {
          context.components.put(component.name(), DefaultVerticleContext.Builder.newBuilder(component.asVerticle()).build().setNetworkContext(context));
        }
      }
      return this;
    }

    /**
     * Adds a component to the network.
     *
     * @param component The component context to add.
     * @return The context builder.
     */
    public Builder addComponent(ComponentContext<?> component) {
      if (context.components == null) {
        context.components = new HashMap<>();
      }
      if (component.isModule()) {
        context.components.put(component.name(), DefaultModuleContext.Builder.newBuilder(component.asModule()).build().setNetworkContext(context));
      } else if (component.isVerticle()) {
        context.components.put(component.name(), DefaultVerticleContext.Builder.newBuilder(component.asVerticle()).build().setNetworkContext(context));
      }
      return this;
    }

    /**
     * Removes a component from the network.
     *
     * @param component The component context to remove.
     * @return The context builder.
     */
    public Builder removeComponent(ComponentContext<?> component) {
      if (context.components == null) {
        context.components = new HashMap<>();
      }
      context.components.remove(component.name());
      return this;
    }
  }

}
