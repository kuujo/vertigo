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
package net.kuujo.vertigo.connection.impl;

import io.vertx.core.json.JsonObject;
import net.kuujo.vertigo.connection.EndpointDefinition;

/**
 * Connection endpoint.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class EndpointDefinitionImpl<T extends EndpointDefinition<T>> implements EndpointDefinition<T> {

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

  protected EndpointDefinitionImpl() {
  }

  protected EndpointDefinitionImpl(T endpoint) {
    this.component = endpoint.getComponent();
    this.port = endpoint.getPort();
  }

  protected EndpointDefinitionImpl(JsonObject endpoint) {
    this.component = endpoint.getString(ENDPOINT_COMPONENT);
    this.port = endpoint.getString(ENDPOINT_PORT);
  }

  @Override
  public String getComponent() {
    return component;
  }

  @Override
  @SuppressWarnings("unchecked")
  public T setComponent(String component) {
    this.component = component;
    return (T) this;
  }

  @Override
  public String getPort() {
    return port;
  }

  @Override
  @SuppressWarnings("unchecked")
  public T setPort(String port) {
    this.port = port;
    return (T) this;
  }

}
