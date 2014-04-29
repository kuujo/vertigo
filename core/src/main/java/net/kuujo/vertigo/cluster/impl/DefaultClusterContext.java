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

import net.kuujo.vertigo.cluster.ClusterContext;
import net.kuujo.vertigo.cluster.ClusterScope;
import net.kuujo.vertigo.impl.BaseContext;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;

/**
 * Default cluster context implementation.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class DefaultClusterContext extends BaseContext<ClusterContext> implements ClusterContext {
  private String address;
  private ClusterScope scope;

  @Override
  public String address() {
    return address;
  }

  @Override
  public ClusterScope scope() {
    return scope;
  }

  @JsonGetter("scope")
  private String getScope() {
    return scope.toString();
  }

  @JsonSetter("scope")
  private void setScope(String scope) {
    this.scope = ClusterScope.parse(scope);
  }

  /**
   * Cluster context builder.
   *
   * @author Jordan Halterman
   */
  public static class Builder extends BaseContext.Builder<Builder, DefaultClusterContext> {

    private Builder() {
      super(new DefaultClusterContext());
    }

    private Builder(DefaultClusterContext context) {
      super(context);
    }

    /**
     * Returns a new context builder.
     *
     * @return A new context builder.
     */
    public static Builder newBuilder() {
      return new Builder();
    }

    /**
     * Creates a new context builder from an existing context.
     *
     * @param context The starting context.
     * @return A new context builder.
     */
    public static Builder newBuilder(DefaultClusterContext context) {
      return new Builder(context);
    }

    /**
     * Sets the cluster address.
     *
     * @param address The cluster address.
     * @return The context builder.
     */
    public Builder setAddress(String address) {
      context.address = address;
      return this;
    }

    /**
     * Sets the cluster scope.
     *
     * @param scope The cluster scope.
     * @return The context builder.
     */
    public Builder setScope(ClusterScope scope) {
      context.scope = scope;
      return this;
    }

  }

}
