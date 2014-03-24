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
package net.kuujo.vertigo.context;

import java.util.Arrays;
import java.util.List;

import net.kuujo.vertigo.input.grouping.Grouping;

/**
 * Output connection context.
 *
 * @author Jordan Halterman
 */
public class OutputConnectionContext extends ConnectionContext<OutputConnectionContext> {
  private String address;
  private List<String> targets;
  private Grouping grouping;

  @Override
  public String address() {
    return address;
  }

  /**
   * Returns a list of output addresses.
   *
   * @return A list of output addresses.
   */
  public List<String> targets() {
    return targets;
  }

  /**
   * Returns the output connection grouping.
   *
   * @return The output connection grouping.
   */
  public Grouping grouping() {
    return grouping;
  }

  /**
   * Output connection context builder.
   *
   * @author Jordan Halterman
   */
  public static class Builder extends net.kuujo.vertigo.context.Context.Builder<OutputConnectionContext> {

    private Builder() {
      super(new OutputConnectionContext());
    }

    private Builder(OutputConnectionContext context) {
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
    public static Builder newBuilder(OutputConnectionContext context) {
      return new Builder(context);
    }

    /**
     * Sets the stream address.
     *
     * @param address The stream address.
     * @return The context builder.
     */
    public Builder setAddress(String address) {
      context.address = address;
      return this;
    }

    /**
     * Sets the connection targets.
     *
     * @param targets The connection targets.
     * @return The context builder.
     */
    public Builder setTargets(String... targets) {
      context.targets = Arrays.asList(targets);
      return this;
    }

    /**
     * Sets the connection targets.
     *
     * @param targets The connection targets.
     * @return The context builder.
     */
    public Builder setTargets(List<String> targets) {
      context.targets = targets;
      return this;
    }

    /**
     * Adds a target to the component.
     *
     * @param target The target to add.
     * @return The context builder.
     */
    public Builder addTarget(String target) {
      context.targets.add(target);
      return this;
    }

    /**
     * Removes a target from the component.
     *
     * @param target The target to remove.
     * @return The context builder.
     */
    public Builder removeTarget(String target) {
      context.targets.remove(target);
      return this;
    }

    /**
     * Sets the connection grouping.
     *
     * @param grouping The connection grouping.
     * @return The context builder.
     */
    public Builder setGrouping(Grouping grouping) {
      context.grouping = grouping;
      return this;
    }
  }

}
