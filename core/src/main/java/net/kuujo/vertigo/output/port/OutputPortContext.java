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
   * Returns a new output port info builder.
   *
   * @return A new output port info builder.
   */
  static Builder builder() {
    return new OutputPortContextImpl.Builder();
  }

  /**
   * Returns a new output port info builder.
   *
   * @param port An existing output port info object to wrap.
   * @return An output port info builder wrapper.
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
   * Output port info builder.
   */
  public static interface Builder extends Context.Builder<OutputPortContext> {

    /**
     * Adds a stream to the output.
     *
     * @param stream The output stream info to add.
     * @return The output port info builder.
     */
    Builder addStream(OutputStreamContext stream);

    /**
     * Removes a stream from the output.
     *
     * @param stream The output stream info to remove.
     * @return The output port info builder.
     */
    Builder removeStream(OutputStreamContext stream);

    /**
     * Sets all streams on the output.
     *
     * @param streams A collection of output stream info to add.
     * @return The output port info builder.
     */
    Builder setStreams(OutputStreamContext... streams);

    /**
     * Sets all streams on the output.
     *
     * @param streams A collection of output stream info to add.
     * @return The output port info builder.
     */
    Builder setStreams(Collection<OutputStreamContext> streams);

    /**
     * Sets the parent output info.
     *
     * @param output The parent output info.
     * @return The output port info builder.
     */
    Builder setOutput(OutputContext output);
  }

}
