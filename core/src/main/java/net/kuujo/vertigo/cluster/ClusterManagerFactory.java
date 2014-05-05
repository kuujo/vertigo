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

import org.vertx.java.core.Vertx;
import org.vertx.java.platform.Container;

/**
 * Cluster manager factory.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public interface ClusterManagerFactory {

  /**
   * Sets the current Vertx instance.
   *
   * @param vertx The current Vertx instance.
   * @return The cluster manager factory.
   */
  ClusterManagerFactory setVertx(Vertx vertx);

  /**
   * Sets the current Container instance.
   *
   * @param container The current Vertx container.
   * @return The cluster manager factory.
   */
  ClusterManagerFactory setContainer(Container container);

  /**
   * Creates a new cluster manager.
   *
   * @param address The cluster address.
   * @return The cluster manager.
   */
  ClusterManager createClusterManager(String address);

}
