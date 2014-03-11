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
package net.kuujo.vertigo.input;

import net.kuujo.vertigo.message.JsonMessage;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;

/**
 * Input connection.
 *
 * @author Jordan Halterman
 */
public interface InputConnection {

  /**
   * Returns the connection address.
   *
   * @return The connection address.
   */
  String address();

  /**
   * Registers a message handler on the connection.
   *
   * @param handler A message handler.
   * @return The input connection.
   */
  InputConnection messageHandler(Handler<JsonMessage> handler);

  /**
   * Opens the connection.
   *
   * @return The input connection.
   */
  InputConnection open();

  /**
   * Opens the connection.
   *
   * @param doneHandler An asynchronous handler to be called once opened.
   * @return The input connection.
   */
  InputConnection open(Handler<AsyncResult<Void>> doneHandler);

  /**
   * Closes the connection.
   */
  void close();

  /**
   * Closes the connection.
   *
   * @param doneHandler An asynchronous handler to be called once the connection is closed.
   */
  void close(Handler<AsyncResult<Void>> doneHandler);

}
