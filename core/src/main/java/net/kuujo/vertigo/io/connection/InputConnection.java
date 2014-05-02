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

import net.kuujo.vertigo.io.Input;
import net.kuujo.vertigo.io.InputBatchSupport;
import net.kuujo.vertigo.io.InputGroupSupport;
import net.kuujo.vertigo.io.batch.InputBatch;

import org.vertx.java.core.Handler;

/**
 * Input connection represents the receiving side of a connection
 * between two instances of two components.<p>
 *
 * Messages on input connections must be received in order. Output
 * and input connections keep track of message order by tagging and
 * comparing messages with monotonically increasing unique identifiers.
 * If the input connection receives a message out of order, it will
 * immediately notify the output connection of the last known ordered
 * message, indicating that the output connection should resend messages
 * after the last known correct message.<p>
 *
 * The input connection will periodically send messages to the output
 * connection indicating the last correct message received, allowing the
 * output to clear its queue.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public interface InputConnection extends Connection<InputConnection>, Input<InputConnection>, InputGroupSupport<InputConnection>, InputBatchSupport<InputConnection> {

  /**
   * Registers an input batch handler.
   *
   * @param handler A handler to be called when an input batch is started.
   * @return The input connection.
   */
  InputConnection batchHandler(Handler<InputBatch> handler);

}
