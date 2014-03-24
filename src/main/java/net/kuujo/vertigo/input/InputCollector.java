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
package net.kuujo.vertigo.input;

import java.util.Collection;

import net.kuujo.vertigo.context.InputContext;
import net.kuujo.vertigo.hooks.InputHook;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;

/**
 * An input collector.
 * <p>
 * 
 * The input collector is the primary interface for receiving input within a component
 * instance. Input collectors are essentially wrappers around multiple {@link Listener}
 * instances. With each component being able to listen to output from multiple addresses,
 * the input collector joins data from each of those sources with a single interface.
 * 
 * @author Jordan Halterman
 */
public interface InputCollector {

  /**
   * Returns the component input context.
   *
   * @return The input context.
   */
  InputContext context();

  /**
   * Adds an input hook to the input collector.
   * 
   * @param hook An input hook.
   * @return The called input collector instance.
   */
  InputCollector addHook(InputHook hook);

  /**
   * Returns a collection of input streams.
   *
   * @return A collection of input streams.
   */
  Collection<InputPort> streams();

  /**
   * Returns an input stream. The stream will be automatically created if it doesn't
   * already exist.
   *
   * @param name The name of the stream to get.
   * @return The input stream.
   */
  InputPort stream(String name);

  /**
   * Opens the input collector.
   * 
   * @return The input collector instance.
   */
  InputCollector open();

  /**
   * Opens the input collector.
   * 
   * @param doneHandler An asynchronous handler to be invoked once the collector is
   *          opened.
   * @return The input collector instance.
   */
  InputCollector open(Handler<AsyncResult<Void>> doneHandler);

  /**
   * Closes the input collector.
   */
  void close();

  /**
   * Closes the input collector.
   * 
   * @param doneHandler An asynchronous handler to be invoked once the collector is
   *          closed.
   */
  void close(Handler<AsyncResult<Void>> doneHandler);

}
