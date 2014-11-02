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
import net.kuujo.vertigo.TypeInfo;
import net.kuujo.vertigo.io.OutputInfo;
import net.kuujo.vertigo.io.port.impl.OutputPortInfoImpl;
import net.kuujo.vertigo.io.stream.OutputStreamInfo;

import java.util.Collection;

/**
 * Output port context represents a set of output connections for a single
 * port within a single partition of a component.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
@VertxGen
public interface OutputPortInfo extends PortInfo<OutputPortInfo> {

  /**
   * Returns a new output port info builder.
   *
   * @return A new output port info builder.
   */
  static Builder builder() {
    return new OutputPortInfoImpl.Builder();
  }

  /**
   * Returns a new output port info builder.
   *
   * @param port An existing output port info object to wrap.
   * @return An output port info builder wrapper.
   */
  static Builder builder(OutputPortInfo port) {
    return new OutputPortInfoImpl.Builder((OutputPortInfoImpl) port);
  }

  /**
   * Returns the parent output context.
   *
   * @return The parent output context.
   */
  OutputInfo output();

  /**
   * Returns a collection of port streams.
   *
   * @return A collection of streams in the port.
   */
  Collection<OutputStreamInfo> streams();

  /**
   * Output port info builder.
   */
  public static interface Builder extends TypeInfo.Builder<OutputPortInfo> {

    /**
     * Adds a stream to the output.
     *
     * @param stream The output stream info to add.
     * @return The output port info builder.
     */
    Builder addStream(OutputStreamInfo stream);

    /**
     * Removes a stream from the output.
     *
     * @param stream The output stream info to remove.
     * @return The output port info builder.
     */
    Builder removeStream(OutputStreamInfo stream);

    /**
     * Sets all streams on the output.
     *
     * @param streams A collection of output stream info to add.
     * @return The output port info builder.
     */
    Builder setStreams(OutputStreamInfo... streams);

    /**
     * Sets all streams on the output.
     *
     * @param streams A collection of output stream info to add.
     * @return The output port info builder.
     */
    Builder setStreams(Collection<OutputStreamInfo> streams);

    /**
     * Sets the parent output info.
     *
     * @param output The parent output info.
     * @return The output port info builder.
     */
    Builder setOutput(OutputInfo output);
  }

}
