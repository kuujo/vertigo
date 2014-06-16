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

import net.kuujo.vertigo.component.ComponentConfig;
import net.kuujo.vertigo.hook.ComponentHook;
import net.kuujo.vertigo.util.Args;

import org.vertx.java.core.json.JsonObject;

/**
 * Base class for component configurations.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 *
 * @param <T> The component type.
 */
public abstract class DefaultComponentConfig<T extends ComponentConfig<T>> implements ComponentConfig<T> {
  private static final int DEFAULT_NUM_INSTANCES = 1;

  private String name;
  private Map<String, Object> config;
  private int instances = DEFAULT_NUM_INSTANCES;
  private String group;
  private List<ComponentHook> hooks = new ArrayList<>();

  @Override
  public String getName() {
    return name;
  }

  @Override
  @SuppressWarnings("unchecked")
  public T setName(String name) {
    Args.checkNotNull(name, "component name must not be null");
    Args.checkUriScheme(name, "%s is not a valid component address. Component addresses must be alpha-numeric, begin with a letter, and may contain the following symbols: -.+", name);
    this.name = name;
    return (T) this;
  }

  @Override
  public JsonObject getConfig() {
    return config != null ? new JsonObject(config) : new JsonObject();
  }

  @Override
  @SuppressWarnings("unchecked")
  public T setConfig(JsonObject config) {
    this.config = config != null ? config.toMap() : new HashMap<String, Object>();
    return (T) this;
  }

  @Override
  public int getInstances() {
    return instances;
  }

  @Override
  @SuppressWarnings("unchecked")
  public T setInstances(int instances) {
    Args.checkPositive(instances, "instances must be a positive number");
    this.instances = instances;
    return (T) this;
  }

  @Override
  @SuppressWarnings("unchecked")
  public T setGroup(String group) {
    this.group = group;
    return (T) this;
  }

  @Override
  public String getGroup() {
    return group;
  }

  @Override
  @SuppressWarnings("unchecked")
  public T addHook(ComponentHook hook) {
    if (hook != null) {
      this.hooks.add(hook);
    }
    return (T) this;
  }

  @Override
  public List<ComponentHook> getHooks() {
    return hooks;
  }

}
