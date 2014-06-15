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
import net.kuujo.vertigo.io.InputContext;
import net.kuujo.vertigo.io.port.InputPortContext;
import net.kuujo.vertigo.io.port.impl.DefaultInputPortContext;

/**
 * Input context represents output information between a
 * source component and a target component. This information
 * is used to indicate where the component should listen
 * for messages.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class DefaultInputContext extends DefaultIOContext<InputContext> implements InputContext {
  private Map<String, DefaultInputPortContext> ports = new HashMap<>();

  @Override
  public DefaultInputContext setInstanceContext(DefaultInstanceContext instance) {
    this.instance = instance;
    return this;
  }

  @Override
  public String address() {
    return null;
  }

  @Override
  public Collection<InputPortContext> ports() {
    List<InputPortContext> ports = new ArrayList<>();
    for (DefaultInputPortContext port : this.ports.values()) {
      ports.add(port.setInputContext(this));
    }
    return ports;
  }

  @Override
  public InputPortContext port(String name) {
    DefaultInputPortContext port = ports.get(name);
    if (port != null) {
      port.setInputContext(this);
    }
    return port;
  }

  @Override
  public void notify(InputContext update) {
    if (update == null) {
      for (InputPortContext port : ports.values()) {
        port.notify(null);
      }
      ports.clear();
    } else {
      Iterator<Map.Entry<String, DefaultInputPortContext>> iter = ports.entrySet().iterator();
      while (iter.hasNext()) {
        Map.Entry<String, DefaultInputPortContext> entry = iter.next();
        DefaultInputPortContext port = entry.getValue();
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
        if (!ports.containsKey(port.name())) {
          ports.put(port.name(), DefaultInputPortContext.Builder.newBuilder(port).build().setInputContext(this));
        }
      }
    }
    super.notify(this);
  }

  @Override
  public String uri() {
    return String.format("%s://%s/%s/%d/in", instance.component().network().cluster(), instance.component().network().name(), instance.component().name(), instance.number());
  }

  @Override
  public String toString() {
    if (instance != null) {
      return String.format("Input[%s]", instance.component().name());
    }
    return "Input[?]";
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
    public static Builder newBuilder(InputContext context) {
      if (context instanceof DefaultInputContext) {
        return new Builder((DefaultInputContext) context);
      } else {
        return new Builder().setAddress(context.address())
            .setPorts(context.ports());
      }
    }

    /**
     * Sets the input ports.
     *
     * @param ports An array of input port contexts.
     * @return The context builder.
     */
    public Builder setPorts(InputPortContext... ports) {
      context.ports = new HashMap<>();
      for (InputPortContext port : ports) {
        context.ports.put(port.name(), DefaultInputPortContext.Builder.newBuilder(port).build().setInputContext(context));
      }
      return this;
    }

    /**
     * Sets the input ports.
     *
     * @param ports A collection of input port contexts.
     * @return The context builder.
     */
    public Builder setPorts(Collection<InputPortContext> ports) {
      context.ports = new HashMap<>();
      for (InputPortContext port : ports) {
        context.ports.put(port.name(), DefaultInputPortContext.Builder.newBuilder(port).build().setInputContext(context));
      }
      return this;
    }

    /**
     * Adds a port to the input.
     *
     * @param port An input port context.
     * @return The context builder.
     */
    public Builder addPort(InputPortContext port) {
      context.ports.put(port.name(), DefaultInputPortContext.Builder.newBuilder(port).build().setInputContext(context));
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
