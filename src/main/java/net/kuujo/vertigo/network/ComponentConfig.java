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
package net.kuujo.vertigo.network;

import java.util.List;

import net.kuujo.vertigo.hooks.ComponentHook;
import net.kuujo.vertigo.network.impl.DefaultModuleConfig;
import net.kuujo.vertigo.network.impl.DefaultVerticleConfig;
import net.kuujo.vertigo.util.serializer.Serializable;

import org.vertx.java.core.json.JsonObject;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Component configuration.
 *
 * @author Jordan Halterman
 */
@JsonTypeInfo(
  use=JsonTypeInfo.Id.NAME,
  include=JsonTypeInfo.As.PROPERTY,
  property="type"
)
@JsonSubTypes({
  @JsonSubTypes.Type(value=DefaultModuleConfig.class, name="module"),
  @JsonSubTypes.Type(value=DefaultVerticleConfig.class, name="verticle")
})
public interface ComponentConfig<T extends ComponentConfig<T>> extends Config, ComponentConfigurable, ConnectionConfigurable {

  /**
   * Component type.
   *
   * @author Jordan Halterman
   */
  public static enum Type {
    MODULE("module"),
    VERTICLE("verticle");

    private final String name;

    private Type(String name) {
      this.name = name;
    }

    /**
     * Returns the component type name.
     *
     * @return The component type name.
     */
    public String getName() {
      return name;
    }

    @Override
    public String toString() {
      return name;
    }
  }

  /**
   * Returns the component type.
   *
   * @return The component type.
   */
  Type getType();

  /**
   * Returns the component name.
   *
   * @return The component name.
   */
  String getName();

  /**
   * Returns the component configuration.
   * 
   * @return The component configuration.
   */
  JsonObject getConfig();

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
  T setConfig(JsonObject config);

  /**
   * Returns the number of component instances to deploy within the network.
   * 
   * @return The number of component instances.
   */
  int getInstances();

  /**
   * Sets the number of component instances to deploy within the network.
   * 
   * @param instances The number of component instances.
   * @return The component configuration.
   */
  T setInstances(int instances);

  /**
   * Sets the component deployment group.
   *
   * @param group The component deployment group.
   * @return The component configuration.
   */
  T setGroup(String group);

  /**
   * Returns the component deployment group.
   *
   * @return The component deployment group.
   */
  String getGroup();

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
  T addHook(ComponentHook hook);

  /**
   * Returns a list of all component hooks.
   * 
   * @return A list of component hooks.
   */
  List<ComponentHook> getHooks();

}
