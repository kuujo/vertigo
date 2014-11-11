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
import net.kuujo.vertigo.io.OutputContext;
import net.kuujo.vertigo.io.connection.OutputConnectionContext;
import net.kuujo.vertigo.io.port.impl.OutputPortContextImpl;

/**
 * Output port context represents a set of output connections for a single
 * port within a single partition of a component.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
@VertxGen
public interface OutputPortContext extends PortContext<OutputPortContext, OutputConnectionContext> {

  /**
   * Returns a new output port context builder.
   *
   * @return A new output port context builder.
   */
  static Builder builder() {
    return new OutputPortContextImpl.Builder();
  }

  /**
   * Returns a new output port context builder.
   *
   * @param port An existing output port context object to wrap.
   * @return An output port context builder wrapper.
   */
  static Builder builder(OutputPortContext port) {
    return new OutputPortContextImpl.Builder((OutputPortContextImpl) port);
  }

  /**
   * Returns the parent output context.
   *
   * @return The parent output context.
   */
  OutputContext output();

  /**
   * Output port context builder.
   */
  public static interface Builder extends PortContext.Builder<Builder, OutputPortContext, OutputConnectionContext> {

    /**
     * Sets the parent output context.
     *
     * @param output The parent output context.
     * @return The output port context builder.
     */
    Builder setOutput(OutputContext output);

  }

}
