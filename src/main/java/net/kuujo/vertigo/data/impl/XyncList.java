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
import net.kuujo.vertigo.data.AsyncList;
import net.kuujo.xync.data.impl.XyncAsyncList;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;

/**
 * An event bus list implementation.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 *
 * @param <T> The list data type.
 */
@ClusterType
public class XyncList<T> implements AsyncList<T> {
  private final net.kuujo.xync.data.AsyncList<T> list;

  @Factory
  public static <T> XyncList<T> factory(String name, Vertx vertx) {
    return new XyncList<T>(new XyncAsyncList<T>(name, vertx.eventBus()));
  }

  private XyncList(net.kuujo.xync.data.AsyncList<T> list) {
    this.list = list;
  }

  @Override
  public String name() {
    return list.name();
  }

  @Override
  public void add(T value) {
    list.add(value);
  }

  @Override
  public void add(T value, final Handler<AsyncResult<Boolean>> doneHandler) {
    list.add(value, doneHandler);
  }

  @Override
  public void remove(T value) {
    list.remove(value);
  }

  @Override
  public void remove(T value, final Handler<AsyncResult<Boolean>> doneHandler) {
    list.remove(value, doneHandler);
  }

  @Override
  public void remove(int index) {
    list.remove(index);
  }

  @Override
  public void remove(int index, final Handler<AsyncResult<T>> doneHandler) {
    list.remove(index, doneHandler);
  }

  @Override
  public void contains(Object value, final Handler<AsyncResult<Boolean>> resultHandler) {
    list.contains(value, resultHandler);
  }

  @Override
  public void size(final Handler<AsyncResult<Integer>> resultHandler) {
    list.size(resultHandler);
  }

  @Override
  public void isEmpty(final Handler<AsyncResult<Boolean>> resultHandler) {
    list.isEmpty(resultHandler);
  }

  @Override
  public void clear() {
    list.clear();
  }

  @Override
  public void clear(final Handler<AsyncResult<Void>> doneHandler) {
    list.clear(doneHandler);
  }

  @Override
  public void get(int index, final Handler<AsyncResult<T>> resultHandler) {
    list.get(index, resultHandler);
  }

  @Override
  public void set(int index, T value) {
    list.set(index, value);
  }

  @Override
  public void set(int index, T value, final Handler<AsyncResult<Void>> doneHandler) {
    list.set(index, value, doneHandler);
  }

}
