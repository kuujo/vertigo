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

import net.kuujo.vertigo.context.InputStreamContext;
import net.kuujo.vertigo.hooks.InputHook;
import net.kuujo.vertigo.message.JsonMessage;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;

/**
 * An input stream.
 *
 * @author Jordan Halterman
 */
public interface InputStream {

  /**
   * Returns the stream name.
   *
   * @return The stream name.
   */
  String name();

  /**
   * Returns the input stream context.
   *
   * @return The input stream context.
   */
  InputStreamContext context();

  /**
   * Adds an input hook to the stream.
   *
   * @param hook An input hook.
   * @return The input stream.
   */
  InputStream addHook(InputHook hook);

  /**
   * Registers a message handler on the stream.
   *
   * @param handler A message handler.
   * @return The input stream.
   */
  InputStream messageHandler(Handler<JsonMessage> handler);

  /**
   * Acknowledges a message.
   * 
   * @param message The message to ack.
   * @return The input stream.
   */
  InputStream ack(JsonMessage message);

  /**
   * Fails a message.
   * 
   * @param message The message to fail.
   * @return The input stream.
   */
  InputStream fail(JsonMessage message);

  /**
   * Opens the input stream.
   *
   * @return The input stream.
   */
  InputStream open();

  /**
   * Opens the input stream.
   *
   * @param doneHandler An asynchronous handler to be called once opened.
   * @return The input stream.
   */
  InputStream open(Handler<AsyncResult<Void>> doneHandler);

  /**
   * Closes the input stream.
   */
  void close();

  /**
   * Closes the input stream.
   *
   * @param doneHandler An asynchronous handler to be called once closed.
   */
  void close(Handler<AsyncResult<Void>> doneHandler);

}
