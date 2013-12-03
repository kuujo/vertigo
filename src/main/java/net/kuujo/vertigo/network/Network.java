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

import net.kuujo.vertigo.serializer.Serializable;
import net.kuujo.vertigo.serializer.SerializationException;
import net.kuujo.vertigo.serializer.Serializers;

import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.impl.ModuleIdentifier;

/**
 * A Vertigo network.
 *
 * @author Jordan Halterman
 */
public final class Network implements Serializable {
  private static final long DEFAULT_ACK_TIMEOUT = 30000;

  private String address;
  private int auditors = 1;
  private boolean acking = true;
  private long timeout = DEFAULT_ACK_TIMEOUT;
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
   * @param json
   *   A JSON representation of the network.
   * @return
   *   A new network instance.
   * @throws MalformedNetworkException
   *   If the network definition is malformed.
   */
  public static Network fromJson(JsonObject json) throws MalformedNetworkException {
    try {
      return Serializers.getDefault().deserialize(json, Network.class);
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
    acking = true;
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
    acking = false;
    return this;
  }

  /**
   * Sets acking on the network.
   *
   * @param enabled
   *   Whether acking is enabled for the network.
   * @return
   *   The called network instance.
   */
  public Network setAckingEnabled(boolean enabled) {
    acking = enabled;
    return this;
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
   * Returns the number of network auditors.
   *
   * @return
   *   The number of network auditors.
   */
  public int getNumAuditors() {
    return auditors;
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
    this.auditors = numAuditors;
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
    this.timeout = timeout;
    return this;
  }

  /**
   * Gets the network ack timeout.
   *
   * @return
   *   Ack timeout for the network. Defaults to 30000
   */
  public long getAckTimeout() {
    return timeout;
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
  @Deprecated
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
  @Deprecated
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
  @Deprecated
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
  @Deprecated
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
  @Deprecated
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
  @Deprecated
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
  @Deprecated
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
  @Deprecated
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
  @Deprecated
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
  @Deprecated
  public Module addModule(String address, String moduleName, JsonObject config, int instances) {
    return addComponent(new Module(address).setModule(moduleName).setConfig(config).setInstances(instances));
  }

  /**
   * Returns a boolean indicating whether the given string is a module name.
   * This validation is performed by using the core Vert.x module name validation
   * contained in the {@link ModuleIdentifier} class.
   */
  private boolean isModuleName(String name) {
    try {
      new ModuleIdentifier(name);
    }
    catch (IllegalArgumentException e) {
      return false;
    }
    return true;
  }

  /**
   * Adds a feeder component to the network.
   *
   * @param address
   *   The component address.
   * @param moduleOrMain
   *   The feeder component main or module name.
   * @return
   *   The new feeder component instance.
   */
  public Component<?> addFeeder(String address, String moduleOrMain) {
    if (isModuleName(moduleOrMain)) {
      return addComponent(new Module(address).setModule(moduleOrMain).setGroup(Component.Group.FEEDER));
    }
    else {
      return addComponent(new Verticle(address).setMain(moduleOrMain).setGroup(Component.Group.FEEDER));
    }
  }

  /**
   * Adds a feeder component to the network.
   *
   * @param address
   *   The component address.
   * @param moduleOrMain
   *   The feeder component main or module name.
   * @param config
   *   The feeder component configuration.
   * @return
   *   The new feeder component instance.
   */
  public Component<?> addFeeder(String address, String moduleOrMain, JsonObject config) {
    if (isModuleName(moduleOrMain)) {
      return addComponent(new Module(address).setModule(moduleOrMain).setConfig(config).setGroup(Component.Group.FEEDER));
    }
    else {
      return addComponent(new Verticle(address).setMain(moduleOrMain).setConfig(config).setGroup(Component.Group.FEEDER));
    }
  }

  /**
   * Adds a feeder component to the network.
   *
   * @param address
   *   The component address.
   * @param moduleOrMain
   *   The feeder component main or module name.
   * @param instances
   *   The number of feeder instances.
   * @return
   *   The new feeder component instance.
   */
  public Component<?> addFeeder(String address, String moduleOrMain, int instances) {
    if (isModuleName(moduleOrMain)) {
      return addComponent(new Module(address).setModule(moduleOrMain).setInstances(instances).setGroup(Component.Group.FEEDER));
    }
    else {
      return addComponent(new Verticle(address).setMain(moduleOrMain).setInstances(instances).setGroup(Component.Group.FEEDER));
    }
  }

  /**
   * Adds a feeder component to the network.
   *
   * @param address
   *   The component address.
   * @param moduleOrMain
   *   The feeder component main or module name.
   * @param config
   *   The feeder component configuration.
   * @param instances
   *   The number of feeder instances.
   * @return
   *   The new feeder component instance.
   */
  public Component<?> addFeeder(String address, String moduleOrMain, JsonObject config, int instances) {
    if (isModuleName(moduleOrMain)) {
      return addComponent(new Module(address).setModule(moduleOrMain).setConfig(config).setInstances(instances).setGroup(Component.Group.FEEDER));
    }
    else {
      return addComponent(new Verticle(address).setMain(moduleOrMain).setConfig(config).setInstances(instances).setGroup(Component.Group.FEEDER));
    }
  }

  /**
   * Adds an executor component to the network.
   *
   * @param address
   *   The component address.
   * @param moduleOrMain
   *   The executor component main or module name.
   * @return
   *   The new executor component instance.
   */
  public Component<?> addExecutor(String address, String moduleOrMain) {
    if (isModuleName(moduleOrMain)) {
      return addComponent(new Module(address).setModule(moduleOrMain).setGroup(Component.Group.EXECUTOR));
    }
    else {
      return addComponent(new Verticle(address).setMain(moduleOrMain).setGroup(Component.Group.EXECUTOR));
    }
  }

  /**
   * Adds an executor component to the network.
   *
   * @param address
   *   The component address.
   * @param moduleOrMain
   *   The executor component main or module name.
   * @param config
   *   The executor component configuration.
   * @return
   *   The new executor component instance.
   */
  public Component<?> addExecutor(String address, String moduleOrMain, JsonObject config) {
    if (isModuleName(moduleOrMain)) {
      return addComponent(new Module(address).setModule(moduleOrMain).setConfig(config).setGroup(Component.Group.EXECUTOR));
    }
    else {
      return addComponent(new Verticle(address).setMain(moduleOrMain).setConfig(config).setGroup(Component.Group.EXECUTOR));
    }
  }

  /**
   * Adds an executor component to the network.
   *
   * @param address
   *   The component address.
   * @param moduleOrMain
   *   The executor component main or module name.
   * @param instances
   *   The number of executor instances.
   * @return
   *   The new executor component instance.
   */
  public Component<?> addExecutor(String address, String moduleOrMain, int instances) {
    if (isModuleName(moduleOrMain)) {
      return addComponent(new Module(address).setModule(moduleOrMain).setInstances(instances).setGroup(Component.Group.EXECUTOR));
    }
    else {
      return addComponent(new Verticle(address).setMain(moduleOrMain).setInstances(instances).setGroup(Component.Group.EXECUTOR));
    }
  }

  /**
   * Adds an executor component to the network.
   *
   * @param address
   *   The component address.
   * @param moduleOrMain
   *   The executor component main or module name.
   * @param config
   *   The executor component configuration.
   * @param instances
   *   The number of executor instances.
   * @return
   *   The new executor component instance.
   */
  public Component<?> addExecutor(String address, String moduleOrMain, JsonObject config, int instances) {
    if (isModuleName(moduleOrMain)) {
      return addComponent(new Module(address).setModule(moduleOrMain).setConfig(config).setInstances(instances).setGroup(Component.Group.EXECUTOR));
    }
    else {
      return addComponent(new Verticle(address).setMain(moduleOrMain).setConfig(config).setInstances(instances).setGroup(Component.Group.EXECUTOR));
    }
  }

  /**
   * Adds a worker component to the network.
   *
   * @param address
   *   The component address.
   * @param moduleOrMain
   *   The worker component main or module name.
   * @return
   *   The new worker component instance.
   */
  public Component<?> addWorker(String address, String moduleOrMain) {
    if (isModuleName(moduleOrMain)) {
      return addComponent(new Module(address).setModule(moduleOrMain).setGroup(Component.Group.WORKER));
    }
    else {
      return addComponent(new Verticle(address).setMain(moduleOrMain).setGroup(Component.Group.WORKER));
    }
  }

  /**
   * Adds a worker component to the network.
   *
   * @param address
   *   The component address.
   * @param moduleOrMain
   *   The worker component main or module name.
   * @param config
   *   The worker component configuration.
   * @return
   *   The new worker component instance.
   */
  public Component<?> addWorker(String address, String moduleOrMain, JsonObject config) {
    if (isModuleName(moduleOrMain)) {
      return addComponent(new Module(address).setModule(moduleOrMain).setConfig(config).setGroup(Component.Group.WORKER));
    }
    else {
      return addComponent(new Verticle(address).setMain(moduleOrMain).setConfig(config).setGroup(Component.Group.WORKER));
    }
  }

  /**
   * Adds a worker component to the network.
   *
   * @param address
   *   The component address.
   * @param moduleOrMain
   *   The worker component main or module name.
   * @param instances
   *   The number of worker instances.
   * @return
   *   The new worker component instance.
   */
  public Component<?> addWorker(String address, String moduleOrMain, int instances) {
    if (isModuleName(moduleOrMain)) {
      return addComponent(new Module(address).setModule(moduleOrMain).setInstances(instances).setGroup(Component.Group.WORKER));
    }
    else {
      return addComponent(new Verticle(address).setMain(moduleOrMain).setInstances(instances).setGroup(Component.Group.WORKER));
    }
  }

  /**
   * Adds a worker component to the network.
   *
   * @param address
   *   The component address.
   * @param moduleOrMain
   *   The worker component main or module name.
   * @param config
   *   The worker component configuration.
   * @param instances
   *   The number of worker instances.
   * @return
   *   The new worker component instance.
   */
  public Component<?> addWorker(String address, String moduleOrMain, JsonObject config, int instances) {
    if (isModuleName(moduleOrMain)) {
      return addComponent(new Module(address).setModule(moduleOrMain).setConfig(config).setInstances(instances).setGroup(Component.Group.WORKER));
    }
    else {
      return addComponent(new Verticle(address).setMain(moduleOrMain).setConfig(config).setInstances(instances).setGroup(Component.Group.WORKER));
    }
  }

  /**
   * Adds a filter component to the network.
   *
   * @param address
   *   The component address.
   * @param moduleOrMain
   *   The filter component main or module name.
   * @return
   *   The new filter component instance.
   */
  public Component<?> addFilter(String address, String moduleOrMain) {
    if (isModuleName(moduleOrMain)) {
      return addComponent(new Module(address).setModule(moduleOrMain).setGroup(Component.Group.FILTER));
    }
    else {
      return addComponent(new Verticle(address).setMain(moduleOrMain).setGroup(Component.Group.FILTER));
    }
  }

  /**
   * Adds a filter component to the network.
   *
   * @param address
   *   The component address.
   * @param moduleOrMain
   *   The filter component main or module name.
   * @param config
   *   The filter component configuration.
   * @return
   *   The new filter component instance.
   */
  public Component<?> addFilter(String address, String moduleOrMain, JsonObject config) {
    if (isModuleName(moduleOrMain)) {
      return addComponent(new Module(address).setModule(moduleOrMain).setConfig(config).setGroup(Component.Group.FILTER));
    }
    else {
      return addComponent(new Verticle(address).setMain(moduleOrMain).setConfig(config).setGroup(Component.Group.FILTER));
    }
  }

  /**
   * Adds a filter component to the network.
   *
   * @param address
   *   The component address.
   * @param moduleOrMain
   *   The filter component main or module name.
   * @param instances
   *   The number of filter instances.
   * @return
   *   The new filter component instance.
   */
  public Component<?> addFilter(String address, String moduleOrMain, int instances) {
    if (isModuleName(moduleOrMain)) {
      return addComponent(new Module(address).setModule(moduleOrMain).setInstances(instances).setGroup(Component.Group.FILTER));
    }
    else {
      return addComponent(new Verticle(address).setMain(moduleOrMain).setInstances(instances).setGroup(Component.Group.FILTER));
    }
  }

  /**
   * Adds a filter component to the network.
   *
   * @param address
   *   The component address.
   * @param moduleOrMain
   *   The filter component main or module name.
   * @param config
   *   The filter component configuration.
   * @param instances
   *   The number of filter instances.
   * @return
   *   The new filter component instance.
   */
  public Component<?> addFilter(String address, String moduleOrMain, JsonObject config, int instances) {
    if (isModuleName(moduleOrMain)) {
      return addComponent(new Module(address).setModule(moduleOrMain).setConfig(config).setInstances(instances).setGroup(Component.Group.FILTER));
    }
    else {
      return addComponent(new Verticle(address).setMain(moduleOrMain).setConfig(config).setInstances(instances).setGroup(Component.Group.FILTER));
    }
  }

}
