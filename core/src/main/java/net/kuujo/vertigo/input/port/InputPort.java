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
package net.kuujo.vertigo.input.port;

import io.vertx.codegen.annotations.VertxGen;
import net.kuujo.vertigo.input.Input;
import net.kuujo.vertigo.util.Closeable;
import net.kuujo.vertigo.util.Openable;

/**
 * Input port on which messages are received.<p>
 *
 * The input port can contain any number of {@link net.kuujo.vertigo.input.connection.InputConnection}
 * on which it receives input messages. Connections are constructed based
 * on current network configuration information. When the network configuration
 * is updated, the port will automatically update its internal connections.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
@VertxGen
public interface InputPort<T> extends Input<InputPort<T>, T>, Openable<InputPort<T>>, Closeable<InputPort<T>> {

  /**
   * Returns the port name.
   *
   * @return The port name.
   */
  String name();

}
