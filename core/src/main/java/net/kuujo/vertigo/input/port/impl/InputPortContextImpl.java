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

package net.kuujo.vertigo.input.port.impl;

import net.kuujo.vertigo.impl.BaseContextImpl;
import net.kuujo.vertigo.input.InputContext;
import net.kuujo.vertigo.input.connection.InputConnectionContext;
import net.kuujo.vertigo.input.port.InputPortContext;
import net.kuujo.vertigo.util.Args;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Input port info implementation.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class InputPortContextImpl extends BaseContextImpl<InputPortContext> implements InputPortContext {
  private String name;
  private InputContext input;
  private List<InputConnectionContext> connections = new ArrayList<>();

  @Override
  public String name() {
    return name;
  }

  @Override
  public InputContext input() {
    return input;
  }

  @Override
  public Collection<InputConnectionContext> connections() {
    return connections;
  }

  /**
   * Input port info builder.
   */
  public static class Builder implements InputPortContext.Builder {
    private final InputPortContextImpl port;

    public Builder() {
      port = new InputPortContextImpl();
    }

    public Builder(InputPortContextImpl port) {
      this.port = port;
    }

    @Override
    public Builder setName(String name) {
      Args.checkNotNull(name, "name cannot be null");
      port.name = name;
      return this;
    }

    @Override
    public Builder addConnection(InputConnectionContext connection) {
      Args.checkNotNull(connection, "connection cannot be null");
      port.connections.add(connection);
      return this;
    }

    @Override
    public Builder removeConnection(InputConnectionContext connection) {
      Args.checkNotNull(connection, "connection cannot be null");
      port.connections.remove(connection);
      return this;
    }

    @Override
    public Builder setConnections(InputConnectionContext... connections) {
      port.connections = new ArrayList<>(Arrays.asList(connections));
      return this;
    }

    @Override
    public Builder setConnections(Collection<InputConnectionContext> connections) {
      Args.checkNotNull(connections, "connections cannot be null");
      port.connections = new ArrayList<>(connections);
      return this;
    }

    @Override
    public Builder setInput(InputContext input) {
      Args.checkNotNull(input, "input cannot be null");
      port.input = input;
      return this;
    }

    @Override
    public InputPortContextImpl build() {
      return port;
    }
  }

}
