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
package net.kuujo.vertigo.output.port;

import io.vertx.codegen.annotations.VertxGen;
import net.kuujo.vertigo.Context;
import net.kuujo.vertigo.output.port.impl.OutputPortContextImpl;
import net.kuujo.vertigo.output.OutputContext;
import net.kuujo.vertigo.output.stream.OutputStreamContext;

import java.util.Collection;

/**
 * Output port context represents a set of output connections for a single
 * port within a single partition of a component.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
@VertxGen
public interface OutputPortContext extends Context<OutputPortContext> {

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
   * Returns the port name.
   *
   * @return The port name.
   */
  String name();

  /**
   * Returns the parent output context.
   *
   * @return The parent output context.
   */
  OutputContext output();

  /**
   * Returns a collection of port streams.
   *
   * @return A collection of streams in the port.
   */
  Collection<OutputStreamContext> streams();

  /**
   * Output port context builder.
   */
  public static interface Builder extends Context.Builder<Builder, OutputPortContext> {

    /**
     * Sets the port name.
     *
     * @param name The output port name.
     * @return The output port builder.
     */
    Builder setName(String name);

    /**
     * Adds a stream to the output.
     *
     * @param stream The output stream context to add.
     * @return The output port context builder.
     */
    Builder addStream(OutputStreamContext stream);

    /**
     * Removes a stream from the output.
     *
     * @param stream The output stream context to remove.
     * @return The output port context builder.
     */
    Builder removeStream(OutputStreamContext stream);

    /**
     * Sets all streams on the output.
     *
     * @param streams A collection of output stream context to add.
     * @return The output port context builder.
     */
    Builder setStreams(OutputStreamContext... streams);

    /**
     * Sets all streams on the output.
     *
     * @param streams A collection of output stream context to add.
     * @return The output port context builder.
     */
    Builder setStreams(Collection<OutputStreamContext> streams);

    /**
     * Sets the parent output context.
     *
     * @param output The parent output context.
     * @return The output port context builder.
     */
    Builder setOutput(OutputContext output);
  }

}
