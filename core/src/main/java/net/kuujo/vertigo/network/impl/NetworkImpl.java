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

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import net.kuujo.vertigo.component.ComponentInfo;
import net.kuujo.vertigo.component.impl.ComponentInfoImpl;
import net.kuujo.vertigo.io.connection.ConnectionInfo;
import net.kuujo.vertigo.io.connection.SourceInfo;
import net.kuujo.vertigo.io.connection.TargetInfo;
import net.kuujo.vertigo.io.connection.impl.ConnectionInfoImpl;
import net.kuujo.vertigo.network.Network;
import net.kuujo.vertigo.util.Args;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.UUID;

/**
 * Network implementation.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class NetworkImpl implements Network {
  private String id;
  private final Collection<ComponentInfo> components = new ArrayList<>();
  private final Collection<ConnectionInfo> connections = new ArrayList<>();

  public NetworkImpl() {
    this(UUID.randomUUID().toString());
  }

  public NetworkImpl(String id) {
    this.id = id;
  }

  public NetworkImpl(JsonObject network) {
    this.id = Args.checkNotNull(network.getString(NETWORK_NAME));
    JsonArray components = network.getJsonArray(NETWORK_COMPONENTS);
    if (components != null) {
      for (Object component : components) {
        this.components.add(new ComponentInfoImpl((JsonObject) component));
      }
    }
    JsonArray connections = network.getJsonArray(NETWORK_CONNECTIONS);
    if (connections != null) {
      for (Object connection : connections) {
        this.connections.add(new ConnectionInfoImpl((JsonObject) connection));
      }
    }
  }

  @Override
  public String getName() {
    return id;
  }

  @Override
  public Network setName(String name) {
    this.id = name;
    return this;
  }

  @Override
  public Collection<ComponentInfo> getComponents() {
    return components;
  }

  @Override
  public ComponentInfo getComponent(String name) {
    for (ComponentInfo component : components) {
      if (component.getName().equals(name)) {
        return component;
      }
    }
    return null;
  }

  @Override
  public boolean hasComponent(String name) {
    for (ComponentInfo component : components) {
      if (component.getName().equals(name)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public ComponentInfo addComponent(String name) {
    ComponentInfo component = new ComponentInfoImpl(name);
    components.add(component);
    return component;
  }

  @Override
  public ComponentInfo addComponent(ComponentInfo component) {
    components.add(component);
    return component;
  }

  @Override
  public ComponentInfo removeComponent(String name) {
    Iterator<ComponentInfo> iterator = components.iterator();
    while (iterator.hasNext()) {
      ComponentInfo component = iterator.next();
      if (component.getName() != null && component.getName().equals(name)) {
        iterator.remove();
        return component;
      }
    }
    return null;
  }

  @Override
  public ComponentInfo removeComponent(ComponentInfo component) {
    Iterator<ComponentInfo> iterator = components.iterator();
    while (iterator.hasNext()) {
      ComponentInfo info = iterator.next();
      if (info.equals(component)) {
        iterator.remove();
        return component;
      }
    }
    return null;
  }

  @Override
  public Collection<ConnectionInfo> getConnections() {
    return connections;
  }

  @Override
  public ConnectionInfo createConnection(ConnectionInfo connection) {
    connections.add(connection);
    return connection;
  }

  @Override
  public ConnectionInfo createConnection(SourceInfo source, TargetInfo target) {
    return createConnection(new ConnectionInfoImpl().setSource(source).setTarget(target));
  }

  @Override
  public ConnectionInfo destroyConnection(ConnectionInfo connection) {
    Iterator<ConnectionInfo> iterator = connections.iterator();
    while (iterator.hasNext()) {
      ConnectionInfo c = iterator.next();
      if (c.equals(connection)) {
        iterator.remove();
        return c;
      }
    }
    return null;
  }

}
