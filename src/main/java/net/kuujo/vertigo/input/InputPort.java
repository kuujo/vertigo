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

import net.kuujo.vertigo.context.InputPortContext;
import net.kuujo.vertigo.message.JsonMessage;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;

/**
 * An input port.
 *
 * @author Jordan Halterman
 */
public interface InputPort {

  /**
   * Returns the port name.
   *
   * @return The port name.
   */
  String name();

  /**
   * Returns the input port context.
   *
   * @return The input port context.
   */
  InputPortContext context();

  /**
   * Registers a message handler on the port.
   *
   * @param handler A message handler.
   * @return The input port.
   */
  InputPort messageHandler(Handler<JsonMessage> handler);

  /**
   * Acknowledges a message.
   * 
   * @param message The message to ack.
   * @return The input port.
   */
  InputPort ack(JsonMessage message);

  /**
   * Opens the input port.
   *
   * @return The input port.
   */
  InputPort open();

  /**
   * Opens the input port.
   *
   * @param doneHandler An asynchronous handler to be called once opened.
   * @return The input port.
   */
  InputPort open(Handler<AsyncResult<Void>> doneHandler);

  /**
   * Closes the input port.
   */
  void close();

  /**
   * Closes the input port.
   *
   * @param doneHandler An asynchronous handler to be called once closed.
   */
  void close(Handler<AsyncResult<Void>> doneHandler);

}
