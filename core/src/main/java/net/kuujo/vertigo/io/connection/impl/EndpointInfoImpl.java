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

import net.kuujo.vertigo.io.connection.EndpointInfo;
import net.kuujo.vertigo.io.connection.EndpointDescriptor;

/**
 * Connection endpoint.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class EndpointInfoImpl<T extends EndpointInfo<T>> implements EndpointInfo<T> {

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

  protected EndpointInfoImpl() {
  }

  protected EndpointInfoImpl(T endpoint) {
    this.component = endpoint.getComponent();
    this.port = endpoint.getPort();
  }

  protected EndpointInfoImpl(EndpointDescriptor endpoint) {
    this.component = endpoint.component();
    this.port = endpoint.port();
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
