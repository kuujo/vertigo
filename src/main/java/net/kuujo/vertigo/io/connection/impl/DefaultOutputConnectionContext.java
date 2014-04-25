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

import net.kuujo.vertigo.hook.OutputHook;
import net.kuujo.vertigo.impl.BaseContext;
import net.kuujo.vertigo.io.connection.OutputConnectionContext;
import net.kuujo.vertigo.io.stream.OutputStreamContext;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Output connection context.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class DefaultOutputConnectionContext extends DefaultConnectionContext<OutputConnectionContext> implements OutputConnectionContext {
  @JsonIgnore
  private OutputStreamContext stream;
  private List<OutputHook> hooks = new ArrayList<>();

  public DefaultOutputConnectionContext setStream(OutputStreamContext stream) {
    this.stream = stream;
    return this;
  }

  @Override
  public String address() {
    return address;
  }

  @Override
  public OutputStreamContext stream() {
    return stream;
  }

  @Override
  public List<OutputHook> hooks() {
    return hooks;
  }

  /**
   * Output connection context builder.
   *
   * @author Jordan Halterman
   */
  public static class Builder extends BaseContext.Builder<Builder, DefaultOutputConnectionContext> {

    private Builder() {
      super(new DefaultOutputConnectionContext());
    }

    private Builder(DefaultOutputConnectionContext context) {
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
    public static Builder newBuilder(DefaultOutputConnectionContext context) {
      return new Builder(context);
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
