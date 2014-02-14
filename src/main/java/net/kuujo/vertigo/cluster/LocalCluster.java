/*
 * Copyright 2013 the original author or authors.
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
import org.vertx.java.platform.Verticle;

import net.kuujo.vertigo.coordinator.LocalCoordinator;

/**
 * A local cluster implementation.<p>
 *
 * The local cluster deploys Vertigo components (modules and verticles) within
 * the local Vert.x instance via the Vert.x {@link Container}.
 *
 * @author Jordan Halterman
 */
public class LocalCluster extends AbstractCluster {

  public LocalCluster(Verticle verticle) {
    super(verticle);
    coordinator = LocalCoordinator.class.getName();
  }

  public LocalCluster(Vertx vertx, Container container) {
    super(vertx, container);
    coordinator = LocalCoordinator.class.getName();
  }

}