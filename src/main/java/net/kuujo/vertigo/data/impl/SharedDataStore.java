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

import net.kuujo.vertigo.annotations.ClusterType;
import net.kuujo.vertigo.annotations.Factory;
import net.kuujo.vertigo.annotations.LocalType;
import net.kuujo.vertigo.data.DataStore;
import net.kuujo.vertigo.data.AsyncIdGenerator;
import net.kuujo.vertigo.data.AsyncList;
import net.kuujo.vertigo.data.AsyncLock;
import net.kuujo.vertigo.data.AsyncMap;
import net.kuujo.vertigo.data.AsyncQueue;
import net.kuujo.vertigo.data.AsyncSet;

import org.vertx.java.core.Vertx;

/**
 * Shared data-based data store.
 *
 * @author Jordan Halterman
 */
@LocalType
@ClusterType
public class SharedDataStore implements DataStore {
  private final Vertx vertx;

  @Factory
  public static SharedDataStore factory(Vertx vertx) {
    return new SharedDataStore(vertx);
  }

  private SharedDataStore(Vertx vertx) {
    this.vertx = vertx;
  }

  @Override
  public <K, V> AsyncMap<K, V> getMap(String name) {
    return SharedDataMap.factory(name, vertx);
  }

  @Override
  public <T> AsyncList<T> getList(String name) {
    return SharedDataList.factory(name, vertx);
  }

  @Override
  public <T> AsyncSet<T> getSet(String name) {
    return SharedDataSet.factory(name, vertx);
  }

  @Override
  public <T> AsyncQueue<T> getQueue(String name) {
    return SharedDataQueue.factory(name, vertx);
  }

  @Override
  public AsyncLock getLock(String name) {
    return SharedDataLock.factory(name, vertx);
  }

  @Override
  public AsyncIdGenerator getIdGenerator(String name) {
    return SharedDataIdGenerator.factory(name, vertx);
  }

}
