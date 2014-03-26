/*
 * Copyright 2014 the original author or authors.
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
package net.kuujo.vertigo.network.impl;

import static net.kuujo.vertigo.util.Components.isModuleName;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.kuujo.vertigo.input.grouping.Grouping;
import net.kuujo.vertigo.network.ComponentConfig;
import net.kuujo.vertigo.network.ConnectionConfig;
import net.kuujo.vertigo.network.ModuleConfig;
import net.kuujo.vertigo.network.NetworkConfig;
import net.kuujo.vertigo.network.VerticleConfig;

import org.vertx.java.core.json.JsonObject;

import com.fasterxml.jackson.annotation.JsonSetter;

/**
 * Default network configuration implementation.
 *
 * @author Jordan Halterman
 */
public class DefaultNetworkConfig implements NetworkConfig {


  /**
   * <code>name</code> is a string indicating the unique network name. This is the
   * address at which the network will monitor network components. This field is required.
   */
  public static final String NETWORK_NAME = "name";

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
   * being an object containing the component configuration. See the {@link ComponentConfig}
   * class for component configuration options.
   */
  public static final String NETWORK_COMPONENTS = "components";

  private static final int DEFAULT_NUM_AUDITORS = 1;
  private static final long DEFAULT_MESSAGE_TIMEOUT = 30000;

  private String name;
  private int auditors = DEFAULT_NUM_AUDITORS;
  private boolean acking = true;
  private long timeout = DEFAULT_MESSAGE_TIMEOUT;
  private Map<String, ComponentConfig<?>> components = new HashMap<String, ComponentConfig<?>>();
  private List<ConnectionConfig> connections = new ArrayList<>();

  public DefaultNetworkConfig() {
    name = UUID.randomUUID().toString();
  }

  public DefaultNetworkConfig(String name) {
    this.name = name;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public NetworkConfig enableAcking() {
    acking = true;
    return this;
  }

  @Override
  public NetworkConfig disableAcking() {
    acking = false;
    return this;
  }

  @Override
  public NetworkConfig setAckingEnabled(boolean enabled) {
    acking = enabled;
    return this;
  }

  @Override
  public boolean isAckingEnabled() {
    return acking;
  }

  @Override
  public int getNumAuditors() {
    return auditors;
  }

  @Override
  public NetworkConfig setNumAuditors(int numAuditors) {
    this.auditors = numAuditors;
    return this;
  }

  @Override
  public NetworkConfig enableMessageTimeouts() {
    if (timeout == 0) {
      timeout = DEFAULT_MESSAGE_TIMEOUT;
    }
    return this;
  }

  @Override
  public NetworkConfig disableMessageTimeouts() {
    timeout = 0;
    return this;
  }

  @Override
  @JsonSetter("timeouts")
  public NetworkConfig setMessageTimeoutsEnabled(boolean isEnabled) {
    if (isEnabled) {
      return enableMessageTimeouts();
    } else {
      return disableMessageTimeouts();
    }
  }

  @Override
  public boolean isMessageTimeoutsEnabled() {
    return timeout > 0;
  }

  @Override
  public NetworkConfig setMessageTimeout(long timeout) {
    this.timeout = timeout;
    return this;
  }

  @Override
  public long getMessageTimeout() {
    return timeout;
  }

  @Override
  public List<ComponentConfig<?>> getComponents() {
    List<ComponentConfig<?>> components = new ArrayList<ComponentConfig<?>>();
    for (Map.Entry<String, ComponentConfig<?>> entry : this.components.entrySet()) {
      components.add(entry.getValue());
    }
    return components;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T extends ComponentConfig<T>> T getComponent(String name) {
    return (T) components.get(name);
  }

  @Override
  @SuppressWarnings("rawtypes")
  public <T extends ComponentConfig> T addComponent(T component) {
    components.put(component.getName(), component);
    return component;
  }

  @Override
  public <T extends ComponentConfig<T>> T addComponent(String name, String moduleOrMain) {
    return addComponent(name, moduleOrMain, null, 1);
  }

  @Override
  public <T extends ComponentConfig<T>> T addComponent(String name, String moduleOrMain, JsonObject config) {
    return addComponent(name, moduleOrMain, config, 1);
  }

  @Override
  public <T extends ComponentConfig<T>> T addComponent(String name, String moduleOrMain, int instances) {
    return addComponent(name, moduleOrMain, null, instances);
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T extends ComponentConfig<T>> T addComponent(String name, String moduleOrMain, JsonObject config, int instances) {
    if (isModuleName(moduleOrMain)) {
      return (T) addModule(name, moduleOrMain, config, instances);
    }
    return (T) addVerticle(name, moduleOrMain, config, instances);
  }

  @Override
  public boolean hasComponent(String name) {
    return components.containsKey(name);
  }

  @Override
  public <T extends ComponentConfig<T>> T removeComponent(T component) {
    return removeComponent(component.getName());
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T extends ComponentConfig<T>> T removeComponent(String name) {
    return (T) components.remove(name);
  }

  @Override
  public ModuleConfig addModule(ModuleConfig module) {
    components.put(module.getName(), module);
    return module;
  }

  @Override
  public ModuleConfig addModule(String name, String moduleName) {
    return addModule(new DefaultModuleConfig(name, moduleName, this));
  }

  @Override
  public ModuleConfig addModule(String name, String moduleName, JsonObject config) {
    return addModule(new DefaultModuleConfig(name, moduleName, this).setConfig(config));
  }

  @Override
  public ModuleConfig addModule(String name, String moduleName, int numInstances) {
    return addModule(new DefaultModuleConfig(name, moduleName, this).setInstances(numInstances));
  }

  @Override
  public ModuleConfig addModule(String name, String moduleName, JsonObject config, int numInstances) {
    return addModule(new DefaultModuleConfig(name, moduleName, this).setConfig(config).setInstances(numInstances));
  }

  @Override
  public ModuleConfig removeModule(ModuleConfig module) {
    return removeModule(module.getName());
  }

  @Override
  public ModuleConfig removeModule(String name) {
    ComponentConfig<?> component = components.get(name);
    if (!(component instanceof ModuleConfig)) {
      throw new IllegalArgumentException("Component is not a module component.");
    }
    return (ModuleConfig) component;
  }

  @Override
  public VerticleConfig addVerticle(VerticleConfig verticle) {
    components.put(verticle.getName(), verticle);
    return verticle;
  }

  @Override
  public VerticleConfig addVerticle(String name, String main) {
    return addVerticle(new DefaultVerticleConfig(name, main, this));
  }

  @Override
  public VerticleConfig addVerticle(String name, String main, JsonObject config) {
    return addVerticle(new DefaultVerticleConfig(name, main, this).setConfig(config));
  }

  @Override
  public VerticleConfig addVerticle(String name, String main, int numInstances) {
    return addVerticle(new DefaultVerticleConfig(name, main, this).setInstances(numInstances));
  }

  @Override
  public VerticleConfig addVerticle(String name, String main, JsonObject config, int numInstances) {
    return addVerticle(new DefaultVerticleConfig(name, main, this).setConfig(config).setInstances(numInstances));
  }

  @Override
  public VerticleConfig removeVerticle(VerticleConfig verticle) {
    return removeVerticle(verticle.getName());
  }

  @Override
  public VerticleConfig removeVerticle(String name) {
    ComponentConfig<?> component = components.get(name);
    if (!(component instanceof VerticleConfig)) {
      throw new IllegalArgumentException("Component is not a verticle component.");
    }
    return (VerticleConfig) component;
  }

  @Override
  public ConnectionConfig createConnection(ConnectionConfig connection) {
    connections.add(connection);
    return connection;
  }

  @Override
  public ConnectionConfig createConnection(String source, String target) {
    ConnectionConfig connection = new DefaultConnectionConfig(source, target, this);
    connections.add(connection);
    return connection;
  }

  @Override
  public ConnectionConfig createConnection(String source, String target, Grouping grouping) {
    ConnectionConfig connection = new DefaultConnectionConfig(source, target, grouping, this);
    connections.add(connection);
    return connection;
  }

  @Override
  public ConnectionConfig createConnection(String source, String out, String target, String in) {
    ConnectionConfig connection = new DefaultConnectionConfig(source, out, target, in, this);
    connections.add(connection);
    return connection;
  }

  @Override
  public ConnectionConfig createConnection(String source, String out, String target, String in, Grouping grouping) {
    ConnectionConfig connection = new DefaultConnectionConfig(source, out, target, in, grouping, this);
    connections.add(connection);
    return connection;
  }

  @Override
  public NetworkConfig destroyConnection(ConnectionConfig connection) {
    Iterator<ConnectionConfig> iter = connections.iterator();
    while (iter.hasNext()) {
      ConnectionConfig check = iter.next();
      if (check.equals(connection)) {
        iter.remove();
      }
    }
    return this;
  }

  @Override
  public NetworkConfig destroyConnection(String source, String target) {
    return destroyConnection(new DefaultConnectionConfig(source, target, this));
  }

  @Override
  public NetworkConfig destroyConnection(String source, String out, String target, String in) {
    return destroyConnection(new DefaultConnectionConfig(source, out, target, in, this));
  }

  @Override
  public Collection<ConnectionConfig> getConnections() {
    return connections;
  }

  @Override
  public String toString() {
    return getName();
  }

}
