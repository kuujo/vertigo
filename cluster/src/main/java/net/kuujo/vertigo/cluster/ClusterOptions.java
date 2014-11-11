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

import io.vertx.codegen.annotations.Options;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Vertigo cluster options.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
@Options
public class ClusterOptions {
  private boolean clustered;
  private String clusterAddress = UUID.randomUUID().toString();
  private String nodeAddress = UUID.randomUUID().toString();
  private Set<String> nodes = new HashSet<>();

  public ClusterOptions() {
  }

  public ClusterOptions(ClusterOptions options) {
    this.clustered = options.isClustered();
    this.clusterAddress = options.getClusterAddress();
    this.nodeAddress = options.getNodeAddress();
    this.nodes = options.getNodes();
  }

  @SuppressWarnings("unchecked")
  public ClusterOptions(JsonObject options) {
    this.clustered = options.containsKey("clustered") ? options.getBoolean("clustered") : options.getString("cluster") != null;
    this.clusterAddress = options.getString("cluster", clusterAddress);
    this.nodeAddress = options.getString("node", nodeAddress);
    this.nodes = new HashSet(options.getJsonArray("nodes", new JsonArray()).getList());
  }

  /**
   * Sets whether Vertigo is clustered.
   *
   * @param clustered Indicates whether Vertigo is clustered.
   * @return The cluster options.
   */
  public ClusterOptions setClustered(boolean clustered) {
    this.clustered = clustered;
    return this;
  }

  /**
   * Returns whether Vertigo is clustered.
   *
   * @return Indicates whether Vertigo is clustered.
   */
  public boolean isClustered() {
    return clustered;
  }

  /**
   * Sets the cluster event bus address.
   *
   * @param address The cluster event bus address.
   * @return The cluster options.
   */
  public ClusterOptions setClusterAddress(String address) {
    this.clusterAddress = address;
    return this;
  }

  /**
   * Returns the cluster event bus address.
   *
   * @return The cluster event bus address.
   */
  public String getClusterAddress() {
    return clusterAddress;
  }

  /**
   * Sets the node event bus address.
   *
   * @param address The node event bus address.
   * @return The cluster options.
   */
  public ClusterOptions setNodeAddress(String address) {
    this.nodeAddress = address;
    return this;
  }

  /**
   * Returns the node event bus address.
   *
   * @return The node event bus address.
   */
  public String getNodeAddress() {
    return nodeAddress;
  }

  /**
   * Sets the cluster membership.
   *
   * @param nodes The cluster node addresses.
   * @return The cluster options.
   */
  public ClusterOptions setNodes(Set<String> nodes) {
    this.nodes = nodes;
    return this;
  }

  /**
   * Returns the cluster membership.
   *
   * @return A set of cluster node addresses.
   */
  public Set<String> getNodes() {
    return nodes;
  }

  @Override
  public int hashCode() {
    int hashCode = 27;
    hashCode = 37 * hashCode + (clustered ? 1 : 0);
    hashCode = 37 * hashCode + clusterAddress.hashCode();
    hashCode = 37 * hashCode + nodeAddress.hashCode();
    hashCode = 37 * hashCode + nodes.hashCode();
    return hashCode;
  }

}
