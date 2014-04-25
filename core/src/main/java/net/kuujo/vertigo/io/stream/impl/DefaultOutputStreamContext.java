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
package net.kuujo.vertigo.io.stream.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.kuujo.vertigo.impl.BaseContext;
import net.kuujo.vertigo.io.connection.OutputConnectionContext;
import net.kuujo.vertigo.io.connection.impl.DefaultOutputConnectionContext;
import net.kuujo.vertigo.io.port.OutputPortContext;
import net.kuujo.vertigo.io.selector.RoundRobinSelector;
import net.kuujo.vertigo.io.selector.Selector;
import net.kuujo.vertigo.io.stream.OutputStreamContext;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Output connection context.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class DefaultOutputStreamContext extends BaseContext<OutputStreamContext> implements OutputStreamContext {
  private List<OutputConnectionContext> connections = new ArrayList<>();
  private Selector selector = new RoundRobinSelector();
  @JsonIgnore
  private OutputPortContext port;

  public DefaultOutputStreamContext setPort(OutputPortContext port) {
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
  public Selector selector() {
    return selector;
  }

  @Override
  public List<OutputConnectionContext> connections() {
    return connections;
  }

  @Override
  public void notify(OutputStreamContext update) {
    if (update == null) {
      for (OutputConnectionContext connection : connections) {
        connection.notify(null);
      }
      connections.clear();
    } else {
      Iterator<OutputConnectionContext> iter = connections.iterator();
      while (iter.hasNext()) {
        OutputConnectionContext connection = iter.next();
        OutputConnectionContext match = null;
        for (OutputConnectionContext c : update.connections()) {
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
  
      for (OutputConnectionContext connection : update.connections()) {
        if (!connections.contains(connection)) {
          connections.add(connection);
        }
      }
    }
    super.notify(this);
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
    public Builder addConnection(DefaultOutputConnectionContext connection) {
      context.connections.add(connection.setStream(context));
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
     * Sets the connection selector.
     *
     * @param selector The connection selector.
     * @return The context builder.
     */
    public Builder setSelector(Selector selector) {
      context.selector = selector;
      return this;
    }
  }

}
