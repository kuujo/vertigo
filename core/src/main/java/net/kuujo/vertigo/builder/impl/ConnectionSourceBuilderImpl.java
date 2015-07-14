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

import java.util.Collection;

import net.kuujo.vertigo.builder.ConnectionSourceBuilder;
import net.kuujo.vertigo.builder.ConnectionSourceComponentBuilder;
import net.kuujo.vertigo.io.connection.ConnectionConfig;
import net.kuujo.vertigo.io.connection.impl.ConnectionConfigImpl;

/**
 * Component source builder implementation.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class ConnectionSourceBuilderImpl implements ConnectionSourceBuilder {
  private final NetworkBuilderImpl network;
  private final Collection<ConnectionConfig> connections;

  public ConnectionSourceBuilderImpl(NetworkBuilderImpl network, Collection<ConnectionConfig> connections) {
    this.network = network;
    this.connections = connections;
  }

  @Override
  public ConnectionSourceComponentBuilder component(String name) {
    ConnectionConfig connection = network.network.createConnection(new ConnectionConfigImpl());
    connections.add(connection);

    connection.getSource().setComponent(name);
    
    return new ConnectionSourceComponentBuilderImpl(network, network.component(name), connections, connection);
  }

}
