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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.kuujo.vertigo.component.impl.DefaultInstanceContext;
import net.kuujo.vertigo.impl.BaseContext;
import net.kuujo.vertigo.io.OutputContext;
import net.kuujo.vertigo.io.port.OutputPortContext;
import net.kuujo.vertigo.io.port.impl.DefaultOutputPortContext;

/**
 * Output context represents output information between a
 * source component and a target component. This information
 * is used to indicate where the component should send messages.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class DefaultOutputContext extends DefaultIOContext<OutputContext> implements OutputContext {
  private Map<String, DefaultOutputPortContext> ports = new HashMap<>();

  @Override
  public DefaultOutputContext setInstanceContext(DefaultInstanceContext instance) {
    this.instance = instance;
    return this;
  }

  @Override
  public String address() {
    return null;
  }

  @Override
  public Collection<OutputPortContext> ports() {
    List<OutputPortContext> ports = new ArrayList<>();
    for (DefaultOutputPortContext port : this.ports.values()) {
      ports.add(port.setOutputContext(this));
    }
    return ports;
  }

  @Override
  public OutputPortContext port(String name) {
    DefaultOutputPortContext port = ports.get(name);
    if (port != null) {
      port.setOutputContext(this);
    }
    return port;
  }

  @Override
  public void notify(OutputContext update) {
    if (update == null) {
      for (OutputPortContext port : ports.values()) {
        port.notify(null);
      }
      ports.clear();
    } else {
      Iterator<Map.Entry<String, DefaultOutputPortContext>> iter = ports.entrySet().iterator();
      while (iter.hasNext()) {
        Map.Entry<String, DefaultOutputPortContext> entry = iter.next();
        DefaultOutputPortContext port = entry.getValue();
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
        if (!ports.containsKey(port.name())) {
          ports.put(port.name(), DefaultOutputPortContext.Builder.newBuilder(port).build().setOutputContext(this));
        }
      }
    }
    super.notify(this);
  }

  @Override
  public String uri() {
    return String.format("%s://%s/%s/%d/out", instance.component().network().cluster(), instance.component().network().name(), instance.component().name(), instance.number());
  }

  @Override
  public String toString() {
    if (instance != null) {
      return String.format("Output[%s]", instance.component().name());
    }
    return "Output[?]";
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
    public static Builder newBuilder(OutputContext context) {
      if (context instanceof DefaultOutputContext) {
        return new Builder((DefaultOutputContext) context);
      } else {
        return new Builder().setAddress(context.address())
            .setPorts(context.ports());
      }
    }

    /**
     * Sets the output ports.
     *
     * @param ports An array of output port contexts.
     * @return The context builder.
     */
    public Builder setPorts(OutputPortContext... ports) {
      context.ports = new HashMap<>();
      for (OutputPortContext port : ports) {
        context.ports.put(port.name(), DefaultOutputPortContext.Builder.newBuilder(port).build().setOutputContext(context));
      }
      return this;
    }

    /**
     * Sets the output ports.
     *
     * @param ports A collection of output port contexts.
     * @return The context builder.
     */
    public Builder setPorts(Collection<OutputPortContext> ports) {
      context.ports = new HashMap<>();
      for (OutputPortContext port : ports) {
        context.ports.put(port.name(), DefaultOutputPortContext.Builder.newBuilder(port).build().setOutputContext(context));
      }
      return this;
    }

    /**
     * Adds a port to the output.
     *
     * @param port An output port context.
     * @return The context builder.
     */
    public Builder addPort(OutputPortContext port) {
      context.ports.put(port.name(), DefaultOutputPortContext.Builder.newBuilder(port).build().setOutputContext(context));
      return this;
    }

    /**
     * Removes a port from the output.
     *
     * @param port An output port context.
     * @return The context builder.
     */
    public Builder removePort(OutputPortContext port) {
      context.ports.remove(port.name());
      return this;
    }
  }

}
