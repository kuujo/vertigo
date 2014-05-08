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

import net.kuujo.vertigo.component.ModuleConfig;
import net.kuujo.vertigo.hook.ComponentHook;
import net.kuujo.vertigo.network.NetworkConfig;

import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.impl.ModuleIdentifier;

/**
 * Default module configuration implementation.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class DefaultModuleConfig implements ModuleConfig {
  private static final int DEFAULT_NUM_INSTANCES = 1;
  private static final String DEFAULT_GROUP = "__DEFAULT__";

  private String name;
  private Map<String, Object> config;
  private int instances = DEFAULT_NUM_INSTANCES;
  private String group = DEFAULT_GROUP;
  private List<ComponentHook> hooks = new ArrayList<>();
  private String module;

  public DefaultModuleConfig() {
    super();
  }

  public DefaultModuleConfig(String name, String moduleName, NetworkConfig network) {
    this.name = name;
    setModule(moduleName);
  }

  @Override
  public Type getType() {
    return Type.MODULE;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public ModuleConfig setName(String name) {
    this.name = name;
    return this;
  }

  @Override
  public JsonObject getConfig() {
    return config != null ? new JsonObject(config) : new JsonObject();
  }

  @Override
  public ModuleConfig setConfig(JsonObject config) {
    this.config = config != null ? config.toMap() : new HashMap<String, Object>();
    return this;
  }

  @Override
  public int getInstances() {
    return instances;
  }

  @Override
  public ModuleConfig setInstances(int instances) {
    if (instances < 1) {
      throw new IllegalArgumentException("Instances must be a positive integer.");
    }
    this.instances = instances;
    return this;
  }

  @Override
  public ModuleConfig setGroup(String group) {
    this.group = group != null ? group : DEFAULT_GROUP;
    return this;
  }

  @Override
  public String getGroup() {
    return group;
  }

  @Override
  public ModuleConfig addHook(ComponentHook hook) {
    this.hooks.add(hook);
    return this;
  }

  @Override
  public List<ComponentHook> getHooks() {
    return hooks;
  }

  @Override
  public ModuleConfig setModule(String moduleName) {
    // Instantiate a module identifier to force it to validate the module name.
    // If the module name is invalid then an IllegalArgumentException will be thrown.
    new ModuleIdentifier(moduleName);
    this.module = moduleName;
    return this;
  }

  @Override
  public String getModule() {
    return module;
  }

}
