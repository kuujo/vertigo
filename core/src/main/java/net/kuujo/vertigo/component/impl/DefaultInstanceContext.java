/*
 * Copyright 2013-2014 the original author or authors.
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
package net.kuujo.vertigo.component.impl;

import net.kuujo.vertigo.component.ComponentContext;
import net.kuujo.vertigo.component.InstanceContext;
import net.kuujo.vertigo.impl.BaseContext;
import net.kuujo.vertigo.io.InputContext;
import net.kuujo.vertigo.io.OutputContext;
import net.kuujo.vertigo.io.impl.DefaultInputContext;
import net.kuujo.vertigo.io.impl.DefaultOutputContext;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * A component instance context which contains information regarding a specific component
 * (module or verticle) instance within a network.
 * 
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public final class DefaultInstanceContext extends BaseContext<InstanceContext> implements InstanceContext {
  private int number;
  private String status;
  private DefaultInputContext input;
  private DefaultOutputContext output;
  @JsonIgnore
  private ComponentContext<?> component;

  private DefaultInstanceContext() {
  }

  /**
   * Sets the instance parent.
   */
  DefaultInstanceContext setComponentContext(DefaultComponentContext<?> component) {
    this.component = component;
    return this;
  }

  @Override
  public int number() {
    return number;
  }

  @Override
  public String address() {
    return address;
  }

  @Override
  public String status() {
    return status;
  }

  @Override
  public InputContext input() {
    return input.setInstanceContext(this);
  }

  @Override
  public OutputContext output() {
    return output.setInstanceContext(this);
  }

  @Override
  @SuppressWarnings({"unchecked", "rawtypes"})
  public <T extends ComponentContext> T component() {
    return (T) component;
  }

  @Override
  public void notify(InstanceContext update) {
    if (update == null) {
      input.notify(null);
      output.notify(null);
    } else {
      input.notify(update.input());
      output.notify(update.output());
    }
    super.notify(this);
  }

  @Override
  public String uri() {
    return String.format("%s://%s/%s/%d", component.network().cluster(), component.network().name(), component.name(), number);
  }

  @Override
  public String toString() {
    return address();
  }

  /**
   * Instance context builder.
   *
   * @author Jordan Halterman
   */
  public static class Builder extends BaseContext.Builder<Builder, DefaultInstanceContext> {

    private Builder() {
      super(new DefaultInstanceContext());
    }

    private Builder(DefaultInstanceContext context) {
      super(context);
    }

    /**
     * Creates a new context builder.
     *
     * @return A new instance context builder.
     */
    public static Builder newBuilder() {
      return new Builder();
    }

    /**
     * Creates a new context builder.
     *
     * @param context A starting instance context.
     * @return A new instance context builder.
     */
    public static Builder newBuilder(InstanceContext context) {
      if (context instanceof DefaultInstanceContext) {
        return new Builder((DefaultInstanceContext) context);
      } else {
        return new Builder().setAddress(context.address())
            .setStatusAddress(context.status())
            .setNumber(context.number())
            .setInput(context.input())
            .setOutput(context.output());
      }
    }

    /**
     * Sets the instance status address.
     *
     * @param address The instance status address.
     * @return The context builder.
     */
    public Builder setStatusAddress(String address) {
      context.status = address;
      return this;
    }

    /**
     * Sets the instance number.
     *
     * @param number The instance number which should be unique to the component.
     * @return The context builder.
     */
    public Builder setNumber(int number) {
      context.number = number;
      return this;
    }

    /**
     * Sets the instance input context.
     *
     * @param input An input context.
     * @return The context builder.
     */
    public Builder setInput(InputContext input) {
      context.input = DefaultInputContext.Builder.newBuilder(input).build().setInstanceContext(context);
      return this;
    }

    /**
     * Sets the instance output context.
     *
     * @param output An output context.
     * @return The context builder.
     */
    public Builder setOutput(OutputContext output) {
      context.output = DefaultOutputContext.Builder.newBuilder(output).build().setInstanceContext(context);
      return this;
    }
  }

}
