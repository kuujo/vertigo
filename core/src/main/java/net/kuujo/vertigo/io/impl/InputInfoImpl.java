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
import net.kuujo.vertigo.component.ComponentInfo;
import net.kuujo.vertigo.io.InputInfo;
import net.kuujo.vertigo.io.port.InputPortInfo;
import net.kuujo.vertigo.io.port.impl.InputPortInfoImpl;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Input info implementation.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class InputInfoImpl implements InputInfo {
  private ComponentInfo component;
  private Map<String, InputPortInfo> ports = new HashMap<>(10);

  public InputInfoImpl(JsonObject output) {
    for (String key : output.fieldNames()) {
      try {
        ports.put(key, new InputPortInfoImpl(key, Class.forName(output.getString(key))));
      } catch (ClassNotFoundException e) {
        throw new VertigoException(e);
      }
    }
  }

  @Override
  public ComponentInfo getComponent() {
    return component;
  }

  @Override
  public InputInfo setComponent(ComponentInfo component) {
    this.component = component;
    return this;
  }

  @Override
  public Collection<InputPortInfo> getPorts() {
    return ports.values();
  }

  @Override
  public InputInfo setPorts(Collection<InputPortInfo> ports) {
    this.ports.clear();
    for (InputPortInfo port : ports) {
      this.ports.put(port.getName(), port.setComponent(component));
    }
    return this;
  }

  @Override
  public InputPortInfo getPort(String name) {
    return ports.get(name);
  }

  @Override
  public InputInfo setPort(String name, Class<?> type) {
    this.ports.put(name, new InputPortInfoImpl(name, type).setComponent(component));
    return this;
  }

  @Override
  public InputPortInfo addPort(String name) {
    return addPort(name, Object.class);
  }

  @Override
  public InputPortInfo addPort(String name, Class<?> type) {
    InputPortInfo port = new InputPortInfoImpl(name, type).setComponent(component);
    ports.put(name, port);
    return port;
  }

  @Override
  public InputInfo removePort(String name) {
    this.ports.remove(name);
    return this;
  }
}
