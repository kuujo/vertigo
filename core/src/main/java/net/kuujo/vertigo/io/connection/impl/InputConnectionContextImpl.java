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

package net.kuujo.vertigo.io.connection.impl;

import net.kuujo.vertigo.io.connection.InputConnectionContext;
import net.kuujo.vertigo.io.connection.SourceContext;
import net.kuujo.vertigo.io.connection.TargetContext;
import net.kuujo.vertigo.io.port.InputPortContext;
import net.kuujo.vertigo.util.Args;

/**
 * Input connection context implementation.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class InputConnectionContextImpl extends BaseConnectionContextImpl<InputConnectionContext, InputPortContext> implements InputConnectionContext {
  private InputPortContext port;

  @Override
  public InputPortContext port() {
    return port;
  }

  /**
   * Input connection context builder.
   */
  public static class Builder implements InputConnectionContext.Builder {
    private InputConnectionContextImpl connection;

    public Builder() {
      connection = new InputConnectionContextImpl();
    }

    public Builder(InputConnectionContextImpl connection) {
      this.connection = connection;
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
    public Builder setPort(InputPortContext port) {
      Args.checkNotNull(port, "port cannot be null");
      connection.port = port;
      return this;
    }

    @Override
    public InputConnectionContext build() {
      return connection;
    }
  }

}
