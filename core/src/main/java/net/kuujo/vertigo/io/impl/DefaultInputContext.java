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
import net.kuujo.vertigo.io.InputContext;
import net.kuujo.vertigo.io.port.InputPortContext;
import net.kuujo.vertigo.io.port.impl.DefaultInputPortContext;
import net.kuujo.vertigo.util.serialization.Serializer;
import net.kuujo.vertigo.util.serialization.SerializerFactory;

import org.vertx.java.core.json.JsonObject;

/**
 * Input context represents output information between a
 * source component and a target component. This information
 * is used to indicate where the component should listen
 * for messages.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class DefaultInputContext extends DefaultIOContext<InputContext> implements InputContext {
  private Collection<InputPortContext> ports = new ArrayList<>();

  /**
   * Creates a new input context from JSON.
   * 
   * @param context A JSON representation of the input context.
   * @return A new input context instance.
   * @throws MalformedContextException If the JSON context is malformed.
   */
  public static InputContext fromJson(JsonObject context) {
    Serializer serializer = SerializerFactory.getSerializer(DefaultInstanceContext.class);
    DefaultInputContext input = serializer.deserializeObject(context.getObject("input"), DefaultInputContext.class);
    DefaultInstanceContext instance = DefaultInstanceContext.fromJson(context);
    return input.setInstanceContext(instance);
  }

  /**
   * Serializes an input context to JSON.
   * 
   * @param context The input context to serialize.
   * @return A Json representation of the input context.
   */
  public static JsonObject toJson(DefaultInputContext context) {
    Serializer serializer = SerializerFactory.getSerializer(DefaultInstanceContext.class);
    JsonObject json = DefaultInstanceContext.toJson(context.instance());
    return json.putObject("input", serializer.serializeToObject(context));
  }

  @Override
  public Collection<InputPortContext> ports() {
    for (InputPortContext port : ports) {
      ((DefaultInputPortContext) port).setInput(this);
    }
    return ports;
  }

  @Override
  public String address() {
    return null;
  }

  @Override
  public void notify(InputContext update) {
    if (update == null) {
      for (InputPortContext port : ports) {
        port.notify(null);
      }
      ports.clear();
    } else {
      Iterator<InputPortContext> iter = ports.iterator();
      while (iter.hasNext()) {
        InputPortContext port = iter.next();
        InputPortContext match = null;
        for (InputPortContext p : update.ports()) {
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
  
      for (InputPortContext port : update.ports()) {
        if (!ports.contains(port)) {
          ports.add(port);
        }
      }
    }
    super.notify(this);
  }

  /**
   * Input context builder.
   *
   * @author Jordan Halterman
   */
  public static class Builder extends BaseContext.Builder<Builder, DefaultInputContext> {

    private Builder() {
      super(new DefaultInputContext());
    }

    private Builder(DefaultInputContext context) {
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
     * @param context A starting input context.
     * @return A new context builder.
     */
    public static Builder newBuilder(DefaultInputContext context) {
      return new Builder(context);
    }

    /**
     * Sets the input ports.
     *
     * @param ports An array of input port contexts.
     * @return The context builder.
     */
    public Builder setPorts(DefaultInputPortContext... ports) {
      context.ports = new ArrayList<>();
      for (DefaultInputPortContext port : ports) {
        context.ports.add(port.setInput(context));
      }
      return this;
    }

    /**
     * Sets the input ports.
     *
     * @param ports A collection of input port contexts.
     * @return The context builder.
     */
    public Builder setPorts(Collection<DefaultInputPortContext> ports) {
      context.ports = new ArrayList<>();
      for (DefaultInputPortContext port : ports) {
        context.ports.add(port.setInput(context));
      }
      return this;
    }

    /**
     * Adds a port to the input.
     *
     * @param port An input port context.
     * @return The context builder.
     */
    public Builder addPort(DefaultInputPortContext port) {
      context.ports.add(port.setInput(context));
      return this;
    }

    /**
     * Removes a port from the input.
     *
     * @param port An input port context.
     * @return The context builder.
     */
    public Builder removePort(InputPortContext port) {
      context.ports.remove(port);
      return this;
    }
  }

}
