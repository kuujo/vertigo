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

import net.kuujo.vertigo.cluster.ClusterScope;
import net.kuujo.vertigo.cluster.ClusterType;
import net.kuujo.vertigo.util.Factory;

import org.vertx.java.core.Vertx;
import org.vertx.java.platform.Container;
import org.vertx.java.platform.Verticle;

/**
 * Xync-based cluster manager implementation.<p>
 *
 * The Xync cluster manager is backed by {@link RemoteCluster} which relies
 * upon the Xync event bus API for fault-tolerant deployments and Hazelcast
 * data structure access. This means the Xync cluster manager depends upon
 * the Xync platform manager or a similar event bus API for operation.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
@ClusterType
public class RemoteClusterManager extends AbstractClusterManager {

  @Factory
  public static RemoteClusterManager factory(String address, Vertx vertx, Container container) {
    return new RemoteClusterManager(address, vertx, container);
  }

  public RemoteClusterManager(String address, Verticle verticle) {
    this(address, verticle.getVertx(), verticle.getContainer());
  }

  public RemoteClusterManager(String address, Vertx vertx, Container container) {
    super(vertx, container, new RemoteCluster(address, vertx, container));
  }

  @Override
  public ClusterScope scope() {
    return ClusterScope.CLUSTER;
  }

}
