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
import net.kuujo.vertigo.component.ComponentInfo;
import net.kuujo.vertigo.io.InputInfo;
import net.kuujo.vertigo.io.OutputInfo;
import net.kuujo.vertigo.io.impl.InputInfoImpl;
import net.kuujo.vertigo.io.impl.OutputInfoImpl;
import net.kuujo.vertigo.util.Args;

import java.util.*;

/**
 * Component info implementation.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class ComponentInfoImpl implements ComponentInfo {
  private String name;
  private String identifier;
  private JsonObject config;
  private boolean worker;
  private boolean multiThreaded;
  private InputInfo input;
  private OutputInfo output;
  private Set<String> resources = new HashSet<>();

  public ComponentInfoImpl() {
  }

  public ComponentInfoImpl(String name) {
    this.name = name;
  }

  public ComponentInfoImpl(ComponentInfo component) {
    this.name = component.getName();
    this.identifier = component.getIdentifier();
    this.config = component.getConfig();
    this.worker = component.isWorker();
    this.multiThreaded = component.isMultiThreaded();
    this.resources = new HashSet<>(component.getResources());
  }

  @SuppressWarnings("unchecked")
  public ComponentInfoImpl(JsonObject component) {
    this.name = component.getString("name", UUID.randomUUID().toString());
    this.identifier = Args.checkNotNull(component.getString("identifier"));
    this.config = component.getJsonObject("config", new JsonObject());
    this.worker = component.getBoolean("worker");
    this.multiThreaded = component.getBoolean("multi-threaded");
    JsonObject inputs = component.getJsonObject("input", new JsonObject());
    if (inputs == null) {
      inputs = new JsonObject();
    }
    this.input = new InputInfoImpl(inputs);
    JsonObject outputs = component.getJsonObject("output", new JsonObject());
    if (outputs == null) {
      outputs = new JsonObject();
    }
    this.output = new OutputInfoImpl(outputs);
    this.resources = new HashSet(component.getJsonArray("resources", new JsonArray()).getList());
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public ComponentInfo setName(String name) {
    this.name = name;
    return this;
  }

  @Override
  public String getIdentifier() {
    return identifier;
  }

  @Override
  public ComponentInfo setIdentifier(String identifier) {
    this.identifier = identifier;
    return this;
  }

  @Override
  public JsonObject getConfig() {
    return config;
  }

  @Override
  public ComponentInfo setConfig(JsonObject config) {
    this.config = config;
    return this;
  }

  @Override
  public boolean isWorker() {
    return worker;
  }

  @Override
  public ComponentInfo setWorker(boolean worker) {
    this.worker = worker;
    return this;
  }

  @Override
  public boolean isMultiThreaded() {
    return multiThreaded;
  }

  @Override
  public ComponentInfo setMultiThreaded(boolean multiThreaded) {
    this.multiThreaded = multiThreaded;
    return this;
  }

  @Override
  public InputInfo getInput() {
    return input;
  }

  @Override
  public ComponentInfo setInput(InputInfo input) {
    this.input = input;
    return this;
  }

  @Override
  public OutputInfo getOutput() {
    return output;
  }

  @Override
  public ComponentInfo setOutput(OutputInfo output) {
    this.output = output;
    return this;
  }

  @Override
  public ComponentInfo addResource(String resource) {
    resources.add(resource);
    return this;
  }

  @Override
  public ComponentInfo removeResource(String resource) {
    resources.remove(resource);
    return this;
  }

  @Override
  public ComponentInfo setResources(String... resources) {
    this.resources = new HashSet<>(Arrays.asList(resources));
    return this;
  }

  @Override
  public ComponentInfo setResources(Collection<String> resources) {
    this.resources = new HashSet<>(resources);
    return this;
  }

  @Override
  public Set<String> getResources() {
    return resources;
  }

}
