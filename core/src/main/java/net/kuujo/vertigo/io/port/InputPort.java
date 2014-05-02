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

import net.kuujo.vertigo.io.Input;
import net.kuujo.vertigo.io.InputBatchSupport;
import net.kuujo.vertigo.io.InputGroupSupport;

/**
 * Input port on which messages are received.<p>
 *
 * The input port can contain any number of {@link net.kuujo.vertigo.io.connection.InputConnection}
 * on which it receives input messages. Connections are constructed based
 * on current network configuration information. When the network configuration
 * is updated, the port will automatically update its internal connections.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public interface InputPort extends Port<InputPort>, Input<InputPort>, InputGroupSupport<InputPort>, InputBatchSupport<InputPort> {
}
