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

import java.util.Iterator;
import java.util.Map;

import net.kuujo.vertigo.cluster.data.AsyncQueue;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.impl.DefaultFutureResult;
import org.vertx.java.core.shareddata.ConcurrentSharedMap;

/**
 * A shared data based queue implementation.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 *
 * @param <T> The queue data type.
 */
public class SharedDataQueue<T> implements AsyncQueue<T> {
  private static final String QUEUE_MAP_PREFIX = "__queue";
  private final String name;
  private final Vertx vertx;
  private final ConcurrentSharedMap<Integer, Object> map;
  private int currentIndex;

  public SharedDataQueue(String name, Vertx vertx) {
    this.name = name;
    this.vertx = vertx;
    this.map = vertx.sharedData().getMap(String.format("%s.%s", QUEUE_MAP_PREFIX, name));
    this.currentIndex = (int) (map.containsKey(-1) ? map.get(-1) : 0);
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
        int index = currentIndex + map.size() - 1;
        map.put(index, value);
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
        synchronized (map) {
          Iterator<Map.Entry<Integer, Object>> iter = map.entrySet().iterator();
          while (iter.hasNext()) {
            Map.Entry<Integer, Object> entry = iter.next();
            if (entry.getValue().equals(value)) {
              iter.remove();
              int index = entry.getKey()+1;
              while (map.containsKey(index)) {
                map.put(index-1, map.remove(index));
                index++;
              }
              new DefaultFutureResult<Boolean>(true).setHandler(doneHandler);
              return;
            }
          }
          new DefaultFutureResult<Boolean>(false).setHandler(doneHandler);
        }
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
        new DefaultFutureResult<Integer>(map.size()-1).setHandler(resultHandler);
      }
    });
  }

  @Override
  public void isEmpty(final Handler<AsyncResult<Boolean>> resultHandler) {
    vertx.runOnContext(new Handler<Void>() {
      @Override
      public void handle(Void _) {
        new DefaultFutureResult<Boolean>(map.size()==1).setHandler(resultHandler);
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
        map.put(-1, currentIndex);
        new DefaultFutureResult<Void>((Void) null).setHandler(doneHandler);
      }
    });
  }

  @Override
  public void offer(T value) {
    offer(value, null);
  }

  @Override
  public void offer(final T value, final Handler<AsyncResult<Boolean>> doneHandler) {
    vertx.runOnContext(new Handler<Void>() {
      @Override
      public void handle(Void _) {
        int index = currentIndex + map.size() - 1;
        map.put(index, value);
        new DefaultFutureResult<Boolean>(true).setHandler(doneHandler);
      }
    });
  }

  @Override
  @SuppressWarnings("unchecked")
  public void element(final Handler<AsyncResult<T>> resultHandler) {
    vertx.runOnContext(new Handler<Void>() {
      @Override
      public void handle(Void _) {
        T value = (T) map.get(currentIndex);
        if (value != null) {
          new DefaultFutureResult<T>(value).setHandler(resultHandler);
        } else {
          new DefaultFutureResult<T>(new IllegalStateException("Queue is empty.")).setHandler(resultHandler);
        }
      }
    });
  }

  @Override
  @SuppressWarnings("unchecked")
  public void peek(final Handler<AsyncResult<T>> resultHandler) {
    vertx.runOnContext(new Handler<Void>() {
      @Override
      public void handle(Void _) {
        T value = (T) map.get(currentIndex);
        new DefaultFutureResult<T>(value).setHandler(resultHandler);
      }
    });
  }

  @Override
  @SuppressWarnings("unchecked")
  public void poll(final Handler<AsyncResult<T>> resultHandler) {
    vertx.runOnContext(new Handler<Void>() {
      @Override
      public void handle(Void _) {
        T value = (T) map.remove(currentIndex);
        if (value != null) {
          currentIndex++;
          map.put(-1, currentIndex);
        }
        new DefaultFutureResult<T>(value).setHandler(resultHandler);
      }
    });
  }

  @Override
  @SuppressWarnings("unchecked")
  public void remove(final Handler<AsyncResult<T>> resultHandler) {
    vertx.runOnContext(new Handler<Void>() {
      @Override
      public void handle(Void _) {
        synchronized (map) {
          if (map.containsKey(currentIndex)) {
            T value = (T) map.remove(currentIndex);
            currentIndex++;
            map.put(-1, currentIndex);
            new DefaultFutureResult<T>(value).setHandler(resultHandler);
          } else {
            new DefaultFutureResult<T>(new IllegalStateException("Queue is empty.")).setHandler(resultHandler);
          }
        }
      }
    });
  }

}
