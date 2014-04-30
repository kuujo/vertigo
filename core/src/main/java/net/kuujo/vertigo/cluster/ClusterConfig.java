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

import net.kuujo.vertigo.Config;
import net.kuujo.vertigo.cluster.impl.DefaultClusterConfig;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Network cluster configuration.<p>
 *
 * The cluster configuration is used by Vertigo to determine how the
 * components of a network should be deployed. When a network is deployed
 * in a Vert.x clustered instance, the network can be deployed across the
 * cluster in what is known as the <code>CLUSTER</code> scope. Users
 * can specify in the cluster configuration how a network should be
 * deployed.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
@JsonTypeInfo(
  use=JsonTypeInfo.Id.CLASS,
  include=JsonTypeInfo.As.PROPERTY,
  property="class",
  defaultImpl=DefaultClusterConfig.class
)
public interface ClusterConfig extends Config<ClusterConfig> {

  /**
   * <code>address</code> is the cluster event bus address.
   */
  public static final String CLUSTER_ADDRESS = "address";

  /**
   * <code>scope</code> is the cluster scope.
   */
  public static final String CLUSTER_SCOPE = "scope";

  /**
   * Sets the cluster address.<p>
   *
   * The cluster address is used by Vertigo to deploy components and work
   * with cluster-wide data structures when the network is clustered. If
   * the network is deployed in <code>LOCAL</code> scope then the cluster
   * address does not need to be set up.
   *
   * @param address The cluster address.
   * @return The cluster configuration.
   */
  ClusterConfig setAddress(String address);

  /**
   * Returns the cluster address.
   *
   * @return The cluster event bus address.
   */
  String getAddress();

  /**
   * Sets the network scope.<p>
   *
   * Setting the network's scope to <code>CLUSTER</code> will result in the
   * network's components being deployed across the cluster when Vert.x is
   * a clustered instance. If the cluster scope is being used you must provide
   * an event bus address to the cluster manager. This is the default scope.<p>
   *
   * If the scope is set to <code>LOCAL</code> then the network will always
   * be deployed in the local Vert.x instance regardless of the Vert.x cluster
   * state.
   *
   * @param scope The network scope.
   * @return The cluster configuration.
   */
  ClusterConfig setScope(ClusterScope scope);

  /**
   * Returns the network scope.
   *
   * @return The network scope. Defaults to <code>CLUSTER</code>
   */
  ClusterScope getScope();

}
