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

import net.kuujo.vertigo.cluster.Cluster;
import net.kuujo.vertigo.cluster.ClusterFactory;

import org.vertx.java.core.Vertx;
import org.vertx.java.platform.Container;

/**
 * Factory for creating clusters for deployment and shared data access.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class DefaultClusterFactory implements ClusterFactory {
  private Vertx vertx;
  private Container container;

  public DefaultClusterFactory(Vertx vertx, Container container) {
    this.vertx = vertx;
    this.container = container;
  }

  @Override
  public ClusterFactory setVertx(Vertx vertx) {
    this.vertx = vertx;
    return this;
  }

  @Override
  public ClusterFactory setContainer(Container container) {
    this.container = container;
    return this;
  }

  @Override
  public Cluster createCluster(String address) {
    return new DefaultCluster(address, vertx, container);
  }

}
