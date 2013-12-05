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
package net.kuujo.vertigo;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Container;

import net.kuujo.vertigo.cluster.LocalCluster;
import net.kuujo.vertigo.cluster.ViaCluster;
import net.kuujo.vertigo.component.Component;
import net.kuujo.vertigo.context.InstanceContext;
import net.kuujo.vertigo.context.NetworkContext;
import net.kuujo.vertigo.network.Network;

/**
 * The primary Vertigo API.
 *
 * @author Jordan Halterman
 *
 * @param <T> The current component instance type.
 */
public interface Vertigo<T extends Component<T>> {

  /**
   * Indicates whether the current Vertigo instance is a component instance.
   *
   * @return
   *   Whether the current instance is a component.
   */
  boolean isComponent();

  /**
   * Gets the current component instance.
   *
   * @return
   *   The current component instance. If the current Vertigo instance is not
   *   a component then this value will be null.
   */
  T component();

  /**
   * Gets the component configuration.
   *
   * @return
   *   The component configuration. If the current Vertigo instance is not a
   *   component then this value will be null.
   */
  JsonObject config();

  /**
   * Gets the current component instance context.
   *
   * @return
   *   The current component instance context.If the current Vertigo instance is
   *   not a component then this value will be null.
   */
  InstanceContext<T> context();

  /**
   * Creates a new network.
   *
   * @param address
   *   The network address.
   * @return
   *   A new network instance.
   */
  public Network createNetwork(String address);

  /**
   * Deploys a network within the current Vert.x instance.
   *
   * This deployment method uses the basic {@link LocalCluster} cluster implementation
   * to deploy network verticles and modules using the current Vert.x {@link Container}
   * instance.
   *
   * @param network
   *   The network to deploy.
   * @return
   *   The called Vertigo instance.
   */
  public Vertigo<T> deployLocalNetwork(Network network);

  /**
   * Deploys a network within the current Vert.x instance.
   *
   * This deployment method uses the basic {@link LocalCluster} cluster implementation
   * to deploy network verticles and modules using the current Vert.x {@link Container}
   * instance.
   *
   * @param network
   *   The network to deploy.
   * @param doneHandler
   *   An asynchronous handler to be called once deployment is complete. The handler
   *   will be called with the deployed network context which contains information
   *   about the network's component locations and event bus addresses.
   * @return
   *   The called Vertigo instance.
   */
  public Vertigo<T> deployLocalNetwork(Network network, Handler<AsyncResult<NetworkContext>> doneHandler);

  /**
   * Deploys a network via the Vert.x event bus.
   *
   * Deployment is performed using a {@link ViaCluster} instance which communicates
   * module and verticle deployments over the event bus rather than performing
   * deployments directly using the Vert.x {@link Container}.C
   *
   * @param address
   *   The address to which to communicate component deployments. This address
   *   will receive message-based commands which contain information on which
   *   modules or verticles to deploy. It is up to the handler at this address
   *   to assign those deployments to specific Vert.x instances or otherwise
   *   handle deployments via the Vert.x {@link Container}.
   * @param network
   *   The network to deploy.
   * @return
   *   The called Vertigo instance.
   */
  public Vertigo<T> deployRemoteNetwork(String address, Network network);

  /**
   * Deploys a network via the Vert.x event bus.
   *
   * Deployment is performed using a {@link ViaCluster} instance which communicates
   * module and verticle deployments over the event bus rather than performing
   * deployments directly using the Vert.x {@link Container}.C
   *
   * @param address
   *   The address to which to communicate component deployments. This address
   *   will receive message-based commands which contain information on which
   *   modules or verticles to deploy. It is up to the handler at this address
   *   to assign those deployments to specific Vert.x instances or otherwise
   *   handle deployments via the Vert.x {@link Container}.
   * @param network
   *   The network to deploy.
   * @param doneHandler
   *   An asynchronous handler to be called once deployment is complete. The handler
   *   will be called with the deployed network context which contains information
   *   about the network's component locations and event bus addresses.
   * @return
   *   The called Vertigo instance.
   */
  public Vertigo<T> deployRemoteNetwork(String address, Network network, Handler<AsyncResult<NetworkContext>> doneHandler);

}
