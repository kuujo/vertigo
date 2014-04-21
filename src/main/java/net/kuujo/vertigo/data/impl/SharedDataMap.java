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
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import net.kuujo.vertigo.annotations.ClusterType;
import net.kuujo.vertigo.annotations.Factory;
import net.kuujo.vertigo.annotations.LocalType;
import net.kuujo.vertigo.data.DataException;
import net.kuujo.vertigo.data.MapEvent;
import net.kuujo.vertigo.data.WatchableAsyncMap;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.impl.DefaultFutureResult;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.shareddata.ConcurrentSharedMap;

/**
 * Shared data based map implementation.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 *
 * @param <K> The map key type.
 * @param <V> The map value type.
 */
@LocalType
@ClusterType
public class SharedDataMap<K, V> implements WatchableAsyncMap<K, V> {
  private final String name;
  private final Vertx vertx;
  private final ConcurrentSharedMap<K, V> map;
  private final ConcurrentSharedMap<K, String> watchers;
  private final Map<Handler<MapEvent<K, V>>, String> watchAddresses = new HashMap<>();
  private final Map<String, Handler<Message<JsonObject>>> messageHandlers = new HashMap<>();

  @Factory
  public static <K, V> SharedDataMap<K, V> factory(String name, Vertx vertx) {
    return new SharedDataMap<K, V>(name, vertx);
  }

  private SharedDataMap(String name, Vertx vertx) {
    this.name = name;
    this.vertx = vertx;
    this.map = vertx.sharedData().getMap(name);
    this.watchers = vertx.sharedData().getMap(String.format("%s.watchers", name));
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
          if (!map.containsKey(key)) {
            map.put(key, value);
            triggerEvent(MapEvent.Type.CHANGE, key, value);
            triggerEvent(MapEvent.Type.CREATE, key, value);
            new DefaultFutureResult<V>((V) null).setHandler(doneHandler);
          } else {
            V result = map.put(key, value);
            triggerEvent(MapEvent.Type.CHANGE, key, value);
            triggerEvent(MapEvent.Type.UPDATE, key, value);
            new DefaultFutureResult<V>(result).setHandler(doneHandler);
          }
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
        V value = map.remove(key);
        triggerEvent(MapEvent.Type.CHANGE, key, value);
        triggerEvent(MapEvent.Type.DELETE, key, value);
        new DefaultFutureResult<V>(value).setHandler(resultHandler);
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

  @Override
  public void watch(K key, Handler<MapEvent<K, V>> handler) {
    watch(key, null, handler, null);
  }

  @Override
  public void watch(K key, MapEvent.Type event, Handler<MapEvent<K, V>> handler) {
    watch(key, event, handler, null);
  }

  @Override
  public void watch(K key, Handler<MapEvent<K, V>> handler, Handler<AsyncResult<Void>> doneHandler) {
    watch(key, null, handler, doneHandler);
  }

  @Override
  public void watch(final K key, final MapEvent.Type event, final Handler<MapEvent<K, V>> handler, final Handler<AsyncResult<Void>> doneHandler) {
    final String address = UUID.randomUUID().toString();
    if (event == null) {
      addWatcher(MapEvent.Type.CREATE, key, address);
      addWatcher(MapEvent.Type.UPDATE, key, address);
      addWatcher(MapEvent.Type.CHANGE, key, address);
      addWatcher(MapEvent.Type.DELETE, key, address);
    } else {
      addWatcher(event, key, address);
    }

    final Handler<Message<JsonObject>> messageHandler = new Handler<Message<JsonObject>>() {
      @Override
      @SuppressWarnings("unchecked")
      public void handle(Message<JsonObject> message) {
        handler.handle(new MapEvent<K, V>(MapEvent.Type.parse(message.body().getString("type")), (K) message.body().getValue("key"), (V) message.body().getValue("value")));
      }
    };

    vertx.eventBus().registerLocalHandler(address, messageHandler);

    messageHandlers.put(address, messageHandler);
    watchAddresses.put(handler, address);

    vertx.runOnContext(new Handler<Void>() {
      @Override
      public void handle(Void _) {
        new DefaultFutureResult<Void>((Void) null).setHandler(doneHandler);
      }
    });
  }

  /**
   * Adds a watcher to a key.
   */
  private void addWatcher(MapEvent.Type event, K key, String address) {
    synchronized (watchers) {
      String swatchers = this.watchers.get(key);
      JsonObject watchers = swatchers != null ? new JsonObject(swatchers) : null;
      if (swatchers == null) {
        watchers = new JsonObject();
      }
      JsonArray addresses = watchers.getArray(event.toString());
      if (addresses == null) {
        addresses = new JsonArray();
        watchers.putArray(event.toString(), addresses);
      }
      if (!addresses.contains(address)) {
        addresses.add(address);
      }
      this.watchers.put(key, watchers.encode());
    }
  }

  @Override
  public void unwatch(K key, Handler<MapEvent<K, V>> handler) {
    unwatch(key, null, handler, null);
  }

  @Override
  public void unwatch(K key, MapEvent.Type event, Handler<MapEvent<K, V>> handler) {
    unwatch(key, event, handler, null);
  }

  @Override
  public void unwatch(K key, Handler<MapEvent<K, V>> handler, Handler<AsyncResult<Void>> doneHandler) {
    unwatch(key, null, handler, doneHandler);
  }

  @Override
  public void unwatch(final K key, final MapEvent.Type event, final Handler<MapEvent<K, V>> handler, final Handler<AsyncResult<Void>> doneHandler) {
    if (watchAddresses.containsKey(handler)) {
      String address = watchAddresses.remove(handler);

      String swatchers = this.watchers.get(key);
      JsonObject watchers = swatchers != null ? new JsonObject(swatchers) : null;
      if (swatchers == null) {
        watchers = new JsonObject();
      }

      if (event == null) {
        removeWatcher(watchers, MapEvent.Type.CREATE, address);
        removeWatcher(watchers, MapEvent.Type.UPDATE, address);
        removeWatcher(watchers, MapEvent.Type.CHANGE, address);
        removeWatcher(watchers, MapEvent.Type.DELETE, address);
      } else {
        removeWatcher(watchers, event, address);
      }

      Handler<Message<JsonObject>> messageHandler = messageHandlers.remove(address);
      if (messageHandler != null) {
        vertx.eventBus().unregisterHandler(address, messageHandler);
      }

      this.watchers.put(key, watchers.encode());

      vertx.runOnContext(new Handler<Void>() {
        @Override
        public void handle(Void _) {
          new DefaultFutureResult<Void>((Void) null).setHandler(doneHandler);
        }
      });
    } else {
      vertx.runOnContext(new Handler<Void>() {
        @Override
        public void handle(Void _) {
          new DefaultFutureResult<Void>(new DataException("Handler not registered."));
        }
      });
    }
  }

  /**
   * Removes a watcher from a key.
   */
  private void removeWatcher(JsonObject watchers, MapEvent.Type event, String address) {
    synchronized (watchers) {
      JsonArray addresses = watchers.getArray(event.toString());
      if (addresses == null) {
        addresses = new JsonArray();
        watchers.putArray(event.toString(), addresses);
      }
  
      Iterator<Object> iter = addresses.iterator();
      while (iter.hasNext()) {
        if (iter.next().equals(address)) {
          iter.remove();
        }
      }
  
      if (addresses.size() == 0) {
        watchers.removeField(event.toString());
      }
    }
  }

  /**
   * Triggers a cluster event.
   */
  private void triggerEvent(MapEvent.Type type, K key, V value) {
    String swatchers = this.watchers.get(key);
    JsonObject watchers = swatchers != null ? new JsonObject(swatchers) : null;
    if (swatchers == null) {
      watchers = new JsonObject();
    }

    JsonArray addresses = watchers.getArray(type.toString());
    if (addresses != null) {
      for (Object address : addresses) {
        vertx.eventBus().send((String) address, new JsonObject().putString("type", type.toString()).putValue("key", key).putValue("value", value));
      }
    }
  }

}
