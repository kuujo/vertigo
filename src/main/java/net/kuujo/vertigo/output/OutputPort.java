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
import net.kuujo.vertigo.message.JsonMessage;

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
   * Sets the output queue max size.
   *
   * @param maxSize The output queue max size.
   * @return The output port.
   */
  OutputPort setSendQueueMaxSize(int maxSize);

  /**
   * Returns a boolean indicating whether the send queue is full.
   *
   * @return Indicates whether the send queue is full.
   */
  boolean sendQueueFull();

  /**
   * Sets a handler to be called when the port is prepared to accept new messages.
   *
   * @param handler A drain handler.
   * @return The output port.
   */
  OutputPort drainHandler(Handler<Void> handler);

  /**
   * Sends a message to the output port.
   *
   * @param body The body of the message to send.
   * @return The emitted message identifier.
   */
  String send(JsonObject body);

  /**
   * Sends a message to the output port.
   *
   * @param body The body of the message to send.
   * @param doneHandler An asynchronous handler to be called once the message has been sent.
   * @return The sent message identifier.
   */
  String send(JsonObject body, Handler<AsyncResult<Void>> doneHandler);

  /**
   * Sends a child message to the output port.
   * 
   * Sending data as the child of an existing message creates a new node in the parent
   * message's message tree. When the new message is sent, the auditor assigned to the
   * parent message will be notified of the change, and the new message will be tracked as
   * a child. This means that the parent message will not be considered fully processed
   * until all of its children have been acked and are considered fully processed (their
   * children are acked... etc). It is strongly recommended that users use this API
   * whenever possible.
   *
   * @param body The body of the message to send.
   * @param parent The parent of the message being sent.
   * @return The sent message identifier.
   */
  String send(JsonObject body, JsonMessage parent);

  /**
   * Sends a child message to the output port.
   * 
   * Sending data as the child of an existing message creates a new node in the parent
   * message's message tree. When the new message is emitted, the auditor assigned to the
   * parent message will be notified of the change, and the new message will be tracked as
   * a child. This means that the parent message will not be considered fully processed
   * until all of its children have been acked and are considered fully processed (their
   * children are acked... etc). It is strongly recommended that users use this API
   * whenever possible.
   *
   * @param body The body of the message to send.
   * @param parent The parent of the message being sent.
   * @param doneHandler An asynchronous handler to be called once the message has been sent.
   * @return The sent message identifier.
   */
  String send(JsonObject body, JsonMessage parent, Handler<AsyncResult<Void>> doneHandler);

  /**
   * Sends a message to the output port.
   *
   * @param message The message to send.
   * @return The sent message identifier.
   */
  String send(JsonMessage message);

  /**
   * Sends a message to the output port.
   *
   * @param message The message to send.
   * @param doneHandler An asynchronous handler to be called once the message has been sent.
   * @return The sent message identifier.
   */
  String send(JsonMessage message, Handler<AsyncResult<Void>> doneHandler);

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
