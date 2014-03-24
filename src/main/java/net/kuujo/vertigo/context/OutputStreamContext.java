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
 * The stream context represents a connection between one component
 * and another component, including the method used to distribute messages
 * between multiple instances of each component.
 *
 * @author Jordan Halterman
 */
public class OutputStreamContext extends Context<OutputStreamContext> {
  private static final String DEFAULT_STREAM = "default";
  private String stream = DEFAULT_STREAM;
  private String address;
  private Collection<OutputConnectionContext> connections = new ArrayList<>();

  /**
   * Returns the stream name.
   *
   * @return The stream name.
   */
  public String name() {
    return stream;
  }

  @Override
  public String address() {
    return address;
  }

  /**
   * Returns a collection of stream connections.
   *
   * @return A collection of connections in the stream.
   */
  public Collection<OutputConnectionContext> connections() {
    return connections;
  }

  @Override
  public void notify(OutputStreamContext update) {
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
   * Stream context builder.
   *
   * @author Jordan Halterman
   */
  public static class Builder extends net.kuujo.vertigo.context.Context.Builder<OutputStreamContext> {

    private Builder() {
      super(new OutputStreamContext());
    }

    private Builder(OutputStreamContext context) {
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
     * @param context A starting stream context.
     * @return A new context builder.
     */
    public static Builder newBuilder(OutputStreamContext context) {
      return new Builder(context);
    }

    /**
     * Sets the stream name.
     *
     * @param stream The stream name.
     * @return The context builder.
     */
    public Builder setName(String stream) {
      context.stream = stream;
      return this;
    }

    /**
     * Sets the stream address.
     *
     * @param address The stream address.
     * @return The context builder.
     */
    public Builder setAddress(String address) {
      context.address = address;
      return this;
    }

    /**
     * Sets the stream connections.
     *
     * @param connections An array of stream connections.
     * @return The context builder.
     */
    public Builder setConnections(OutputConnectionContext... connections) {
      context.connections = Arrays.asList(connections);
      return this;
    }

    /**
     * Sets the stream connections.
     *
     * @param connections A collection of stream connections.
     * @return The context builder.
     */
    public Builder setConnections(Collection<OutputConnectionContext> connections) {
      context.connections = connections;
      return this;
    }

    /**
     * Adds a connection to the stream.
     *
     * @param connection A stream connection to add.
     * @return The context builder.
     */
    public Builder addConnection(OutputConnectionContext connection) {
      context.connections.add(connection);
      return this;
    }

    /**
     * Removes a connection from the stream.
     *
     * @param connection A stream connection to remove.
     * @return The context builder.
     */
    public Builder removeConnection(OutputConnectionContext connection) {
      context.connections.remove(connection);
      return this;
    }
  }

}
