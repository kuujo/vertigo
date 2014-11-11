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

import net.kuujo.vertigo.io.OutputContext;
import net.kuujo.vertigo.io.port.OutputPortContext;
import net.kuujo.vertigo.io.stream.OutputStreamContext;
import net.kuujo.vertigo.util.Args;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Output port context implementation.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class OutputPortContextImpl extends BasePortContextImpl<OutputPortContext> implements OutputPortContext {
  private OutputContext output;
  private Set<OutputStreamContext> streams = new HashSet<>();

  @Override
  public OutputContext output() {
    return output;
  }

  @Override
  public Collection<OutputStreamContext> streams() {
    return streams;
  }

  /**
   * Output port context builder.
   */
  public static class Builder implements OutputPortContext.Builder {
    private final OutputPortContextImpl port;

    public Builder() {
      port = new OutputPortContextImpl();
    }

    public Builder(OutputPortContextImpl port) {
      this.port = port;
    }

    @Override
    public Builder setName(String name) {
      Args.checkNotNull(name, "name cannot be null");
      port.name = name;
      return this;
    }

    @Override
    public Builder setType(Class<?> type) {
      port.type = Args.checkNotNull(type, "type cannot be null");
      return this;
    }

    @Override
    public Builder addStream(OutputStreamContext stream) {
      Args.checkNotNull(stream, "stream cannot be null");
      port.streams.add(stream);
      return this;
    }

    @Override
    public Builder removeStream(OutputStreamContext stream) {
      Args.checkNotNull(stream, "stream cannot be null");
      port.streams.remove(stream);
      return this;
    }

    @Override
    public Builder setStreams(OutputStreamContext... streams) {
      port.streams = new HashSet<>(Arrays.asList(streams));
      return this;
    }

    @Override
    public Builder setStreams(Collection<OutputStreamContext> streams) {
      Args.checkNotNull(streams, "streams cannot be null");
      port.streams = new HashSet<>(streams);
      return this;
    }

    @Override
    public Builder setOutput(OutputContext output) {
      Args.checkNotNull(output, "output cannot be null");
      port.output = output;
      return this;
    }

    @Override
    public OutputPortContextImpl build() {
      return port;
    }
  }

}
