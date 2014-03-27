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

import net.kuujo.vertigo.cluster.data.AsyncQueue;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;

/**
 * An event bus queue implementation.
 *
 * @author Jordan Halterman
 *
 * @param <T> The queue data type.
 */
public class XyncQueue<T> implements AsyncQueue<T> {
  private final net.kuujo.xync.data.AsyncQueue<T> queue;

  public XyncQueue(net.kuujo.xync.data.AsyncQueue<T> queue) {
    this.queue = queue;
  }

  @Override
  public String name() {
    return queue.name();
  }

  @Override
  public void add(T value) {
    queue.add(value);
  }

  @Override
  public void add(T value, final Handler<AsyncResult<Boolean>> doneHandler) {
    queue.add(value, doneHandler);
  }

  @Override
  public void offer(T value) {
    queue.offer(value);
  }

  @Override
  public void offer(T value, Handler<AsyncResult<Boolean>> doneHandler) {
    queue.offer(value, doneHandler);
  }

  @Override
  public void remove(T value) {
    queue.remove(value);
  }

  @Override
  public void remove(T value, final Handler<AsyncResult<Boolean>> doneHandler) {
    queue.remove(value, doneHandler);
  }

  @Override
  public void contains(Object value, final Handler<AsyncResult<Boolean>> resultHandler) {
    queue.contains(value, resultHandler);
  }

  @Override
  public void size(final Handler<AsyncResult<Integer>> resultHandler) {
    queue.size(resultHandler);
  }

  @Override
  public void isEmpty(final Handler<AsyncResult<Boolean>> resultHandler) {
    queue.isEmpty(resultHandler);
  }

  @Override
  public void clear() {
    queue.clear();
  }

  @Override
  public void clear(final Handler<AsyncResult<Void>> doneHandler) {
    queue.clear(doneHandler);
  }

  @Override
  public void element(final Handler<AsyncResult<T>> resultHandler) {
    queue.element(resultHandler);
  }

  @Override
  public void peek(final Handler<AsyncResult<T>> resultHandler) {
    queue.peek(resultHandler);
  }

  @Override
  public void poll(final Handler<AsyncResult<T>> resultHandler) {
    queue.poll(resultHandler);
  }

  @Override
  public void remove(final Handler<AsyncResult<T>> resultHandler) {
    queue.remove(resultHandler);
  }

}
