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
package net.kuujo.vertigo.io;

import java.util.Collection;

import net.kuujo.vertigo.io.port.OutputPort;

/**
 * Interface for sending messages on output ports.<p>
 *
 * The output collector exposes a simple interface to {@link OutputPort} instances
 * through which messages can be sent.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public interface OutputCollector extends IOCollector<OutputCollector> {

  /**
   * Returns a collection of output ports.
   *
   * @return A collection of output ports.
   */
  Collection<OutputPort> ports();

  /**
   * Returns an output port.<p>
   *
   * If the port doesn't already exist then the input collector will lazily
   * create the port. Ports can be referenced prior to the output event starting
   * up, and once the output starts ports will be properly configured.
   *
   * @param name The name of the port to load.
   * @return An output port.
   */
  OutputPort port(String name);

}
