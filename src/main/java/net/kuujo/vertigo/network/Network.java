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
import java.util.List;
import java.util.UUID;

import net.kuujo.vertigo.serializer.Serializable;

import org.vertx.java.core.json.JsonObject;

/**
 * A Vertigo network.
 *
 * @author Jordan Halterman
 */
public class Network implements Serializable {
  public static final String ADDRESS = "address";
  public static final String BROADCAST = "broadcast";
  public static final String AUDITORS = "auditors";
  public static final String ACKING = "acking";
  public static final String ACK_TIMEOUT = "timeout";
  public static final String ACK_DELAY = "ack_delay";
  public static final String COMPONENTS = "components";

  public static final long DEFAULT_ACK_TIMEOUT = 30000;
  public static final long DEFAULT_ACK_DELAY = 0;

  private JsonObject definition;

  public Network() {
    definition = new JsonObject();
    init();
  }

  private Network(JsonObject definition) {
    this.definition = definition;
    init();
  }

  public Network(String address) {
    definition = new JsonObject().putString(ADDRESS, address);
    init();
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
    JsonObject components = json.getObject(COMPONENTS);
    if (components == null) {
      components = new JsonObject();
      json.putObject(COMPONENTS, components);
    }

    for (String address : components.getFieldNames()) {
      JsonObject componentInfo = components.getObject(address);
      if (componentInfo == null) {
        components.removeField(address);
      }
      else {
        componentInfo.putString(Component.ADDRESS, address);
        // Construct the component instance to make sure the definition is not malformed.
        Component.fromJson(componentInfo);
      }
    }
    return new Network(json);
  }

  /**
   * Initializes the internal definition.
   */
  private void init() {
    String address = definition.getString(ADDRESS);
    if (address == null) {
      address = UUID.randomUUID().toString();
      definition.putString(ADDRESS, address);
    }

    String broadcast = definition.getString(BROADCAST);
    if (broadcast == null) {
      definition.putString(BROADCAST, String.format("%s.%s", address, BROADCAST));
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
    return definition.getString(ADDRESS);
  }

  /**
   * Returns the network broadcaster address.
   *
   * @return
   *   The network broadcast address.
   */
  public String getBroadcastAddress() {
    return definition.getString(BROADCAST, String.format("%s.%s", getAddress(), BROADCAST));
  }

  /**
   * Sets the network broadcast event bus address.
   *
   * @param address
   *   The network broadcast address. This is the address at which the network
   *   will publish ack/fail/timeout messages.
   * @return
   *   The called network instance.
   */
  public Network setBroadcastAddress(String address) {
    definition.putString(BROADCAST, address);
    return this;
  }

  /**
   * Returns the number of network auditors.
   *
   * @return
   *   The number of network auditors.
   */
  public int getNumAuditors() {
    return definition.getInteger(AUDITORS, 1);
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
  public Network setNumAuditors(int numAuditors) {
    definition.putNumber(AUDITORS, numAuditors);
    return this;
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
    definition.putBoolean(ACKING, true);
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
    definition.putBoolean(ACKING, false);
    return this;
  }

  /**
   * Returns a boolean indicating whether acking is enabled.
   *
   * @return
   *   Indicates whether acking is enabled for the network.
   */
  public boolean isAckingEnabled() {
    return definition.getBoolean(ACKING, true);
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
    definition.putNumber(ACK_TIMEOUT, timeout);
    return this;
  }

  /**
   * Gets the network ack timeout.
   *
   * @return
   *   Ack timeout for the network. Defaults to 30000
   */
  public long getAckTimeout() {
    return definition.getLong(ACK_TIMEOUT, DEFAULT_ACK_TIMEOUT);
  }

  /**
   * Sets the network ack delay.
   *
   * This indicates the amount of time an auditor will wait before notifying a
   * message source that a message has complete processing. If a new descendant
   * message is created during the delay period, the auditor will continue tracking
   * the tree and will not notify the message source until the new message and
   * its descendants have completed processing. This allows component implementations
   * to hold messages in memory for some period before creating new children. It
   * may also help resolve issues where blocking is preventing auditors from working
   * properly.
   *
   * @param delay
   *   The ack delay.
   * @return
   *   The called network instance.
   */
  public Network setAckDelay(long delay) {
    definition.putNumber(ACK_DELAY, delay);
    return this;
  }

  /**
   * Gets the network ack delay.
   *
   * @return
   *   Ack delay for the network. Defaults to 0
   */
  public long getAckDelay() {
    return definition.getLong(ACK_DELAY, DEFAULT_ACK_DELAY);
  }

  /**
   * Gets a list of network components.
   *
   * @return
   *   A list of network components.
   */
  public List<Component<?>> getComponents() {
    List<Component<?>> components = new ArrayList<Component<?>>();
    JsonObject componentInfo = definition.getObject(COMPONENTS);
    if (componentInfo == null) {
      componentInfo = new JsonObject();
      definition.putObject(COMPONENTS, componentInfo);
    }

    for (String address : componentInfo.getFieldNames()) {
      Component<?> component = getComponent(address);
      if (component != null) {
        components.add(component);
      }
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
    JsonObject components = definition.getObject(COMPONENTS);
    if (components == null) {
      components = new JsonObject();
      definition.putObject(COMPONENTS, components);
    }

    if (components.getFieldNames().contains(address)) {
      try {
        return Component.fromJson(components.getObject(address));
      }
      catch (MalformedNetworkException e) {
        return null;
      }
    }
    return null;
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
    JsonObject components = definition.getObject(COMPONENTS);
    if (components == null) {
      components = new JsonObject();
      definition.putObject(COMPONENTS, components);
    }
    components.putObject(component.getAddress(), component.getState());
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

  @Override
  public JsonObject getState() {
    return definition;
  }

  @Override
  public void setState(JsonObject state) {
    definition = state;
  }

}
