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
package net.kuujo.vertigo.context.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import net.kuujo.vertigo.context.InputConnectionContext;
import net.kuujo.vertigo.context.InputContext;
import net.kuujo.vertigo.context.InputPortContext;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Input port context.
 *
 * @author Jordan Halterman
 */
public class DefaultInputPortContext extends BaseContext<InputPortContext> implements InputPortContext {
  private String address;
  private String port;
  private Collection<InputConnectionContext> connections = new ArrayList<>();
  @JsonIgnore
  private InputContext input;

  DefaultInputPortContext setInput(InputContext input) {
    this.input = input;
    return this;
  }

  /**
   * Returns the port name.
   *
   * @return The input port name.
   */
  public String name() {
    return port;
  }

  @Override
  public InputContext input() {
    return input;
  }

  @Override
  public String address() {
    return address;
  }

  /**
   * Returns a collection of input port connections.
   *
   * @return A list of input connections.
   */
  public Collection<InputConnectionContext> connections() {
    for (InputConnectionContext connection : connections) {
      ((DefaultInputConnectionContext) connection).setPort(this);
    }
    return connections;
  }

  @Override
  public void notify(InputPortContext update) {
    super.notify(update);
    for (InputConnectionContext connection : connections) {
      boolean updated = false;
      for (InputConnectionContext c : update.connections()) {
        if (connection.equals(c)) {
          connection.notify(c);
          updated = true;
          break;
        }
      }
      if (!updated) {
        connection.notify(null);
      }
    }
  }

  /**
   * Connection context builder.
   *
   * @author Jordan Halterman
   */
  public static class Builder extends BaseContext.Builder<DefaultInputPortContext> {

    private Builder() {
      super(new DefaultInputPortContext());
    }

    private Builder(DefaultInputPortContext context) {
      super(context);
    }

    /**
     * Creates a new context builder.
     *
     * @return A new input port context builder.
     */
    public static Builder newBuilder() {
      return new Builder();
    }

    /**
     * Creates a new context builder.
     *
     * @param context A starting input port context.
     * @return A new input port context builder.
     */
    public static Builder newBuilder(DefaultInputPortContext context) {
      return new Builder(context);
    }

    /**
     * Sets the input port address.
     *
     * @param address The input port address.
     * @return The context builder.
     */
    public Builder setAddress(String address) {
      context.address = address;
      return this;
    }

    /**
     * Sets the input port name.
     *
     * @param port The port name.
     * @return The context builder.
     */
    public Builder setName(String port) {
      context.port = port;
      return this;
    }

    /**
     * Sets the port connections.
     *
     * @param connections An array of port connections.
     * @return The context builder.
     */
    public Builder setConnections(InputConnectionContext... connections) {
      context.connections = Arrays.asList(connections);
      return this;
    }

    /**
     * Sets the port connections.
     *
     * @param connections A collection of port connections.
     * @return The context builder.
     */
    public Builder setConnections(Collection<InputConnectionContext> connections) {
      context.connections = connections;
      return this;
    }

    /**
     * Adds a connection to the port.
     *
     * @param connection A port connection to add.
     * @return The context builder.
     */
    public Builder addConnection(InputConnectionContext connection) {
      context.connections.add(connection);
      return this;
    }

    /**
     * Removes a connection from the port.
     *
     * @param connection A port connection to remove.
     * @return The context builder.
     */
    public Builder removeConnection(InputConnectionContext connection) {
      context.connections.remove(connection);
      return this;
    }
  }

}
