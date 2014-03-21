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
package net.kuujo.vertigo.context;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.vertx.java.core.json.JsonObject;

import net.kuujo.vertigo.util.serializer.SerializerFactory;

/**
 * A network context which contains information regarding the complete structure of a
 * deployed network. Network contexts are immutable as they are constructed after a
 * network is deployed.
 * 
 * @author Jordan Halterman
 */
public final class NetworkContext extends Context<NetworkContext> {
  private String address;
  private String status;
  private Set<String> auditors = new HashSet<>();
  private boolean acking = true;
  private long timeout = 30000;
  private Map<String, ComponentContext<?>> components = new HashMap<>();

  private NetworkContext() {
  }

  /**
   * Creates a network context from JSON.
   * 
   * @param context A JSON representation of the network context.
   * @return A new network context instance.
   * @throws MalformedContextException If the network context is malformed.
   */
  public static NetworkContext fromJson(JsonObject context) {
    return SerializerFactory.getSerializer(Context.class).deserializeObject(context.getObject("network"), NetworkContext.class);
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
  public String address() {
    return address;
  }

  /**
   * Returns the network status address.
   *
   * @return The network status address.
   */
  public String status() {
    return status;
  }

  /**
   * Returns the number of network auditors.
   * 
   * @return The number of auditors in the network.
   */
  public int numAuditors() {
    return auditors().size();
  }

  /**
   * Returns a set of network auditor addresses.
   * 
   * @return A set of network auditors.
   */
  public Set<String> auditors() {
    return auditors;
  }

  /**
   * Returns a boolean indicating whether acking is enabled.
   * 
   * @return Indicates whether acking is enabled for the network.
   */
  public boolean isAckingEnabled() {
    return acking;
  }

  /**
   * Returns a boolean indicating whether timeouts are enabled for the network.
   * 
   * @return Indicates whether timeouts are enabled for the network.
   */
  public boolean isMessageTimeoutsEnabled() {
    return timeout > 0;
  }

  /**
   * Returns network message timeout.
   * 
   * @return The message timeout for the network.
   */
  public long messageTimeout() {
    return timeout;
  }

  /**
   * Returns a list of network component contexts.
   * 
   * @return A list of network component contexts.
   */
  public List<ComponentContext<?>> components() {
    List<ComponentContext<?>> components = new ArrayList<>();
    for (ComponentContext<?> component : this.components.values()) {
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

  /**
   * Returns a component context by name.
   * 
   * @param name The component name.
   * @return A component context.
   * @throws IllegalArgumentException If a component does not exist at the given name.
   */
  @SuppressWarnings("unchecked")
  public <T extends ComponentContext<T>> T component(String name) {
    if (components.containsKey(name)) {
      return (T) components.get(name).setNetworkContext(this);
    }
    throw new IllegalArgumentException(name + " is not a valid component in " + address());
  }

  @Override
  @SuppressWarnings("unchecked")
  public void notify(NetworkContext update) {
    super.notify(update);
    for (@SuppressWarnings("rawtypes") ComponentContext component : components.values()) {
      boolean updated = false;
      for (@SuppressWarnings("rawtypes") ComponentContext c : update.components()) {
        if (component.equals(c)) {
          component.notify(c);
          updated = true;
          break;
        }
      }
      if (!updated) {
        component.notify(null);
      }
    }
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
  public static class Builder extends net.kuujo.vertigo.context.Context.Builder<NetworkContext> {

    private Builder() {
      super(new NetworkContext());
    }

    private Builder(NetworkContext context) {
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
      return new Builder(context);
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
     * Sets the network auditors.
     *
     * @param addresses An array of auditor addresses.
     * @return The context builder.
     */
    public Builder setAuditors(String... addresses) {
      context.auditors = new HashSet<String>(Arrays.asList(addresses));
      return this;
    }

    /**
     * Sets the network auditors.
     *
     * @param addresses A set of auditor addresses.
     * @return The context builder.
     */
    public Builder setAuditors(Set<String> addresses) {
      context.auditors = addresses;
      return this;
    }

    /**
     * Adds an auditor to the network.
     *
     * @param address An auditor address.
     * @return The context builder.
     */
    public Builder addAuditor(String address) {
      context.auditors.add(address);
      return this;
    }

    /**
     * Removes an auditor from the network.
     *
     * @param address An auditor address.
     * @return The context builder.
     */
    public Builder removeAuditor(String address) {
      context.auditors.remove(address);
      return this;
    }

    /**
     * Sets whether acking is enabled.
     *
     * @param enabled Whether acking is enabled.
     * @return The context builder.
     */
    public Builder setAckingEnabled(boolean enabled) {
      context.acking = enabled;
      return this;
    }

    /**
     * Sets whether message timeouts are enabled.
     *
     * @param enabled Whether message timeouts are enabled.
     * @return The context builder.
     */
    public Builder setMessageTimeoutsEnabled(boolean enabled) {
      if (!enabled) {
        context.timeout = 0;
      }
      else if (context.timeout == 0) {
        context.timeout = 30000;
      }
      return this;
    }

    /**
     * Sets the network message timeout.
     *
     * @param timeout The network message timeout.
     * @return The context builder.
     */
    public Builder setMessageTimeout(long timeout) {
      context.timeout = timeout;
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
    public Builder addComponent(ComponentContext<?> component) {
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
    public Builder removeComponent(ComponentContext<?> component) {
      if (context.components == null) {
        context.components = new HashMap<>();
      }
      context.components.remove(component.name());
      return this;
    }
  }

}
