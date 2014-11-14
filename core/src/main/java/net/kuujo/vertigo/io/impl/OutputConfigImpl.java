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
package net.kuujo.vertigo.io.impl;

import io.vertx.core.json.JsonObject;
import net.kuujo.vertigo.VertigoException;
import net.kuujo.vertigo.component.ComponentConfig;
import net.kuujo.vertigo.io.OutputConfig;
import net.kuujo.vertigo.io.port.OutputPortConfig;
import net.kuujo.vertigo.io.port.impl.OutputPortConfigImpl;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Output info implementation.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class OutputConfigImpl implements OutputConfig {
  private ComponentConfig component;
  private Map<String, OutputPortConfig> ports = new HashMap<>(10);

  public OutputConfigImpl(JsonObject output) {
    for (String key : output.fieldNames()) {
      try {
        ports.put(key, new OutputPortConfigImpl(key, Class.forName(output.getString(key))));
      } catch (ClassNotFoundException e) {
        throw new VertigoException(e);
      }
    }
  }

  @Override
  public ComponentConfig getComponent() {
    return component;
  }

  @Override
  public OutputConfig setComponent(ComponentConfig component) {
    this.component = component;
    return this;
  }

  @Override
  public Collection<OutputPortConfig> getPorts() {
    return ports.values();
  }

  @Override
  public OutputConfig setPorts(Collection<OutputPortConfig> ports) {
    this.ports.clear();
    for (OutputPortConfig port : ports) {
      this.ports.put(port.getName(), port.setComponent(component));
    }
    return this;
  }

  @Override
  public OutputPortConfig getPort(String name) {
    return ports.get(name);
  }

  @Override
  public OutputConfig setPort(String name, Class<?> type) {
    this.ports.put(name, new OutputPortConfigImpl(name, type).setComponent(component));
    return this;
  }

  @Override
  public OutputPortConfig addPort(String name) {
    return addPort(name, Object.class);
  }

  @Override
  public OutputPortConfig addPort(String name, Class<?> type) {
    OutputPortConfig port = new OutputPortConfigImpl(name, type).setComponent(component);
    ports.put(name, port);
    return port;
  }

  @Override
  public OutputConfig removePort(String name) {
    this.ports.remove(name);
    return this;
  }
}
