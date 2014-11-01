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
package net.kuujo.vertigo.component.impl;

import io.vertx.core.json.JsonObject;
import net.kuujo.vertigo.TypeInfo;
import net.kuujo.vertigo.component.ComponentInfo;
import net.kuujo.vertigo.component.InstanceInfo;
import net.kuujo.vertigo.impl.BaseTypeInfoImpl;
import net.kuujo.vertigo.network.NetworkInfo;
import net.kuujo.vertigo.util.Args;

import java.util.*;

/**
 * Component info implementation.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class ComponentInfoImpl extends BaseTypeInfoImpl<ComponentInfo> implements ComponentInfo {
  private String name;
  private String main;
  private JsonObject config;
  private List<InstanceInfo> instances = new ArrayList<>();
  private boolean worker;
  private boolean multiThreaded;
  private NetworkInfo network;

  @Override
  public String name() {
    return null;
  }

  @Override
  public String main() {
    return main;
  }

  @Override
  public JsonObject config() {
    return config;
  }

  @Override
  public List<InstanceInfo> instances() {
    return instances;
  }

  @Override
  public int numInstances() {
    return instances.size();
  }

  @Override
  public InstanceInfo instance(int instanceNumber) {
    InstanceInfo instance = null;
    for (InstanceInfo info : instances) {
      if (info.number() == instanceNumber) {
        instance = info;
        break;
      }
    }
    return instance;
  }

  @Override
  public InstanceInfo instance(String id) {
    InstanceInfo instance = null;
    for (InstanceInfo info : instances) {
      if (info.id().equals(id)) {
        instance = info;
        break;
      }
    }
    return instance;
  }

  @Override
  public boolean isWorker() {
    return worker;
  }

  @Override
  public boolean isMultiThreaded() {
    return multiThreaded;
  }

  @Override
  public NetworkInfo network() {
    return network;
  }

  /**
   * Component info builder.
   */
  public static class Builder implements TypeInfo.Builder<ComponentInfo> {
    private final ComponentInfoImpl component;

    public Builder() {
      component = new ComponentInfoImpl();
    }

    public Builder(ComponentInfoImpl component) {
      this.component = component;
    }

    /**
     * Sets the component name.
     *
     * @param name The component name.
     * @return The component info builder.
     */
    public Builder setName(String name) {
      Args.checkNotNull(name, "name cannot be null");
      component.name = name;
      return this;
    }

    /**
     * Sets the component main.
     *
     * @param main The component main.
     * @return The component info builder.
     */
    public Builder setMain(String main) {
      Args.checkNotNull(main, "main cannot be null");
      component.main = main;
      return this;
    }

    /**
     * Sets the component configuration.
     *
     * @param config The component configuration.
     * @return The component info builder.
     */
    public Builder setConfig(JsonObject config) {
      component.config = config;
      return this;
    }

    /**
     * Sets whether the component should be deployed as a worker.
     *
     * @param isWorker Indicates whether to deploy the component as a worker.
     * @return The component info builder.
     */
    public Builder setWorker(boolean isWorker) {
      component.worker = isWorker;
      return this;
    }

    /**
     * Sets whether the component should be deployed in a multi-threaded context.
     *
     * @param isMultiThreaded Indicates whether to deploy the component as multi-threaded.
     * @return The component info builder.
     */
    public Builder setMultiThreaded(boolean isMultiThreaded) {
      component.multiThreaded = isMultiThreaded;
      return this;
    }

    /**
     * Adds an instance to the component info.
     *
     * @param instance The instance info to add.
     * @return The component info builder.
     */
    public Builder addInstance(InstanceInfo instance) {
      Args.checkNotNull(instance, "instance cannot be null");
      for (InstanceInfo info : component.instances) {
        if (info.id().equals(instance.id()) || info.number() == instance.number()) {
          return this;
        }
      }
      component.instances.add(instance);
      return this;
    }

    /**
     * Removes an instance from the component info.
     *
     * @param instance The instance info to remove.
     * @return The component info builder.
     */
    public Builder removeInstance(InstanceInfo instance) {
      Args.checkNotNull(instance, "instance cannot be null");
      Iterator<InstanceInfo> iterator = component.instances.iterator();
      while (iterator.hasNext()) {
        if (iterator.next().id().equals(instance.id())) {
          iterator.remove();
        }
      }
      return this;
    }

    /**
     * Sets the component instances.
     *
     * @param instances A collection of instance info to add.
     * @return The component info builder.
     */
    public Builder setInstances(InstanceInfo... instances) {
      component.instances = new ArrayList<>(Arrays.asList(instances));
      return this;
    }

    /**
     * Sets the component instances.
     *
     * @param instances A collection of instance info to add.
     * @return The component info builder.
     */
    public Builder setInstances(Collection<InstanceInfo> instances) {
      Args.checkNotNull(instances, "instances cannot be null");
      component.instances = new ArrayList<>(instances);
      return this;
    }

    /**
     * Clears all component instance info.
     *
     * @return The component info builder.
     */
    public Builder clearInstances() {
      component.instances.clear();
      return this;
    }

    /**
     * Sets the parent network info.
     *
     * @param network The parent network info.
     * @return The component info builder.
     */
    public Builder setNetwork(NetworkInfo network) {
      Args.checkNotNull(network, "network cannot be null");
      component.network = network;
      return this;
    }

    /**
     * Checks all fields in the constructed component.
     */
    private void checkFields() {
      Args.checkNotNull(component.name, "name cannot be null");
      Args.checkNotNull(component.main, "main cannot be null");
    }

    @Override
    public ComponentInfoImpl build() {
      checkFields();
      return component;
    }
  }

}
