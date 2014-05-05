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

import net.kuujo.vertigo.cluster.ClusterManager;
import net.kuujo.vertigo.cluster.ClusterManagerFactory;

import org.vertx.java.core.Vertx;
import org.vertx.java.platform.Container;

/**
 * Factory for creating cluster managers.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class DefaultClusterManagerFactory implements ClusterManagerFactory {
  private Vertx vertx;
  private Container container;

  public DefaultClusterManagerFactory(Vertx vertx, Container container) {
    this.vertx = vertx;
    this.container = container;
  }

  @Override
  public ClusterManagerFactory setVertx(Vertx vertx) {
    this.vertx = vertx;
    return this;
  }

  @Override
  public ClusterManagerFactory setContainer(Container container) {
    this.container = container;
    return this;
  }

  @Override
  public ClusterManager createClusterManager(String address) {
    return new DefaultClusterManager(address, vertx, container);
  }

}
