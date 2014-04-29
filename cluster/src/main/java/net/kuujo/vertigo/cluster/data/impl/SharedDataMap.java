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

import java.util.Collection;
import java.util.Set;

import net.kuujo.vertigo.cluster.data.AsyncMap;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.impl.DefaultFutureResult;
import org.vertx.java.core.shareddata.ConcurrentSharedMap;

/**
 * Shared data based map implementation.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 *
 * @param <K> The map key type.
 * @param <V> The map value type.
 */
public class SharedDataMap<K, V> implements AsyncMap<K, V> {
  private final String name;
  private final Vertx vertx;
  private final ConcurrentSharedMap<K, V> map;

  public SharedDataMap(String name, Vertx vertx) {
    this.name = name;
    this.vertx = vertx;
    this.map = vertx.sharedData().getMap(name);
  }

  @Override
  public String name() {
    return name;
  }

  @Override
  public void put(K key, V value) {
    put(key, value, null);
  }

  @Override
  public void put(final K key, final V value, final Handler<AsyncResult<V>> doneHandler) {
    vertx.runOnContext(new Handler<Void>() {
      @Override
      public void handle(Void _) {
        synchronized (map) {
          new DefaultFutureResult<V>(map.put(key, value)).setHandler(doneHandler);
        }
      }
    });
  }

  @Override
  public void get(final K key, final Handler<AsyncResult<V>> resultHandler) {
    vertx.runOnContext(new Handler<Void>() {
      @Override
      public void handle(Void _) {
        new DefaultFutureResult<V>((V) map.get(key)).setHandler(resultHandler);
      }
    });
  }

  @Override
  public void remove(K key) {
    remove(key, null);
  }

  @Override
  public void remove(final K key, final Handler<AsyncResult<V>> resultHandler) {
    vertx.runOnContext(new Handler<Void>() {
      @Override
      public void handle(Void _) {
        new DefaultFutureResult<V>(map.remove(key)).setHandler(resultHandler);
      }
    });
  }

  @Override
  public void containsKey(final K key, final Handler<AsyncResult<Boolean>> resultHandler) {
    vertx.runOnContext(new Handler<Void>() {
      @Override
      public void handle(Void _) {
        new DefaultFutureResult<Boolean>(map.containsKey(key)).setHandler(resultHandler);
      }
    });
  }

  @Override
  public void keySet(final Handler<AsyncResult<Set<K>>> resultHandler) {
    vertx.runOnContext(new Handler<Void>() {
      @Override
      public void handle(Void _) {
        new DefaultFutureResult<Set<K>>(map.keySet()).setHandler(resultHandler);
      }
    });
  }

  @Override
  public void values(final Handler<AsyncResult<Collection<V>>> resultHandler) {
    vertx.runOnContext(new Handler<Void>() {
      @Override
      public void handle(Void _) {
        new DefaultFutureResult<Collection<V>>(map.values()).setHandler(resultHandler);
      }
    });
  }

  @Override
  public void size(final Handler<AsyncResult<Integer>> resultHandler) {
    vertx.runOnContext(new Handler<Void>() {
      @Override
      public void handle(Void _) {
        new DefaultFutureResult<Integer>(map.size()).setHandler(resultHandler);
      }
    });
  }

  @Override
  public void isEmpty(final Handler<AsyncResult<Boolean>> resultHandler) {
    vertx.runOnContext(new Handler<Void>() {
      @Override
      public void handle(Void _) {
        new DefaultFutureResult<Boolean>(map.isEmpty()).setHandler(resultHandler);
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
        new DefaultFutureResult<Void>((Void) null).setHandler(doneHandler);
      }
    });
  }

}
