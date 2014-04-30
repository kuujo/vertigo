/*
 * Copyright 2013-2014 the original author or authors.
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

import net.kuujo.vertigo.io.port.InputPort;

/**
 * Interface for receiving messages on input ports.<p>
 * 
 * The input collector is the primary interface for receiving input within a component
 * instance. Input collectors are simple wrappers around {@link InputPort} instances.
 * 
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public interface InputCollector extends IOCollector<InputCollector> {

  /**
   * Returns a collection of input ports.
   *
   * @return A collection of input ports.
   */
  Collection<InputPort> ports();

  /**
   * Returns an input port.<p>
   *
   * If the port doesn't already exist then the input collector will lazily
   * create the port. Ports can be referenced prior to the input event starting
   * up, and once the input starts ports will be properly configured.
   *
   * @param name The name of the port to load.
   * @return The input port.
   */
  InputPort port(String name);

}
