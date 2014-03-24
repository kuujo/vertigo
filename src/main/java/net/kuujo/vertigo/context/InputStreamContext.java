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
 * Input stream context.
 *
 * @author Jordan Halterman
 */
public class InputStreamContext extends Context<InputStreamContext> {
  private String address;
  private String stream;
  private Collection<InputConnectionContext> connections = new ArrayList<>();

  /**
   * Returns the stream name.
   *
   * @return The input stream name.
   */
  public String name() {
    return stream;
  }

  @Override
  public String address() {
    return address;
  }

  /**
   * Returns a collection of input stream connections.
   *
   * @return A list of input connections.
   */
  public Collection<InputConnectionContext> connections() {
    return connections;
  }

  @Override
  public void notify(InputStreamContext update) {
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
  public static class Builder extends net.kuujo.vertigo.context.Context.Builder<InputStreamContext> {

    private Builder() {
      super(new InputStreamContext());
    }

    private Builder(InputStreamContext context) {
      super(context);
    }

    /**
     * Creates a new context builder.
     *
     * @return A new input stream context builder.
     */
    public static Builder newBuilder() {
      return new Builder();
    }

    /**
     * Creates a new context builder.
     *
     * @param context A starting input stream context.
     * @return A new input stream context builder.
     */
    public static Builder newBuilder(InputStreamContext context) {
      return new Builder(context);
    }

    /**
     * Sets the input stream address.
     *
     * @param address The input stream address.
     * @return The context builder.
     */
    public Builder setAddress(String address) {
      context.address = address;
      return this;
    }

    /**
     * Sets the input stream name.
     *
     * @param stream The stream name.
     * @return The context builder.
     */
    public Builder setName(String stream) {
      context.stream = stream;
      return this;
    }

    /**
     * Sets the stream connections.
     *
     * @param connections An array of stream connections.
     * @return The context builder.
     */
    public Builder setConnections(InputConnectionContext... connections) {
      context.connections = Arrays.asList(connections);
      return this;
    }

    /**
     * Sets the stream connections.
     *
     * @param connections A collection of stream connections.
     * @return The context builder.
     */
    public Builder setConnections(Collection<InputConnectionContext> connections) {
      context.connections = connections;
      return this;
    }

    /**
     * Adds a connection to the stream.
     *
     * @param connection A stream connection to add.
     * @return The context builder.
     */
    public Builder addConnection(InputConnectionContext connection) {
      context.connections.add(connection);
      return this;
    }

    /**
     * Removes a connection from the stream.
     *
     * @param connection A stream connection to remove.
     * @return The context builder.
     */
    public Builder removeConnection(InputConnectionContext connection) {
      context.connections.remove(connection);
      return this;
    }
  }

}
