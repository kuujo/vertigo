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
package net.kuujo.vertigo.io.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import net.kuujo.vertigo.component.impl.DefaultInstanceContext;
import net.kuujo.vertigo.impl.BaseContext;
import net.kuujo.vertigo.io.OutputContext;
import net.kuujo.vertigo.io.port.OutputPortContext;
import net.kuujo.vertigo.io.port.impl.DefaultOutputPortContext;
import net.kuujo.vertigo.util.serialization.Serializer;
import net.kuujo.vertigo.util.serialization.SerializerFactory;

import org.vertx.java.core.json.JsonObject;

/**
 * Output context represents output information between a
 * source component and a target component. This information
 * is used to indicate where the component should send messages.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
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
    if (update == null) {
      for (OutputPortContext port : ports) {
        port.notify(null);
      }
      ports.clear();
    } else {
      Iterator<OutputPortContext> iter = ports.iterator();
      while (iter.hasNext()) {
        OutputPortContext port = iter.next();
        OutputPortContext match = null;
        for (OutputPortContext p : update.ports()) {
          if (port.name().equals(p.name())) {
            match = p;
            break;
          }
        }
        if (match != null) {
          port.notify(match);
        } else {
          port.notify(null);
          iter.remove();
        }
      }
  
      for (OutputPortContext port : update.ports()) {
        boolean exists = false;
        for (OutputPortContext p : ports) {
          if (p.name().equals(port.name())) {
            exists = true;
            break;
          }
        }
        if (!exists) {
          ports.add(port);
        }
      }
    }
    super.notify(this);
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
    public Builder setPorts(DefaultOutputPortContext... ports) {
      context.ports = new ArrayList<>();
      for (DefaultOutputPortContext port : ports) {
        context.ports.add(port.setOutput(context));
      }
      return this;
    }

    /**
     * Sets the output ports.
     *
     * @param ports A collection of output port contexts.
     * @return The context builder.
     */
    public Builder setPorts(Collection<DefaultOutputPortContext> ports) {
      context.ports = new ArrayList<>();
      for (DefaultOutputPortContext port : ports) {
        context.ports.add(port.setOutput(context));
      }
      return this;
    }

    /**
     * Adds a port to the output.
     *
     * @param port An output port context.
     * @return The context builder.
     */
    public Builder addPort(DefaultOutputPortContext port) {
      context.ports.add(port.setOutput(context));
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
