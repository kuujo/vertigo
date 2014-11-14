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
  private Set<String> resources = new HashSet<>();
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

  @SuppressWarnings("unchecked")
  public ComponentConfigImpl(JsonObject component) {
    this.name = component.getString("name", UUID.randomUUID().toString());
    this.identifier = Args.checkNotNull(component.getString("identifier"));
    this.config = component.getJsonObject("config", new JsonObject());
    this.worker = component.getBoolean("worker", false);
    this.multiThreaded = component.getBoolean("multi-threaded", false);
    this.stateful = component.getBoolean("stateful", false);
    this.replicas = component.getInteger("replicas", 1);
    JsonObject inputs = component.getJsonObject("input", new JsonObject());
    if (inputs == null) {
      inputs = new JsonObject();
    }
    this.input = new InputConfigImpl(inputs).setComponent(this);
    JsonObject outputs = component.getJsonObject("output", new JsonObject());
    if (outputs == null) {
      outputs = new JsonObject();
    }
    this.output = new OutputConfigImpl(outputs).setComponent(this);
    this.resources = new HashSet(component.getJsonArray("resources", new JsonArray()).getList());
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

}
