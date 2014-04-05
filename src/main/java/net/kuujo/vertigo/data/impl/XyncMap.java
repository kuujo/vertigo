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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import net.kuujo.vertigo.annotations.ClusterType;
import net.kuujo.vertigo.annotations.Factory;
import net.kuujo.vertigo.data.DataException;
import net.kuujo.vertigo.data.MapEvent;
import net.kuujo.vertigo.data.WatchableAsyncMap;
import net.kuujo.xync.data.impl.XyncAsyncMap;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.impl.DefaultFutureResult;

/**
 * An event bus map implementation.
 *
 * @author Jordan Halterman
 *
 * @param <K> The map key type.
 * @param <V> The map value type.
 */
@ClusterType
public class XyncMap<K, V> implements WatchableAsyncMap<K, V> {
  private final net.kuujo.xync.data.WatchableAsyncMap<K, V> map;
  private final Map<Handler<MapEvent<K, V>>, Handler<net.kuujo.xync.data.MapEvent<K, V>>> handlers = new HashMap<>();

  @Factory
  public static <K, V> XyncMap<K, V> factory(String name, Vertx vertx) {
    return new XyncMap<K, V>(new XyncAsyncMap<K, V>(name, vertx.eventBus()));
  }

  private XyncMap(net.kuujo.xync.data.WatchableAsyncMap<K, V> map) {
    this.map = map;
  }

  @Override
  public String name() {
    return map.name();
  }

  @Override
  public void put(K key, V value) {
    map.put(key, value);
  }

  @Override
  public void put(K key, V value, final Handler<AsyncResult<V>> doneHandler) {
    map.put(key, value, doneHandler);
  }

  @Override
  public void get(K key, final Handler<AsyncResult<V>> resultHandler) {
    map.get(key, resultHandler);
  }

  @Override
  public void remove(K key) {
    map.remove(key);
  }

  @Override
  public void remove(K key, final Handler<AsyncResult<V>> resultHandler) {
    map.remove(key, resultHandler);
  }

  @Override
  public void containsKey(K key, final Handler<AsyncResult<Boolean>> resultHandler) {
    map.containsKey(key, resultHandler);
  }

  @Override
  public void keySet(final Handler<AsyncResult<Set<K>>> resultHandler) {
    map.keySet(resultHandler);
  }

  @Override
  public void values(final Handler<AsyncResult<Collection<V>>> resultHandler) {
    map.values(resultHandler);
  }

  @Override
  public void size(final Handler<AsyncResult<Integer>> resultHandler) {
    map.size(resultHandler);
  }

  @Override
  public void isEmpty(final Handler<AsyncResult<Boolean>> resultHandler) {
    map.isEmpty(resultHandler);
  }

  @Override
  public void clear() {
    map.clear();
  }

  @Override
  public void clear(final Handler<AsyncResult<Void>> doneHandler) {
    map.clear(doneHandler);
  }

  @Override
  public void watch(String key, Handler<MapEvent<K, V>> handler) {
    final Handler<net.kuujo.xync.data.MapEvent<K, V>> wrappedHandler = wrapHandler(handler);
    handlers.put(handler, wrappedHandler);
    map.watch(key, wrappedHandler);
  }

  @Override
  public void watch(String key, Handler<MapEvent<K, V>> handler, Handler<AsyncResult<Void>> doneHandler) {
    final Handler<net.kuujo.xync.data.MapEvent<K, V>> wrappedHandler = wrapHandler(handler);
    handlers.put(handler, wrappedHandler);
    map.watch(key, wrappedHandler, doneHandler);
  }

  @Override
  public void watch(String key, MapEvent.Type event, Handler<MapEvent<K, V>> handler) {
    final Handler<net.kuujo.xync.data.MapEvent<K, V>> wrappedHandler = wrapHandler(handler);
    handlers.put(handler, wrappedHandler);
    map.watch(key, net.kuujo.xync.data.MapEvent.Type.parse(event.name()), wrappedHandler);
  }

  @Override
  public void watch(final String key, final MapEvent.Type event, final Handler<MapEvent<K, V>> handler, final Handler<AsyncResult<Void>> doneHandler) {
    final Handler<net.kuujo.xync.data.MapEvent<K, V>> wrappedHandler = wrapHandler(handler);
    handlers.put(handler, wrappedHandler);
    map.watch(key, net.kuujo.xync.data.MapEvent.Type.parse(event.name()), wrappedHandler, doneHandler);
  }

  private Handler<net.kuujo.xync.data.MapEvent<K, V>> wrapHandler(final Handler<MapEvent<K, V>> handler) {
    return new Handler<net.kuujo.xync.data.MapEvent<K, V>>() {
      @Override
      public void handle(net.kuujo.xync.data.MapEvent<K, V> event) {
        handler.handle(new MapEvent<K, V>(MapEvent.Type.parse(event.type().name()), event.key(), event.value()));
      }
    };
  }

  @Override
  public void unwatch(String key, Handler<MapEvent<K, V>> handler) {
    Handler<net.kuujo.xync.data.MapEvent<K, V>> wrappedHandler = handlers.get(handler);
    if (wrappedHandler != null) {
      map.unwatch(key, wrappedHandler);
    }
  }

  @Override
  public void unwatch(String key, MapEvent.Type event, Handler<MapEvent<K, V>> handler) {
    Handler<net.kuujo.xync.data.MapEvent<K, V>> wrappedHandler = handlers.get(handler);
    if (wrappedHandler != null) {
      map.unwatch(key, net.kuujo.xync.data.MapEvent.Type.parse(event.name()), wrappedHandler);
    }
  }

  @Override
  public void unwatch(String key, Handler<MapEvent<K, V>> handler, Handler<AsyncResult<Void>> doneHandler) {
    Handler<net.kuujo.xync.data.MapEvent<K, V>> wrappedHandler = handlers.get(handler);
    if (wrappedHandler != null) {
      map.unwatch(key, wrappedHandler);
    } else {
      new DefaultFutureResult<Void>(new DataException("Handler not registered.")).setHandler(doneHandler);
    }
  }

  @Override
  public void unwatch(final String key, final MapEvent.Type event, final Handler<MapEvent<K, V>> handler, final Handler<AsyncResult<Void>> doneHandler) {
    Handler<net.kuujo.xync.data.MapEvent<K, V>> wrappedHandler = handlers.get(handler);
    if (wrappedHandler != null) {
      map.unwatch(key, net.kuujo.xync.data.MapEvent.Type.parse(event.name()), wrappedHandler);
    } else {
      new DefaultFutureResult<Void>(new DataException("Handler not registered.")).setHandler(doneHandler);
    }
  }

}
