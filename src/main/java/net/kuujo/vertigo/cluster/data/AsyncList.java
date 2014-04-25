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
package net.kuujo.vertigo.cluster.data;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;

/**
 * Asynchronous list.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 *
 * @param <T> The list data type.
 */
public interface AsyncList<T> extends AsyncCollection<T> {

  /**
   * Gets a value at a specific index in the list.
   *
   * @param index The index of the value to get.
   * @param resultHandler An asynchronous handler to be called with the value result.
   */
  void get(int index, Handler<AsyncResult<T>> resultHandler);

  /**
   * Sets an index in the list.
   *
   * @param index The index to set.
   * @param value The value to set.
   */
  void set(int index, T value);

  /**
   * Sets an index in the list.
   *
   * @param index The index to set.
   * @param value The value to set.
   * @param doneHandler An asynchronous handler to be called once complete.
   */
  void set(int index, T value, Handler<AsyncResult<Void>> doneHandler);

  /**
   * Removes an index in the list.
   *
   * @param index The index to remove.
   */
  void remove(int index);

  /**
   * Removes an index in the list.
   *
   * @param index The index to remove.
   * @param doneHandler An asynchronous handler to be called once complete. The handler
   *                    result will be the value that was removed from the list.
   */
  void remove(int index, Handler<AsyncResult<T>> doneHandler);

}
