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

import java.util.Set;

import net.kuujo.vertigo.cluster.ClusterType;
import net.kuujo.vertigo.cluster.LocalType;
import net.kuujo.vertigo.cluster.data.AsyncSet;
import net.kuujo.vertigo.util.Factory;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.impl.DefaultFutureResult;

/**
 * Shared data based set implementation.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 *
 * @param <T> The set data type.
 */
@LocalType
@ClusterType
public class SharedDataSet<T> implements AsyncSet<T> {
  private final String name;
  private final Vertx vertx;
  private final Set<T> set;

  @Factory
  public static <T> SharedDataSet<T> factory(String name, Vertx vertx) {
    return new SharedDataSet<T>(name, vertx);
  }

  private SharedDataSet(String name, Vertx vertx) {
    this.name = name;
    this.vertx = vertx;
    this.set = vertx.sharedData().getSet(name);
  }

  @Override
  public String name() {
    return name;
  }

  @Override
  public void add(T value) {
    add(value);
  }

  @Override
  public void add(final T value, final Handler<AsyncResult<Boolean>> doneHandler) {
    vertx.runOnContext(new Handler<Void>() {
      @Override
      public void handle(Void _) {
        new DefaultFutureResult<Boolean>(set.add(value)).setHandler(doneHandler);
      }
    });
  }

  @Override
  public void remove(T value) {
    remove(value, null);
  }

  @Override
  public void remove(final T value, final Handler<AsyncResult<Boolean>> doneHandler) {
    vertx.runOnContext(new Handler<Void>() {
      @Override
      public void handle(Void _) {
        new DefaultFutureResult<Boolean>(set.remove(value)).setHandler(doneHandler);
      }
    });
  }

  @Override
  public void contains(final Object value, final Handler<AsyncResult<Boolean>> resultHandler) {
    vertx.runOnContext(new Handler<Void>() {
      @Override
      public void handle(Void _) {
        new DefaultFutureResult<Boolean>(set.contains(value)).setHandler(resultHandler);
      }
    });
  }

  @Override
  public void size(final Handler<AsyncResult<Integer>> resultHandler) {
    vertx.runOnContext(new Handler<Void>() {
      @Override
      public void handle(Void _) {
        new DefaultFutureResult<Integer>(set.size()).setHandler(resultHandler);
      }
    });
  }

  @Override
  public void isEmpty(final Handler<AsyncResult<Boolean>> resultHandler) {
    vertx.runOnContext(new Handler<Void>() {
      @Override
      public void handle(Void _) {
        new DefaultFutureResult<Boolean>(set.isEmpty()).setHandler(resultHandler);
      }
    });
  }

  @Override
  public void clear() {
    clear(null);
  }

  @Override
  public void clear(final Handler<AsyncResult<Void>> doneHandler) {
    vertx.runOnContext(new Handler<Void>() {
      @Override
      public void handle(Void _) {
        set.clear();
        new DefaultFutureResult<Void>((Void) null).setHandler(doneHandler);
      }
    });
  }

}
