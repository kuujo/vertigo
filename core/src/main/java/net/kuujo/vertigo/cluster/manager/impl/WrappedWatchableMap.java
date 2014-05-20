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
package net.kuujo.vertigo.cluster.manager.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import net.kuujo.vertigo.cluster.data.MapEvent;
import net.kuujo.vertigo.util.CountingCompletionHandler;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.impl.DefaultFutureResult;
import org.vertx.java.core.json.JsonObject;

/**
 * Watchable map that implements the same event bus watch pattern as
 * the asynchronous watchable map. The difference is that this map is
 * used on a synchronous map by the cluster manager.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 *
 * @param <K> The map key type.
 * @param <V> The map value type.
 */
class WrappedWatchableMap<K, V> implements Map<K, V> {
  private final String name;
  private final Map<K, V> map;
  private final EventBus eventBus;
  private final Map<MapEvent.Type, Map<Handler<MapEvent<K, V>>, Handler<Message<JsonObject>>>> watchHandlers = new HashMap<>();

  public WrappedWatchableMap(String name, Map<K, V> map, Vertx vertx) {
    this.name = name;
    this.map = map;
    this.eventBus = vertx.eventBus();
  }

  @Override
  public int size() {
    return map.size();
  }

  @Override
  public boolean isEmpty() {
    return map.isEmpty();
  }

  @Override
  public boolean containsKey(Object key) {
    return map.containsKey(key);
  }

  @Override
  public boolean containsValue(Object value) {
    return map.containsValue(value);
  }

  @Override
  public V get(Object key) {
    return map.get(key);
  }

  @Override
  public V put(K key, V value) {
    V result = map.put(key, value);
    eventBus.publish(String.format("%s.%s.%s", name, key, MapEvent.Type.CHANGE.toString()), new JsonObject()
        .putString("type", MapEvent.Type.CHANGE.toString())
        .putValue("key", key)
        .putValue("value", value));
    String event = result == null ? MapEvent.Type.CREATE.toString() : MapEvent.Type.UPDATE.toString();
    eventBus.publish(String.format("%s.%s.%s", name, key, event), new JsonObject()
        .putString("type", event)
        .putValue("key", key)
        .putValue("value", value));
    return result;
  }

  @Override
  public V remove(Object key) {
    V result = map.remove(key);
    eventBus.publish(String.format("%s.%s.%s", name, key, MapEvent.Type.CHANGE.toString()), new JsonObject()
        .putString("type", MapEvent.Type.CHANGE.toString())
        .putValue("key", key)
        .putValue("value", result));
    eventBus.publish(String.format("%s.%s.%s", name, key, MapEvent.Type.DELETE.toString()), new JsonObject()
        .putString("type", MapEvent.Type.DELETE.toString())
        .putValue("key", key)
        .putValue("value", result));
    return result;
  }

  @Override
  public void putAll(Map<? extends K, ? extends V> m) {
    map.putAll(m);
  }

  @Override
  public void clear() {
    map.clear();
  }

  @Override
  public Set<K> keySet() {
    return map.keySet();
  }

  @Override
  public Collection<V> values() {
    return map.values();
  }

  @Override
  public Set<Map.Entry<K, V>> entrySet() {
    return map.entrySet();
  }

  public void watch(final K key, final MapEvent.Type event, final Handler<MapEvent<K, V>> handler, Handler<AsyncResult<Void>> doneHandler) {
    if (event == null) {
      final CountingCompletionHandler<Void> counter = new CountingCompletionHandler<Void>(4).setHandler(doneHandler);
      addWatcher(key, MapEvent.Type.CHANGE, handler, counter);
      addWatcher(key, MapEvent.Type.CREATE, handler, counter);
      addWatcher(key, MapEvent.Type.UPDATE, handler, counter);
      addWatcher(key, MapEvent.Type.DELETE, handler, counter);
    } else {
      addWatcher(key, event, handler, doneHandler);
    }
  }

  private void addWatcher(K key, final MapEvent.Type event, final Handler<MapEvent<K, V>> handler, Handler<AsyncResult<Void>> doneHandler) {
    Map<Handler<MapEvent<K, V>>, Handler<Message<JsonObject>>> watchHandlers = this.watchHandlers.get(event);
    if (watchHandlers == null) {
      watchHandlers = new HashMap<>();
      this.watchHandlers.put(event, watchHandlers);
    }

    Handler<Message<JsonObject>> wrappedHandler;
    if (!watchHandlers.containsKey(handler)) {
      wrappedHandler = new Handler<Message<JsonObject>>() {
        @Override
        public void handle(Message<JsonObject> message) {
          K key = message.body().getValue("key");
          V value = message.body().getValue("value");
          handler.handle(new MapEvent<K, V>(event, key, value));
        }
      };
      watchHandlers.put(handler, wrappedHandler);
    } else {
      wrappedHandler = watchHandlers.get(handler);
    }
    eventBus.registerHandler(String.format("%s.%s.%s", name, key, event.toString()), wrappedHandler, doneHandler);
  }

  public void unwatch(K key, MapEvent.Type event, Handler<MapEvent<K, V>> handler, Handler<AsyncResult<Void>> doneHandler) {
    if (event == null) {
      final CountingCompletionHandler<Void> counter = new CountingCompletionHandler<Void>(4).setHandler(doneHandler);
      removeWatcher(key, MapEvent.Type.CHANGE, handler, counter);
      removeWatcher(key, MapEvent.Type.CREATE, handler, counter);
      removeWatcher(key, MapEvent.Type.UPDATE, handler, counter);
      removeWatcher(key, MapEvent.Type.DELETE, handler, counter);
    } else {
      removeWatcher(key, event, handler, doneHandler);
    }
  }

  private void removeWatcher(K key, MapEvent.Type event, Handler<MapEvent<K, V>> handler, Handler<AsyncResult<Void>> doneHandler) {
    Map<Handler<MapEvent<K, V>>, Handler<Message<JsonObject>>> watchHandlers = this.watchHandlers.get(event);
    if (watchHandlers == null || !watchHandlers.containsKey(handler)) {
      new DefaultFutureResult<Void>((Void) null).setHandler(doneHandler);
    } else {
      Handler<Message<JsonObject>> wrappedHandler = watchHandlers.remove(handler);
      eventBus.unregisterHandler(String.format("%s.%s.%s", name, key, event.toString()), wrappedHandler, doneHandler);
    }
  }

}
