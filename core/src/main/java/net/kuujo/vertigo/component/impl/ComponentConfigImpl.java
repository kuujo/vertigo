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

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import net.kuujo.vertigo.component.ComponentConfig;
import net.kuujo.vertigo.io.InputConfig;
import net.kuujo.vertigo.io.OutputConfig;
import net.kuujo.vertigo.io.impl.InputConfigImpl;
import net.kuujo.vertigo.io.impl.OutputConfigImpl;
import net.kuujo.vertigo.io.port.InputPortConfig;
import net.kuujo.vertigo.io.port.OutputPortConfig;
import net.kuujo.vertigo.network.Network;
import net.kuujo.vertigo.util.Args;

import java.util.*;

/**
 * Component info implementation.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class ComponentConfigImpl implements ComponentConfig {
  private String name;
  private String identifier;
  private JsonObject config;
  private boolean worker;
  private boolean multiThreaded;
  private boolean stateful;
  private int replicas;
  private InputConfig input;
  private OutputConfig output;
  private Set<String> resources = new HashSet<>(10);
  private Network network;

  public ComponentConfigImpl() {
  }

  public ComponentConfigImpl(String name) {
    this.name = name;
  }

  public ComponentConfigImpl(ComponentConfig component) {
    this.name = component.getName();
    this.identifier = component.getIdentifier();
    this.config = component.getConfig();
    this.worker = component.isWorker();
    this.multiThreaded = component.isMultiThreaded();
    this.stateful = component.isStateful();
    this.replicas = component.getReplicas();
    this.resources = new HashSet<>(component.getResources());
    this.input = component.getInput();
    this.output = component.getOutput();
    this.network = component.getNetwork();
  }

  public ComponentConfigImpl(JsonObject component) {
    update(component);
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public ComponentConfig setName(String name) {
    this.name = name;
    return this;
  }

  @Override
  public String getIdentifier() {
    return identifier;
  }

  @Override
  public ComponentConfig setIdentifier(String identifier) {
    this.identifier = identifier;
    return this;
  }

  @Override
  public JsonObject getConfig() {
    return config;
  }

  @Override
  public ComponentConfig setConfig(JsonObject config) {
    this.config = config;
    return this;
  }

  @Override
  public boolean isWorker() {
    return worker;
  }

  @Override
  public ComponentConfig setWorker(boolean worker) {
    this.worker = worker;
    return this;
  }

  @Override
  public boolean isMultiThreaded() {
    return multiThreaded;
  }

  @Override
  public ComponentConfig setMultiThreaded(boolean multiThreaded) {
    this.multiThreaded = multiThreaded;
    return this;
  }

  @Override
  public ComponentConfig setStateful(boolean stateful) {
    this.stateful = stateful;
    return this;
  }

  @Override
  public boolean isStateful() {
    return stateful;
  }

  @Override
  public ComponentConfig setReplicas(int replication) {
    this.replicas = replicas;
    return this;
  }

  @Override
  public int getReplicas() {
    return replicas;
  }

  @Override
  public InputConfig getInput() {
    return input;
  }

  @Override
  public ComponentConfig setInput(InputConfig input) {
    this.input = input;
    return this;
  }

  @Override
  public OutputConfig getOutput() {
    return output;
  }

  @Override
  public ComponentConfig setOutput(OutputConfig output) {
    this.output = output;
    return this;
  }

  @Override
  public ComponentConfig addResource(String resource) {
    resources.add(resource);
    return this;
  }

  @Override
  public ComponentConfig removeResource(String resource) {
    resources.remove(resource);
    return this;
  }

  @Override
  public ComponentConfig setResources(String... resources) {
    this.resources = new HashSet<>(Arrays.asList(resources));
    return this;
  }

  @Override
  public ComponentConfig setResources(Collection<String> resources) {
    this.resources = new HashSet<>(resources);
    return this;
  }

  @Override
  public Set<String> getResources() {
    return resources;
  }

  @Override
  public ComponentConfig setNetwork(Network network) {
    this.network = network;
    return this;
  }

  @Override
  public Network getNetwork() {
    return network;
  }

  @Override
  @SuppressWarnings("unchecked")
  public void update(JsonObject component) {
    if (this.name == null) {
      this.name = component.getString(COMPONENT_NAME, UUID.randomUUID().toString());
    }
    if (this.identifier == null) {
      this.identifier = Args.checkNotNull(component.getString(COMPONENT_IDENTIFIER));
    }
    if (component.containsKey(COMPONENT_CONFIG)) {
      this.config = component.getJsonObject(COMPONENT_CONFIG);
    }
    if (component.containsKey(COMPONENT_WORKER)) {
      this.worker = component.getBoolean(COMPONENT_WORKER);
    }
    if (component.containsKey(COMPONENT_MULTI_THREADED)) {
      this.multiThreaded = component.getBoolean(COMPONENT_MULTI_THREADED);
    }
    if (component.containsKey(COMPONENT_STATEFUL)) {
      this.stateful = component.getBoolean(COMPONENT_STATEFUL);
    }
    if (component.containsKey(COMPONENT_REPLICAS)) {
      this.replicas = component.getInteger(COMPONENT_REPLICAS, 1);
    }
    if (component.containsKey(COMPONENT_RESOURCES)) {
      this.resources.addAll(component.getJsonArray(COMPONENT_RESOURCES, new JsonArray()).getList());
    }
    JsonObject inputs = component.getJsonObject(COMPONENT_INPUT);
    if (inputs == null) {
      inputs = new JsonObject();
    }
    if (this.input == null) {
      this.input = new InputConfigImpl(inputs).setComponent(this);
    } else {
      this.input.update(inputs);
    }
    JsonObject outputs = component.getJsonObject(COMPONENT_OUTPUT);
    if (outputs == null) {
      outputs = new JsonObject();
    }
    if (this.output == null) {
      this.output = new OutputConfigImpl(outputs).setComponent(this);
    } else {
      this.output.update(outputs);
    }
  }

  @Override
  public JsonObject toJson() {
    JsonObject json = new JsonObject();
    json.put(COMPONENT_NAME, name);
    json.put(COMPONENT_IDENTIFIER, identifier);
    json.put(COMPONENT_CONFIG, config);
    json.put(COMPONENT_WORKER, worker);
    json.put(COMPONENT_MULTI_THREADED, multiThreaded);
    json.put(COMPONENT_STATEFUL, stateful);
    json.put(COMPONENT_REPLICAS, replicas);
    json.put(COMPONENT_RESOURCES, new JsonArray(Arrays.asList(resources.toArray(new String[resources.size()]))));
    JsonArray input = new JsonArray();
    for (InputPortConfig port : this.input.getPorts()) {
      input.add(port.toJson());
    }
    json.put(COMPONENT_INPUT, input);
    JsonArray output = new JsonArray();
    for (OutputPortConfig port : this.output.getPorts()) {
      output.add(port.toJson());
    }
    json.put(COMPONENT_OUTPUT, output);
    return json;
  }

}
