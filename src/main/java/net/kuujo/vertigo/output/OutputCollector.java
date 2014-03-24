/*
 * Copyright 2013-2014 the original author or authors.
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

import java.util.Collection;

import net.kuujo.vertigo.context.OutputContext;
import net.kuujo.vertigo.hooks.OutputHook;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;

/**
 * An output collector.
 * <p>
 * 
 * The output collector is the primary interface for emitting new messages from a
 * component. When a new component instance is started, the output collector registers an
 * event bus handler at the component address. This is the address at which other
 * components publish listen requests. When a new listen request is received, the output
 * collector sets up an output stream and any new messages emitted from the
 * component will be sent to the new channel as well.
 * 
 * @author Jordan Halterman
 */
public interface OutputCollector {

  /**
   * Returns the component output context.
   *
   * @return The current component output context.
   */
  OutputContext context();

  /**
   * Adds an output hook to the output collector.
   * 
   * @param hook The hook to add.
   * @return The called output collector instance.
   */
  OutputCollector addHook(OutputHook hook);

  /**
   * Returns a collection of output streams.
   *
   * @return A collection of output streams.
   */
  Collection<OutputStream> streams();

  /**
   * Returns an output stream. The stream will be automatically created if
   * it doesn't already exist.
   *
   * @param name The output stream name.
   * @return An output stream.
   */
  OutputStream stream(String name);

  /**
   * Opens the output.
   * 
   * @return The output instance.
   */
  OutputCollector open();

  /**
   * Opens the output collector.
   * 
   * @param doneHandler An asynchronous handler to be invoked once the collector is
   *          opened.
   * @return The output instance.
   */
  OutputCollector open(Handler<AsyncResult<Void>> doneHandler);

  /**
   * Closes the output collector.
   */
  void close();

  /**
   * Closes the output collector.
   * 
   * @param doneHandler An asynchronous handler to be invoked once the collector is
   *          closed.
   */
  void close(Handler<AsyncResult<Void>> doneHandler);

}
