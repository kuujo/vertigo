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
package net.kuujo.vertigo.network;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.kuujo.vertigo.serializer.SerializationException;
import net.kuujo.vertigo.serializer.Serializer;

import org.vertx.java.core.json.JsonObject;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A Vertigo network.
 *
 * @author Jordan Halterman
 */
public final class Network {
  private static final long DEFAULT_ACK_TIMEOUT = 30000;

  @JsonProperty(required=true) private String address;
  @JsonProperty("auditors")    private int numAuditors = 1;
  @JsonProperty("acking")      private boolean isAcking = true;
  @JsonProperty("timeout")     private long ackTimeout = DEFAULT_ACK_TIMEOUT;
  @JsonProperty                private Map<String, Component<?>> components = new HashMap<String, Component<?>>();

  public Network() {
    address = UUID.randomUUID().toString();
  }

  public Network(String address) {
    this.address = address;
  }

  /**
   * Creates a network from JSON.
   *
   * @param json
   *   A JSON representation of the network.
   * @return
   *   A new network instance.
   * @throws MalformedNetworkException
   *   If the network definition is malformed.
   */
  public static Network fromJson(JsonObject json) throws MalformedNetworkException {
    try {
      return Serializer.getInstance().deserialize(json, Network.class);
    }
    catch (SerializationException e) {
      throw new MalformedNetworkException(e);
    }
  }

  /**
   * Returns the network address.
   *
   * This is the event bus address at which the network's coordinator will register
   * a handler for components to connect to once deployed.
   *
   * @return
   *   The network address.
   */
  public String getAddress() {
    return address;
  }

  /**
   * Enables acking on the network.
   *
   * When acking is enabled, network auditors will track message trees throughout
   * the network and notify messages sources once messages have completed processing.
   *
   * @return
   *   The called network instance.
   */
  public Network enableAcking() {
    isAcking = true;
    return this;
  }

  /**
   * Disables acking on the network.
   *
   * When acking is disabled, network auditors will immediately ack any new
   * messages that are made known to them.
   *
   * @return
   *   The called network instance.
   */
  public Network disableAcking() {
    isAcking = false;
    return this;
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
   * Returns the number of network auditors.
   *
   * @return
   *   The number of network auditors.
   */
  public int getNumAuditors() {
    return numAuditors;
  }

  /**
   * Sets the number of network auditors.
   *
   * This is the number of auditor verticle instances that will be used to track
   * messages throughout a network. If the network is slowing due to acking
   * overflows, you may need to increase the number of network auditors.
   *
   * @param numAuditors
   *   The number of network auditors.
   * @return
   *   The called network instance.
   */
  public Network setNumAckers(int numAuditors) {
    this.numAuditors = numAuditors;
    return this;
  }

  /**
   * Sets the network ack timeout.
   *
   * This indicates the maximum amount of time an auditor will hold message
   * information in memory before considering it to be timed out.
   *
   * @param timeout
   *   An ack timeout.
   * @return
   *   The called network instance.
   */
  public Network setAckTimeout(long timeout) {
    ackTimeout = timeout;
    return this;
  }

  /**
   * Gets the network ack timeout.
   *
   * @return
   *   Ack timeout for the network. Defaults to 30000
   */
  public long getAckTimeout() {
    return ackTimeout;
  }

  /**
   * Gets a list of network components.
   *
   * @return
   *   A list of network components.
   */
  public List<Component<?>> getComponents() {
    List<Component<?>> components = new ArrayList<Component<?>>();
    for (Component<?> component : this.components.values()) {
      components.add(component);
    }
    return components;
  }

  /**
   * Gets a component by address.
   *
   * @param address
   *   The component address.
   * @return
   *   A component instance, or null if the component does not exist in the network.
   */
  public Component<?> getComponent(String address) {
    return components.get(address);
  }

  /**
   * Adds a component to the network.
   *
   * @param component
   *   The component to add.
   * @return
   *   The added component instance.
   */
  public <T extends Component<?>> T addComponent(T component) {
    components.put(component.getAddress(), component);
    return component;
  }

  /**
   * Adds a verticle component to the network.
   *
   * @param address
   *   The component address. This is the address to which other components may
   *   connect to listen to the component's output.
   * @return
   *   The new verticle component instance.
   */
  public Verticle addVerticle(String address) {
    return addComponent(new Verticle(address));
  }

  /**
   * Adds a verticle component to the network.
   *
   * @param address
   *   The component address. This is the address to which other components may
   *   connect to listen to the component's output.
   * @param main
   *   The verticle main.
   * @return
   *   The new verticle component instance.
   */
  public Verticle addVerticle(String address, String main) {
    return addComponent(new Verticle(address).setMain(main));
  }

  /**
   * Adds a verticle component to the network.
   *
   * @param address
   *   The component address. This is the address to which other components may
   *   connect to listen to the component's output.
   * @param main
   *   The verticle main.
   * @param config
   *   The verticle component configuration. This configuration will be made
   *   available as the normal Vert.x container configuration within the verticle.
   * @return
   *   The new verticle component instance.
   */
  public Verticle addVerticle(String address, String main, JsonObject config) {
    return addComponent(new Verticle(address).setMain(main).setConfig(config));
  }

  /**
   * Adds a verticle component to the network.
   *
   * @param address
   *   The component address. This is the address to which other components may
   *   connect to listen to the component's output.
   * @param main
   *   The verticle main.
   * @param instances
   *   The number of component instances.
   * @return
   *   The new verticle component instance.
   */
  public Verticle addVerticle(String address, String main, int instances) {
    return addComponent(new Verticle(address).setMain(main).setInstances(instances));
  }

  /**
   * Adds a verticle component to the network.
   *
   * @param address
   *   The component address. This is the address to which other components may
   *   connect to listen to the component's output.
   * @param main
   *   The verticle main.
   * @param config
   *   The verticle component configuration. This configuration will be made
   *   available as the normal Vert.x container configuration within the verticle.
   * @param instances
   *   The number of component instances.
   * @return
   *   The new verticle component instance.
   */
  public Verticle addVerticle(String address, String main, JsonObject config, int instances) {
    return addComponent(new Verticle(address).setMain(main).setConfig(config).setInstances(instances));
  }

  /**
   * Adds a module component to the network.
   *
   * @param address
   *   The component address. This is the address to which other components may
   *   connect to listen to the component's output.
   * @return
   *   The new module component instance.
   */
  public Module addModule(String address) {
    return addComponent(new Module(address));
  }

  /**
   * Adds a module component to the network.
   *
   * @param address
   *   The component address. This is the address to which other components may
   *   connect to listen to the component's output.
   * @param moduleName
   *   The module name.
   * @return
   *   The new module component instance.
   */
  public Module addModule(String address, String moduleName) {
    return addComponent(new Module(address).setModule(moduleName));
  }

  /**
   * Adds a module component to the network.
   *
   * @param address
   *   The component address. This is the address to which other components may
   *   connect to listen to the component's output.
   * @param moduleName
   *   The module name.
   * @param config
   *   The module component configuration. This configuration will be made
   *   available as the normal Vert.x container configuration within the module.
   * @return
   *   The new module component instance.
   */
  public Module addModule(String address, String moduleName, JsonObject config) {
    return addComponent(new Module(address).setModule(moduleName).setConfig(config));
  }

  /**
   * Adds a module component to the network.
   *
   * @param address
   *   The component address. This is the address to which other components may
   *   connect to listen to the component's output.
   * @param moduleName
   *   The module name.
   * @param instances
   *   The number of component instances.
   * @return
   *   The new module component instance.
   */
  public Module addModule(String address, String moduleName, int instances) {
    return addComponent(new Module(address).setModule(moduleName).setInstances(instances));
  }

  /**
   * Adds a module component to the network.
   *
   * @param address
   *   The component address. This is the address to which other components may
   *   connect to listen to the component's output.
   * @param moduleName
   *   The module name.
   * @param config
   *   The module component configuration. This configuration will be made
   *   available as the normal Vert.x container configuration within the module.
   * @param instances
   *   The number of component instances.
   * @return
   *   The new module component instance.
   */
  public Module addModule(String address, String moduleName, JsonObject config, int instances) {
    return addComponent(new Module(address).setModule(moduleName).setConfig(config).setInstances(instances));
  }

}
