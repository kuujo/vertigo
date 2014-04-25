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

import net.kuujo.vertigo.util.Factory;

import org.vertx.java.core.Vertx;
import org.vertx.java.core.shareddata.SharedData;
import org.vertx.java.platform.Container;
import org.vertx.java.platform.Verticle;

/**
 * Local cluster manager implementation.<p>
 *
 * The local cluster manager is backed by {@link LocalCluster} which is simply
 * an interface to the Vert.x {@link Container} and {@link SharedData}. Thus,
 * deployments and coordination via the <code>LocalClusterManager</code> is
 * is inherently done within a single Vert.x instance.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
@LocalType
public class LocalClusterManager extends AbstractClusterManager {

  @Factory
  public static LocalClusterManager factory(Vertx vertx, Container container) {
    return new LocalClusterManager(vertx, container);
  }

  public LocalClusterManager(Verticle verticle) {
    this(verticle.getVertx(), verticle.getContainer());
  }

  public LocalClusterManager(Vertx vertx, Container container) {
    super(vertx, container, new LocalCluster(vertx, container));
  }

  @Override
  public ClusterScope scope() {
    return ClusterScope.LOCAL;
  }

}
