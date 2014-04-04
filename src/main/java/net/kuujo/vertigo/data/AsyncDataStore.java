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
package net.kuujo.vertigo.data;

import net.kuujo.vertigo.annotations.ClusterTypeInfo;
import net.kuujo.vertigo.annotations.LocalTypeInfo;
import net.kuujo.vertigo.data.impl.HazelcastDataStore;
import net.kuujo.vertigo.data.impl.SharedDataStore;

/**
 * Asynchronous data store.
 *
 * @author Jordan Halterman
 */
@LocalTypeInfo(defaultImpl=SharedDataStore.class)
@ClusterTypeInfo(defaultImpl=HazelcastDataStore.class)
public interface AsyncDataStore {

  /**
   * Returns an asynchronous map backed by the data store.
   *
   * @param name The map name.
   * @return An asynchronous data store backed map.
   */
  <K, V> AsyncMap<K, V> getMap(String name);

  /**
   * Returns an asynchronous list backed by the data store.
   *
   * @param name The list name.
   * @return An asynchronous data store backed list.
   */
  <T> AsyncList<T> getList(String name);

  /**
   * Returns an asynchronous set backed by the data store.
   *
   * @param name The set name.
   * @return An asynchronous data store backed set.
   */
  <T> AsyncSet<T> getSet(String name);

  /**
   * Returns an asynchronous queue backed by the data store.
   *
   * @param name The queue name.
   * @return An asynchronous data store backed queue.
   */
  <T> AsyncQueue<T> getQueue(String name);

  /**
   * Returns an asynchronous lock backed by the data store.
   *
   * @param name The lock name.
   * @return An asynchronous data store backed lock.
   */
  AsyncLock getLock(String name);

  /**
   * Returns an asynchronous unique ID generator backed by the data store.
   *
   * @param name The ID generator name.
   * @return An asynchronous data store backed unique ID generator.
   */
  AsyncIdGenerator getIdGenerator(String name);

}
