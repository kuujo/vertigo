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

import java.util.Collection;
import java.util.Set;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;

/**
 * Asynchronous multi-map.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 *
 * @param <K> The map key type.
 * @param <V> The map value type.
 */
public interface AsyncMultiMap<K, V> {

  /**
   * Returns the map name.
   *
   * @return The map name.
   */
  String name();

  /**
   * Sets a key value in the map.
   *
   * @param key The key to set.
   * @param value The value to set.
   */
  void put(K key, V value);

  /**
   * Sets a key value in the map.
   *
   * @param key The key to set.
   * @param value The value to set
   * @param doneHandler An asynchronous handler to be called once complete.
   */
  void put(K key, V value, Handler<AsyncResult<Boolean>> doneHandler);

  /**
   * Gets a key value in the map.
   *
   * @param key The key to get.
   * @param resultHandler An asynchronous handler to be called with the result.
   */
  void get(K key, Handler<AsyncResult<Collection<V>>> resultHandler);

  /**
   * Removes a key from the map.
   *
   * @param key The key to remove.
   */
  void remove(K key);

  /**
   * Removes a key from the map.
   *
   * @param key The key to remove.
   * @param doneHandler An asynchronous handler to be called once complete.
   */
  void remove(K key, Handler<AsyncResult<Collection<V>>> doneHandler);

  /**
   * Removes a value from a key in the map.
   *
   * @param key The key from which to remove the value.
   * @param value The value to remove.
   */
  void remove(K key, V value);

  /**
   * Removes a value from a key in the map.
   *
   * @param key The key from which to remove the value.
   * @param value The value to remove.
   * @param doneHandler An asynchronous handler to be called once complete.
   */
  void remove(K key, V value, Handler<AsyncResult<Boolean>> doneHandler);

  /**
   * Checks if the map contains a key.
   *
   * @param key The key to check.
   * @param resultHandler An asynchronous handler to be called with the result.
   */
  void containsKey(K key, Handler<AsyncResult<Boolean>> resultHandler);

  /**
   * Checks if the map contains a value.
   *
   * @param value The value to check.
   * @param resultHandler An asynchronous handler to be called with the result.
   */
  void containsValue(V value, Handler<AsyncResult<Boolean>> resultHandler);

  /**
   * Checks if the map contains a key/value pair.
   *
   * @param key The key to check.
   * @param value The value to check.
   * @param resultHandler An asynchronous handler to be called with the result.
   */
  void containsEntry(K key, V value, Handler<AsyncResult<Boolean>> resultHandler);

  /**
   * Gets a set of keys in the map.
   *
   * @param resultHandler An asynchronous handler to be called with the key set once complete.
   */
  void keySet(Handler<AsyncResult<Set<K>>> resultHandler);

  /**
   * Gets a collection of values in the map.
   *
   * @param resultHandler An asynchronous handler to be called with the values once complete.
   */
  void values(Handler<AsyncResult<Collection<V>>> resultHandler);

  /**
   * Gets the current size of the map.
   *
   * @param resultHandler An asynchronous handler to be called with the result.
   */
  void size(Handler<AsyncResult<Integer>> resultHandler);

  /**
   * Checks whether the map is empty.
   *
   * @param resultHandler An asynchronous handler to be called with the result indicating
   *                      whether the map is empty.
   */
  void isEmpty(Handler<AsyncResult<Boolean>> resultHandler);

  /**
   * Clears all keys from the map.
   */
  void clear();

  /**
   * Clears all keys from the map.
   *
   * @param doneHandler An asynchronous handler to be called once complete.
   */
  void clear(Handler<AsyncResult<Void>> doneHandler);

}
