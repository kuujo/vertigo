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

import net.kuujo.vertigo.impl.BaseContextImpl;
import net.kuujo.vertigo.io.connection.SourceContext;
import net.kuujo.vertigo.util.Args;

/**
 * Connection source context implementation.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class SourceContextImpl extends BaseContextImpl<SourceContext> implements SourceContext {
  private String component;
  private String port;
  private int partition;

  @Override
  public String component() {
    return component;
  }

  @Override
  public String port() {
    return port;
  }

  @Override
  public int partition() {
    return partition;
  }

  /**
   * Source context builder.
   */
  public static class Builder implements SourceContext.Builder {
    private final SourceContextImpl source;

    public Builder() {
      source = new SourceContextImpl();
    }

    public Builder(SourceContextImpl source) {
      this.source = source;
    }

    @Override
    public SourceContext.Builder setId(String id) {
      Args.checkNotNull(id, "id cannot be null");
      source.id = id;
      return this;
    }

    @Override
    public Builder setComponent(String component) {
      source.component = component;
      return this;
    }

    @Override
    public Builder setPort(String port) {
      source.port = port;
      return this;
    }

    @Override
    public Builder setPartition(int partition) {
      source.partition = partition;
      return this;
    }

    @Override
    public SourceContextImpl build() {
      return source;
    }
  }

}
