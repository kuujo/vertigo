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
import net.kuujo.vertigo.component.Component;
import net.kuujo.vertigo.component.ComponentContext;
import net.kuujo.vertigo.component.ComponentOptions;
import net.kuujo.vertigo.component.PartitionInfo;
import net.kuujo.vertigo.impl.BaseContextImpl;
import net.kuujo.vertigo.network.NetworkContext;
import net.kuujo.vertigo.util.Args;

import java.util.*;

/**
 * Component info implementation.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class ComponentContextImpl extends BaseContextImpl<ComponentContext> implements ComponentContext {
  private String name;
  private String main;
  private Component component;
  private JsonObject config;
  private List<PartitionInfo> partitions = new ArrayList<>();
  private boolean worker;
  private boolean multiThreaded;
  private Set<String> resources = new HashSet<>();
  private NetworkContext network;

  @Override
  public String name() {
    return null;
  }

  @Override
  public String main() {
    return main;
  }

  @Override
  public Component component() {
    return component;
  }

  @Override
  public JsonObject config() {
    return config;
  }

  @Override
  public List<PartitionInfo> partitions() {
    return partitions;
  }

  @Override
  public PartitionInfo partition(int partitionNumber) {
    PartitionInfo instance = null;
    for (PartitionInfo info : partitions) {
      if (info.number() == partitionNumber) {
        instance = info;
        break;
      }
    }
    return instance;
  }

  @Override
  public PartitionInfo partition(String id) {
    PartitionInfo instance = null;
    for (PartitionInfo info : partitions) {
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
  public Set<String> resources() {
    return resources;
  }

  @Override
  public NetworkContext network() {
    return network;
  }

  /**
   * Component info builder.
   */
  public static class Builder implements ComponentContext.Builder {
    private final ComponentContextImpl component;

    public Builder() {
      component = new ComponentContextImpl();
    }

    public Builder(ComponentContextImpl component) {
      this.component = component;
    }

    @Override
    public Builder setName(String name) {
      Args.checkNotNull(name, "name cannot be null");
      component.name = name;
      return this;
    }

    @Override
    public Builder setComponent(Component component) {
      Args.checkNotNull(component, "component cannot be null");
      this.component.component = component;
      return this;
    }

    @Override
    public Builder setMain(String main) {
      Args.checkNotNull(main, "main cannot be null");
      component.main = main;
      return this;
    }

    @Override
    public Builder setConfig(JsonObject config) {
      component.config = config;
      return this;
    }

    @Override
    public Builder setWorker(boolean isWorker) {
      component.worker = isWorker;
      return this;
    }

    @Override
    public Builder setMultiThreaded(boolean isMultiThreaded) {
      component.multiThreaded = isMultiThreaded;
      return this;
    }

    @Override
    public Builder setOptions(ComponentOptions options) {
      if (options.getName() != null) {
        component.name = options.getName();
      }
      if (options.getMain() != null) {
        component.main = options.getMain();
      }
      if (options.getConfig() != null) {
        component.config = options.getConfig();
      }
      component.worker = options.isWorker();
      component.multiThreaded = options.isMultiThreaded();
      return this;
    }

    @Override
    public Builder addPartition(PartitionInfo partition) {
      Args.checkNotNull(partition, "partition cannot be null");
      for (PartitionInfo info : component.partitions) {
        if (info.id().equals(partition.id()) || info.number() == partition.number()) {
          return this;
        }
      }
      component.partitions.add(partition);
      return this;
    }

     @Override
    public Builder removePartition(PartitionInfo partition) {
      Args.checkNotNull(partition, "partition cannot be null");
      Iterator<PartitionInfo> iterator = component.partitions.iterator();
      while (iterator.hasNext()) {
        if (iterator.next().id().equals(partition.id())) {
          iterator.remove();
        }
      }
      return this;
    }

    @Override
    public Builder setPartitions(PartitionInfo... partitions) {
      component.partitions = new ArrayList<>(Arrays.asList(partitions));
      return this;
    }

    @Override
    public Builder setPartitions(Collection<PartitionInfo> partitions) {
      Args.checkNotNull(partitions, "partitions cannot be null");
      component.partitions = new ArrayList<>(partitions);
      return this;
    }

    @Override
    public Builder clearPartitions() {
      component.partitions.clear();
      return this;
    }

    @Override
    public Builder addResource(String resource) {
      component.resources.add(resource);
      return this;
    }

    @Override
    public Builder removeResource(String resource) {
      component.resources.remove(resource);
      return this;
    }

    @Override
    public Builder setResources(String... resources) {
      component.resources = new HashSet<>(Arrays.asList(resources));
      return this;
    }

    @Override
    public Builder setResources(Collection<String> resources) {
      component.resources = new HashSet<>(resources);
      return this;
    }

    @Override
    public Builder setNetwork(NetworkContext network) {
      Args.checkNotNull(network, "network cannot be null");
      component.network = network;
      return this;
    }

    /**
     * Checks all fields in the constructed component.
     */
    private void checkFields() {
      Args.checkNotNull(component.name, "name cannot be null");
    }

    @Override
    public ComponentContextImpl build() {
      checkFields();
      return component;
    }
  }

}
