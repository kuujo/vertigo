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

import net.kuujo.vertigo.context.OutputContext;
import net.kuujo.vertigo.context.OutputPortContext;
import net.kuujo.vertigo.context.OutputStreamContext;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Output port context.
 *
 * @author Jordan Halterman
 */
public class DefaultOutputPortContext extends BaseContext<OutputPortContext> implements OutputPortContext {
  private static final String DEFAULT_PORT = "default";
  private String port = DEFAULT_PORT;
  private Collection<OutputStreamContext> streams = new ArrayList<>();
  @JsonIgnore
  private OutputContext output;

  DefaultOutputPortContext setOutput(OutputContext output) {
    this.output = output;
    return this;
  }

  /**
   * Returns the port name.
   *
   * @return The port name.
   */
  public String name() {
    return port;
  }

  @Override
  public OutputContext output() {
    return output;
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
  public Collection<OutputStreamContext> streams() {
    for (OutputStreamContext stream : streams) {
      ((DefaultOutputStreamContext) stream).setPort(this);
    }
    return streams;
  }

  @Override
  public void notify(OutputPortContext update) {
    super.notify(update);
    for (OutputStreamContext stream : streams) {
      boolean updated = false;
      for (OutputStreamContext s : update.streams()) {
        if (stream.equals(s)) {
          stream.notify(s);
          updated = true;
          break;
        }
      }
      if (!updated) {
        stream.notify(null);
      }
    }
  }

  /**
   * port context builder.
   *
   * @author Jordan Halterman
   */
  public static class Builder extends BaseContext.Builder<Builder, DefaultOutputPortContext> {

    private Builder() {
      super(new DefaultOutputPortContext());
    }

    private Builder(DefaultOutputPortContext context) {
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
    public static Builder newBuilder(DefaultOutputPortContext context) {
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
     * Sets the port connections.
     *
     * @param connections An array of port connections.
     * @return The context builder.
     */
    public Builder setStreams(OutputStreamContext... streams) {
      context.streams = Arrays.asList(streams);
      return this;
    }

    /**
     * Sets the port connections.
     *
     * @param connections A collection of port connections.
     * @return The context builder.
     */
    public Builder setStreams(Collection<OutputStreamContext> streams) {
      context.streams = streams;
      return this;
    }

    /**
     * Adds a connection to the port.
     *
     * @param connection A port connection to add.
     * @return The context builder.
     */
    public Builder addStream(OutputStreamContext stream) {
      context.streams.add(stream);
      return this;
    }

    /**
     * Removes a connection from the port.
     *
     * @param connection A port connection to remove.
     * @return The context builder.
     */
    public Builder removeStream(OutputStreamContext stream) {
      context.streams.remove(stream);
      return this;
    }
  }

}
