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
package net.kuujo.vertigo.output;

import java.util.Collection;

import net.kuujo.vertigo.context.OutputContext;
import net.kuujo.vertigo.util.Closeable;
import net.kuujo.vertigo.util.Openable;

/**
 * Output collector.<p>
 *
 * The output collector exposes a simple interface to {@link OutputPort} instances
 * through which messages can be sent.
 *
 * @author Jordan Halterman
 */
public interface OutputCollector extends Openable<OutputCollector>, Closeable<OutputCollector> {

  /**
   * Returns the component output context.
   *
   * @return The current component output context.
   */
  OutputContext context();

  /**
   * Returns a collection of output ports.
   *
   * @return A collection of output ports.
   */
  Collection<OutputPort> ports();

  /**
   * Returns an output port. The port will be automatically created if
   * it doesn't already exist.
   *
   * @param name The output port name.
   * @return An output port.
   */
  OutputPort port(String name);

}
