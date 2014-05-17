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

import com.hazelcast.core.MultiMap;

/**
 * Cluster data provider.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
interface ClusterData {

  <K, V> MultiMap<K, V> getMultiMap(String name);

  <K, V> Map<K, V> getMap(String name);

  <T> Set<T> getSet(String name);

  <T> List<T> getList(String name);

  <T> Queue<T> getQueue(String name);

}
