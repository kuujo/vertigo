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
import java.util.List;

import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import net.kuujo.vertigo.network.Network;
import net.kuujo.vertigo.serializer.Serializable;

/**
 * A network context.
 *
 * @author Jordan Halterman
 */
public class NetworkContext implements Serializable {
  public static final String ADDRESS = "address";
  public static final String BROADCAST = "broadcast";
  public static final String AUDITORS = "auditors";
  public static final String ACKING = "acking";
  public static final String ACK_TIMEOUT = "timeout";
  public static final String ACK_DELAY = "ack_delay";
  public static final String COMPONENTS = "components";

  private JsonObject context;

  public NetworkContext() {
    context = new JsonObject();
  }

  private NetworkContext(JsonObject context) {
    this.context = context;
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
  public static NetworkContext fromJson(JsonObject context) throws MalformedContextException {
    JsonObject components = context.getObject(COMPONENTS);
    if (components == null) {
      components = new JsonObject();
      context.putObject(COMPONENTS, components);
    }

    for (String address : components.getFieldNames()) {
      JsonObject componentInfo = components.getObject(address);
      if (componentInfo == null) {
        components.removeField(address);
      }
      else {
        // Instantiate a component context to throw an exception if the context is malformed.
        ComponentContext.fromJson(componentInfo);
      }
    }
    return new NetworkContext(context);
  }

  /**
   * Returns the network address.
   *
   * @return
   *   The network address.
   */
  public String getAddress() {
    return context.getString(ADDRESS);
  }

  /**
   * Returns the broadcast address.
   *
   * @return
   *   The network broadcast address.
   */
  public String getBroadcastAddress() {
    return context.getString(BROADCAST);
  }

  /**
   * Returns a list of network auditors.
   *
   * @return
   *   A list of network auditors.
   */
  public List<String> getAuditors() {
    List<String> auditors = new ArrayList<>();
    JsonArray auditorInfo = context.getArray(AUDITORS);
    for (Object address : auditorInfo) {
      auditors.add((String) address);
    }
    return auditors;
  }

  /**
   * Returns a boolean indicating whether acking is enabled.
   *
   * @return
   *   Indicates whether acking is enabled for the network.
   */
  public boolean isAckingEnabled() {
    return context.getBoolean(ACKING, true);
  }

  /**
   * Returns network ack timeout.
   *
   * @return
   *   Ack timeout for the network.
   */
  public long getAckTimeout() {
    return context.getLong(ACK_TIMEOUT, Network.DEFAULT_ACK_TIMEOUT);
  }

  /**
   * Returns network ack delay.
   *
   * @return
   *   Ack delay for the network.
   */
  public long getAckDelay() {
    return context.getLong(ACK_DELAY, Network.DEFAULT_ACK_DELAY);
  }

  /**
   * Returns a list of network component contexts.
   *
   * @return
   *   A list of network component contexts.
   */
  public List<ComponentContext> getComponents() {
    List<ComponentContext> components = new ArrayList<>();
    JsonObject componentContexts = context.getObject(COMPONENTS);
    for (String address : componentContexts.getFieldNames()) {
      try {
        ComponentContext component = ComponentContext.fromJson(componentContexts.getObject(address)).setParent(this);
        if (component != null) {
          components.add(component);
        }
      }
      catch (MalformedContextException e) {
        continue;
      }
    }
    return components;
  }

  /**
   * Returns a component context by address.
   *
   * @param address
   *   The component address.
   * @return
   *   A component context, or null if the component does not exist.
   */
  public ComponentContext getComponent(String address) {
    JsonObject components = context.getObject(COMPONENTS);
    if (components == null) {
      components = new JsonObject();
    }
    if (components.getFieldNames().contains(address)) {
      try {
        return ComponentContext.fromJson(components.getObject(address)).setParent(this);
      }
      catch (MalformedContextException e) {
        return null;
      }
    }
    return null;
  }

  @Override
  public JsonObject getState() {
    // Always copy the context state so it can't be modified externally.
    return context.copy();
  }

  @Override
  public void setState(JsonObject state) {
    context = state.copy();
  }

}
