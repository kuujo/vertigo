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
import net.kuujo.vertigo.network.ComponentConfig;

import org.vertx.java.core.json.JsonObject;

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

  protected AbstractComponentConfig() {
  }

  protected AbstractComponentConfig(String name) {
    this.name = name;
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
  public String toString() {
    return name;
  }

}
