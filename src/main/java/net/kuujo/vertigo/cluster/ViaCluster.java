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

import net.kuujo.vertigo.coordinator.RemoteCoordinator;

/**
 * A Via-based cluster implementation.
 *
 * The Via cluster supports deploying networks across a cluster of Vert.x instances
 * using an event bus based deployment mechanism. Rather than deploying modules
 * and verticles using the Vert.x container, Via sends messages to supervisors
 * on different Vert.x instances in a cluster, with each supervisor deploying
 * and monitoring modules and verticles within its own instance. This results in
 * a much more reliable network deployment as Via can reassign deployments to
 * new nodes when an existing node dies.
 *
 * @author Jordan Halterman
 */
public class ViaCluster extends AbstractCluster {

  public ViaCluster(Vertx vertx, Container container, String address) {
    super(vertx, container);
    coordinator = RemoteCoordinator.class.getName();
    this.master = address;
  }

}
