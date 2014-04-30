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

import net.kuujo.vertigo.io.Closeable;
import net.kuujo.vertigo.io.Openable;

/**
 * Input/output port.<p>
 *
 * Ports represent abstract named interfaces between components. Rather than
 * communicating via direct event bus messaging, components send messages to
 * output ports and receive messages on input ports. Ports do not have to be
 * identically named between components in order to send and receive messages
 * to one another. Instead, connections between ports on different components
 * are defined in network configurations.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public interface Port<T extends Port<T>> extends Openable<T>, Closeable<T> {

  /**
   * Returns the port name.
   *
   * @return The port name.
   */
  String name();

}
