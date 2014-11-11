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
import net.kuujo.vertigo.io.OutputInfo;
import net.kuujo.vertigo.io.port.OutputPortInfo;
import net.kuujo.vertigo.io.port.impl.OutputPortInfoImpl;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * Output info implementation.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class OutputInfoImpl implements OutputInfo {
  private ComponentInfo component;
  private Map<String, OutputPortInfo> ports = new HashMap<>(10);

  public OutputInfoImpl(JsonObject output) {
    for (String key : output.fieldNames()) {
      try {
        ports.put(key, new OutputPortInfoImpl(key, Class.forName(output.getString(key))));
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
  public OutputInfo setComponent(ComponentInfo component) {
    this.component = component;
    return this;
  }

  @Override
  public Collection<OutputPortInfo> getPorts() {
    return ports.values();
  }

  @Override
  public OutputInfo setPorts(Collection<OutputPortInfo> ports) {
    this.ports.clear();
    for (OutputPortInfo port : ports) {
      this.ports.put(port.getName(), port.setComponent(component));
    }
    return this;
  }

  @Override
  public OutputPortInfo getPort(String name) {
    return ports.get(name);
  }

  @Override
  public OutputInfo setPort(String name, Class<?> type) {
    this.ports.put(name, new OutputPortInfoImpl(name, type).setComponent(component));
    return this;
  }

  @Override
  public OutputPortInfo addPort(String name) {
    return addPort(name, Object.class);
  }

  @Override
  public OutputPortInfo addPort(String name, Class<?> type) {
    OutputPortInfo port = new OutputPortInfoImpl(name, type).setComponent(component);
    ports.put(name, port);
    return port;
  }

  @Override
  public OutputInfo removePort(String name) {
    this.ports.remove(name);
    return this;
  }
}
