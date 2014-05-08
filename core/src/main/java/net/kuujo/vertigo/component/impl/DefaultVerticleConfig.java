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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.kuujo.vertigo.component.VerticleConfig;
import net.kuujo.vertigo.hook.ComponentHook;
import net.kuujo.vertigo.network.NetworkConfig;

import org.vertx.java.core.json.JsonObject;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Default verticle configuration implementation.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class DefaultVerticleConfig implements VerticleConfig {
  private static final int DEFAULT_NUM_INSTANCES = 1;
  private static final String DEFAULT_GROUP = "__DEFAULT__";

  private String name;
  private Map<String, Object> config;
  private int instances = DEFAULT_NUM_INSTANCES;
  private String group = DEFAULT_GROUP;
  private List<ComponentHook> hooks = new ArrayList<>();
  private String main;
  private boolean worker = false;
  @JsonProperty("multi-threaded")
  private boolean multiThreaded = false;

  public DefaultVerticleConfig() {
    super();
  }

  public DefaultVerticleConfig(String name, String main, NetworkConfig network) {
    this.name = name;
    this.main = main;
  }

  @Override
  public Type getType() {
    return Type.VERTICLE;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public VerticleConfig setName(String name) {
    this.name = name;
    return this;
  }

  @Override
  public JsonObject getConfig() {
    return config != null ? new JsonObject(config) : new JsonObject();
  }

  @Override
  public VerticleConfig setConfig(JsonObject config) {
    this.config = config != null ? config.toMap() : new HashMap<String, Object>();
    return this;
  }

  @Override
  public int getInstances() {
    return instances;
  }

  @Override
  public VerticleConfig setInstances(int instances) {
    if (instances < 1) {
      throw new IllegalArgumentException("Instances must be a positive integer.");
    }
    this.instances = instances;
    return this;
  }

  @Override
  public VerticleConfig setGroup(String group) {
    this.group = group != null ? group : DEFAULT_GROUP;
    return this;
  }

  @Override
  public String getGroup() {
    return group;
  }

  @Override
  public VerticleConfig addHook(ComponentHook hook) {
    this.hooks.add(hook);
    return this;
  }

  @Override
  public List<ComponentHook> getHooks() {
    return hooks;
  }

  @Override
  public VerticleConfig setMain(String main) {
    this.main = main;
    return this;
  }

  @Override
  public String getMain() {
    return main;
  }

  @Override
  public VerticleConfig setWorker(boolean isWorker) {
    this.worker = isWorker;
    if (!worker) {
      multiThreaded = false;
    }
    return this;
  }

  @Override
  public boolean isWorker() {
    return worker;
  }

  @Override
  public VerticleConfig setMultiThreaded(boolean isMultiThreaded) {
    this.multiThreaded = isMultiThreaded;
    return this;
  }

  @Override
  public boolean isMultiThreaded() {
    return multiThreaded;
  }

}
