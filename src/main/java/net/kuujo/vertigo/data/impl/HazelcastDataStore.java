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

import net.kuujo.vertigo.annotations.Factory;
import net.kuujo.vertigo.annotations.ClusterType;
import net.kuujo.vertigo.data.DataStore;
import net.kuujo.vertigo.data.AsyncIdGenerator;
import net.kuujo.vertigo.data.AsyncList;
import net.kuujo.vertigo.data.AsyncLock;
import net.kuujo.vertigo.data.AsyncMap;
import net.kuujo.vertigo.data.AsyncQueue;
import net.kuujo.vertigo.data.AsyncSet;
import net.kuujo.xync.data.impl.XyncAsyncIdGenerator;
import net.kuujo.xync.data.impl.XyncAsyncList;
import net.kuujo.xync.data.impl.XyncAsyncLock;
import net.kuujo.xync.data.impl.XyncAsyncMap;
import net.kuujo.xync.data.impl.XyncAsyncQueue;
import net.kuujo.xync.data.impl.XyncAsyncSet;

import org.vertx.java.core.Vertx;

/**
 * Xync-based hazelcast cluster data store.
 *
 * @author Jordan Halterman
 */
@ClusterType
public class HazelcastDataStore implements DataStore {
  private final Vertx vertx;

  @Factory
  public static HazelcastDataStore factory(Vertx vertx) {
    return new HazelcastDataStore(vertx);
  }

  public HazelcastDataStore(Vertx vertx) {
    this.vertx = vertx;
  }

  @Override
  public <K, V> AsyncMap<K, V> getMap(String name) {
    return new XyncMap<K, V>(new XyncAsyncMap<K, V>(name, vertx.eventBus()));
  }

  @Override
  public <T> AsyncList<T> getList(String name) {
    return new XyncList<T>(new XyncAsyncList<T>(name, vertx.eventBus()));
  }

  @Override
  public <T> AsyncSet<T> getSet(String name) {
    return new XyncSet<T>(new XyncAsyncSet<T>(name, vertx.eventBus()));
  }

  @Override
  public <T> AsyncQueue<T> getQueue(String name) {
    return new XyncQueue<T>(new XyncAsyncQueue<T>(name, vertx.eventBus()));
  }

  @Override
  public AsyncLock getLock(String name) {
    return new XyncLock(new XyncAsyncLock(name, vertx.eventBus()));
  }

  @Override
  public AsyncIdGenerator getIdGenerator(String name) {
    return new XyncIdGenerator(new XyncAsyncIdGenerator(name, vertx.eventBus()));
  }

}
