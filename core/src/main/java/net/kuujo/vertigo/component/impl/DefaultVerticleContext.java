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
package net.kuujo.vertigo.component.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import net.kuujo.vertigo.component.VerticleContext;
import net.kuujo.vertigo.hook.ComponentHook;
import net.kuujo.vertigo.impl.BaseContext;

import org.vertx.java.core.json.JsonObject;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Verticle component context.
 * 
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 * 
 * @param <T> The component type
 */
public class DefaultVerticleContext extends DefaultComponentContext<VerticleContext> implements VerticleContext {
  private String main;
  private boolean worker;
  @JsonProperty("multi-threaded")
  private boolean multiThreaded;

  @Override
  protected String type() {
    return "verticle";
  }

  @Override
  public boolean isVerticle() {
    return true;
  }

  @Override
  public String main() {
    return main;
  }

  @Override
  public boolean isWorker() {
    return worker;
  }

  @Override
  public boolean isMultiThreaded() {
    return isWorker() && multiThreaded;
  }

  /**
   * Verticle context builder.
   *
   * @author Jordan Halterman
   */
  public static class Builder extends BaseContext.Builder<Builder, DefaultVerticleContext> {

    private Builder() {
      super(new DefaultVerticleContext());
    }

    private Builder(DefaultVerticleContext context) {
      super(context);
    }

    /**
     * Creates a new context builder.
     *
     * @return A new verticle context builder.
     */
    public static Builder newBuilder() {
      return new Builder();
    }

    /**
     * Creates a new context builder.
     *
     * @param context A starting verticle context.
     * @return A new verticle context builder.
     */
    public static Builder newBuilder(DefaultVerticleContext context) {
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
     * Sets the verticle main.
     *
     * @param main The verticle component main.
     * @return The context builder.
     */
    public Builder setMain(String main) {
      context.main = main;
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
     * Sets whether the verticle is a worker.
     *
     * @param isWorker Whether the verticle is a worker.
     * @return The context builder.
     */
    public Builder setWorker(boolean isWorker) {
      context.worker = isWorker;
      return this;
    }

    /**
     * Sets whether the verticle is a multi-threaded worker.
     *
     * @param isMultiThreaded Whether the verticle is a multi-threaded worker.
     * @return The context builder.
     */
    public Builder setMultiThreaded(boolean isMultiThreaded) {
      if (isMultiThreaded) {
        context.worker = true;
      }
      context.multiThreaded = isMultiThreaded;
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
     * Sets the component instance contexts.
     *
     * @param instances An array of instance contexts.
     * @return The context builder.
     */
    public Builder setInstances(DefaultInstanceContext... instances) {
      context.instances = new ArrayList<>();
      for (DefaultInstanceContext instance : instances) {
        context.instances.add(instance.setComponentContext(context));
      }
      return this;
    }

    /**
     * Sets the component instance contexts.
     *
     * @param instances A list of instance contexts.
     * @return The context builder.
     */
    public Builder setInstances(List<DefaultInstanceContext> instances) {
      context.instances = new ArrayList<>();
      for (DefaultInstanceContext instance : instances) {
        context.instances.add(instance.setComponentContext(context));
      }
      return this;
    }

    /**
     * Adds an instance context to the component.
     *
     * @param instance An instance context to add.
     * @return The context builder.
     */
    public Builder addInstance(DefaultInstanceContext instance) {
      instance.setComponentContext(context);
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

    /**
     * Sets the component hooks.
     *
     * @param hooks An array of hooks.
     * @return The context builder.
     */
    public Builder setHooks(ComponentHook... hooks) {
      context.hooks = Arrays.asList(hooks);
      return this;
    }

    /**
     * Sets the component hooks.
     *
     * @param hooks A list of hooks.
     * @return The context builder.
     */
    public Builder setHooks(List<ComponentHook> hooks) {
      context.hooks = hooks;
      return this;
    }

    /**
     * Adds a hook to the component.
     *
     * @param hook The hook to add.
     * @return The context builder.
     */
    public Builder addHook(ComponentHook hook) {
      context.hooks.add(hook);
      return this;
    }

    /**
     * Removes a hook from the component.
     *
     * @param hook The hook to remove.
     * @return The context builder.
     */
    public Builder removeHook(ComponentHook hook) {
      context.hooks.remove(hook);
      return this;
    }
  }

}
