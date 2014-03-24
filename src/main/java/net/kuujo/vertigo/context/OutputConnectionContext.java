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

import java.util.Arrays;
import java.util.List;

import net.kuujo.vertigo.input.grouping.Grouping;

/**
 * Output connection context.
 *
 * @author Jordan Halterman
 */
public class OutputConnectionContext extends ConnectionContext<OutputConnectionContext> {
  private String address;
  private String target;
  private List<String> ports;
  private Grouping grouping;

  @Override
  public String address() {
    return address;
  }

  /**
   * 
   * @return
   */
  public String target() {
    return target;
  }

  public List<String> ports() {
    return ports;
  }

  public Grouping grouping() {
    return grouping;
  }

  /**
   * Output connection context builder.
   *
   * @author Jordan Halterman
   */
  public static class Builder extends net.kuujo.vertigo.context.Context.Builder<OutputConnectionContext> {

    private Builder() {
      super(new OutputConnectionContext());
    }

    private Builder(OutputConnectionContext context) {
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
    public static Builder newBuilder(OutputConnectionContext context) {
      return new Builder(context);
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
     * Sets the connection target.
     *
     * @param target The connection target.
     * @return The context builder.
     */
    public Builder setTarget(String target) {
      context.target = target;
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
      return this;
    }

    /**
     * Sets the connection ports.
     *
     * @param ports An array of output ports.
     * @return The context builder.
     */
    public Builder setPorts(String... ports) {
      context.ports = Arrays.asList(ports);
      return this;
    }

    /**
     * Sets the connection ports.
     *
     * @param ports A collection of output ports.
     * @return The context builder.
     */
    public Builder setPorts(List<String> ports) {
      context.ports = ports;
      return this;
    }

    /**
     * Adds a port to the connection.
     *
     * @param port The port to add.
     * @return The context builder.
     */
    public Builder addPort(String port) {
      context.ports.add(port);
      return this;
    }

    /**
     * Removes a port from the connection.
     *
     * @param port The port to remove.
     * @return The context builder.
     */
    public Builder removePort(String port) {
      context.ports.remove(port);
      return this;
    }
  }

}
