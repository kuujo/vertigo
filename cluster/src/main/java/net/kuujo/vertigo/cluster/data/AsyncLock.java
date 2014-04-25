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
 * Asynchronous lock.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public interface AsyncLock {

  /**
   * Returns the lock name.
   *
   * @return The lock name.
   */
  String name();

  /**
   * Acquires the lock.
   *
   * @param doneHandler An asynchronous handler to be called once the lock has been acquired.
   */
  void lock(Handler<AsyncResult<Void>> doneHandler);

  /**
   * Acquires the lock only if its available at the time of invocation.
   *
   * @param doneHandler An asynchronous handler to be called indicating whether the lock has been acquired.
   */
  void tryLock(Handler<AsyncResult<Boolean>> doneHandler);

  /**
   * Acquires the lock only if its available at the time of invocation.
   *
   * @param timeout A timeout in milliseconds after which the lock acquisition will expire.
   * @param doneHandler An asynchronous handler to be called once the lock has been acquired.
   */
  void tryLock(long timeout, Handler<AsyncResult<Boolean>> doneHandler);

  /**
   * Releases the lock.
   */
  void unlock();

  /**
   * Releases the lock.
   *
   * @param doneHandler An asynchronous handler to be called once the lock has been released.
   */
  void unlock(Handler<AsyncResult<Void>> doneHandler);

}
