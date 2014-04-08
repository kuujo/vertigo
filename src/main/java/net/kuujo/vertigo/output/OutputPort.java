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

import net.kuujo.vertigo.context.OutputPortContext;
import net.kuujo.vertigo.hooks.OutputPortHook;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonObject;

/**
 * An input port.
 *
 * @author Jordan Halterman
 */
public interface OutputPort {

  /**
   * Returns the output port name.
   *
   * @return The output port name.
   */
  String name();

  /**
   * Returns the output port context.
   *
   * @return The output port context.
   */
  OutputPortContext context();

  /**
   * Adds a hook to the port.
   *
   * @param hook The output hook to add.
   * @return The output port.
   */
  OutputPort addHook(OutputPortHook hook);

  /**
   * Sends a message to the output port.
   *
   * @param body The body of the message to send.
   * @return The emitted message identifier.
   */
  String send(JsonObject body);

  /**
   * Opens the output port.
   *
   * @return The output port.
   */
  OutputPort open();

  /**
   * Opens the output port.
   *
   * @param doneHandler An asynchronous handler to be called once opened.
   * @return The output port.
   */
  OutputPort open(Handler<AsyncResult<Void>> doneHandler);

  /**
   * Closes the output port.
   */
  void close();

  /**
   * Closes the output port.
   *
   * @param doneHandler An asynchronous handler to be called once closed.
   */
  void close(Handler<AsyncResult<Void>> doneHandler);

}
