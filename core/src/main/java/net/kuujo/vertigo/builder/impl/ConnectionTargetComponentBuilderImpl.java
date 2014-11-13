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
package net.kuujo.vertigo.builder.impl;

import io.vertx.core.json.JsonObject;
import net.kuujo.vertigo.builder.*;
import net.kuujo.vertigo.io.connection.ConnectionInfo;

import java.util.Collection;

/**
 * Connection target component builder implementation.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class ConnectionTargetComponentBuilderImpl implements ConnectionTargetComponentBuilder {
  private final NetworkBuilderImpl network;
  private final ComponentBuilder component;
  private final Collection<ConnectionInfo> connections;

  public ConnectionTargetComponentBuilderImpl(NetworkBuilderImpl network, ComponentBuilder component, Collection<ConnectionInfo> connections) {
    this.network = network;
    this.component = component;
    this.connections = connections;
  }

  @Override
  public ConnectionTargetComponentBuilder identifier(String identifier) {
    component.identifier(identifier);
    return this;
  }

  @Override
  public ConnectionTargetComponentBuilder config(JsonObject config) {
    component.config(config);
    return this;
  }

  @Override
  public ConnectionTargetComponentBuilder worker() {
    component.worker();
    return this;
  }

  @Override
  public ConnectionTargetComponentBuilder worker(boolean worker) {
    component.worker(worker);
    return this;
  }

  @Override
  public ConnectionTargetComponentBuilder multiThreaded() {
    component.multiThreaded();
    return this;
  }

  @Override
  public ConnectionTargetComponentBuilder multiThreaded(boolean multiThreaded) {
    component.multiThreaded(multiThreaded);
    return this;
  }

  @Override
  public ConnectionTargetComponentBuilder stateful() {
    component.stateful();
    return this;
  }

  @Override
  public ConnectionTargetComponentBuilder stateful(boolean stateful) {
    component.stateful(stateful);
    return this;
  }

  @Override
  public ConnectionTargetComponentBuilder replicas(int replicas) {
    component.replicas(replicas);
    return this;
  }

  @Override
  public ConnectionTargetComponentBuilder port(String port) {
    for (ConnectionInfo connection : connections) {
      connection.getTarget().setPort(port);
    }
    return this;
  }

  @Override
  public ConnectionTargetBuilder and() {
    return new ConnectionTargetBuilderImpl(network, connections);
  }

  @Override
  public ConnectionTargetComponentBuilder and(String component) {
    return and().component(component);
  }

  @Override
  public ComponentBuilder component() {
    return network.component();
  }

  @Override
  public ComponentBuilder component(String name) {
    return network.component(name);
  }

  @Override
  public ConnectionSourceBuilder connect() {
    return network.connect();
  }

  @Override
  public ConnectionSourceComponentBuilder connect(String component) {
    return network.connect(component);
  }
}
