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

import net.kuujo.vertigo.component.ComponentContext;
import net.kuujo.vertigo.component.PartitionContext;
import net.kuujo.vertigo.impl.BaseContextImpl;
import net.kuujo.vertigo.io.InputContext;
import net.kuujo.vertigo.io.OutputContext;
import net.kuujo.vertigo.util.Args;

/**
 * Instance context implementation.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class PartitionContextImpl extends BaseContextImpl<PartitionContext> implements PartitionContext {
  private int number;
  private InputContext input;
  private OutputContext output;
  private ComponentContext component;

  @Override
  public int number() {
    return number;
  }

  @Override
  public InputContext input() {
    return input;
  }

  @Override
  public OutputContext output() {
    return output;
  }

  @Override
  public ComponentContext component() {
    return component;
  }

  /**
   * Instance context builder.
   */
  public static class Builder implements PartitionContext.Builder {
    private final PartitionContextImpl partition;

    public Builder() {
      partition = new PartitionContextImpl();
    }

    public Builder(PartitionContextImpl partition) {
      this.partition = partition;
    }

    @Override
    public Builder setId(String id) {
      Args.checkNotNull(id, "id cannot be null");
      partition.id = id;
      return this;
    }

    @Override
    public Builder setNumber(int number) {
      Args.checkPositive(number, "partition number must be positive");
      partition.number = number;
      return this;
    }

    @Override
    public Builder setInput(InputContext input) {
      Args.checkNotNull(input, "input cannot be null");
      partition.input = input;
      return this;
    }

    @Override
    public Builder setOutput(OutputContext output) {
      Args.checkNotNull(output, "output cannot be null");
      partition.output = output;
      return this;
    }

    @Override
    public Builder setComponent(ComponentContext component) {
      Args.checkNotNull(component, "component cannot be null");
      partition.component = component;
      return this;
    }

    @Override
    public PartitionContextImpl build() {
      return partition;
    }
  }

}
