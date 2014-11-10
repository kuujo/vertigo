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
package net.kuujo.vertigo.spi;

import io.vertx.core.Vertx;
import net.kuujo.vertigo.cluster.Cluster;

/**
 * Vertigo cluster factory.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public interface ClusterFactory {

  /**
   * Creates a new cluster instance.
   *
   * @param vertx The Vert.x instance.
   * @return A new cluster instance.
   */
  Cluster createCluster(Vertx vertx);

  /**
   * Creates a new cluster proxy.
   *
   * @param vertx The Vert.x instance.
   * @return A new cluster proxy instance.
   */
  Cluster createClusterProxy(Vertx vertx);

}
