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

import net.kuujo.vertigo.util.serializer.SerializationException;
import net.kuujo.vertigo.util.serializer.SerializerFactory;

import org.vertx.java.core.json.JsonObject;

import com.fasterxml.jackson.annotation.JsonSetter;

/**
 * A Vertigo network configuration.
 * <p>
 * 
 * A network is a collection of <code>components</code> - Vert.x verticles or modules -
 * that are interconnected in a meaningful and reliable manner. This class is used to
 * define such structures.
 * 
 * @author Jordan Halterman
 */
public final class Network implements Config {

  /**
   * <code>address</code> is a string indicating the unique network address. This is the
   * address at which the network will monitor network components. This field is required.
   */
  public static final String NETWORK_ADDRESS = "address";

  /**
   * <code>auditors</code> is a number indicating the number of auditor instances to
   * deploy with the network. Auditors are used to track messages through the network, and
   * increasing the number of auditors may help improve performace in networks with high
   * message volume. Defaults to <code>1</code>
   */
  public static final String NETWORK_NUM_AUDITORS = "auditors";

  /**
   * <code>acking</code> is a boolean indicating whether acking is enabled for the
   * network. If acking is disabled then messages will not be tracked through the network.
   * Instead, messages will be immediately "completed" once they have been emitted from a
   * component. Defaults to <code>true</code> (acking enabled).
   */
  public static final String NETWORK_ACKING_ENABLED = "acking";

  /**
   * <code>timeouts</code> is a boolean indicating whether message timeouts are enabled
   * for the network. If message timeouts are disabled then auditors will never time out
   * messages. Ack and failure mechanisms will continue to work. Defaults to
   * <code>true</code> (timweouts enabled).
   */
  public static final String NETWORK_MESSAGE_TIMEOUTS_ENABLED = "timeouts";

  /**
   * <code>timeout</code> is a number indicating the number of milliseconds after which a
   * not-yet-completed message should be timed out. Defaults to <code>30000</code> (30
   * seconds).
   */
  public static final String NETWORK_MESSAGE_TIMEOUT = "timeout";

  /**
   * <code>components</code> is an object defining network component configurations. Each
   * item in the object must be keyed by the unique component address, with each item
   * being an object containing the component configuration. See the {@link Component}
   * class for component configuration options.
   */
  public static final String NETWORK_COMPONENTS = "components";

  private static final int DEFAULT_NUM_AUDITORS = 1;
  private static final long DEFAULT_MESSAGE_TIMEOUT = 30000;

  private String address;
  private int auditors = DEFAULT_NUM_AUDITORS;
  private boolean acking = true;
  private long timeout = DEFAULT_MESSAGE_TIMEOUT;
  private Map<String, Component<?>> components = new HashMap<String, Component<?>>();

  public Network() {
    address = UUID.randomUUID().toString();
  }

  public Network(String address) {
    this.address = address;
  }

  /**
   * Creates a network from JSON.
   * 
   * @param json A JSON representation of the network.
   * @return A new network configuration.
   * @throws MalformedNetworkException If the network definition is malformed.
   */
  public static Network fromJson(JsonObject json) {
    try {
      return SerializerFactory.getSerializer(Network.class).deserializeObject(json, Network.class);
    }
    catch (SerializationException e) {
      throw new MalformedNetworkException(e);
    }
  }

  /**
   * Returns the network address.
   * 
   * This is the event bus address at which the network's coordinator will register a
   * handler for components to connect to once deployed.
   * 
   * @return The network address.
   */
  public String getAddress() {
    return address;
  }

  /**
   * Enables acking on the network.
   * 
   * When acking is enabled, network auditors will track message trees throughout the
   * network and notify messages sources once messages have completed processing.
   * 
   * @return The network configuration.
   */
  public Network enableAcking() {
    acking = true;
    return this;
  }

  /**
   * Disables acking on the network.
   * 
   * When acking is disabled, messages will not be tracked through networks. This
   * essentially meands that all messages will be assumed to have been successfully
   * processed. Disable acking at your own risk.
   * 
   * @return The network configuration.
   */
  public Network disableAcking() {
    acking = false;
    return this;
  }

  /**
   * Sets acking on the network.
   * 
   * @param enabled Whether acking is enabled for the network.
   * @return The network configuration.
   */
  public Network setAckingEnabled(boolean enabled) {
    acking = enabled;
    return this;
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
   * Returns the number of network auditors.
   * 
   * @return The number of network auditors.
   */
  public int getNumAuditors() {
    return auditors;
  }

  /**
   * Sets the number of network auditors.
   * 
   * This is the number of auditor verticle instances that will be used to track messages
   * throughout a network. The Vertigo message tracking algorithm is designed to be
   * extremely memory efficient, so it's unlikely that memory will be an issue. However,
   * if performance of your network is an issue (particularly in larger networks) you may
   * need to increase the number of network auditors.
   * 
   * @param numAuditors The number of network auditors.
   * @return The network configuration.
   */
  public Network setNumAuditors(int numAuditors) {
    this.auditors = numAuditors;
    return this;
  }

  /**
   * Enables message timeouts for the network.
   * 
   * @return The network configuration.
   */
  public Network enableMessageTimeouts() {
    if (timeout == 0) {
      timeout = DEFAULT_MESSAGE_TIMEOUT;
    }
    return this;
  }

  /**
   * Disables message timeouts for the network.
   * 
   * @return The network configuration.
   */
  public Network disableMessageTimeouts() {
    timeout = 0;
    return this;
  }

  /**
   * Sets whether message timeouts are enabled for the network.
   * 
   * @param isEnabled Indicates whether to enable message timeouts.
   * @return The network configuration.
   */
  @JsonSetter("timeouts")
  public Network setMessageTimeoutsEnabled(boolean isEnabled) {
    if (isEnabled) {
      return enableMessageTimeouts();
    }
    else {
      return disableMessageTimeouts();
    }
  }

  /**
   * Returns a boolean indicating whether message timeouts are enabled for the network.
   * 
   * @return Indicates whether message timeouts are enabled.
   */
  public boolean isMessageTimeoutsEnabled() {
    return timeout > 0;
  }

  /**
   * Sets the network message timeout.
   * 
   * This indicates the maximum amount of time an auditor will hold message information in
   * memory before considering it to be timed out.
   * 
   * @param timeout A message timeout in milliseconds.
   * @return The network configuration.
   */
  public Network setMessageTimeout(long timeout) {
    this.timeout = timeout;
    return this;
  }

  /**
   * Gets the network message timeout.
   * 
   * @return The message timeout for the network in milliseconds. Defaults to 30000
   */
  public long getMessageTimeout() {
    return timeout;
  }

  /**
   * Gets a list of network components.
   * 
   * @return A list of network components.
   */
  public List<Component<?>> getComponents() {
    List<Component<?>> components = new ArrayList<Component<?>>();
    for (Map.Entry<String, Component<?>> entry : this.components.entrySet()) {
      components.add(entry.getValue().setName(entry.getKey()));
    }
    return components;
  }

  /**
   * Gets a component by name.
   * 
   * @param name The component name.
   * @return The component configuration.
   * @throws IllegalArgumentException If the given component address does not exist within
   *           the network.
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public <T extends Component> T getComponent(String name) {
    if (components.containsKey(name)) {
      return (T) components.get(name);
    }
    throw new IllegalArgumentException(name + " is not a valid component name in " + getAddress());
  }

  /**
   * Adds a component to the network.
   * 
   * @param component The component to add.
   * @return The added component configuration.
   */
  public <T extends Component<T>> T addComponent(T component) {
    components.put(component.getName(), component);
    return component;
  }

  /**
   * Adds a module to the network.
   * 
   * @param module The module to add.
   * @return The added module component configuration.
   */
  public Module addModule(Module module) {
    components.put(module.getName(), module);
    return module;
  }

  /**
   * Adds a module to the network.
   * 
   * @param name The component name. This will be used as the basis for internal
   *          component addresses.
   * @param moduleName The module name.
   * @return The new module configuration.
   * @throws IllegalArgumentException If the module name is not a valid module identifier.
   */
  public Module addModule(String name, String moduleName) {
    return addModule(new Module(name, moduleName));
  }

  /**
   * Adds a module to the network.
   * 
   * @param name The component name. This will be used as the basis for internal
   *          component addresses.
   * @param moduleName The module name.
   * @param config The module configuration. This configuration will be made
   *          available as the verticle configuration within deployed module instances.
   * @return The new module configuration.
   * @throws IllegalArgumentException If the module name is not a valid module identifier.
   */
  public Module addModule(String name, String moduleName, JsonObject config) {
    return addModule(new Module(name, moduleName).setConfig(config));
  }

  /**
   * Adds a module to the network.
   * 
   * @param name The component name. This will be used as the basis for internal
   *          component addresses.
   * @param moduleName The module name.
   * @param numInstances The number of module instances. If multiple instances are
   *          defined, groupings will be used to determine how messages are distributed
   *          between multiple component instances.
   * @return The new module configuration.
   * @throws IllegalArgumentException If the module name is not a valid module identifier.
   */
  public Module addModule(String name, String moduleName, int numInstances) {
    return addModule(new Module(name, moduleName).setNumInstances(numInstances));
  }

  /**
   * Adds a module to the network.
   * 
   * @param name The component name. This will be used as the basis for internal
   *          component addresses.
   * @param moduleName The module name.
   * @param config The module configuration. This configuration will be made
   *          available as the verticle configuration within deployed module instances.
   * @param numInstances The number of module instances. If multiple instances are
   *          defined, groupings will be used to determine how messages are distributed
   *          between multiple component instances.
   * @return The new module configuration.
   * @throws IllegalArgumentException If the module name is not a valid module identifier.
   */
  public Module addModule(String name, String moduleName, JsonObject config, int numInstances) {
    return addModule(new Module(name, moduleName).setConfig(config).setNumInstances(numInstances));
  }

  /**
   * Adds a verticle to the network.
   * 
   * @param verticle The verticle to add.
   * @return The added verticle component configuration.
   */
  public Verticle addVerticle(Verticle verticle) {
    components.put(verticle.getName(), verticle);
    return verticle;
  }

  /**
   * Adds a verticle to the network.
   * 
   * @param name The component name. This will be used as the basis for internal
   *          component addresses.
   * @param main The verticle main.
   * @return The new verticle configuration.
   */
  public Verticle addVerticle(String name, String main) {
    return addVerticle(new Verticle(name, main));
  }

  /**
   * Adds a verticle to the network.
   * 
   * @param name The component name. This will be used as the basis for internal
   *          component addresses.
   * @param main The verticle main.
   * @param config The verticle configuration. This configuration will be made
   *          available as the verticle configuration within deployed module instances.
   * @return The new verticle configuration.
   */
  public Verticle addVerticle(String name, String main, JsonObject config) {
    return addVerticle(new Verticle(name, main).setConfig(config));
  }

  /**
   * Adds a verticle to the network.
   * 
   * @param name The component name. This will be used as the basis for internal
   *          component addresses.
   * @param main The verticle main.
   * @param numInstances The number of verticle instances. If multiple instances are
   *          defined, groupings will be used to determine how messages are distributed
   *          between multiple component instances.
   * @return The new verticle configuration.
   */
  public Verticle addVerticle(String name, String main, int numInstances) {
    return addVerticle(new Verticle(name, main).setNumInstances(numInstances));
  }

  /**
   * Adds a verticle to the network.
   * 
   * @param name The component name. This will be used as the basis for internal
   *          component addresses.
   * @param main The verticle main.
   * @param config The verticle configuration. This configuration will be made
   *          available as the verticle configuration within deployed module instances.
   * @param numInstances The number of verticle instances. If multiple instances are
   *          defined, groupings will be used to determine how messages are distributed
   *          between multiple component instances.
   * @return The new verticle configuration.
   */
  public Verticle addVerticle(String name, String main, JsonObject config, int numInstances) {
    return addVerticle(new Verticle(name, main).setConfig(config).setNumInstances(numInstances));
  }

  @Override
  public String toString() {
    return getAddress();
  }

}
