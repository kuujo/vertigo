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
package net.kuujo.vertigo.context;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 * Output port context.
 *
 * @author Jordan Halterman
 */
public class OutputPortContext extends Context<OutputPortContext> {
  private static final String DEFAULT_PORT = "default";
  private String port = DEFAULT_PORT;
  private String address;
  private Collection<OutputConnectionContext> connections = new ArrayList<>();

  /**
   * Returns the port name.
   *
   * @return The port name.
   */
  public String name() {
    return port;
  }

  @Override
  public String address() {
    return address;
  }

  /**
   * Returns a collection of port connections.
   *
   * @return A collection of connections in the port.
   */
  public Collection<OutputConnectionContext> connections() {
    return connections;
  }

  @Override
  public void notify(OutputPortContext update) {
    super.notify(update);
    for (OutputConnectionContext connection : connections) {
      boolean updated = false;
      for (OutputConnectionContext c : update.connections()) {
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
   * port context builder.
   *
   * @author Jordan Halterman
   */
  public static class Builder extends net.kuujo.vertigo.context.Context.Builder<OutputPortContext> {

    private Builder() {
      super(new OutputPortContext());
    }

    private Builder(OutputPortContext context) {
      super(context);
    }

    /**
     * Creates a new context builder.
     *
     * @return A new context builder.
     */
    public static Builder newBuilder() {
      return new Builder();
    }

    /**
     * Creates a new context builder.
     *
     * @param context A starting port context.
     * @return A new context builder.
     */
    public static Builder newBuilder(OutputPortContext context) {
      return new Builder(context);
    }

    /**
     * Sets the port name.
     *
     * @param port The port name.
     * @return The context builder.
     */
    public Builder setName(String port) {
      context.port = port;
      return this;
    }

    /**
     * Sets the port address.
     *
     * @param address The port address.
     * @return The context builder.
     */
    public Builder setAddress(String address) {
      context.address = address;
      return this;
    }

    /**
     * Sets the port connections.
     *
     * @param connections An array of port connections.
     * @return The context builder.
     */
    public Builder setConnections(OutputConnectionContext... connections) {
      context.connections = Arrays.asList(connections);
      return this;
    }

    /**
     * Sets the port connections.
     *
     * @param connections A collection of port connections.
     * @return The context builder.
     */
    public Builder setConnections(Collection<OutputConnectionContext> connections) {
      context.connections = connections;
      return this;
    }

    /**
     * Adds a connection to the port.
     *
     * @param connection A port connection to add.
     * @return The context builder.
     */
    public Builder addConnection(OutputConnectionContext connection) {
      context.connections.add(connection);
      return this;
    }

    /**
     * Removes a connection from the port.
     *
     * @param connection A port connection to remove.
     * @return The context builder.
     */
    public Builder removeConnection(OutputConnectionContext connection) {
      context.connections.remove(connection);
      return this;
    }
  }

}
