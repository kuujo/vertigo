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
import org.vertx.java.core.json.JsonObject;

/**
 * Redis-based data store.
 *
 * @author Jordan Halterman
 */
@LocalType
@ClusterType
public class RedisDataStore implements DataStore {

  /**
   * Indicates the address configuration field.
   */
  public static final String ADDRESS = "address";

  private final String address;
  private final Vertx vertx;

  @Factory
  public static RedisDataStore factory(JsonObject config, Vertx vertx) {
    if (!config.containsField("address")) throw new IllegalArgumentException("No Redis address specified.");
    return new RedisDataStore(config.getString("address"), vertx);
  }

  private RedisDataStore(String address, Vertx vertx) {
    this.address = address;
    this.vertx = vertx;
  }

  @Override
  public <K, V> AsyncMap<K, V> getMap(String name) {
    return RedisMap.factory(name, address, vertx);
  }

  @Override
  public <T> AsyncList<T> getList(String name) {
    return RedisList.factory(name, address, vertx);
  }

  @Override
  public <T> AsyncSet<T> getSet(String name) {
    return RedisSet.factory(name, address, vertx);
  }

  @Override
  public <T> AsyncQueue<T> getQueue(String name) {
    return RedisQueue.factory(name, address, vertx);
  }

  @Override
  public AsyncLock getLock(String name) {
    return RedisLock.factory(name, address, vertx);
  }

  @Override
  public AsyncIdGenerator getIdGenerator(String name) {
    return RedisIdGenerator.factory(name, address, vertx);
  }

}
