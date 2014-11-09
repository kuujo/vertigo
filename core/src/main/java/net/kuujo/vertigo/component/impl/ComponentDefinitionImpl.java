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
import net.kuujo.vertigo.component.ComponentDefinition;
import net.kuujo.vertigo.component.ComponentDescriptor;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Component definition implementation.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class ComponentDefinitionImpl implements ComponentDefinition {
  private String id;
  private String main;
  private JsonObject config;
  private int partitions;
  private boolean worker;
  private boolean multiThreaded;
  private Set<String> resources = new HashSet<>();

  public ComponentDefinitionImpl() {
  }

  public ComponentDefinitionImpl(String id) {
    this.id = id;
  }

  public ComponentDefinitionImpl(ComponentDefinition component) {
    this.id = component.getId();
    this.main = component.getMain();
    this.config = component.getConfig();
    this.partitions = component.getPartitions();
    this.worker = component.isWorker();
    this.multiThreaded = component.isMultiThreaded();
    this.resources = new HashSet<>(component.getResources());
  }

  public ComponentDefinitionImpl(ComponentDescriptor component) {
    this.id = component.id();
    this.main = component.main();
    this.config = component.config();
    this.partitions = component.partitions();
    this.worker = component.worker();
    this.multiThreaded = component.multiThreaded();
    this.resources = new HashSet<>(component.resources());
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public ComponentDefinition setId(String id) {
    this.id = id;
    return this;
  }

  @Override
  public String getMain() {
    return main;
  }

  @Override
  public ComponentDefinition setMain(String main) {
    this.main = main;
    return this;
  }

  @Override
  public JsonObject getConfig() {
    return config;
  }

  @Override
  public ComponentDefinition setConfig(JsonObject config) {
    this.config = config;
    return this;
  }

  @Override
  public int getPartitions() {
    return partitions;
  }

  @Override
  public ComponentDefinition setPartitions(int partitions) {
    this.partitions = partitions;
    return this;
  }

  @Override
  public boolean isWorker() {
    return worker;
  }

  @Override
  public ComponentDefinition setWorker(boolean worker) {
    this.worker = worker;
    return this;
  }

  @Override
  public boolean isMultiThreaded() {
    return multiThreaded;
  }

  @Override
  public ComponentDefinition setMultiThreaded(boolean multiThreaded) {
    this.multiThreaded = multiThreaded;
    return this;
  }

  @Override
  public ComponentDefinition addResource(String resource) {
    resources.add(resource);
    return this;
  }

  @Override
  public ComponentDefinition removeResource(String resource) {
    resources.remove(resource);
    return this;
  }

  @Override
  public ComponentDefinition setResources(String... resources) {
    this.resources = new HashSet<>(Arrays.asList(resources));
    return this;
  }

  @Override
  public ComponentDefinition setResources(Collection<String> resources) {
    this.resources = new HashSet<>(resources);
    return this;
  }

  @Override
  public Set<String> getResources() {
    return resources;
  }

}
