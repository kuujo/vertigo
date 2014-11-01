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

import net.kuujo.vertigo.io.OutputInfo;
import net.kuujo.vertigo.io.port.OutputPortInfo;
import net.kuujo.vertigo.io.stream.OutputStreamInfo;
import net.kuujo.vertigo.util.Args;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Output port info implementation.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class OutputPortInfoImpl extends BasePortInfoImpl<OutputPortInfo> implements OutputPortInfo {
  private OutputInfo output;
  private Set<OutputStreamInfo> streams = new HashSet<>();

  @Override
  public OutputInfo output() {
    return output;
  }

  @Override
  public Collection<OutputStreamInfo> streams() {
    return streams;
  }

  /**
   * Output port info builder.
   */
  public static class Builder implements OutputPortInfo.Builder {
    private final OutputPortInfoImpl port;

    public Builder() {
      port = new OutputPortInfoImpl();
    }

    public Builder(OutputPortInfoImpl port) {
      this.port = port;
    }

    @Override
    public Builder addStream(OutputStreamInfo stream) {
      Args.checkNotNull(stream, "stream cannot be null");
      port.streams.add(stream);
      return this;
    }

    @Override
    public Builder removeStream(OutputStreamInfo stream) {
      Args.checkNotNull(stream, "stream cannot be null");
      port.streams.remove(stream);
      return this;
    }

    @Override
    public Builder setStreams(OutputStreamInfo... streams) {
      port.streams = new HashSet<>(Arrays.asList(streams));
      return this;
    }

    @Override
    public Builder setStreams(Collection<OutputStreamInfo> streams) {
      Args.checkNotNull(streams, "streams cannot be null");
      port.streams = new HashSet<>(streams);
      return this;
    }

    @Override
    public Builder setOutput(OutputInfo output) {
      Args.checkNotNull(output, "output cannot be null");
      port.output = output;
      return this;
    }

    @Override
    public OutputPortInfoImpl build() {
      return port;
    }
  }

}
