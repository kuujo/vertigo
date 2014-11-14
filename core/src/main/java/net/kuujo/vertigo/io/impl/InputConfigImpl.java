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
import net.kuujo.vertigo.component.ComponentConfig;
import net.kuujo.vertigo.io.InputConfig;
import net.kuujo.vertigo.io.port.InputPortConfig;
import net.kuujo.vertigo.io.port.PortConfig;
import net.kuujo.vertigo.io.port.impl.InputPortConfigImpl;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Input info implementation.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class InputConfigImpl implements InputConfig {
  private ComponentConfig component;
  private Map<String, InputPortConfig> ports = new HashMap<>(10);

  public InputConfigImpl() {
  }

  public InputConfigImpl(JsonObject output) {
    update(output);
  }

  @Override
  public ComponentConfig getComponent() {
    return component;
  }

  @Override
  public InputConfig setComponent(ComponentConfig component) {
    this.component = component;
    return this;
  }

  @Override
  public Collection<InputPortConfig> getPorts() {
    return ports.values();
  }

  @Override
  public InputConfig setPorts(Collection<InputPortConfig> ports) {
    this.ports.clear();
    for (InputPortConfig port : ports) {
      this.ports.put(port.getName(), port.setComponent(component));
    }
    return this;
  }

  @Override
  public InputPortConfig getPort(String name) {
    return ports.get(name);
  }

  @Override
  public InputConfig setPort(String name, Class<?> type) {
    this.ports.put(name, new InputPortConfigImpl(name, type).setComponent(component));
    return this;
  }

  @Override
  public InputPortConfig addPort(String name) {
    return addPort(name, Object.class);
  }

  @Override
  public InputPortConfig addPort(String name, Class<?> type) {
    InputPortConfig port = new InputPortConfigImpl(name, type).setComponent(component);
    ports.put(name, port);
    return port;
  }

  @Override
  public InputConfig removePort(String name) {
    this.ports.remove(name);
    return this;
  }

  @Override
  public void update(JsonObject output) {
    for (String key : output.fieldNames()) {
      ports.put(key, new InputPortConfigImpl(output.put(PortConfig.PORT_NAME, key)));
    }
  }

  @Override
  public JsonObject toJson() {
    JsonObject json = new JsonObject();
    for (Map.Entry<String, InputPortConfig> entry : ports.entrySet()) {
      json.put(entry.getKey(), entry.getValue().toJson());
    }
    return json;
  }

}
