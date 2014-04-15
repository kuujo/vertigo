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
import java.util.List;

import net.kuujo.vertigo.context.OutputConnectionContext;
import net.kuujo.vertigo.context.OutputPortContext;
import net.kuujo.vertigo.context.OutputStreamContext;
import net.kuujo.vertigo.input.grouping.CustomGrouping;
import net.kuujo.vertigo.input.grouping.Grouping;
import net.kuujo.vertigo.input.grouping.RoundGrouping;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Output connection context.
 *
 * @author Jordan Halterman
 */
public class DefaultOutputStreamContext extends BaseContext<OutputStreamContext> implements OutputStreamContext {
  private List<OutputConnectionContext> connections = new ArrayList<>();
  private Grouping grouping = new RoundGrouping();
  @JsonProperty("custom-grouping")
  private CustomGrouping customGrouping;
  @JsonIgnore
  private OutputPortContext port;

  DefaultOutputStreamContext setPort(OutputPortContext port) {
    this.port = port;
    return this;
  }

  @Override
  public String address() {
    return address;
  }

  @Override
  public OutputPortContext port() {
    return port;
  }

  @Override
  public Grouping grouping() {
    return customGrouping != null ? customGrouping : grouping;
  }

  @Override
  public List<OutputConnectionContext> connections() {
    for (OutputConnectionContext connection : connections) {
      ((DefaultOutputConnectionContext) connection).setStream(this);
    }
    return connections;
  }

  /**
   * Output connection context builder.
   *
   * @author Jordan Halterman
   */
  public static class Builder extends BaseContext.Builder<Builder, DefaultOutputStreamContext> {

    private Builder() {
      super(new DefaultOutputStreamContext());
    }

    private Builder(DefaultOutputStreamContext context) {
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
     * @param context A starting connection context.
     * @return A new context builder.
     */
    public static Builder newBuilder(DefaultOutputStreamContext context) {
      return new Builder(context);
    }

    /**
     * Adds a connection to the component.
     *
     * @param connection The connection to add.
     * @return The context builder.
     */
    public Builder addConnection(OutputConnectionContext connection) {
      context.connections.add(connection);
      return this;
    }

    /**
     * Removes a connection from the component.
     *
     * @param connection The connection to remove.
     * @return The context builder.
     */
    public Builder removeConnection(OutputConnectionContext connection) {
      context.connections.remove(connection);
      return this;
    }

    /**
     * Sets the connection grouping.
     *
     * @param grouping The connection grouping.
     * @return The context builder.
     */
    public Builder setGrouping(Grouping grouping) {
      context.grouping = grouping;
      if (grouping instanceof CustomGrouping) {
        context.customGrouping = (CustomGrouping) grouping;
      }
      return this;
    }
  }

}
