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

import com.fasterxml.jackson.annotation.JsonProperty;

import net.kuujo.vertigo.serializer.SerializationException;
import net.kuujo.vertigo.serializer.Serializer;

/**
 * A network context.
 *
 * @author Jordan Halterman
 */
public class NetworkContext {
  @JsonProperty            private String address;
  @JsonProperty            private List<String> auditors = new ArrayList<>();
  @JsonProperty("acking")  private boolean isAcking = true;
  @JsonProperty("timeout") private long ackTimeout = 30000;
  @JsonProperty            private Map<String, ComponentContext> components = new HashMap<>();

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
  public static NetworkContext fromJson(JsonObject context) throws MalformedContextException {
    try {
      return Serializer.getInstance().deserialize(context, NetworkContext.class);
    }
    catch (SerializationException e) {
      throw new MalformedContextException(e);
    }
  }

  /**
   * Returns the network address.
   *
   * @return
   *   The network address.
   */
  public String getAddress() {
    return address;
  }

  /**
   * Returns a list of network auditor addresses.
   *
   * @return
   *   A list of network auditors.
   */
  public List<String> getAuditors() {
    return auditors;
  }

  /**
   * Returns a boolean indicating whether acking is enabled.
   *
   * @return
   *   Indicates whether acking is enabled for the network.
   */
  public boolean isAckingEnabled() {
    return isAcking;
  }

  /**
   * Returns network ack timeout.
   *
   * @return
   *   Ack timeout for the network.
   */
  public long getAckTimeout() {
    return ackTimeout;
  }

  /**
   * Returns a list of network component contexts.
   *
   * @return
   *   A list of network component contexts.
   */
  public List<ComponentContext> getComponents() {
    List<ComponentContext> components = new ArrayList<>();
    for (ComponentContext component : this.components.values()) {
      components.add(component);
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
    return components.get(address);
  }

}
