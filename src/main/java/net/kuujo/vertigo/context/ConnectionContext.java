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
package net.kuujo.vertigo.context;

/**
 * Connection contexts represent a direct connection between
 * one component instance and another component instance.
 *
 * @author Jordan Halterman
 */
public class ConnectionContext extends Context<ConnectionContext> {
  private String address;

  /**
   * Returns the connection address.
   *
   * @return The connection address.
   */
  public String address() {
    return address;
  }

  /**
   * Connection context builder.
   *
   * @author Jordan Halterman
   */
  public static class Builder {
    private ConnectionContext context;

    private Builder() {
      context = new ConnectionContext();
    }

    private Builder(ConnectionContext context) {
      this.context = context;
    }

    /**
     * Creates a new context builder.
     *
     * @return A new context builder.
     */
    public static Builder newBuilder() {
      return new Builder();
    }

    /**
     * Creates a new context builder.
     *
     * @param context A starting connection context.
     * @return A new context builder.
     */
    public static Builder newBuilder(ConnectionContext context) {
      return new Builder(context);
    }

    /**
     * Sets the connection address.
     *
     * @param address The connection address.
     * @return The context builder.
     */
    public Builder setAddress(String address) {
      context.address = address;
      return this;
    }

    /**
     * Builds the connection context.
     *
     * @return A new connection context.
     */
    public ConnectionContext build() {
      return context;
    }
  }

}
