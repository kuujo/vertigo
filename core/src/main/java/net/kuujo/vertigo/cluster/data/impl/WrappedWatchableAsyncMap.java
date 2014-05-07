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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import net.kuujo.vertigo.cluster.data.AsyncMap;
import net.kuujo.vertigo.cluster.data.MapEvent;
import net.kuujo.vertigo.cluster.data.MapEvent.Type;
import net.kuujo.vertigo.cluster.data.WatchableAsyncMap;
import net.kuujo.vertigo.util.CountingCompletionHandler;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.impl.DefaultFutureResult;
import org.vertx.java.core.json.JsonObject;

/**
 * Wrapped watchable asynchronous map.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 *
 * @param <K> The map key type.
 * @param <V> The map value type.
 */
public class WrappedWatchableAsyncMap<K, V> implements WatchableAsyncMap<K, V> {
  private final AsyncMap<K, V> map;
  private final EventBus eventBus;
  private final Map<MapEvent.Type, Map<Handler<MapEvent<K, V>>, Handler<Message<JsonObject>>>> watchHandlers = new HashMap<>();

  public WrappedWatchableAsyncMap(AsyncMap<K, V> map, Vertx vertx) {
    this.map = map;
    this.eventBus = vertx.eventBus();
  }

  @Override
  public String name() {
    return map.name();
  }

  @Override
  public void put(K key, V value) {
    put(key, value, null);
  }

  @Override
  public void put(final K key, final V value, final Handler<AsyncResult<V>> doneHandler) {
    map.put(key, value, new Handler<AsyncResult<V>>() {
      @Override
      public void handle(AsyncResult<V> result) {
        eventBus.publish(String.format("%s.%s.%s", map.name(), key, MapEvent.Type.CHANGE.toString()), new JsonObject()
            .putString("type", MapEvent.Type.CHANGE.toString())
            .putValue("key", key)
            .putValue("value", value));
        String event = result.result() == null ? MapEvent.Type.CREATE.toString() : MapEvent.Type.UPDATE.toString();
        eventBus.publish(String.format("%s.%s.%s", map.name(), key, event), new JsonObject()
            .putString("type", event)
            .putValue("key", key)
            .putValue("value", value));
        if (doneHandler != null) {
          doneHandler.handle(result);
        }
      }
    });
  }

  @Override
  public void get(K key, Handler<AsyncResult<V>> resultHandler) {
    map.get(key, resultHandler);
  }

  @Override
  public void remove(K key) {
    remove(key, null);
  }

  @Override
  public void remove(final K key, final Handler<AsyncResult<V>> resultHandler) {
    map.remove(key, new Handler<AsyncResult<V>>() {
      @Override
      public void handle(AsyncResult<V> result) {
        eventBus.publish(String.format("%s.%s.%s", map.name(), key, MapEvent.Type.CHANGE.toString()), new JsonObject()
            .putString("type", MapEvent.Type.CHANGE.toString())
            .putValue("key", key)
            .putValue("value", result.result()));
        eventBus.publish(String.format("%s.%s.%s", map.name(), key, MapEvent.Type.DELETE.toString()), new JsonObject()
            .putString("type", MapEvent.Type.DELETE.toString())
            .putValue("key", key)
            .putValue("value", result.result()));
        if (resultHandler != null) {
          resultHandler.handle(result);
        }
      }
    });
  }

  @Override
  public void containsKey(K key, Handler<AsyncResult<Boolean>> resultHandler) {
    map.containsKey(key, resultHandler);
  }

  @Override
  public void keySet(Handler<AsyncResult<Set<K>>> resultHandler) {
    map.keySet(resultHandler);
  }

  @Override
  public void values(Handler<AsyncResult<Collection<V>>> resultHandler) {
    map.values(resultHandler);
  }

  @Override
  public void size(Handler<AsyncResult<Integer>> resultHandler) {
    map.size(resultHandler);
  }

  @Override
  public void isEmpty(Handler<AsyncResult<Boolean>> resultHandler) {
    map.isEmpty(resultHandler);
  }

  @Override
  public void clear() {
    map.clear();
  }

  @Override
  public void clear(Handler<AsyncResult<Void>> doneHandler) {
    map.clear(doneHandler);
  }

  @Override
  public void watch(K key, Handler<MapEvent<K, V>> handler) {
    watch(key, null, handler, null);
  }

  @Override
  public void watch(K key, Handler<MapEvent<K, V>> handler, Handler<AsyncResult<Void>> doneHandler) {
    watch(key, null, handler, doneHandler);
  }

  @Override
  public void watch(K key, Type event, Handler<MapEvent<K, V>> handler) {
    watch(key, event, handler, null);
  }

  @Override
  public void watch(final K key, final Type event, final Handler<MapEvent<K, V>> handler, Handler<AsyncResult<Void>> doneHandler) {
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
    eventBus.registerHandler(String.format("%s.%s.%s", map.name(), key, event.toString()), wrappedHandler, doneHandler);
  }

  @Override
  public void unwatch(K key, Handler<MapEvent<K, V>> handler) {
    unwatch(key, null, handler, null);
  }

  @Override
  public void unwatch(K key, Handler<MapEvent<K, V>> handler, Handler<AsyncResult<Void>> doneHandler) {
    unwatch(key, null, handler, doneHandler);
  }

  @Override
  public void unwatch(K key, Type event, Handler<MapEvent<K, V>> handler) {
    unwatch(key, event, handler, null);
  }

  @Override
  public void unwatch(K key, Type event, Handler<MapEvent<K, V>> handler, Handler<AsyncResult<Void>> doneHandler) {
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
      eventBus.unregisterHandler(String.format("%s.%s.%s", map.name(), key, event.toString()), wrappedHandler, doneHandler);
    }
  }

}
