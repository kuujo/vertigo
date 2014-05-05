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
import net.kuujo.vertigo.impl.BaseContext;
import net.kuujo.vertigo.network.NetworkConfig;
import net.kuujo.vertigo.network.NetworkContext;
import net.kuujo.vertigo.util.serialization.SerializerFactory;

import org.vertx.java.core.json.JsonObject;

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
  private String status;
  private Map<String, ComponentContext<?>> components = new HashMap<>();

  private DefaultNetworkContext() {
  }

  /**
   * Creates a network context from JSON.
   * 
   * @param context A JSON representation of the network context.
   * @return A new network context instance.
   * @throws MalformedContextException If the network context is malformed.
   */
  public static DefaultNetworkContext fromJson(JsonObject context) {
    return SerializerFactory.getSerializer(BaseContext.class).deserializeObject(context.getObject("network"), DefaultNetworkContext.class);
  }

  /**
   * Serializes a network context to JSON.
   * 
   * @param context The network context to serialize.
   * @return A serialized network context.
   */
  public static JsonObject toJson(NetworkContext context) {
    return new JsonObject().putObject("network", SerializerFactory.getSerializer(NetworkContext.class).serializeToObject(context));
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
  public String address() {
    return address;
  }

  @Override
  public String status() {
    return status;
  }

  @Override
  @SuppressWarnings("rawtypes")
  public List<ComponentContext<?>> components() {
    List<ComponentContext<?>> components = new ArrayList<>();
    for (ComponentContext<?> component : this.components.values()) {
      components.add(((DefaultComponentContext) component).setNetworkContext(this));
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
  public <T extends ComponentContext<T>> T component(String name) {
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
      Iterator<Map.Entry<String, ComponentContext<?>>> iter = components.entrySet().iterator();
      while (iter.hasNext()) {
        ComponentContext component = iter.next().getValue();
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
        if (!components.values().contains(component)) {
          components.put(component.name(), component);
        }
      }
    }
    super.notify(this);
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
    public static Builder newBuilder(DefaultNetworkContext context) {
      return new Builder(context);
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
    public Builder setComponents(Collection<DefaultComponentContext<?>> components) {
      context.components = new HashMap<>();
      for (DefaultComponentContext<?> component : components) {
        component.setNetworkContext(context);
        context.components.put(component.name(), component);
      }
      return this;
    }

    /**
     * Adds a component to the network.
     *
     * @param component The component context to add.
     * @return The context builder.
     */
    public Builder addComponent(DefaultComponentContext<?> component) {
      component.setNetworkContext(context);
      if (context.components == null) {
        context.components = new HashMap<>();
      }
      context.components.put(component.name(), component);
      return this;
    }

    /**
     * Removes a component from the network.
     *
     * @param component The component context to remove.
     * @return The context builder.
     */
    public Builder removeComponent(DefaultComponentContext<?> component) {
      if (context.components == null) {
        context.components = new HashMap<>();
      }
      context.components.remove(component.name());
      return this;
    }
  }

}
