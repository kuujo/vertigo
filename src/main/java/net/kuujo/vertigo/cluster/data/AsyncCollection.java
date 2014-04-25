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
 * Asynchronous collection.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 *
 * @param <T> The collection data type.
 */
public interface AsyncCollection<T> {

  /**
   * Returns the collection name.
   *
   * @return The collection name.
   */
  String name();

  /**
   * Adds a value to the collection.
   *
   * @param value The value to add.
   */
  void add(T value);

  /**
   * Adds a value to the collection.
   *
   * @param value The value to add.
   * @param doneHandler An asynchronous handler to be called once complete.
   */
  void add(T value, Handler<AsyncResult<Boolean>> doneHandler);

  /**
   * Removes a value from the collection.
   *
   * @param value The value to remove.
   */
  void remove(T value);

  /**
   * Removes a value from the collection.
   *
   * @param value The value to remove.
   * @param doneHandler An asynchronous handler to be called once complete.
   */
  void remove(T value, Handler<AsyncResult<Boolean>> doneHandler);

  /**
   * Checks whether the collection contains a value.
   *
   * @param value The value to check.
   * @param resultHandler An asynchronous handler to be called with the result indicating
   *                      whether the collection contains the given value.
   */
  void contains(Object value, Handler<AsyncResult<Boolean>> resultHandler);

  /**
   * Gets the current collection size.
   *
   * @param resultHandler An asynchronous handler to be called with the collection size.
   */
  void size(Handler<AsyncResult<Integer>> resultHandler);

  /**
   * Checks whether the collection is empty.
   *
   * @param resultHandler An asynchronous handler to be called with the result indicating
   *                      whether the collection is empty.
   */
  void isEmpty(Handler<AsyncResult<Boolean>> resultHandler);

  /**
   * Clears all values from the collection.
   */
  void clear();

  /**
   * Clears all values from the collection.
   *
   * @param doneHandler An asynchronous handler to be called once the collection has been cleared.
   */
  void clear(Handler<AsyncResult<Void>> doneHandler);

}
