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

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;

/**
 * Input buffer interface.<p>
 *
 * This interface exposes an API intentionally similar to that of the
 * Vert.x {@link org.vertx.java.core.streams.ReadStream} interface. Users
 * can use the interface to control the flow of messages into the input.
 *
 * @author Jordan Halterman
 *
 * @param <T> The input type.
 */
public interface InputBuffer<T extends InputBuffer<T>> extends Input<T> {

  /**
   * Opens the input.
   *
   * @return The input.
   */
  T open();

  /**
   * Opens the input.
   *
   * @param doneHandler An asynchronous handler to be called once complete.
   * @return The input.
   */
  T open(Handler<AsyncResult<Void>> doneHandler);

  /**
   * Pauses the input.
   *
   * @return The input.
   */
  T pause();

  /**
   * Resumes the input.
   *
   * @return The input.
   */
  T resume();

  /**
   * Closes the input.
   */
  void close();

  /**
   * Closes the input.
   *
   * @param doneHandler An asynchronous handler to be called once complete.
   */
  void close(Handler<AsyncResult<Void>> doneHandler);

}
