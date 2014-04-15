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
package net.kuujo.vertigo.context.impl;

import net.kuujo.vertigo.context.InputConnectionContext;
import net.kuujo.vertigo.context.InputPortContext;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Input connection context.
 *
 * @author Jordan Halterman
 */
public class DefaultInputConnectionContext extends DefaultConnectionContext<InputConnectionContext> implements InputConnectionContext {
  @JsonIgnore
  private InputPortContext port;

  DefaultInputConnectionContext setPort(InputPortContext port) {
    this.port = port;
    return this;
  }

  @Override
  public String address() {
    return address;
  }

  @Override
  public InputPortContext port() {
    return port;
  }

  /**
   * Input connection context builder.
   *
   * @author Jordan Halterman
   */
  public static class Builder extends BaseContext.Builder<Builder, DefaultInputConnectionContext> {

    private Builder() {
      super(new DefaultInputConnectionContext());
    }

    private Builder(DefaultInputConnectionContext context) {
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
    public static Builder newBuilder(DefaultInputConnectionContext context) {
      return new Builder(context);
    }
  }

}
