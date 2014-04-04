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

import java.util.Iterator;
import java.util.Map;

import net.kuujo.vertigo.annotations.ClusterType;
import net.kuujo.vertigo.annotations.Factory;
import net.kuujo.vertigo.annotations.LocalType;
import net.kuujo.vertigo.data.AsyncList;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.impl.DefaultFutureResult;
import org.vertx.java.core.shareddata.ConcurrentSharedMap;

/**
 * Shared data-based list implementation.
 *
 * @author Jordan Halterman
 */
@LocalType
@ClusterType
public class SharedDataList<T> implements AsyncList<T> {
  private static final String LIST_MAP_NAME = "__LIST__";
  private final String name;
  private final Vertx vertx;
  private final ConcurrentSharedMap<Integer, Object> map;
  private int currentSize = 0;

  @Factory
  public static <T> SharedDataList<T> factory(String name, Vertx vertx) {
    return new SharedDataList<T>(name, vertx);
  }

  public SharedDataList(String name, Vertx vertx) {
    this.name = name;
    this.vertx = vertx;
    this.map = vertx.sharedData().getMap(LIST_MAP_NAME);
    this.currentSize = (int) map.get(-1);
  }

  @Override
  public String name() {
    return name;
  }

  @Override
  public void add(T value) {
    add(value, null);
  }

  @Override
  public void add(final T value, final Handler<AsyncResult<Boolean>> doneHandler) {
    vertx.runOnContext(new Handler<Void>() {
      @Override
      public void handle(Void _) {
        map.put(currentSize, value);
        currentSize++;
        map.put(-1, currentSize);
        new DefaultFutureResult<Boolean>(true).setHandler(doneHandler);
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
        Iterator<Map.Entry<Integer, Object>> iter = map.entrySet().iterator();
        while (iter.hasNext()) {
          if (iter.next().getValue().equals(value)) {
            iter.remove();
            new DefaultFutureResult<Boolean>(true).setHandler(doneHandler);
            return;
          }
        }
        new DefaultFutureResult<Boolean>(false).setHandler(doneHandler);
      }
    });
  }

  @Override
  public void contains(final Object value, final Handler<AsyncResult<Boolean>> resultHandler) {
    vertx.runOnContext(new Handler<Void>() {
      @Override
      public void handle(Void _) {
        new DefaultFutureResult<Boolean>(map.values().contains(value)).setHandler(resultHandler);
      }
    });
  }

  @Override
  public void size(final Handler<AsyncResult<Integer>> resultHandler) {
    vertx.runOnContext(new Handler<Void>() {
      @Override
      public void handle(Void _) {
        new DefaultFutureResult<Integer>(currentSize).setHandler(resultHandler);
      }
    });
  }

  @Override
  public void isEmpty(final Handler<AsyncResult<Boolean>> resultHandler) {
    vertx.runOnContext(new Handler<Void>() {
      @Override
      public void handle(Void _) {
        new DefaultFutureResult<Boolean>(currentSize==0).setHandler(resultHandler);
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
        map.clear();
        currentSize = 0;
        map.put(-1, currentSize);
        new DefaultFutureResult<Void>((Void) null).setHandler(doneHandler);
      }
    });
  }

  @Override
  @SuppressWarnings("unchecked")
  public void get(final int index, final Handler<AsyncResult<T>> resultHandler) {
    vertx.runOnContext(new Handler<Void>() {
      @Override
      public void handle(Void _) {
        if (index > currentSize-1) {
          new DefaultFutureResult<T>(new IndexOutOfBoundsException("Index out of bounds.")).setHandler(resultHandler);
        } else {
          new DefaultFutureResult<T>((T) map.get(index)).setHandler(resultHandler);
        }
      }
    });
  }

  @Override
  public void set(int index, T value) {
    set(index, value, null);
  }

  @Override
  public void set(final int index, final T value, final Handler<AsyncResult<Void>> doneHandler) {
    vertx.runOnContext(new Handler<Void>() {
      @Override
      public void handle(Void _) {
        map.put(index, value);
        new DefaultFutureResult<Void>((Void) null).setHandler(doneHandler);
      }
    });
  }

  @Override
  public void remove(int index) {
    remove(index, null);
  }

  @Override
  @SuppressWarnings("unchecked")
  public void remove(final int index, final Handler<AsyncResult<T>> doneHandler) {
    vertx.runOnContext(new Handler<Void>() {
      @Override
      public void handle(Void _) {
        if (index > currentSize-1) {
          new DefaultFutureResult<T>(new IndexOutOfBoundsException("Index out of bounds.")).setHandler(doneHandler);
        } else {
          T value = (T) map.remove(index);
          int i = index+1;
          while (map.containsKey(i)) {
            map.put(i-1, map.remove(i));
            i++;
          }
          currentSize--;
          map.put(-1, currentSize);
          new DefaultFutureResult<T>(value).setHandler(doneHandler);
        }
      }
    });
  }

}
