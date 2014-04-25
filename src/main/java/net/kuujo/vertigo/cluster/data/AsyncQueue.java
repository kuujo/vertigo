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
 * Asynchronous queue.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 *
 * @param <T> The queue data type.
 */
public interface AsyncQueue<T> extends AsyncCollection<T> {

  /**
   * Inserts the element into the queue if it's immediately possible to do so.
   *
   * @param value The value to insert.
   */
  void offer(T value);

  /**
   * Inserts the element into the queue if it's immediately possible to do so.
   *
   * @param value The value to insert.
   * @param doneHandler An asynchronous handler to be called once complete.
   */
  void offer(T value, Handler<AsyncResult<Boolean>> doneHandler);

  /**
   * Retrieves but does not remove the head of the queue.
   *
   * @param resultHandler An asynchronous handler to be called with the result.
   */
  void element(Handler<AsyncResult<T>> resultHandler);

  /**
   * Retrieves but does not remove the head of the queue. If there is no value at the
   * head of the queue then the result value will be <code>null</code>.
   *
   * @param resultHandler An asynchronous handler to be called with the result.
   */
  void peek(Handler<AsyncResult<T>> resultHandler);

  /**
   * Retrieves and removed the head of the queue. If there is no value at the
   * head of the queue then the result value will be <code>null</code>.
   *
   * @param resultHandler An asynchronous handler to be called with the result.
   */
  void poll(Handler<AsyncResult<T>> resultHandler);

  /**
   * Retrieves and removed the head of the queue.
   *
   * @param resultHandler An asynchronous handler to be called with the result.
   */
  void remove(Handler<AsyncResult<T>> resultHandler);

}
