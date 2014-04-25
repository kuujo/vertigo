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
package net.kuujo.vertigo.io.port.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import net.kuujo.vertigo.hook.InputHook;
import net.kuujo.vertigo.impl.BaseContext;
import net.kuujo.vertigo.io.InputContext;
import net.kuujo.vertigo.io.connection.InputConnectionContext;
import net.kuujo.vertigo.io.connection.impl.DefaultInputConnectionContext;
import net.kuujo.vertigo.io.port.InputPortContext;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Input port context.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class DefaultInputPortContext extends BaseContext<InputPortContext> implements InputPortContext {
  private String port;
  private Collection<InputConnectionContext> connections = new ArrayList<>();
  private List<InputHook> hooks = new ArrayList<>();
  @JsonIgnore
  private InputContext input;

  public DefaultInputPortContext setInput(InputContext input) {
    this.input = input;
    return this;
  }

  /**
   * Returns the port name.
   *
   * @return The input port name.
   */
  public String name() {
    return port;
  }

  @Override
  public InputContext input() {
    return input;
  }

  @Override
  public String address() {
    return address;
  }

  @Override
  public Collection<InputConnectionContext> connections() {
    return connections;
  }

  @Override
  public List<InputHook> hooks() {
    return hooks;
  }

  @Override
  public void notify(InputPortContext update) {
    if (update == null) {
      for (InputConnectionContext connection : connections) {
        connection.notify(null);
      }
      connections.clear();
    } else {
      Iterator<InputConnectionContext> iter = connections.iterator();
      while (iter.hasNext()) {
        InputConnectionContext connection = iter.next();
        InputConnectionContext match = null;
        for (InputConnectionContext c : update.connections()) {
          if (connection.equals(c)) {
            match = c;
            break;
          }
        }
        if (match != null) {
          connection.notify(match);
        } else {
          connection.notify(null);
          iter.remove();
        }
      }
  
      for (InputConnectionContext connection : update.connections()) {
        if (!connections.contains(connection)) {
          connections.add(connection);
        }
      }
    }
    super.notify(this);
  }

  /**
   * Connection context builder.
   *
   * @author Jordan Halterman
   */
  public static class Builder extends BaseContext.Builder<Builder, DefaultInputPortContext> {

    private Builder() {
      super(new DefaultInputPortContext());
    }

    private Builder(DefaultInputPortContext context) {
      super(context);
    }

    /**
     * Creates a new context builder.
     *
     * @return A new input port context builder.
     */
    public static Builder newBuilder() {
      return new Builder();
    }

    /**
     * Creates a new context builder.
     *
     * @param context A starting input port context.
     * @return A new input port context builder.
     */
    public static Builder newBuilder(DefaultInputPortContext context) {
      return new Builder(context);
    }

    /**
     * Sets the input port name.
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
    public Builder setConnections(DefaultInputConnectionContext... connections) {
      context.connections = new ArrayList<>();
      for (DefaultInputConnectionContext connection : connections) {
        context.connections.add(connection.setPort(context));
      }
      return this;
    }

    /**
     * Sets the port connections.
     *
     * @param connections A collection of port connections.
     * @return The context builder.
     */
    public Builder setConnections(Collection<DefaultInputConnectionContext> connections) {
      context.connections = new ArrayList<>();
      for (DefaultInputConnectionContext connection : connections) {
        context.connections.add(connection.setPort(context));
      }
      return this;
    }

    /**
     * Adds a connection to the port.
     *
     * @param connection A port connection to add.
     * @return The context builder.
     */
    public Builder addConnection(DefaultInputConnectionContext connection) {
      context.connections.add(connection.setPort(context));
      return this;
    }

    /**
     * Removes a connection from the port.
     *
     * @param connection A port connection to remove.
     * @return The context builder.
     */
    public Builder removeConnection(InputConnectionContext connection) {
      context.connections.remove(connection);
      return this;
    }

    /**
     * Sets the input hooks.
     *
     * @param hooks An array of hooks.
     * @return The context builder.
     */
    public Builder setHooks(InputHook... hooks) {
      context.hooks = Arrays.asList(hooks);
      return this;
    }

    /**
     * Sets the input hooks.
     *
     * @param hooks A list of hooks.
     * @return The context builder.
     */
    public Builder setHooks(List<InputHook> hooks) {
      context.hooks = hooks;
      return this;
    }

    /**
     * Adds a hook to the input.
     *
     * @param hook The hook to add.
     * @return The context builder.
     */
    public Builder addHook(InputHook hook) {
      context.hooks.add(hook);
      return this;
    }

    /**
     * Removes a hook from the input.
     *
     * @param hook The hook to remove.
     * @return The context builder.
     */
    public Builder removeHook(InputHook hook) {
      context.hooks.remove(hook);
      return this;
    }
  }

}
