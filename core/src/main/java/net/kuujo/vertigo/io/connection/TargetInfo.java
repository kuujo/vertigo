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
package net.kuujo.vertigo.io.connection;

import io.vertx.codegen.annotations.VertxGen;
import net.kuujo.vertigo.TypeInfo;
import net.kuujo.vertigo.io.connection.impl.TargetInfoImpl;

/**
 * Connection target info.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
@VertxGen
public interface TargetInfo extends EndpointInfo<TargetInfo> {

  /**
   * Returns a new target info builder.
   *
   * @return A new target info builder.
   */
  static Builder builder() {
    return new TargetInfoImpl.Builder();
  }

  /**
   * Returns a new target info builder.
   *
   * @param target An existing target info object.
   * @return A target info builder wrapper.
   */
  static Builder builder(TargetInfo target) {
    return new TargetInfoImpl.Builder((TargetInfoImpl) target);
  }

  /**
   * Target info builder.
   */
  public static interface Builder extends TypeInfo.Builder<TargetInfo> {

    /**
     * Sets the target component.
     *
     * @param component The target component name.
     * @return The target info builder.
     */
    Builder setComponent(String component);

    /**
     * Sets the target port.
     *
     * @param port The target port name.
     * @return The target info builder.
     */
    Builder setPort(String port);

    /**
     * Sets the target partition.
     *
     * @param partition The target partition number.
     * @return The target info builder.
     */
    Builder setPartition(int partition);
  }

}
