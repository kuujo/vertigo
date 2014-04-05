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
package net.kuujo.vertigo.data.impl;

import net.kuujo.vertigo.annotations.ClusterType;
import net.kuujo.vertigo.annotations.Factory;
import net.kuujo.vertigo.annotations.LocalType;
import net.kuujo.vertigo.data.AsyncLock;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;

/**
 * Redis-based lock implementation.
 *
 * @author Jordan Halterman
 */
@LocalType
@ClusterType
public class RedisLock implements AsyncLock {
  private final String name;

  @Factory
  public static RedisLock factory(String name, String address, Vertx vertx) {
    return new RedisLock(name, address, vertx);
  }

  private RedisLock(String name, String address, Vertx vertx) {
    this.name = name;
  }

  @Override
  public String name() {
    return name;
  }

  @Override
  public void lock(final Handler<AsyncResult<Void>> doneHandler) {
    throw new UnsupportedOperationException("Locks not supported by Redis.");
  }

  @Override
  public void tryLock(Handler<AsyncResult<Boolean>> doneHandler) {
    tryLock(30000, null);
  }

  @Override
  public void tryLock(long timeout, Handler<AsyncResult<Boolean>> doneHandler) {
    throw new UnsupportedOperationException("Locks not supported by Redis.");
  }

  @Override
  public void unlock() {
    unlock(null);
  }

  @Override
  public void unlock(Handler<AsyncResult<Void>> doneHandler) {
    throw new UnsupportedOperationException("Locks not supported by Redis.");
  }

}
