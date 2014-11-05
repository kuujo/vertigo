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
package net.kuujo.vertigo.network.impl;

import net.kuujo.vertigo.component.ComponentOptions;
import net.kuujo.vertigo.connection.ConnectionOptions;
import net.kuujo.vertigo.connection.SourceOptions;
import net.kuujo.vertigo.connection.TargetOptions;
import net.kuujo.vertigo.network.NetworkDefinition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Network implementation.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class NetworkDefinitionImpl implements NetworkDefinition {
  private final String name;
  private final List<ComponentOptions> components = new ArrayList<>();
  private final List<ConnectionOptions> connections = new ArrayList<>();

  public NetworkDefinitionImpl(String name) {
    this.name = name;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public Collection<ComponentOptions> getComponents() {
    return components;
  }

  @Override
  public ComponentOptions getComponent(String name) {
    for (ComponentOptions component : components) {
      if (component.getName().equals(name)) {
        return component;
      }
    }
    return null;
  }

  @Override
  public boolean hasComponent(String name) {
    for (ComponentOptions component : components) {
      if (component.getName().equals(name)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public NetworkDefinition addComponent(ComponentOptions options) {
    components.add(options);
    return this;
  }

  @Override
  public NetworkDefinition addComponent(String main) {
    components.add(new ComponentOptions().setMain(main));
    return this;
  }

  @Override
  public NetworkDefinition addComponent(String main, ComponentOptions options) {
    components.add(options.setMain(main));
    return this;
  }

  @Override
  public NetworkDefinition addComponent(String name, String main) {
    components.add(new ComponentOptions().setName(name).setMain(main));
    return this;
  }

  @Override
  public NetworkDefinition addComponent(String name, String main, ComponentOptions options) {
    components.add(options.setName(name).setMain(main));
    return this;
  }

  @Override
  public NetworkDefinition removeComponent(String name) {
    Iterator<ComponentOptions> iterator = components.iterator();
    while (iterator.hasNext()) {
      ComponentOptions options = iterator.next();
      if (options.getName() != null && options.getName().equals(name)) {
        iterator.remove();
      }
    }
    return this;
  }

  @Override
  public Collection<ConnectionOptions> getConnections() {
    return connections;
  }

  @Override
  public NetworkDefinition createConnection(ConnectionOptions connection) {
    connections.add(connection);
    return this;
  }

  @Override
  public NetworkDefinition createConnection(SourceOptions source, TargetOptions target) {
    connections.add(new ConnectionOptions().setSource(source).setTarget(target));
    return this;
  }

  @Override
  public NetworkDefinition destroyConnection(ConnectionOptions connection) {
    Iterator<ConnectionOptions> iterator = connections.iterator();
    while (iterator.hasNext()) {
      if (iterator.next().equals(connection)) {
        iterator.remove();
      }
    }
    return this;
  }

}
