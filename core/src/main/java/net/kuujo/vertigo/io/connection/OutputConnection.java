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
package net.kuujo.vertigo.io.connection;

import net.kuujo.vertigo.io.Output;
import net.kuujo.vertigo.io.OutputGroupSupport;

import org.vertx.java.core.Handler;

/**
 * Output connection.<p>
 *
 * Output connections represent a direct connection between two instances
 * of separate components. Each output connection points to a single
 * input connection which receives messages from this connection and this
 * connection only.<p>
 *
 * When the connection is opened, it will attempt to connect to the
 * corresponding input connection (the other side of the connection) by
 * periodically sending <code>connect</code> messages to the input connection.
 * Once the input connection replies to the output connection, the connection
 * is opened.<p>
 *
 * The output connection sends messages for the connection and on behalf
 * of any groups created on the connection. Each message that is sent
 * by the output connection is tagged with a monotonically increasing
 * number. The connection listens for messages indicating that a message
 * was received out of order. If a message was received out of order, the
 * output connection will begin resending messages from the last known
 * correct message.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public interface OutputConnection extends Connection<OutputConnection>, Output<OutputConnection>, OutputGroupSupport<OutputConnection> {

  /**
   * Creates an output batch on the connection.
   *
   * @param id The unique batch ID.
   * @param handler A handler to be called once the batch is started.
   * @return The output connection.
   */
  OutputConnection batch(String id, Handler<ConnectionOutputBatch> handler);

}
