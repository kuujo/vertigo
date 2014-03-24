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

import net.kuujo.vertigo.context.OutputStreamContext;
import net.kuujo.vertigo.hooks.OutputHook;
import net.kuujo.vertigo.message.JsonMessage;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonObject;

/**
 * An input stream.
 *
 * @author Jordan Halterman
 */
public interface OutputStream {

  /**
   * Returns the output stream name.
   *
   * @return The output stream name.
   */
  String name();

  /**
   * Returns the output stream context.
   *
   * @return The output stream context.
   */
  OutputStreamContext context();

  /**
   * Adds a hook to the stream.
   *
   * @param hook An output hook.
   * @return The output stream.
   */
  OutputStream addHook(OutputHook hook);

  /**
   * Emits a message to the output stream.
   *
   * @param body The body of the message to emit.
   * @return The emitted message ID.
   */
  String emit(JsonObject body);

  /**
   * Emits a child message to the output stream.
   * 
   * Emitting data as the child of an existing message creates a new node in the parent
   * message's message tree. When the new message is emitted, the auditor assigned to the
   * parent message will be notified of the change, and the new message will be tracked as
   * a child. This means that the parent message will not be considered fully processed
   * until all of its children have been acked and are considered fully processed (their
   * children are acked... etc). It is strongly recommended that users use this API
   * whenever possible.
   *
   * @param body The body of the message to emit.
   * @param parent The parent of the message being emitted.
   * @return The emitted message ID.
   */
  String emit(JsonObject body, JsonMessage parent);

  /**
   * Emits a message to the output stream.
   *
   * @param message The message to emit.
   * @return The emitted message ID.
   */
  String emit(JsonMessage message);

  /**
   * Opens the output stream.
   *
   * @return The output stream.
   */
  OutputStream open();

  /**
   * Opens the output stream.
   *
   * @param doneHandler An asynchronous handler to be called once opened.
   * @return The output stream.
   */
  OutputStream open(Handler<AsyncResult<Void>> doneHandler);

  /**
   * Closes the output stream.
   */
  void close();

  /**
   * Closes the output stream.
   *
   * @param doneHandler An asynchronous handler to be called once closed.
   */
  void close(Handler<AsyncResult<Void>> doneHandler);

}
