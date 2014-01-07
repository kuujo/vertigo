/*
 * Copyright 2013 the original author or authors.
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.vertx.java.core.json.JsonObject;

import net.kuujo.vertigo.serializer.SerializerFactory;

/**
 * A network context which contains information regarding the complete
 * structure of a deployed network. Network contexts are immutable as
 * they are constructed after a network is deployed.
 *
 * @author Jordan Halterman
 */
public final class NetworkContext implements Context {
  private String address;
  private List<String> auditors = new ArrayList<>();
  private boolean acking = true;
  private long timeout = 30000;
  private Map<String, ComponentContext<?>> components = new HashMap<>();

  private NetworkContext() {
  }

  /**
   * Creates a network context from JSON.
   *
   * @param context
   *   A JSON representation of the network context.
   * @return
   *   A new network context instance.
   * @throws MalformedContextException
   *   If the network context is malformed.
   */
  public static NetworkContext fromJson(JsonObject context) {
    return SerializerFactory.getSerializer(Context.class)
        .deserialize(context.getObject("network"), NetworkContext.class);
  }

  /**
   * Serializes a network context to JSON.
   *
   * @param context
   *   The network context to serialize.
   * @return
   *   A serialized network context.
   */
  public static JsonObject toJson(NetworkContext context) {
    return new JsonObject().putObject("network", SerializerFactory.getSerializer(Context.class).serialize(context));
  }

  /**
   * Returns the network address.
   *
   * @return
   *   The network address.
   */
  public String address() {
    return address;
  }

  @Deprecated
  public String getAddress() {
    return address();
  }

  /**
   * Returns a list of network auditor addresses.
   *
   * @return
   *   A list of network auditors.
   */
  public List<String> auditors() {
    return auditors;
  }

  @Deprecated
  public List<String> getAuditors() {
    return auditors();
  }

  /**
   * Returns a boolean indicating whether acking is enabled.
   *
   * @return
   *   Indicates whether acking is enabled for the network.
   */
  public boolean isAckingEnabled() {
    return acking;
  }

  /**
   * Returns a boolean indicating whether timeouts are enabled for the network.
   *
   * @return
   *   Indicates whether timeouts are enabled for the network.
   */
  public boolean isMessageTimeoutsEnabled() {
    return timeout > 0;
  }

  /**
   * Returns network message timeout.
   *
   * @return
   *   The message timeout for the network.
   */
  public long messageTimeout() {
    return timeout;
  }

  @Deprecated
  public long getAckTimeout() {
    return messageTimeout();
  }

  /**
   * Returns a list of network component contexts.
   *
   * @return
   *   A list of network component contexts.
   */
  public List<ComponentContext<?>> components() {
    List<ComponentContext<?>> components = new ArrayList<>();
    for (ComponentContext<?> component : this.components.values()) {
      components.add(component.setNetworkContext(this));
    }
    return components;
  }

  @Deprecated
  public List<ComponentContext<?>> getComponents() {
    return components();
  }

  /**
   * Returns a component context by address.
   *
   * @param address
   *   The component address.
   * @return
   *   A component context.
   * @throws IllegalArgumentException
   *   If a component does not exist at the given address.
   */
  @SuppressWarnings({"unchecked", "rawtypes"})
  public <T extends ComponentContext> T component(String address) {
    if (components.containsKey(address)) {
      return (T) components.get(address).setNetworkContext(this);
    }
    throw new IllegalArgumentException(address + " is not a valid component in " + address());
  }

  @Deprecated
  @SuppressWarnings("rawtypes")
  public <T extends net.kuujo.vertigo.component.Component> ComponentContext<T> getComponent(String address) {
    return component(address);
  }

  @Override
  public String toString() {
    return address();
  }

}
