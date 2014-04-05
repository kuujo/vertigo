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
import net.kuujo.vertigo.data.AsyncSet;
import net.kuujo.xync.data.impl.XyncAsyncSet;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;

/**
 * An event bus set implementation.
 *
 * @author Jordan Halterman
 *
 * @param <T> The set data type.
 */
@ClusterType
public class XyncSet<T> implements AsyncSet<T> {
  private final net.kuujo.xync.data.AsyncSet<T> set;

  @Factory
  public static <T> XyncSet<T> factory(String name, Vertx vertx) {
    return new XyncSet<T>(new XyncAsyncSet<T>(name, vertx.eventBus()));
  }

  private XyncSet(net.kuujo.xync.data.AsyncSet<T> set) {
    this.set = set;
  }

  @Override
  public String name() {
    return set.name();
  }

  @Override
  public void add(T value) {
    set.add(value);
  }

  @Override
  public void add(T value, final Handler<AsyncResult<Boolean>> doneHandler) {
    set.add(value, doneHandler);
  }

  @Override
  public void remove(T value) {
    set.remove(value);
  }

  @Override
  public void remove(T value, final Handler<AsyncResult<Boolean>> doneHandler) {
    set.remove(value, doneHandler);
  }

  @Override
  public void contains(Object value, final Handler<AsyncResult<Boolean>> resultHandler) {
    set.contains(value, resultHandler);
  }

  @Override
  public void size(final Handler<AsyncResult<Integer>> resultHandler) {
    set.size(resultHandler);
  }

  @Override
  public void isEmpty(final Handler<AsyncResult<Boolean>> resultHandler) {
    set.isEmpty(resultHandler);
  }

  @Override
  public void clear() {
    set.clear();
  }

  @Override
  public void clear(final Handler<AsyncResult<Void>> doneHandler) {
    set.clear(doneHandler);
  }

}
