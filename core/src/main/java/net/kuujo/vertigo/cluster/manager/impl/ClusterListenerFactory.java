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
package net.kuujo.vertigo.cluster.manager.impl;

import org.vertx.java.core.Vertx;

import com.hazelcast.config.MapConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

/**
 * Cluster membership listener factory.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
class ClusterListenerFactory {
  private final Vertx vertx;

  ClusterListenerFactory(Vertx vertx) {
    this.vertx = vertx;
  }

  /**
   * Returns the Vert.x Hazelcast instance.
   *
   * @return The Vert.x Hazelcast instance.
   */
  static HazelcastInstance getHazelcastInstance() {
    for (HazelcastInstance instance : Hazelcast.getAllHazelcastInstances()) {
      MapConfig map = instance.getConfig().findMapConfig("subs");
      if (map != null && map.getName().equals("subs")) {
        return instance;
      }
    }
    return null;
  }

  /**
   * Creates a cluster listener.
   *
   * @return A new cluster listener.
   */
  public ClusterListener createClusterListener() {
    HazelcastInstance hazelcast = getHazelcastInstance();
    if (hazelcast != null) {
      return new HazelcastClusterListener(hazelcast, vertx);
    } else {
      return new NoClusterListener();
    }
  }

}
