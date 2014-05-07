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

import java.util.HashMap;
import java.util.Map;

import net.kuujo.vertigo.cluster.impl.DefaultClusterFactory;

import org.vertx.java.core.Vertx;
import org.vertx.java.platform.Container;

/**
 * Cluster factory.<p>
 *
 * The cluster factory implementation can be configured using the
 * <code>net.kuujo.vertigo.cluster-factory</code> system property.
 * Any custom cluster factories must extend this class.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public abstract class ClusterFactory {
  private static final String CLUSTER_FACTORY_PROPERTY_NAME = "net.kuujo.vertigo.cluster-factory";
  private static ClusterFactory instance;
  private static final Map<String, Cluster> clusters = new HashMap<>();
  protected Vertx vertx;
  protected Container container;

  @SuppressWarnings("unchecked")
  private static ClusterFactory getInstance(Vertx vertx, Container container) {
    if (instance == null) {
      String className = null;
      try {
        className = System.getProperty(CLUSTER_FACTORY_PROPERTY_NAME);
      } catch (Exception e) {
      }

      if (className != null) {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        try {
          Class<? extends ClusterFactory> clazz = (Class<? extends ClusterFactory>) loader.loadClass(className);
          instance = clazz.newInstance().setVertx(vertx).setContainer(container);
        } catch (Exception e) {
          throw new IllegalArgumentException("Error instantiating cluster factory.", e);
        }
      } else {
        instance = new DefaultClusterFactory().setVertx(vertx).setContainer(container);
      }
    }
    return instance;
  }

  /**
   * Gets the cluster client for the given cluster.
   *
   * @param address The cluster address.
   * @return The cluster client.
   */
  public static Cluster getCluster(String address, Vertx vertx, Container container) {
    Cluster cluster = clusters.get(address);
    if (cluster == null) {
      cluster = getInstance(vertx, container).createCluster(address);
      clusters.put(address, cluster);
    }
    return cluster;
  }

  /**
   * Sets the factory vertx instance.
   *
   * @param vertx The current Vertx instance.
   * @return The cluster factory.
   */
  protected ClusterFactory setVertx(Vertx vertx) {
    this.vertx = vertx;
    return this;
  }

  /**
   * Sets the factory container instance.
   *
   * @param container The current Vert.x container.
   * @return The cluster factory.
   */
  protected ClusterFactory setContainer(Container container) {
    this.container = container;
    return this;
  }

  /**
   * Creates a new cluster client.
   *
   * @param address The cluster address.
   * @return The cluster.
   */
  public abstract Cluster createCluster(String address);

}
