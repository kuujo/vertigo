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

import net.kuujo.vertigo.component.ComponentInfo;
import net.kuujo.vertigo.component.PartitionInfo;
import net.kuujo.vertigo.impl.BaseTypeInfoImpl;
import net.kuujo.vertigo.io.InputInfo;
import net.kuujo.vertigo.io.OutputInfo;
import net.kuujo.vertigo.util.Args;

/**
 * Instance info implementation.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class PartitionInfoImpl extends BaseTypeInfoImpl<PartitionInfo> implements PartitionInfo {
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
  public static class Builder implements PartitionInfo.Builder {
    private final PartitionInfoImpl instance;

    public Builder() {
      instance = new PartitionInfoImpl();
    }

    public Builder(PartitionInfoImpl instance) {
      this.instance = instance;
    }

    @Override
    public Builder setNumber(int number) {
      Args.checkPositive(number, "partition number must be positive");
      instance.number = number;
      return this;
    }

    @Override
    public Builder setInput(InputInfo input) {
      Args.checkNotNull(input, "input cannot be null");
      instance.input = input;
      return this;
    }

    @Override
    public Builder setOutput(OutputInfo output) {
      Args.checkNotNull(output, "output cannot be null");
      instance.output = output;
      return this;
    }

    @Override
    public Builder setComponent(ComponentInfo component) {
      Args.checkNotNull(component, "component cannot be null");
      instance.component = component;
      return this;
    }

    @Override
    public PartitionInfoImpl build() {
      return instance;
    }
  }

}
