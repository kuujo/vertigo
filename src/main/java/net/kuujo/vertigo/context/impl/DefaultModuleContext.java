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
package net.kuujo.vertigo.context.impl;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import net.kuujo.vertigo.context.ModuleContext;
import net.kuujo.vertigo.data.DataStore;

import org.vertx.java.core.json.JsonObject;

/**
 * Module component context.
 * 
 * @author Jordan Halterman
 * 
 * @param <T> The component type
 */
public class DefaultModuleContext extends DefaultComponentContext<ModuleContext> implements ModuleContext {
  private String module;

  @Override
  protected String type() {
    return "module";
  }

  @Override
  public boolean isModule() {
    return true;
  }

  @Override
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
  public static class Builder extends BaseContext.Builder<Builder, DefaultModuleContext> {

    private Builder() {
      super(new DefaultModuleContext());
    }

    private Builder(DefaultModuleContext context) {
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
    public static Builder newBuilder(DefaultModuleContext context) {
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
     * Sets the component status address.
     *
     * @param address The component status address.
     * @return The context builder.
     */
    public Builder setStatusAddress(String address) {
      context.status = address;
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
    public Builder setGroup(String group) {
      context.group = group;
      return this;
    }

    /**
     * Sets the component storage type.
     *
     * @param storage The component storage type.
     * @return The ccontext builder.
     */
    public Builder setStorageType(Class<? extends DataStore> storage) {
      context.storage.put("class", storage);
      return this;
    }

    /**
     * Sets the component storage configuration.
     *
     * @param config The component storage configuration.
     * @return The context builder.
     */
    public Builder setStorageConfig(JsonObject config) {
      for (String fieldName : config.getFieldNames()) {
        context.storage.put(fieldName, config.getValue(fieldName));
      }
      return this;
    }

    /**
     * Sets the component instance contexts.
     *
     * @param instances An array of instance contexts.
     * @return The context builder.
     */
    public Builder setInstances(DefaultInstanceContext... instances) {
      context.instances = Arrays.asList(instances);
      return this;
    }

    /**
     * Sets the component instance contexts.
     *
     * @param instances A list of instance contexts.
     * @return The context builder.
     */
    public Builder setInstances(List<DefaultInstanceContext> instances) {
      context.instances = instances;
      return this;
    }

    /**
     * Adds an instance context to the component.
     *
     * @param instance An instance context to add.
     * @return The context builder.
     */
    public Builder addInstance(DefaultInstanceContext instance) {
      context.instances.add(instance);
      return this;
    }

    /**
     * Removes an instance context from the component.
     *
     * @param instance An instance context to remove.
     * @return The context builder.
     */
    public Builder removeInstance(DefaultInstanceContext instance) {
      context.instances.remove(instance);
      return this;
    }
  }

}
