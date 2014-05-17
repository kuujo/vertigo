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
package net.kuujo.vertigo.cluster.impl;

import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.MultiMap;

/**
 * Hazelcast-based cluster data provider.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
class HazelcastClusterData implements ClusterData {
  private final HazelcastInstance hazelcast;

  public HazelcastClusterData(HazelcastInstance hazelcast) {
    this.hazelcast = hazelcast;
  }

  @Override
  public <K, V> MultiMap<K, V> getMultiMap(String name) {
    return hazelcast.getMultiMap(name);
  }

  @Override
  public <K, V> Map<K, V> getMap(String name) {
    return hazelcast.getMap(name);
  }

  @Override
  public <T> Set<T> getSet(String name) {
    return hazelcast.getSet(name);
  }

  @Override
  public <T> List<T> getList(String name) {
    return hazelcast.getList(name);
  }

  @Override
  public <T> Queue<T> getQueue(String name) {
    return hazelcast.getQueue(name);
  }

}
