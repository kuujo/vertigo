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
package net.kuujo.vertigo.component.impl;

import net.kuujo.vertigo.TypeInfo;
import net.kuujo.vertigo.component.ComponentInfo;
import net.kuujo.vertigo.component.InstanceInfo;
import net.kuujo.vertigo.impl.BaseTypeInfoImpl;
import net.kuujo.vertigo.io.InputInfo;
import net.kuujo.vertigo.io.OutputInfo;
import net.kuujo.vertigo.io.impl.InputInfoImpl;
import net.kuujo.vertigo.util.Args;

/**
 * Instance info implementation.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class InstanceInfoImpl extends BaseTypeInfoImpl<InstanceInfo> implements InstanceInfo {
  private int number;
  private InputInfo input;
  private OutputInfo output;
  private ComponentInfo component;

  @Override
  public int number() {
    return number;
  }

  @Override
  public InputInfo input() {
    return input;
  }

  @Override
  public OutputInfo output() {
    return output;
  }

  @Override
  public ComponentInfo component() {
    return component;
  }

  /**
   * Instance info builder.
   */
  public static class Builder implements TypeInfo.Builder<InstanceInfo> {
    private final InstanceInfoImpl instance;

    public Builder() {
      instance = new InstanceInfoImpl();
    }

    public Builder(InstanceInfoImpl instance) {
      this.instance = instance;
    }

    /**
     * Sets the instance number.
     *
     * @param number The instance number.
     * @return The instance info builder.
     */
    public Builder setNumber(int number) {
      Args.checkPositive(number, "instance number must be positive");
      instance.number = number;
      return this;
    }

    /**
     * Sets the instance input info.
     *
     * @param input The instance input info.
     * @return The instance info builder.
     */
    public Builder setInput(InputInfo input) {
      Args.checkNotNull(input, "input cannot be null");
      instance.input = input;
      return this;
    }

    /**
     * Sets the instance output info.
     *
     * @param output The instance output info.
     * @return The instance info builder.
     */
    public Builder setOutput(OutputInfo output) {
      Args.checkNotNull(output, "output cannot be null");
      instance.output = output;
      return this;
    }

    /**
     * Sets the instance component info.
     *
     * @param component The instance component info.
     * @return The instance info builder.
     */
    public Builder setComponent(ComponentInfo component) {
      Args.checkNotNull(component, "component cannot be null");
      instance.component = component;
      return this;
    }

    @Override
    public InstanceInfoImpl build() {
      return instance;
    }
  }

}
