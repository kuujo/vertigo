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
import net.kuujo.vertigo.component.ComponentInfo;
import net.kuujo.vertigo.component.ComponentDescriptor;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Component info implementation.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class ComponentInfoImpl implements ComponentInfo {
  private String id;
  private String main;
  private JsonObject config;
  private int partitions;
  private boolean worker;
  private boolean multiThreaded;
  private Set<String> resources = new HashSet<>();

  public ComponentInfoImpl() {
  }

  public ComponentInfoImpl(String id) {
    this.id = id;
  }

  public ComponentInfoImpl(ComponentInfo component) {
    this.id = component.getId();
    this.main = component.getMain();
    this.config = component.getConfig();
    this.partitions = component.getPartitions();
    this.worker = component.isWorker();
    this.multiThreaded = component.isMultiThreaded();
    this.resources = new HashSet<>(component.getResources());
  }

  public ComponentInfoImpl(ComponentDescriptor component) {
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
  public ComponentInfo setId(String id) {
    this.id = id;
    return this;
  }

  @Override
  public String getMain() {
    return main;
  }

  @Override
  public ComponentInfo setMain(String main) {
    this.main = main;
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
  public int getPartitions() {
    return partitions;
  }

  @Override
  public ComponentInfo setPartitions(int partitions) {
    this.partitions = partitions;
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
