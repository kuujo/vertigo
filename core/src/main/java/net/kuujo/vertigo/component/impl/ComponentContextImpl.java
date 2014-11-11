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
import net.kuujo.vertigo.component.PartitionContext;
import net.kuujo.vertigo.impl.BaseContextImpl;
import net.kuujo.vertigo.network.NetworkContext;
import net.kuujo.vertigo.util.Args;

import java.util.*;

/**
 * Component context implementation.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class ComponentContextImpl extends BaseContextImpl<ComponentContext> implements ComponentContext {
  private String id;
  private String address;
  private String main;
  private Component component;
  private JsonObject config;
  private List<PartitionContext> partitions = new ArrayList<>();
  private boolean worker;
  private boolean multiThreaded;
  private Set<String> resources = new HashSet<>();
  private NetworkContext network;

  @Override
  public String name() {
    return id;
  }

  @Override
  public String address() {
    return address;
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
  public List<PartitionContext> partitions() {
    return partitions;
  }

  @Override
  public PartitionContext partition(int partitionNumber) {
    PartitionContext instance = null;
    for (PartitionContext info : partitions) {
      if (info.number() == partitionNumber) {
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
   * Component context builder.
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
    public ComponentContext.Builder setName(String name) {
      Args.checkNotNull(name, "name cannot be null");
      component.id = name;
      return this;
    }

    @Override
    public ComponentContext.Builder setAddress(String address) {
      component.address = Args.checkNotNull(address, "address cannot be null");
      return this;
    }

    @Override
    public Builder setIdentifier(String identifier) {
      Args.checkNotNull(identifier, "identifier cannot be null");
      component.main = identifier;
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
    public Builder addPartition(PartitionContext partition) {
      Args.checkNotNull(partition, "partition cannot be null");
      for (PartitionContext context : component.partitions) {
        if (context.number() == partition.number()) {
          return this;
        }
      }
      component.partitions.add(partition);
      return this;
    }

     @Override
    public Builder removePartition(PartitionContext partition) {
      Args.checkNotNull(partition, "partition cannot be null");
      Iterator<PartitionContext> iterator = component.partitions.iterator();
      while (iterator.hasNext()) {
        if (iterator.next().number() == partition.number()) {
          iterator.remove();
        }
      }
      return this;
    }

    @Override
    public Builder setPartitions(PartitionContext... partitions) {
      component.partitions = new ArrayList<>(Arrays.asList(partitions));
      return this;
    }

    @Override
    public Builder setPartitions(Collection<PartitionContext> partitions) {
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
      Args.checkNotNull(component.id, "name cannot be null");
      Args.checkNotNull(component.address, "address cannot be null");
    }

    @Override
    public ComponentContextImpl build() {
      checkFields();
      return component;
    }
  }

}
