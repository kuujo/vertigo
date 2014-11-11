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
package net.kuujo.vertigo.io.port;

import io.vertx.codegen.annotations.VertxGen;
import net.kuujo.vertigo.io.InputContext;
import net.kuujo.vertigo.io.connection.InputConnectionContext;
import net.kuujo.vertigo.io.port.impl.InputPortContextImpl;

/**
 * Input port context represents a set of input connections for a single
 * port within a single partition of a component.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
@VertxGen
public interface InputPortContext extends PortContext<InputPortContext, InputConnectionContext> {

  /**
   * Returns a new input port context builder.
   *
   * @return A new input port context builder.
   */
  static Builder builder() {
    return new InputPortContextImpl.Builder();
  }

  /**
   * Returns a new input port context builder.
   *
   * @param port An existing input port context builder to wrap.
   * @return An input port context builder wrapper.
   */
  static Builder builder(InputPortContext port) {
    return new InputPortContextImpl.Builder((InputPortContextImpl) port);
  }

  /**
   * Returns the parent input context.
   *
   * @return The parent input context.
   */
  InputContext input();

  /**
   * Input port context builder.
   */
  public static interface Builder extends PortContext.Builder<Builder, InputPortContext, InputConnectionContext> {

    /**
     * Sets the parent input.
     *
     * @param input The parent input.
     * @return The input port builder.
     */
    Builder setInput(InputContext input);

  }

}
