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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.kuujo.vertigo.hooks.ComponentHook;
import net.kuujo.vertigo.input.grouping.MessageGrouping;
import net.kuujo.vertigo.network.ComponentConfig;
import net.kuujo.vertigo.network.ConnectionConfig;
import net.kuujo.vertigo.network.ModuleConfig;
import net.kuujo.vertigo.network.NetworkConfig;
import net.kuujo.vertigo.network.VerticleConfig;

import org.vertx.java.core.json.JsonObject;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Base component configuration.
 *
 * @author Jordan Halterman
 *
 * @param <T> The component type interface.
 */
abstract class AbstractComponentConfig<T extends ComponentConfig<T>> implements ComponentConfig<T> {

  /**
   * <code>name</code> is a string indicating the network unique component name. This
   * name is used as the basis for generating unique event bus addresses.
   */
  public static final String COMPONENT_NAME = "name";

  /**
   * <code>type</code> is a string indicating the type of component that will be deployed.
   * This can be either <code>module</code> or <code>verticle</code>. This field is required.
   */
  public static final String COMPONENT_TYPE = "type";

  /**
   * <code>module</code> is the module component type.
   */
  public static final String COMPONENT_TYPE_MODULE = "module";

  /**
   * <code>verticle</code> is the verticle component type.
   */
  public static final String COMPONENT_TYPE_VERTICLE = "verticle";

  /**
   * <code>config</code> is an object defining the configuration to pass to each instance
   * of the component. If no configuration is provided then an empty configuration will be
   * passed to component instances.
   */
  public static final String COMPONENT_CONFIG = "config";

  /**
   * <code>instances</code> is a number indicating the number of instances of the
   * component to deploy. Defaults to <code>1</code>
   */
  public static final String COMPONENT_NUM_INSTANCES = "instances";

  /**
   * <code>group</code> is a component deployment group for HA. This option applies when
   * deploying the network to a cluster.
   */
  public static final String COMPONENT_GROUP = "group";

  /**
   * <code>hooks</code> is an array of hook configurations. Each hook configuration must
   * contain at least a <code>type</code> key which indicates the fully qualified name of
   * the hook class. Other configuration options depend on the specific hook
   * implementation. In most cases, json properties are directly correlated to fields
   * within the hook class.
   */
  public static final String COMPONENT_HOOKS = "hooks";

  private static final int DEFAULT_NUM_INSTANCES = 1;
  private static final String DEFAULT_GROUP = "__DEFAULT__";

  private String name;
  private Map<String, Object> config;
  private int instances = DEFAULT_NUM_INSTANCES;
  private String group = DEFAULT_GROUP;
  private List<ComponentHook> hooks = new ArrayList<>();
  @JsonIgnore
  private NetworkConfig network;

  protected AbstractComponentConfig() {
  }

  protected AbstractComponentConfig(String name, NetworkConfig network) {
    this.name = name;
    this.network = network;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public JsonObject getConfig() {
    return config != null ? new JsonObject(config) : new JsonObject();
  }

  @Override
  @SuppressWarnings("unchecked")
  public T setConfig(JsonObject config) {
    this.config = config != null ? config.toMap() : new HashMap<String, Object>();
    return (T) this;
  }

  @Override
  public int getInstances() {
    return instances;
  }

  @Override
  @SuppressWarnings("unchecked")
  public T setInstances(int instances) {
    if (instances < 1) {
      throw new IllegalArgumentException("Instances must be a positive integer.");
    }
    this.instances = instances;
    return (T) this;
  }

  @Override
  @SuppressWarnings("unchecked")
  public T setGroup(String group) {
    this.group = group != null ? group : DEFAULT_GROUP;
    return (T) this;
  }

  @Override
  public String getGroup() {
    return group;
  }

  @Override
  @SuppressWarnings("unchecked")
  public T addHook(ComponentHook hook) {
    hooks.add(hook);
    return (T) this;
  }

  @Override
  public List<ComponentHook> getHooks() {
    return hooks;
  }

  @Override
  @SuppressWarnings("rawtypes")
  public <U extends ComponentConfig> U addComponent(U component) {
    return network.addComponent(component);
  }

  @Override
  public <U extends ComponentConfig<U>> U addComponent(String name, String moduleOrMain) {
    return network.addComponent(name, moduleOrMain);
  }

  @Override
  public <U extends ComponentConfig<U>> U addComponent(String name, String moduleOrMain, JsonObject config) {
    return network.addComponent(name, moduleOrMain, config);
  }

  @Override
  public <U extends ComponentConfig<U>> U addComponent(String name, String moduleOrMain, int instances) {
    return network.addComponent(name, moduleOrMain, instances);
  }

  @Override
  public <U extends ComponentConfig<U>> U addComponent(String name, String moduleOrMain, JsonObject config, int instances) {
    return network.addComponent(name, moduleOrMain, config, instances);
  }

  @Override
  public <U extends ComponentConfig<U>> U removeComponent(U component) {
    return network.removeComponent(component);
  }

  @Override
  public <U extends ComponentConfig<U>> U removeComponent(String name) {
    return network.removeComponent(name);
  }

  @Override
  public ModuleConfig addModule(ModuleConfig module) {
    return network.addModule(module);
  }

  @Override
  public ModuleConfig addModule(String name, String moduleName) {
    return network.addModule(name, moduleName);
  }

  @Override
  public ModuleConfig addModule(String name, String moduleName, JsonObject config) {
    return network.addModule(name, moduleName, config);
  }

  @Override
  public ModuleConfig addModule(String name, String moduleName, int instances) {
    return network.addModule(name, moduleName, instances);
  }

  @Override
  public ModuleConfig addModule(String name, String moduleName, JsonObject config, int instances) {
    return network.addModule(name, moduleName, config, instances);
  }

  @Override
  public ModuleConfig removeModule(ModuleConfig module) {
    return network.removeModule(module);
  }

  @Override
  public ModuleConfig removeModule(String name) {
    return network.removeModule(name);
  }

  @Override
  public VerticleConfig addVerticle(VerticleConfig verticle) {
    return network.addVerticle(verticle);
  }

  @Override
  public VerticleConfig addVerticle(String name, String main) {
    return network.addVerticle(name, main);
  }

  @Override
  public VerticleConfig addVerticle(String name, String main, JsonObject config) {
    return network.addVerticle(name, main, config);
  }

  @Override
  public VerticleConfig addVerticle(String name, String main, int instances) {
    return network.addVerticle(name, main, instances);
  }

  @Override
  public VerticleConfig addVerticle(String name, String main, JsonObject config, int instances) {
    return network.addVerticle(name, main, config, instances);
  }

  @Override
  public VerticleConfig removeVerticle(VerticleConfig verticle) {
    return network.removeVerticle(verticle);
  }

  @Override
  public VerticleConfig removeVerticle(String name) {
    return network.removeVerticle(name);
  }

  @Override
  public ConnectionConfig createConnection(ConnectionConfig connection) {
    return network.createConnection(connection);
  }

  @Override
  public ConnectionConfig createConnection(String source, String target) {
    return network.createConnection(source, target);
  }

  @Override
  public ConnectionConfig createConnection(String source, String target, MessageGrouping grouping) {
    return network.createConnection(source, target, grouping);
  }

  @Override
  public ConnectionConfig createConnection(String source, String out, String target, String in) {
    return network.createConnection(source, out, target, in);
  }

  @Override
  public ConnectionConfig createConnection(String source, String out, String target, String in, MessageGrouping grouping) {
    return network.createConnection(source, out, target, in, grouping);
  }

  @Override
  public NetworkConfig destroyConnection(ConnectionConfig connection) {
    return network.destroyConnection(connection);
  }

  @Override
  public NetworkConfig destroyConnection(String source, String target) {
    return network.destroyConnection(source, target);
  }

  @Override
  public NetworkConfig destroyConnection(String source, String out, String target, String in) {
    return network.destroyConnection(source, out, target, in);
  }

  @Override
  public String toString() {
    return name;
  }

}
