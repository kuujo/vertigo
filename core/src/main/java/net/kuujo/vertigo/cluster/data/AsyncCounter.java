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
 * Asynchronous cluster-wide counter.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public interface AsyncCounter {

  /**
   * Returns the counter name.
   *
   * @return The counter name.
   */
  String name();

  /**
   * Gets the current counter value.
   *
   * @param doneHandler The current counter value.
   */
  void get(Handler<AsyncResult<Long>> doneHandler);

  /**
   * Increments the counter value.
   */
  void increment();

  /**
   * Increments the counter value.
   *
   * @param doneHandler An asynchronous handler to be called once complete.
   */
  void increment(Handler<AsyncResult<Void>> doneHandler);

  /**
   * Increments the counter and gets the new value.
   *
   * @param doneHandler An asynchronous handler to be called with the result.
   */
  void incrementAndGet(Handler<AsyncResult<Long>> doneHandler);

  /**
   * Decrements the counter value.
   */
  void decrement();

  /**
   * Decrements the counter value.
   *
   * @param doneHandler An asynchronous handler to be called once complete.
   */
  void decrement(Handler<AsyncResult<Void>> doneHandler);

  /**
   * Decrements the counter and gets the new value.
   *
   * @param doneHandler An asynchronous handler to be called with the result.
   */
  void decrementAndGet(Handler<AsyncResult<Long>> doneHandler);

}
