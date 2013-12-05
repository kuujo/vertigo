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
import net.kuujo.vertigo.filter.Filter;
import net.kuujo.vertigo.splitter.Splitter;
import net.kuujo.vertigo.aggregator.Aggregator;

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
   *   The component address.
   * @param moduleOrMain
   *   The feeder component main or module name.
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
   *   The component address.
   * @param moduleOrMain
   *   The feeder component main or module name.
   * @param config
   *   The feeder component configuration.
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
   *   The component address.
   * @param moduleOrMain
   *   The feeder component main or module name.
   * @param instances
   *   The number of feeder instances.
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
  public Component<Feeder> addFeeder(String address, String moduleOrMain, JsonObject config, int instances) {
    return addComponent(new Component<Feeder>(Feeder.class, address, moduleOrMain).setConfig(config).setInstances(instances));
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
  public Component<Executor> addExecutor(String address, String moduleOrMain) {
    return addComponent(new Component<Executor>(Executor.class, address, moduleOrMain));
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
  public Component<Executor> addExecutor(String address, String moduleOrMain, JsonObject config) {
    return addComponent(new Component<Executor>(Executor.class, address, moduleOrMain).setConfig(config));
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
  public Component<Executor> addExecutor(String address, String moduleOrMain, int instances) {
    return addComponent(new Component<Executor>(Executor.class, address, moduleOrMain).setInstances(instances));
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
  public Component<Executor> addExecutor(String address, String moduleOrMain, JsonObject config, int instances) {
    return addComponent(new Component<Executor>(Executor.class, address, moduleOrMain).setConfig(config).setInstances(instances));
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
  public Component<Worker> addWorker(String address, String moduleOrMain) {
    return addComponent(new Component<Worker>(Worker.class, address, moduleOrMain));
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
  public Component<Worker> addWorker(String address, String moduleOrMain, JsonObject config) {
    return addComponent(new Component<Worker>(Worker.class, address, moduleOrMain).setConfig(config));
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
  public Component<Worker> addWorker(String address, String moduleOrMain, int instances) {
    return addComponent(new Component<Worker>(Worker.class, address, moduleOrMain).setInstances(instances));
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
  public Component<Worker> addWorker(String address, String moduleOrMain, JsonObject config, int instances) {
    return addComponent(new Component<Worker>(Worker.class, address, moduleOrMain).setConfig(config).setInstances(instances));
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
  public Component<Filter> addFilter(String address, String moduleOrMain) {
    return addComponent(new Component<Filter>(Filter.class, address, moduleOrMain));
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
  public Component<Filter> addFilter(String address, String moduleOrMain, JsonObject config) {
    return addComponent(new Component<Filter>(Filter.class, address, moduleOrMain).setConfig(config));
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
  public Component<Filter> addFilter(String address, String moduleOrMain, int instances) {
    return addComponent(new Component<Filter>(Filter.class, address, moduleOrMain).setInstances(instances));
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
  public Component<Filter> addFilter(String address, String moduleOrMain, JsonObject config, int instances) {
    return addComponent(new Component<Filter>(Filter.class, address, moduleOrMain).setConfig(config).setInstances(instances));
  }

  /**
   * Adds a splitter component to the network.
   *
   * @param address
   *   The component address.
   * @param moduleOrMain
   *   The splitter component main or module name.
   * @return
   *   The new splitter component instance.
   */
  public Component<Splitter> addSplitter(String address, String moduleOrMain) {
    return addComponent(new Component<Splitter>(Splitter.class, address, moduleOrMain));
  }

  /**
   * Adds a splitter component to the network.
   *
   * @param address
   *   The component address.
   * @param moduleOrMain
   *   The splitter component main or module name.
   * @param config
   *   The splitter component configuration.
   * @return
   *   The new splitter component instance.
   */
  public Component<Splitter> addSplitter(String address, String moduleOrMain, JsonObject config) {
    return addComponent(new Component<Splitter>(Splitter.class, address, moduleOrMain).setConfig(config));
  }

  /**
   * Adds a splitter component to the network.
   *
   * @param address
   *   The component address.
   * @param moduleOrMain
   *   The splitter component main or module name.
   * @param instances
   *   The number of splitter instances.
   * @return
   *   The new splitter component instance.
   */
  public Component<Splitter> addSplitter(String address, String moduleOrMain, int instances) {
    return addComponent(new Component<Splitter>(Splitter.class, address, moduleOrMain).setInstances(instances));
  }

  /**
   * Adds a splitter component to the network.
   *
   * @param address
   *   The component address.
   * @param moduleOrMain
   *   The splitter component main or module name.
   * @param config
   *   The splitter component configuration.
   * @param instances
   *   The number of splitter instances.
   * @return
   *   The new splitter component instance.
   */
  public Component<Splitter> addSplitter(String address, String moduleOrMain, JsonObject config, int instances) {
    return addComponent(new Component<Splitter>(Splitter.class, address, moduleOrMain).setConfig(config).setInstances(instances));
  }

  /**
   * Adds an aggregator component to the network.
   *
   * @param address
   *   The component address.
   * @param moduleOrMain
   *   The aggregator component main or module name.
   * @return
   *   The new aggregator component instance.
   */
  @SuppressWarnings("rawtypes")
  public Component<Aggregator> addAggregator(String address, String moduleOrMain) {
    return addComponent(new Component<Aggregator>(Aggregator.class, address, moduleOrMain));
  }

  /**
   * Adds an aggregator component to the network.
   *
   * @param address
   *   The component address.
   * @param moduleOrMain
   *   The aggregator component main or module name.
   * @param config
   *   The aggregator component configuration.
   * @return
   *   The new aggregator component instance.
   */
  @SuppressWarnings("rawtypes")
  public Component<Aggregator> addAggregator(String address, String moduleOrMain, JsonObject config) {
    return addComponent(new Component<Aggregator>(Aggregator.class, address, moduleOrMain).setConfig(config));
  }

  /**
   * Adds an aggregator component to the network.
   *
   * @param address
   *   The component address.
   * @param moduleOrMain
   *   The aggregator component main or module name.
   * @param instances
   *   The number of aggregator instances.
   * @return
   *   The new aggregator component instance.
   */
  @SuppressWarnings("rawtypes")
  public Component<Aggregator> addAggregator(String address, String moduleOrMain, int instances) {
    return addComponent(new Component<Aggregator>(Aggregator.class, address, moduleOrMain).setInstances(instances));
  }

  /**
   * Adds an aggregator component to the network.
   *
   * @param address
   *   The component address.
   * @param moduleOrMain
   *   The aggregator component main or module name.
   * @param config
   *   The aggregator component configuration.
   * @param instances
   *   The number of aggregator instances.
   * @return
   *   The new aggregator component instance.
   */
  @SuppressWarnings("rawtypes")
  public Component<Aggregator> addAggregator(String address, String moduleOrMain, JsonObject config, int instances) {
    return addComponent(new Component<Aggregator>(Aggregator.class, address, moduleOrMain).setConfig(config).setInstances(instances));
  }

}
