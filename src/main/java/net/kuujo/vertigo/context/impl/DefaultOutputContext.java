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
import net.kuujo.vertigo.util.serializer.Serializer;
import net.kuujo.vertigo.util.serializer.SerializerFactory;

import org.vertx.java.core.json.JsonObject;

/**
 * Output context represents output information between a
 * source component and a target component. This information
 * is used to indicate where the component should send messages.
 *
 * @author Jordan Halterman
 */
public class DefaultOutputContext extends DefaultIOContext<OutputContext> implements OutputContext {
  private Collection<OutputPortContext> ports = new ArrayList<>();

  /**
   * Creates a new output context from JSON.
   * 
   * @param context A JSON representation of the output context.
   * @return A new output context instance.
   * @throws MalformedContextException If the JSON context is malformed.
   */
  public static DefaultOutputContext fromJson(JsonObject context) {
    Serializer serializer = SerializerFactory.getSerializer(DefaultInstanceContext.class);
    DefaultOutputContext output = serializer.deserializeObject(context.getObject("output"), DefaultOutputContext.class);
    DefaultInstanceContext instance = DefaultInstanceContext.fromJson(context);
    return (DefaultOutputContext) output.setInstanceContext(instance);
  }

  /**
   * Serializes an output context to JSON.
   * 
   * @param context The IO context to serialize.
   * @return A Json representation of the IO context.
   */
  public static JsonObject toJson(DefaultOutputContext context) {
    Serializer serializer = SerializerFactory.getSerializer(DefaultInstanceContext.class);
    JsonObject json = DefaultInstanceContext.toJson(context.instance());
    return json.putObject("output", serializer.serializeToObject(context));
  }

  @Override
  public String address() {
    return null;
  }

  /**
   * Returns the output's port contexts.
   *
   * @return A collection of output port contexts.
   */
  public Collection<OutputPortContext> ports() {
    for (OutputPortContext port : ports) {
      ((DefaultOutputPortContext) port).setOutput(this);
    }
    return ports;
  }

  @Override
  public void notify(OutputContext update) {
    super.notify(update);
    for (OutputPortContext port : ports) {
      boolean updated = false;
      for (OutputPortContext s : update.ports()) {
        if (port.equals(s)) {
          port.notify(s);
          updated = true;
          break;
        }
      }
      if (!updated) {
        port.notify(null);
      }
    }
  }

  /**
   * Output context builder.
   *
   * @author Jordan Halterman
   */
  public static class Builder extends BaseContext.Builder<Builder, DefaultOutputContext> {

    private Builder() {
      super(new DefaultOutputContext());
    }

    private Builder(DefaultOutputContext context) {
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
     * @param context A starting output context.
     * @return A new context builder.
     */
    public static Builder newBuilder(DefaultOutputContext context) {
      return new Builder(context);
    }

    /**
     * Sets the output ports.
     *
     * @param ports An array of output port contexts.
     * @return The context builder.
     */
    public Builder setPorts(OutputPortContext... ports) {
      context.ports = Arrays.asList(ports);
      return this;
    }

    /**
     * Sets the output ports.
     *
     * @param ports A collection of output port contexts.
     * @return The context builder.
     */
    public Builder setPorts(Collection<OutputPortContext> ports) {
      context.ports = ports;
      return this;
    }

    /**
     * Adds a port to the output.
     *
     * @param port An output port context.
     * @return The context builder.
     */
    public Builder addPort(OutputPortContext port) {
      context.ports.add(port);
      return this;
    }

    /**
     * Removes a port from the output.
     *
     * @param port An output port context.
     * @return The context builder.
     */
    public Builder removePort(OutputPortContext port) {
      context.ports.remove(port);
      return this;
    }
  }

}
