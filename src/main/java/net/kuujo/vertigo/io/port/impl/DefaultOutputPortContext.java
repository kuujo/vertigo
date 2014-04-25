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

import net.kuujo.vertigo.hook.OutputHook;
import net.kuujo.vertigo.impl.BaseContext;
import net.kuujo.vertigo.io.OutputContext;
import net.kuujo.vertigo.io.port.OutputPortContext;
import net.kuujo.vertigo.io.stream.OutputStreamContext;
import net.kuujo.vertigo.io.stream.impl.DefaultOutputStreamContext;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Output port context.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class DefaultOutputPortContext extends BaseContext<OutputPortContext> implements OutputPortContext {
  private static final String DEFAULT_PORT = "default";
  private String port = DEFAULT_PORT;
  private Collection<OutputStreamContext> streams = new ArrayList<>();
  private List<OutputHook> hooks = new ArrayList<>();
  @JsonIgnore
  private OutputContext output;

  public DefaultOutputPortContext setOutput(OutputContext output) {
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

  @Override
  public Collection<OutputStreamContext> streams() {
    return streams;
  }

  @Override
  public List<OutputHook> hooks() {
    return hooks;
  }

  @Override
  public void notify(OutputPortContext update) {
    if (update == null) {
      for (OutputStreamContext stream : streams) {
        stream.notify(null);
      }
      streams.clear();
    } else {
      Iterator<OutputStreamContext> iter = streams.iterator();
      while (iter.hasNext()) {
        OutputStreamContext stream = iter.next();
        OutputStreamContext match = null;
        for (OutputStreamContext s : update.streams()) {
          if (stream.equals(s)) {
            match = s;
            break;
          }
        }
        if (match != null) {
          stream.notify(match);
        } else {
          stream.notify(null);
          iter.remove();
        }
      }
  
      for (OutputStreamContext stream : update.streams()) {
        if (!streams.contains(stream)) {
          streams.add(stream);
        }
      }
    }
    super.notify(this);
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
    public Builder setStreams(DefaultOutputStreamContext... streams) {
      context.streams = new ArrayList<>();
      for (DefaultOutputStreamContext stream : streams) {
        context.streams.add(stream.setPort(context));
      }
      return this;
    }

    /**
     * Sets the port connections.
     *
     * @param connections A collection of port connections.
     * @return The context builder.
     */
    public Builder setStreams(Collection<DefaultOutputStreamContext> streams) {
      context.streams = new ArrayList<>();
      for (DefaultOutputStreamContext stream : streams) {
        context.streams.add(stream.setPort(context));
      }
      return this;
    }

    /**
     * Adds a connection to the port.
     *
     * @param connection A port connection to add.
     * @return The context builder.
     */
    public Builder addStream(DefaultOutputStreamContext stream) {
      context.streams.add(stream.setPort(context));
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

    /**
     * Sets the output hooks.
     *
     * @param hooks An array of hooks.
     * @return The context builder.
     */
    public Builder setHooks(OutputHook... hooks) {
      context.hooks = Arrays.asList(hooks);
      return this;
    }

    /**
     * Sets the output hooks.
     *
     * @param hooks A list of hooks.
     * @return The context builder.
     */
    public Builder setHooks(List<OutputHook> hooks) {
      context.hooks = hooks;
      return this;
    }

    /**
     * Adds a hook to the output.
     *
     * @param hook The hook to add.
     * @return The context builder.
     */
    public Builder addHook(OutputHook hook) {
      context.hooks.add(hook);
      return this;
    }

    /**
     * Removes a hook from the output.
     *
     * @param hook The hook to remove.
     * @return The context builder.
     */
    public Builder removeHook(OutputHook hook) {
      context.hooks.remove(hook);
      return this;
    }
  }

}
