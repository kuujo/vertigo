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

package net.kuujo.vertigo.output.connection.impl;

import net.kuujo.vertigo.connection.TargetContext;
import net.kuujo.vertigo.connection.impl.BaseConnectionContextImpl;
import net.kuujo.vertigo.output.connection.OutputConnectionContext;
import net.kuujo.vertigo.connection.SourceContext;
import net.kuujo.vertigo.output.stream.OutputStreamContext;
import net.kuujo.vertigo.util.Args;

/**
 * Output connection info implementation.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class OutputConnectionContextImpl extends BaseConnectionContextImpl<OutputConnectionContext> implements OutputConnectionContext {
  private OutputStreamContext stream;

  @Override
  public OutputStreamContext stream() {
    return stream;
  }

  /**
   * Output connection info builder.
   */
  public static class Builder implements OutputConnectionContext.Builder {
    private OutputConnectionContextImpl connection;

    public Builder() {
      connection = new OutputConnectionContextImpl();
    }

    public Builder(OutputConnectionContextImpl connection) {
      this.connection = connection;
    }

    @Override
    public Builder setAddress(String address) {
      Args.checkNotNull(address, "address cannot be null");
      connection.address = address;
      return this;
    }

    @Override
    public Builder setSource(SourceContext source) {
      Args.checkNotNull(source, "source cannot be null");
      connection.source = source;
      return this;
    }

    @Override
    public Builder setTarget(TargetContext target) {
      Args.checkNotNull(target, "target cannot be null");
      connection.target = target;
      return this;
    }

    @Override
    public Builder setStream(OutputStreamContext stream) {
      Args.checkNotNull(stream, "stream cannot be null");
      connection.stream = stream;
      return this;
    }

    @Override
    public OutputConnectionContextImpl build() {
      return connection;
    }
  }

}
