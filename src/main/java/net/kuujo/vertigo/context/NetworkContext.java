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
import net.kuujo.vertigo.serializer.SerializationException;
import net.kuujo.vertigo.serializer.Serializer;

/**
 * A network context.
 *
 * @author Jordan Halterman
 */
public class NetworkContext implements Serializable {
  private JsonObject context;
  private static final long DEFAULT_ACK_EXPIRE = 30000;

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
   */
  public static NetworkContext fromJson(JsonObject context) {
    return new NetworkContext(context);
  }

  /**
   * Returns the network address.
   *
   * @return
   *   The network address.
   */
  public String getAddress() {
    return context.getString(Network.ADDRESS);
  }

  /**
   * Returns the broadcast address.
   *
   * @return
   *   The network broadcast address.
   */
  public String getBroadcastAddress() {
    return context.getString(Network.BROADCAST);
  }

  /**
   * Returns a list of network auditors.
   *
   * @return
   *   A list of network auditors.
   */
  public List<String> getAuditors() {
    List<String> auditors = new ArrayList<>();
    JsonArray auditorInfo = context.getArray(Network.AUDITORS);
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
    return context.getBoolean(Network.ACKING, true);
  }

  /**
   * Returns network ack expiration.
   *
   * @return
   *   Ack expirations for the network.
   */
  public long getAckExpire() {
    return context.getLong(Network.ACK_EXPIRE, DEFAULT_ACK_EXPIRE);
  }

  /**
   * Returns a list of network component contexts.
   *
   * @return
   *   A list of network component contexts.
   */
  public List<ComponentContext> getComponents() {
    List<ComponentContext> components = new ArrayList<>();
    JsonObject componentContexts = context.getObject(Network.COMPONENTS);
    for (String address : componentContexts.getFieldNames()) {
      try {
        ComponentContext component = Serializer.<ComponentContext>deserialize(componentContexts.getObject(address)).setParent(this);
        if (component != null) {
          components.add(component);
        }
      }
      catch (SerializationException e) {
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
    JsonObject components = context.getObject("components");
    if (components == null) {
      components = new JsonObject();
    }
    if (components.getFieldNames().contains(address)) {
      try {
        return Serializer.<ComponentContext>deserialize(components.getObject(address)).setParent(this);
      }
      catch (SerializationException e) {
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
