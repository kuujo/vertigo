/*
 * Copyright 2013-2014 the original author or authors.
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
package net.kuujo.vertigo.component;

import net.kuujo.vertigo.cluster.Cluster;
import net.kuujo.vertigo.io.InputCollector;
import net.kuujo.vertigo.io.OutputCollector;
import net.kuujo.vertigo.network.NetworkConfig;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.platform.Container;

/**
 * The component is the primary unit of processing in Vertigo. Vertigo
 * networks consist of any number of components that are arbitrarily
 * connected to one another and communicate with one another using
 * message passing through internally-defined input and output ports.<p>
 *
 * Components receive messages on input ports and send messages on output
 * ports, but component-to-component relationships are defined externally
 * to the component implementation in the {@link NetworkConfig}. This means
 * that when a message is sent to an output port, the component doesn't
 * know where the message will go. Instead, Vertigo handles message routing
 * internally, abstracting relationship details from component implementations.<p>
 *
 * Each component may define any number of input and output ports. Ports are
 * created lazily by simply referencing them in {@link InputCollector} and
 * {@link OutputCollector} interfaces. If a message is sent to a port that
 * has no connections then the message will simply disappear.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public interface Component {

  /**
   * Gets the component Vertx instance.
   *
   * @return The component Vertx instance.
   */
  Vertx vertx();

  /**
   * Gets the component container instance.
   *
   * @return The component container instance.
   */
  Container container();

  /**
   * Returns the component's {@link InputCollector}. This is the element of the
   * component which provides an interface for receiving messages from other components.
   *
   * @return The components {@link InputCollector}.
   */
  InputCollector input();

  /**
   * Returns the component's {@link OutputCollector}. This is the element of the
   * component which provides an interface for sending messages to other components.
   *
   * @return The component's {@link OutputCollector}.
   */
  OutputCollector output();

  /**
   * Returns the component instance context.
   *
   * The instance context can be used to retrieve useful information about an
   * entire network.
   *
   * @return The instance context.
   */
  InstanceContext context();

  /**
   * Returns the cluster to which the component belongs.<p>
   *
   * The cluster type is dependent upon the method by which the component's
   * network was deployed. If the network was deployed in local-mode then the
   * cluster will be locally supported. If the network was deployed in cluster-mode
   * then the cluster will be backed by fault-tolerant data structures.
   *
   * @return The cluster to which the component belongs.
   */
  Cluster cluster();

  /**
   * Returns the instance logger. This is a special logger that references the
   * Vertigo component instance and can allow for fine grained control of logging
   * within Vertigo components.
   *
   * @return The logger for the component instance.
   */
  Logger logger();

  /**
   * Starts the component.
   *
   * When the component is started, the component will use a {@link ComponentCoordinator}
   * to retrieve its context from the current cluster context. This means that
   * input and output connections are not set up until the component has been started.
   *
   * @return The component instance.
   */
  Component start();

  /**
   * Starts the component.<p>
   *
   * When the component is started, the component will use a {@link ComponentCoordinator}
   * to retrieve its context from the current cluster context. This means that
   * input and output connections are not set up until the component has been started.
   *
   * @param doneHandler An asynchronous handler to be called once the component is started.
   * @return The component instance.
   */
  Component start(Handler<AsyncResult<Component>> doneHandler);

}
