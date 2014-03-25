/*
 * Copyright 2013-2014 the original author or authors.
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
import java.util.Map;
import java.util.UUID;

import net.kuujo.vertigo.hooks.ComponentHook;
import net.kuujo.vertigo.util.serializer.Serializable;

import org.vertx.java.core.json.JsonObject;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * A network component definition.
 * <p>
 * 
 * Components are the primary elements of processing in Vertigo. They can be represented
 * as <code>feeders</code>, <code>executors</code>, or <code>workers</code>. Each network
 * may consist of any number of components, each of which may subscribe to the output of
 * other components inside or outside of the network. Internally, components are
 * represented as Vert.x modules or verticles. Just as with Vert.x modules and verticles,
 * components may consist of several instances.
 * <p>
 * 
 * @author Jordan Haltermam
 */
@JsonTypeInfo(
  use=JsonTypeInfo.Id.NAME,
  include=JsonTypeInfo.As.PROPERTY,
  property="type"
)
@JsonSubTypes({
  @JsonSubTypes.Type(value=Module.class, name=Component.COMPONENT_TYPE_MODULE),
  @JsonSubTypes.Type(value=Verticle.class, name=Component.COMPONENT_TYPE_VERTICLE)
})
public abstract class Component<T extends Component<T>> implements Config {

  /**
   * <code>name</code> is a string indicating the network unique component name. This
   * name is used as the basis for generating unique event bus addresses.
   */
  public static final String COMPONENT_NAME = "name";

  /**
   * <code>address</code> is a string indicating the globally unique component event bus
   * address. Components will use this address to register a handler which listens for
   * subscriptions from other components, so this address must be unique across a Vert.x
   * cluster. If no address is provided then a unique address will be automatically generated
   * by the network.
   */
  public static final String COMPONENT_ADDRESS = "address";

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
  private String address;
  private Map<String, Object> config;
  private int instances = DEFAULT_NUM_INSTANCES;
  private String group = DEFAULT_GROUP;
  private List<ComponentHook> hooks = new ArrayList<>();

  public Component() {
    address = UUID.randomUUID().toString();
  }

  public Component(String name) {
    this.name = name;
  }

  /**
   * Returns the component type.
   *
   * @return The component type - either "module" or "verticle".
   */
  @JsonGetter("type")
  protected abstract String getType();

  @SuppressWarnings("unchecked")
  T setName(String name) {
    this.name = name;
    return (T) this;
  }

  /**
   * Returns the component name.
   *
   * @return The component name.
   */
  public String getName() {
    return name != null ? name : address; // for backwards compatibility
  }

  /**
   * Returns the component address.
   * 
   * This address is an event bus address at which the component will register a handler
   * to listen for connections when started. Thus, this address must be unique.
   * 
   * @return The component address.
   */
  public String getAddress() {
    return address;
  }

  /**
   * Sets the component address.
   *
   * @param address The component event bus address.
   * @return The component configuration.
   */
  @SuppressWarnings("unchecked")
  public T setAddress(String address) {
    this.address = address;
    return (T) this;
  }

  /**
   * Returns a boolean indicating whether the component is a module.
   * 
   * @return Indicates whether the component is a module.
   */
  public boolean isModule() {
    return false;
  }

  /**
   * Returns a boolean indicating whether the component is a verticle.
   * 
   * @return Indicates whether the component is a verticle.
   */
  public boolean isVerticle() {
    return false;
  }

  /**
   * Returns the component configuration.
   * 
   * @return The component configuration.
   */
  public JsonObject getConfig() {
    return config != null ? new JsonObject(config) : new JsonObject();
  }

  /**
   * Sets the component configuration.
   * <p>
   * 
   * This configuration will be passed to component implementations as the verticle or
   * module configuration when the component is started.
   * 
   * @param config The component configuration.
   * @return The component configuration.
   */
  @SuppressWarnings("unchecked")
  public T setConfig(JsonObject config) {
    this.config = config.toMap();
    return (T) this;
  }

  /**
   * Returns the number of component instances to deploy within the network.
   * 
   * @return The number of component instances.
   */
  public int getNumInstances() {
    return instances;
  }

  /**
   * Sets the number of component instances to deploy within the network.
   * 
   * @param numInstances The number of component instances.
   * @return The component configuration.
   */
  @SuppressWarnings("unchecked")
  public T setNumInstances(int numInstances) {
    instances = numInstances;
    return (T) this;
  }

  /**
   * Sets the component deployment group.
   *
   * @param group The component deployment group.
   * @return The component configuration.
   */
  @SuppressWarnings("unchecked")
  public T setDeploymentGroup(String group) {
    this.group = group;
    return (T) this;
  }

  /**
   * Returns the component deployment group.
   *
   * @return The component deployment group.
   */
  public String getDeploymentGroup() {
    return group;
  }

  /**
   * Adds a component hook to the component.
   * 
   * The output hook can be used to receive notifications on events that occur within the
   * component instance's inputs and outputs. Hooks should implement the
   * {@link ComponentHook} interface. Hook state will be automatically serialized to json
   * using an internal <code>Serializer</code>. By default, this means that any
   * primitives, primitive wrappers, collections, or {@link Serializable} fields will be
   * serialized. Finer grained control over serialization of hooks can be provided by
   * either using Jackson annotations within the hook implementation or by providing a
   * custom serializer for the hook.
   * 
   * @param hook A component hook.
   * @return The component configuration.
   * @see ComponentHook
   */
  @SuppressWarnings("unchecked")
  public T addHook(ComponentHook hook) {
    hooks.add(hook);
    return (T) this;
  }

  /**
   * Returns a list of all component hooks.
   * 
   * @return A list of component hooks.
   */
  public List<ComponentHook> getHooks() {
    return hooks;
  }

  @Override
  public String toString() {
    return getAddress();
  }

}
