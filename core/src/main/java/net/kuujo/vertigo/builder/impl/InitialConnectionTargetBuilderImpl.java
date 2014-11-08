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

import net.kuujo.vertigo.builder.ConnectionTargetBuilder;
import net.kuujo.vertigo.builder.ConnectionTargetComponentBuilder;
import net.kuujo.vertigo.io.connection.ConnectionDefinition;

import java.util.Collection;

/**
 * Special connection target builder for the first connection target builder.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class InitialConnectionTargetBuilderImpl implements ConnectionTargetBuilder {
  private final NetworkBuilderImpl network;
  private final Collection<ConnectionDefinition> connections;

  public InitialConnectionTargetBuilderImpl(NetworkBuilderImpl network, Collection<ConnectionDefinition> connections) {
    this.network = network;
    this.connections = connections;
  }

  @Override
  public ConnectionTargetComponentBuilder component(String id) {
    return new ConnectionTargetComponentBuilderImpl(network, network.component(id), connections);
  }

}
