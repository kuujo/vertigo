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
package net.kuujo.vertigo.cluster.data.impl;

import net.kuujo.vertigo.cluster.data.AsyncLock;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;

/**
 * An event bus lock implementation.
 *
 * @author Jordan Halterman
 */
public class XyncLock implements AsyncLock {
  private final net.kuujo.xync.data.AsyncLock lock;

  public XyncLock(net.kuujo.xync.data.AsyncLock lock) {
    this.lock = lock;
  }

  @Override
  public String name() {
    return lock.name();
  }

  @Override
  public void lock(final Handler<AsyncResult<Void>> resultHandler) {
    lock.lock(resultHandler);
  }

  @Override
  public void tryLock(final Handler<AsyncResult<Boolean>> resultHandler) {
    lock.tryLock(resultHandler);
  }

  @Override
  public void tryLock(long timeout, final Handler<AsyncResult<Boolean>> resultHandler) {
    lock.tryLock(timeout, resultHandler);
  }

  @Override
  public void unlock() {
    lock.unlock();
  }

  @Override
  public void unlock(final Handler<AsyncResult<Void>> doneHandler) {
    lock.unlock(doneHandler);
  }

}
