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
package net.kuujo.vertigo.cluster.impl;

import net.kuujo.vertigo.cluster.ClusterConfig;
import net.kuujo.vertigo.cluster.ClusterScope;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;

/**
 * Default cluster configuration.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class DefaultClusterConfig implements ClusterConfig {
  private String address;
  private ClusterScope scope = ClusterScope.CLUSTER;

  @Override
  public ClusterConfig setAddress(String address) {
    this.address = address;
    return this;
  }

  @Override
  public String getAddress() {
    return address;
  }

  @Override
  public ClusterConfig setScope(ClusterScope scope) {
    this.scope = scope;
    return this;
  }

  @JsonGetter("scope")
  private String getScopeName() {
    return scope.toString();
  }

  @Override
  public ClusterScope getScope() {
    return scope;
  }

  @JsonSetter("scope")
  private void setScopeName(String scope) {
    this.scope = ClusterScope.parse(scope);
  }

}
