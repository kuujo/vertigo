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
package net.kuujo.vertigo.io.connection.impl;

import io.vertx.core.json.JsonObject;
import net.kuujo.vertigo.io.connection.EndpointConfig;

/**
 * Connection endpoint.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
abstract class EndpointConfigImpl<T extends EndpointConfig<T>> implements EndpointConfig<T> {
  protected String component;
  protected String port;

  protected EndpointConfigImpl() {
  }

  protected EndpointConfigImpl(T endpoint) {
    this.component = endpoint.getComponent();
    this.port = endpoint.getPort();
  }

  protected EndpointConfigImpl(String component, String port) {
    this.component = component;
    this.port = port;
  }

  protected EndpointConfigImpl(JsonObject endpoint) {
    update(endpoint);
  }

  @Override
  public void update(JsonObject endpoint) {
    if (endpoint.containsKey(ENDPOINT_COMPONENT)) {
      this.component = endpoint.getString(ENDPOINT_COMPONENT);
    }
    if (endpoint.containsKey(ENDPOINT_PORT)) {
      this.port = endpoint.getString(ENDPOINT_PORT);
    }
  }

  @Override
  public JsonObject toJson() {
    JsonObject json = new JsonObject();
    json.put(ENDPOINT_COMPONENT, component);
    json.put(ENDPOINT_PORT, port);
    return json;
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
