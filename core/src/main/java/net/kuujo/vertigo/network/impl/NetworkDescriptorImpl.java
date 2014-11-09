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
import net.kuujo.vertigo.component.ComponentDescriptor;
import net.kuujo.vertigo.component.impl.ComponentDescriptorImpl;
import net.kuujo.vertigo.io.connection.ConnectionDescriptor;
import net.kuujo.vertigo.io.connection.impl.ConnectionDescriptorImpl;
import net.kuujo.vertigo.network.NetworkDescriptor;
import net.kuujo.vertigo.util.Args;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Network descriptor implementation.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class NetworkDescriptorImpl implements NetworkDescriptor {
  private final String id;
  private final List<ComponentDescriptor> components;
  private final List<ConnectionDescriptor> connections;

  public NetworkDescriptorImpl(JsonObject network) {
    this.id = Args.checkNotNull(network.getString("id"));
    JsonArray components = Args.checkNotNull(network.getJsonArray("components"));
    this.components = new ArrayList<>(components.size());
    for (Object component : components) {
      this.components.add(new ComponentDescriptorImpl((JsonObject) component));
    }
    JsonArray connections = Args.checkNotNull(network.getJsonArray("connections"));
    this.connections = new ArrayList<>(components.size());
    for (Object connection : connections) {
      this.connections.add(new ConnectionDescriptorImpl((JsonObject) connection));
    }
  }

  @Override
  public String id() {
    return id;
  }

  @Override
  public Collection<ComponentDescriptor> components() {
    return components;
  }

  @Override
  public Collection<ConnectionDescriptor> connections() {
    return connections;
  }

}
