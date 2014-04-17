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

/**
 * The cluster scope indicates the Vertigo clustering method.<p>
 *
 * The scope is used to identify both the current Vert.x cluster
 * type and the scope in which a Vertigo network should be deployed.
 * Networks can be run in either the local or cluster scope regardless
 * of the current Vert.x cluster status, but network coordination is
 * always done at the highest level. For example, if the current Vert.x
 * instance is a Xync clustered instance then coordination will always
 * occur through cluster-wide shared data structures. This ensures
 * Vertigo can synchronize local networks across the cluster and prevent
 * event bus address clashes.
 *
 * @author Jordan Halterman
 */
public enum ClusterScope {

  /**
   * The <code>local</code> scope indicates that the network should be deployed
   * only within the current Vert.x instance.
   */
  LOCAL("local"),

  /**
   * The <code>cluster</code> scope indicates that the network should be deployed
   * across the Vert.x cluster whenever possible.
   */
  CLUSTER("cluster");

  private final String name;

  private ClusterScope(String name) {
    this.name = name;
  }

  @Override
  public String toString() {
    return name;
  }

  /**
   * Parses a scope name.
   *
   * @param name The scope name to parse.
   * @return The scope.
   * @throws IllegalArgumentException If the scope name is invalid.
   */
  public static ClusterScope parse(String name) {
    switch (name) {
      case "local":
        return LOCAL;
      case "cluster":
        return CLUSTER;
      default:
        throw new IllegalArgumentException(name + " is not a valid scope.");
    }
  }

}
