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
package net.kuujo.vertigo.component;

import java.util.List;

import net.kuujo.vertigo.Config;
import net.kuujo.vertigo.component.impl.DefaultModuleConfig;
import net.kuujo.vertigo.component.impl.DefaultVerticleConfig;
import net.kuujo.vertigo.hook.ComponentHook;

import org.vertx.java.core.json.JsonObject;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Components are synonymous with Vert.x modules and verticles.
 * Each component can have its own configuration and any number
 * of instances.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
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
public interface ComponentConfig<T extends ComponentConfig<T>> extends Config<T> {

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
   * <code>hooks</code> is an array defining component hooks. Each element in the array
   * must be an object containing a <code>hook</code> field which indicates the hook
   * class name.
   */
  public static final String COMPONENT_HOOKS = "hooks";

  /**
   * Component type.
   *
   * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
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
   * Sets the component name.
   *
   * @param name The component name.
   * @return The component configuration.
   */
  T setName(String name);

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
   * Adds a hook to the component.
   *
   * @param hook The hook to add.
   * @return The component configuration.
   */
  T addHook(ComponentHook hook);

  /**
   * Returns a list of component hooks.
   *
   * @return A list of component hooks.
   */
  List<ComponentHook> getHooks();

}
