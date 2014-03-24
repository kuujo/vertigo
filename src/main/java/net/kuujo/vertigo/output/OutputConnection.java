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

import java.util.List;

import net.kuujo.vertigo.context.OutputConnectionContext;
import net.kuujo.vertigo.message.JsonMessage;
import net.kuujo.vertigo.message.MessageId;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;

/**
 * A component-to-component output connection.
 * 
 * @author Jordan Halterman
 */
public interface OutputConnection {

  /**
   * Returns the output connection context.
   *
   * @return The output connection context.
   */
  OutputConnectionContext context();

  /**
   * Returns the remote connection ports.
   * 
   * @return The remote connection ports.
   */
  List<String> ports();

  /**
   * Sends a message on the connection.
   * 
   * @param message The message to send on the connection.
   * @return The sent message identifier.
   */
  List<MessageId> send(JsonMessage message);

  /**
   * Opens the connection.
   *
   * @return The output connection.
   */
  OutputConnection open();

  /**
   * Opens the connection.
   *
   * @param doneHandler An asynchronous handler to be called once the connection has
   *                    been opened.
   * @return The output connection.
   */
  OutputConnection open(Handler<AsyncResult<Void>> doneHandler);

  /**
   * Closes the connection.
   */
  void close();

  /**
   * Closes the connection.
   *
   * @param doneHandler An asynchronous handler to be called once the connection has
   *                    been closed.
   */
  void close(Handler<AsyncResult<Void>> doneHandler);

}
