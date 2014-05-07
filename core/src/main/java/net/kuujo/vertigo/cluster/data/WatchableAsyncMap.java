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
package net.kuujo.vertigo.cluster.data;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;

/**
 * Watchable asynchronous map.<p>
 *
 * This is an extension of the {@link AsyncMap} interface that supports
 * map key events. Types that implement this interface will trigger
 * event handlers when data changes in the map. Note that events can
 * be received from anywhere in the cluster, so the async map events
 * system is distributed.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 *
 * @param <K> The map key type.
 * @param <V> The map value type.
 */
public interface WatchableAsyncMap<K, V> extends AsyncMap<K, V> {

  /**
   * Watches a key for changes.
   *
   * @param key The key to watch.
   * @param handler The handler to call when an event occurs.
   */
  void watch(K key, Handler<MapEvent<K, V>> handler);

  /**
   * Watches a key for changes.
   *
   * @param key The key to watch.
   * @param handler The handler to call when an event occurs.
   * @param doneHandler An asynchronous handler to be called once the key is being watched.
   */
  void watch(K key, Handler<MapEvent<K, V>> handler, Handler<AsyncResult<Void>> doneHandler);

  /**
   * Watches a key for changes.
   *
   * @param key The key to watch.
   * @param event The event type to watch.
   * @param handler The handler to call when an event occurs.
   */
  void watch(K key, MapEvent.Type event, Handler<MapEvent<K, V>> handler);

  /**
   * Watches a key for changes.
   *
   * @param key The key to watch.
   * @param event The event type to watch.
   * @param handler The handler to call when an event occurs.
   * @param doneHandler An asynchronous handler to be called once the key is being watched.
   */
  void watch(K key, MapEvent.Type event, Handler<MapEvent<K, V>> handler, Handler<AsyncResult<Void>> doneHandler);

  /**
   * Stops watching a key for changes.
   *
   * @param key The key to unwatch.
   * @param handler The handler that was watching the key/event.
   */
  void unwatch(K key, Handler<MapEvent<K, V>> handler);

  /**
   * Stops watching a key for changes.
   *
   * @param key The key to unwatch.
   * @param handler The handler that was watching the key/event.
   * @param doneHandler An asynchronous handler to be called once the key is no longer being watched.
   */
  void unwatch(K key, Handler<MapEvent<K, V>> handler, Handler<AsyncResult<Void>> doneHandler);

  /**
   * Stops watching a key for changes.
   *
   * @param key The key to unwatch.
   * @param event The event type to unwatch.
   * @param handler The handler that was watching the key/event.
   */
  void unwatch(K key, MapEvent.Type event, Handler<MapEvent<K, V>> handler);

  /**
   * Stops watching a key for changes.
   *
   * @param key The key to unwatch.
   * @param event The event type to unwatch.
   * @param handler The handler that was watching the key/event.
   * @param doneHandler An asynchronous handler to be called once the key is no longer being watched.
   */
  void unwatch(K key, MapEvent.Type event, Handler<MapEvent<K, V>> handler, Handler<AsyncResult<Void>> doneHandler);

}
