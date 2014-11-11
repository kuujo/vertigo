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
import net.kuujo.vertigo.TypeContext;
import net.kuujo.vertigo.io.connection.impl.TargetContextImpl;

/**
 * Connection target context.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
@VertxGen
public interface TargetContext extends EndpointContext<TargetContext> {

  /**
   * Returns a new target context builder.
   *
   * @return A new target context builder.
   */
  static Builder builder() {
    return new TargetContextImpl.Builder();
  }

  /**
   * Returns a new target context builder.
   *
   * @param target An existing target context object.
   * @return A target context builder wrapper.
   */
  static Builder builder(TargetContext target) {
    return new TargetContextImpl.Builder((TargetContextImpl) target);
  }

  /**
   * Target context builder.
   */
  public static interface Builder extends TypeContext.Builder<Builder, TargetContext> {

    /**
     * Sets the target component.
     *
     * @param component The target component name.
     * @return The target context builder.
     */
    Builder setComponent(String component);

    /**
     * Sets the target port.
     *
     * @param port The target port name.
     * @return The target context builder.
     */
    Builder setPort(String port);

  }

}
