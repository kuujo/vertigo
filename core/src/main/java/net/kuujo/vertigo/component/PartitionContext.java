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
package net.kuujo.vertigo.component;

import io.vertx.codegen.annotations.VertxGen;
import net.kuujo.vertigo.Context;
import net.kuujo.vertigo.component.impl.PartitionContextImpl;
import net.kuujo.vertigo.io.InputContext;
import net.kuujo.vertigo.io.OutputContext;

/**
 * A component partition context which contains information regarding a specific component
 * (module or verticle) partition within a network.<p>
 *
 * The partition context is the primary immutable configuration used by
 * each partition of a component verticle or module to construct its input
 * and output ports/connections. Note that the context is observable, and
 * component partition observe the context for changes within the cluster
 * in order to support runtime network configuration changes by automatically
 * updating their input and output connections when the context changes.
 * 
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
@VertxGen
public interface PartitionContext extends Context<PartitionContext> {

  /**
   * Returns a new partition context builder.
   *
   * @return A new partition context builder.
   */
  static Builder builder() {
    return new PartitionContextImpl.Builder();
  }

  /**
   * Returns a new partition context builder.
   *
   * @param partition An existing partition context object to wrap.
   * @return An partition context builder wrapper.
   */
  static Builder builder(PartitionContext partition) {
    return new PartitionContextImpl.Builder((PartitionContextImpl) partition);
  }

  /**
   * Returns the partition number.
   * 
   * @return The partition number.
   */
  int number();

  /**
   * Returns the partition input context.
   *
   * @return The partition input context.
   */
  InputContext input();

  /**
   * Returns the partition output context.
   *
   * @return The partition output context.
   */
  OutputContext output();

  /**
   * Returns the parent component context.
   * 
   * @return The parent component context.
   */
  ComponentContext component();

  /**
   * Instance context builder.
   */
  public static interface Builder extends Context.Builder<Builder, PartitionContext> {

    /**
     * Sets the partition number.
     *
     * @param number The partition number.
     * @return The partition context builder.
     */
    Builder setNumber(int number);

    /**
     * Sets the partition input context.
     *
     * @param input The partition input context.
     * @return The partition context builder.
     */
    Builder setInput(InputContext input);

    /**
     * Sets the partition output context.
     *
     * @param output The partition output context.
     * @return The partition context builder.
     */
    Builder setOutput(OutputContext output);

    /**
     * Sets the partition component context.
     *
     * @param component The partition component context.
     * @return The partition context builder.
     */
    Builder setComponent(ComponentContext component);
  }

}
