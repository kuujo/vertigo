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
package net.kuujo.vertigo.cluster;

import net.kuujo.vertigo.annotations.ClusterType;
import net.kuujo.vertigo.annotations.Factory;

import org.vertx.java.core.Vertx;
import org.vertx.java.platform.Container;
import org.vertx.java.platform.Verticle;

/**
 * Remote cluster manager implementation.<p>
 *
 * The remote cluster manager is backed by {@link RemoteCluster} which relies
 * upon the Xync event bus API for fault-tolerant deployments and Hazelcast
 * data structure access. This means the remote cluster manager depends upon
 * the Xync platform manager or a similar event bus API for operation.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
@ClusterType
public class RemoteClusterManager extends AbstractClusterManager {

  @Factory
  public static RemoteClusterManager factory(Vertx vertx, Container container) {
    return new RemoteClusterManager(vertx, container);
  }

  public RemoteClusterManager(Verticle verticle) {
    this(verticle.getVertx(), verticle.getContainer());
  }

  public RemoteClusterManager(Vertx vertx, Container container) {
    super(vertx, container, new RemoteCluster(vertx));
  }

  @Override
  public ClusterScope scope() {
    return ClusterScope.CLUSTER;
  }

}
