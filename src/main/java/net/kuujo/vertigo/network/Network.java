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
import net.kuujo.vertigo.feeder.Feeder;
import net.kuujo.vertigo.rpc.Executor;
import net.kuujo.vertigo.worker.Worker;

import org.vertx.java.core.json.JsonObject;

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
  public static Network fromJson(JsonObject json) {
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
   * When acking is disabled, messages will not be tracked through networks. This
   * essentially meands that all messages will be assumed to have been successfully
   * processed. Disable acking at your own risk.
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
   * messages throughout a network. The Vertigo message tracking algorithm is
   * designed to be extremely memory efficient, so it's unlikely that memory will
   * be an issue. However, if performance of your network is an issue (particularly
   * in larger networks) you may need to increase the number of network auditors.
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
  @SuppressWarnings({"unchecked", "rawtypes"})
  public <T extends net.kuujo.vertigo.component.Component> Component<T> getComponent(String address) {
    return (Component<T>) components.get(address);
  }

  /**
   * Adds a component to the network.
   *
   * @param component
   *   The component to add.
   * @return
   *   The added component instance.
   */
  @SuppressWarnings("rawtypes")
  public <T extends net.kuujo.vertigo.component.Component> Component<T> addComponent(Component<T> component) {
    components.put(component.getAddress(), component);
    return component;
  }

  /**
   * Adds a feeder component to the network.
   *
   * @param address
   *   The component address. This should be a globally unique event bus address
   *   and can be any string.
   * @param moduleOrMain
   *   The feeder component main or module name. Vertigo will automatically detect
   *   whether the feeder is a module or a verticle based on module naming conventions.
   * @return
   *   The new feeder component instance.
   */
  public Component<Feeder> addFeeder(String address, String moduleOrMain) {
    return addComponent(new Component<Feeder>(Feeder.class, address, moduleOrMain));
  }

  /**
   * Adds a feeder component to the network.
   *
   * @param address
   *   The component address. This should be a globally unique event bus address
   *   and can be any string.
   * @param moduleOrMain
   *   The feeder component main or module name. Vertigo will automatically detect
   *   whether the feeder is a module or a verticle based on module naming conventions.
   * @param config
   *   The feeder component configuration. This configuration will be made available
   *   as the verticle configuration within the component implementation.
   * @return
   *   The new feeder component instance.
   */
  public Component<Feeder> addFeeder(String address, String moduleOrMain, JsonObject config) {
    return addComponent(new Component<Feeder>(Feeder.class, address, moduleOrMain).setConfig(config));
  }

  /**
   * Adds a feeder component to the network.
   *
   * @param address
   *   The component address. This should be a globally unique event bus address
   *   and can be any string.
   * @param moduleOrMain
   *   The feeder component main or module name. Vertigo will automatically detect
   *   whether the feeder is a module or a verticle based on module naming conventions.
   * @param instances
   *   The number of feeder instances. If multiple instances are defined, groupings
   *   will be used to determine how messages are distributed between multiple
   *   component instances.
   * @return
   *   The new feeder component instance.
   */
  public Component<Feeder> addFeeder(String address, String moduleOrMain, int instances) {
    return addComponent(new Component<Feeder>(Feeder.class, address, moduleOrMain).setInstances(instances));
  }

  /**
   * Adds a feeder component to the network.
   *
   * @param address
   *   The component address. This should be a globally unique event bus address
   *   and can be any string.
   * @param moduleOrMain
   *   The feeder component main or module name. Vertigo will automatically detect
   *   whether the feeder is a module or a verticle based on module naming conventions.
   * @param config
   *   The feeder component configuration. This configuration will be made available
   *   as the verticle configuration within the component implementation.
   * @param instances
   *   The number of feeder instances. If multiple instances are defined, groupings
   *   will be used to determine how messages are distributed between multiple
   *   component instances.
   * @return
   *   The new feeder component instance.
   */
  public Component<Feeder> addFeeder(String address, String moduleOrMain, JsonObject config, int instances) {
    return addComponent(new Component<Feeder>(Feeder.class, address, moduleOrMain).setConfig(config).setInstances(instances));
  }

  /**
   * Adds an executor component to the network.
   *
   * @param address
   *   The component address. This should be a globally unique event bus address
   *   and can be any string.
   * @param moduleOrMain
   *   The executor component main or module name. Vertigo will automatically detect
   *   whether the feeder is a module or a verticle based on module naming conventions.
   * @return
   *   The new executor component instance.
   */
  public Component<Executor> addExecutor(String address, String moduleOrMain) {
    return addComponent(new Component<Executor>(Executor.class, address, moduleOrMain));
  }

  /**
   * Adds an executor component to the network.
   *
   * @param address
   *   The component address. This should be a globally unique event bus address
   *   and can be any string.
   * @param moduleOrMain
   *   The executor component main or module name. Vertigo will automatically detect
   *   whether the feeder is a module or a verticle based on module naming conventions.
   * @param config
   *   The executor component configuration. This configuration will be made available
   *   as the verticle configuration within the component implementation.
   * @return
   *   The new executor component instance.
   */
  public Component<Executor> addExecutor(String address, String moduleOrMain, JsonObject config) {
    return addComponent(new Component<Executor>(Executor.class, address, moduleOrMain).setConfig(config));
  }

  /**
   * Adds an executor component to the network.
   *
   * @param address
   *   The component address. This should be a globally unique event bus address
   *   and can be any string.
   * @param moduleOrMain
   *   The executor component main or module name. Vertigo will automatically detect
   *   whether the feeder is a module or a verticle based on module naming conventions.
   * @param instances
   *   The number of executor instances. If multiple instances are defined, groupings
   *   will be used to determine how messages are distributed between multiple
   *   component instances.
   * @return
   *   The new executor component instance.
   */
  public Component<Executor> addExecutor(String address, String moduleOrMain, int instances) {
    return addComponent(new Component<Executor>(Executor.class, address, moduleOrMain).setInstances(instances));
  }

  /**
   * Adds an executor component to the network.
   *
   * @param address
   *   The component address. This should be a globally unique event bus address
   *   and can be any string.
   * @param moduleOrMain
   *   The executor component main or module name. Vertigo will automatically detect
   *   whether the feeder is a module or a verticle based on module naming conventions.
   * @param config
   *   The executor component configuration. This configuration will be made available
   *   as the verticle configuration within the component implementation.
   * @param instances
   *   The number of executor instances. If multiple instances are defined, groupings
   *   will be used to determine how messages are distributed between multiple
   *   component instances.
   * @return
   *   The new executor component instance.
   */
  public Component<Executor> addExecutor(String address, String moduleOrMain, JsonObject config, int instances) {
    return addComponent(new Component<Executor>(Executor.class, address, moduleOrMain).setConfig(config).setInstances(instances));
  }

  /**
   * Adds a worker component to the network.
   *
   * @param address
   *   The component address. This should be a globally unique event bus address
   *   and can be any string.
   * @param moduleOrMain
   *   The worker component main or module name. Vertigo will automatically detect
   *   whether the feeder is a module or a verticle based on module naming conventions.
   * @return
   *   The new worker component instance.
   */
  public Component<Worker> addWorker(String address, String moduleOrMain) {
    return addComponent(new Component<Worker>(Worker.class, address, moduleOrMain));
  }

  /**
   * Adds a worker component to the network.
   *
   * @param address
   *   The component address. This should be a globally unique event bus address
   *   and can be any string.
   * @param moduleOrMain
   *   The worker component main or module name. Vertigo will automatically detect
   *   whether the feeder is a module or a verticle based on module naming conventions.
   * @param config
   *   The worker component configuration. This configuration will be made available
   *   as the verticle configuration within the component implementation.
   * @return
   *   The new worker component instance.
   */
  public Component<Worker> addWorker(String address, String moduleOrMain, JsonObject config) {
    return addComponent(new Component<Worker>(Worker.class, address, moduleOrMain).setConfig(config));
  }

  /**
   * Adds a worker component to the network.
   *
   * @param address
   *   The component address. This should be a globally unique event bus address
   *   and can be any string.
   * @param moduleOrMain
   *   The worker component main or module name. Vertigo will automatically detect
   *   whether the feeder is a module or a verticle based on module naming conventions.
   * @param instances
   *   The number of worker instances. If multiple instances are defined, groupings
   *   will be used to determine how messages are distributed between multiple
   *   component instances.
   * @return
   *   The new worker component instance.
   */
  public Component<Worker> addWorker(String address, String moduleOrMain, int instances) {
    return addComponent(new Component<Worker>(Worker.class, address, moduleOrMain).setInstances(instances));
  }

  /**
   * Adds a worker component to the network.
   *
   * @param address
   *   The component address. This should be a globally unique event bus address
   *   and can be any string.
   * @param moduleOrMain
   *   The worker component main or module name. Vertigo will automatically detect
   *   whether the feeder is a module or a verticle based on module naming conventions.
   * @param config
   *   The worker component configuration. This configuration will be made available
   *   as the verticle configuration within the component implementation.
   * @param instances
   *   The number of worker instances. If multiple instances are defined, groupings
   *   will be used to determine how messages are distributed between multiple
   *   component instances.
   * @return
   *   The new worker component instance.
   */
  public Component<Worker> addWorker(String address, String moduleOrMain, JsonObject config, int instances) {
    return addComponent(new Component<Worker>(Worker.class, address, moduleOrMain).setConfig(config).setInstances(instances));
  }

}
