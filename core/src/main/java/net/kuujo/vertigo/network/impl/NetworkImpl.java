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

import net.kuujo.vertigo.component.ComponentDefinition;
import net.kuujo.vertigo.component.impl.ComponentDefinitionImpl;
import net.kuujo.vertigo.io.connection.ConnectionDefinition;
import net.kuujo.vertigo.io.connection.SourceDefinition;
import net.kuujo.vertigo.io.connection.TargetDefinition;
import net.kuujo.vertigo.io.connection.impl.ConnectionDefinitionImpl;
import net.kuujo.vertigo.network.Network;

import java.util.Collection;
import java.util.Iterator;

/**
 * Network implementation.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class NetworkImpl implements Network {
  private String id;
  private final Collection<ComponentDefinition> components;
  private final Collection<ConnectionDefinition> connections;

  public NetworkImpl(String id, Collection<ComponentDefinition> components, Collection<ConnectionDefinition> connections) {
    this.id = id;
    this.components = components;
    this.connections = connections;
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public Network setId(String id) {
    this.id = id;
    return this;
  }

  @Override
  public Collection<ComponentDefinition> getComponents() {
    return components;
  }

  @Override
  public ComponentDefinition getComponent(String name) {
    for (ComponentDefinition component : components) {
      if (component.getName().equals(name)) {
        return component;
      }
    }
    return null;
  }

  @Override
  public boolean hasComponent(String name) {
    for (ComponentDefinition component : components) {
      if (component.getName().equals(name)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public ComponentDefinition addComponent(String name) {
    ComponentDefinition component = new ComponentDefinitionImpl(name);
    components.add(component);
    return component;
  }

  @Override
  public ComponentDefinition addComponent(ComponentDefinition component) {
    components.add(component);
    return component;
  }

  @Override
  public ComponentDefinition removeComponent(String name) {
    Iterator<ComponentDefinition> iterator = components.iterator();
    while (iterator.hasNext()) {
      ComponentDefinition component = iterator.next();
      if (component.getName() != null && component.getName().equals(name)) {
        iterator.remove();
        return component;
      }
    }
    return null;
  }

  @Override
  public ComponentDefinition removeComponent(ComponentDefinition component) {
    Iterator<ComponentDefinition> iterator = components.iterator();
    while (iterator.hasNext()) {
      ComponentDefinition definition = iterator.next();
      if (definition.equals(component)) {
        iterator.remove();
        return component;
      }
    }
    return null;
  }

  @Override
  public Collection<ConnectionDefinition> getConnections() {
    return connections;
  }

  @Override
  public ConnectionDefinition createConnection(ConnectionDefinition connection) {
    connections.add(connection);
    return connection;
  }

  @Override
  public ConnectionDefinition createConnection(SourceDefinition source, TargetDefinition target) {
    return createConnection(new ConnectionDefinitionImpl().setSource(source).setTarget(target));
  }

  @Override
  public ConnectionDefinition destroyConnection(ConnectionDefinition connection) {
    Iterator<ConnectionDefinition> iterator = connections.iterator();
    while (iterator.hasNext()) {
      ConnectionDefinition c = iterator.next();
      if (c.equals(connection)) {
        iterator.remove();
        return c;
      }
    }
    return null;
  }

}
