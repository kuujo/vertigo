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
package net.kuujo.vertigo.io.connection;

import io.vertx.core.json.JsonObject;

/**
 * Connection endpoint.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class Endpoint<T extends Endpoint<T>> {

  /**
   * <code>component</code> indicates the endpoint component name.
   */
  public static final String ENDPOINT_COMPONENT = "component";

  /**
   * <code>port</code> indicates the endpoint port.
   */
  public static final String ENDPOINT_PORT = "port";

  protected String component;
  protected String port;

  protected Endpoint() {
  }

  protected Endpoint(T endpoint) {
    this.component = endpoint.getComponent();
    this.port = endpoint.getPort();
  }

  protected Endpoint(JsonObject endpoint) {
    this.component = endpoint.getString(ENDPOINT_COMPONENT);
    this.port = endpoint.getString(ENDPOINT_PORT);
  }

  /**
   * Returns the endpoint component.
   *
   * @return The component name.
   */
  public String getComponent() {
    return component;
  }

  /**
   * Sets the endpoint component.
   *
   * @param component The endpoint component.
   * @return The endpoint instance.
   */
  @SuppressWarnings("unchecked")
  public T setComponent(String component) {
    this.component = component;
    return (T) this;
  }

  /**
   * Returns the endpoint port.
   *
   * @return The endpoint port.
   */
  public String getPort() {
    return port;
  }

  /**
   * Sets the endpoint port.
   *
   * @param port The endpoint port.
   * @return The endpoint instance.
   */
  @SuppressWarnings("unchecked")
  public T setPort(String port) {
    this.port = port;
    return (T) this;
  }

}
