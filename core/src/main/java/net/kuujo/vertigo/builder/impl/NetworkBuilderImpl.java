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

import net.kuujo.vertigo.builder.ComponentBuilder;
import net.kuujo.vertigo.builder.ConnectionSourceBuilder;
import net.kuujo.vertigo.builder.ConnectionSourceComponentBuilder;
import net.kuujo.vertigo.builder.NetworkBuilder;
import net.kuujo.vertigo.io.connection.ConnectionInfo;
import net.kuujo.vertigo.io.connection.impl.ConnectionInfoImpl;
import net.kuujo.vertigo.network.Network;
import net.kuujo.vertigo.network.impl.NetworkImpl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

/**
 * Network builder implementation.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class NetworkBuilderImpl implements NetworkBuilder {
  final Network network;

  public NetworkBuilderImpl() {
    this.network = new NetworkImpl();
  }

  public NetworkBuilderImpl(String name) {
    this.network = new NetworkImpl(name);
  }

  @Override
  public NetworkBuilder name(String name) {
    network.setName(name);
    return this;
  }

  @Override
  public ComponentBuilder component() {
    return new ComponentBuilderImpl(this, network.addComponent(UUID.randomUUID().toString()));
  }

  @Override
  public ComponentBuilder component(String id) {
    return new ComponentBuilderImpl(this, network.hasComponent(id) ? network.getComponent(id) : network.addComponent(id));
  }

  @Override
  public ConnectionSourceBuilder connect() {
    return new ConnectionSourceBuilderImpl(this, new ArrayList<>());
  }

  @Override
  public ConnectionSourceComponentBuilder connect(String component) {
    ConnectionInfo connection = network.createConnection(new ConnectionInfoImpl());
    return new ConnectionSourceBuilderImpl(this, new ArrayList<>(Arrays.asList(connection))).component(component);
  }

  @Override
  public Network build() {
    return network;
  }

}
