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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.kuujo.vertigo.hook.InputHook;
import net.kuujo.vertigo.impl.BaseContext;
import net.kuujo.vertigo.io.connection.InputConnectionContext;
import net.kuujo.vertigo.io.port.InputPortContext;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Input connection context.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class DefaultInputConnectionContext extends DefaultConnectionContext<InputConnectionContext> implements InputConnectionContext {
  @JsonIgnore
  private InputPortContext port;
  private List<InputHook> hooks = new ArrayList<>();

  public DefaultInputConnectionContext setPort(InputPortContext port) {
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

  @Override
  public List<InputHook> hooks() {
    return hooks;
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
