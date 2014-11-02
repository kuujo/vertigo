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
import net.kuujo.vertigo.TypeInfo;
import net.kuujo.vertigo.component.impl.PartitionInfoImpl;
import net.kuujo.vertigo.input.InputInfo;
import net.kuujo.vertigo.output.OutputInfo;

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
public interface PartitionInfo extends TypeInfo<PartitionInfo> {

  /**
   * Returns a new partition info builder.
   *
   * @return A new partition info builder.
   */
  static Builder builder() {
    return new PartitionInfoImpl.Builder();
  }

  /**
   * Returns a new partition info builder.
   *
   * @param partition An existing partition info object to wrap.
   * @return An partition info builder wrapper.
   */
  static Builder builder(PartitionInfo partition) {
    return new PartitionInfoImpl.Builder((PartitionInfoImpl) partition);
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
  InputInfo input();

  /**
   * Returns the partition output context.
   *
   * @return The partition output context.
   */
  OutputInfo output();

  /**
   * Returns the parent component context.
   * 
   * @return The parent component context.
   */
  ComponentInfo component();

  /**
   * Instance info builder.
   */
  public static interface Builder extends TypeInfo.Builder<PartitionInfo> {

    /**
     * Sets the partition number.
     *
     * @param number The partition number.
     * @return The partition info builder.
     */
    Builder setNumber(int number);

    /**
     * Sets the partition input info.
     *
     * @param input The partition input info.
     * @return The partition info builder.
     */
    Builder setInput(InputInfo input);

    /**
     * Sets the partition output info.
     *
     * @param output The partition output info.
     * @return The partition info builder.
     */
    Builder setOutput(OutputInfo output);

    /**
     * Sets the partition component info.
     *
     * @param component The partition component info.
     * @return The partition info builder.
     */
    Builder setComponent(ComponentInfo component);
  }

}
