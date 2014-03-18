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
package net.kuujo.vertigo.context;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.vertx.java.core.json.JsonObject;

import net.kuujo.vertigo.hooks.ComponentHook;
import net.kuujo.vertigo.network.Component;

/**
 * Module component context.
 * 
 * @author Jordan Halterman
 * 
 * @param <T> The component type
 */
public class ModuleContext extends ComponentContext<ModuleContext> {
  private String module;

  @Override
  protected String getDeploymentType() {
    return Component.COMPONENT_MODULE;
  }

  @Override
  public boolean isModule() {
    return true;
  }

  /**
   * Returns the module name.
   * 
   * @return The module name.
   */
  public String module() {
    return module;
  }

  public String moduleName() {
    return module();
  }

  /**
   * Module context builder.
   *
   * @author Jordan Halterman
   */
  public static class Builder extends net.kuujo.vertigo.context.Context.Builder<ModuleContext> {
    private ModuleContext context;

    private Builder() {
      super(new ModuleContext());
    }

    private Builder(ModuleContext context) {
      super(context);
    }

    /**
     * Creates a new context builder.
     *
     * @return A new module context builder.
     */
    public static Builder newBuilder() {
      return new Builder();
    }

    /**
     * Creates a new context builder.
     *
     * @param context A starting module context.
     * @return A new module context builder.
     */
    public static Builder newBuilder(ModuleContext context) {
      return new Builder(context);
    }

    /**
     * Sets the component name.
     *
     * @param name The component name.
     * @return The context builder.
     */
    public Builder setName(String name) {
      context.name = name;
      return this;
    }

    /**
     * Sets the component address.
     *
     * @param address The component event bus address.
     * @return The context builder.
     */
    public Builder setAddress(String address) {
      context.address = address;
      return this;
    }

    /**
     * Sets the component type.
     *
     * @param type A component type constant.
     * @return The context builder.
     */
    public Builder setType(Component.Type type) {
      context.type = type;
      return this;
    }

    /**
     * Sets the module name.
     *
     * @param moduleName The component module name.
     * @return The context builder.
     */
    public Builder setModule(String moduleName) {
      context.module = moduleName;
      return this;
    }

    /**
     * Sets the component configuration.
     *
     * @param config The component configuration.
     * @return The context builder.
     */
    public Builder setConfig(JsonObject config) {
      context.config = config.toMap();
      return this;
    }

    /**
     * Sets the component configuration.
     *
     * @param config The component configuration.
     * @return The context builder.
     */
    public Builder setConfig(Map<String, Object> config) {
      context.config = config;
      return this;
    }

    /**
     * Sets the component deployment group.
     *
     * @param group The component deployment group.
     * @return The context builder.
     */
    public Builder setDeploymentGroup(String group) {
      context.group = group;
      return this;
    }

    /**
     * Sets component hooks.
     *
     * @param hooks An array of component hooks.
     * @return The context builder.
     */
    public Builder setHooks(ComponentHook... hooks) {
      context.hooks = Arrays.asList(hooks);
      return this;
    }

    /**
     * Sets the component hooks.
     *
     * @param hooks A list of component hooks.
     * @return The context builder.
     */
    public Builder setHooks(List<ComponentHook> hooks) {
      context.hooks = hooks;
      return this;
    }

    /**
     * Adds a hook to the component.
     *
     * @param hook A component hook instance.
     * @return The context builder.
     */
    public Builder addHook(ComponentHook hook) {
      context.hooks.add(hook);
      return this;
    }

    /**
     * Removes a hook from the component.
     *
     * @param hook A component hook instance.
     * @return The context builder.
     */
    public Builder removeHook(ComponentHook hook) {
      context.hooks.remove(hook);
      return this;
    }

    /**
     * Sets the component instance contexts.
     *
     * @param instances An array of instance contexts.
     * @return The context builder.
     */
    public Builder setInstances(InstanceContext... instances) {
      context.instances = Arrays.asList(instances);
      return this;
    }

    /**
     * Sets the component instance contexts.
     *
     * @param instances A list of instance contexts.
     * @return The context builder.
     */
    public Builder setInstances(List<InstanceContext> instances) {
      context.instances = instances;
      return this;
    }

    /**
     * Adds an instance context to the component.
     *
     * @param instance An instance context to add.
     * @return The context builder.
     */
    public Builder addInstance(InstanceContext instance) {
      context.instances.add(instance);
      return this;
    }

    /**
     * Removes an instance context from the component.
     *
     * @param instance An instance context to remove.
     * @return The context builder.
     */
    public Builder removeInstance(InstanceContext instance) {
      context.instances.remove(instance);
      return this;
    }

    /**
     * Builds the module context.
     *
     * @return A new module context.
     */
    public ModuleContext build() {
      return context;
    }
  }

}
