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
package net.kuujo.vertigo.io.port.impl;

import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import net.kuujo.vertigo.io.port.InputPortReference;

/**
 * Input port reference implementation.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class InputPortReferenceImpl<T> implements InputPortReference<T> {
  private final Vertx vertx;
  private final String address;
  private final String name;

  public InputPortReferenceImpl(Vertx vertx, String address, String name) {
    this.vertx = vertx;
    this.address = address;
    this.name = name;
  }

  @Override
  public String name() {
    return name;
  }

  @Override
  public InputPortReference<T> send(T message) {
    vertx.eventBus().send(address, message, new DeliveryOptions().addHeader("port", name));
    return this;
  }

  @Override
  public InputPortReference<T> send(T message, MultiMap headers) {
    vertx.eventBus().send(address, message, new DeliveryOptions().setHeaders(headers.add("port", name)));
    return this;
  }

}
