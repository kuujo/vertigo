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

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import net.kuujo.vertigo.io.VertigoMessage;
import net.kuujo.vertigo.io.impl.VertigoMessageImpl;
import net.kuujo.vertigo.io.port.InputPort;
import net.kuujo.vertigo.io.port.OutputPortReference;

/**
 * Output port reference implementation.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class OutputPortReferenceImpl<T> implements OutputPortReference<T> {
  private final Vertx vertx;
  private final String address;
  private final String name;

  public OutputPortReferenceImpl(Vertx vertx, String address, String name) {
    this.vertx = vertx;
    this.address = address;
    this.name = name;
  }

  @Override
  public String name() {
    return name;
  }

  @Override
  public InputPort<T> messageHandler(Handler<VertigoMessage<T>> handler) {
    vertx.eventBus().<T>consumer(address).handler(message -> {
      handler.handle(new VertigoMessageImpl<T>(message.headers().get("id"), name, message.body(), message.headers()));
    });
    return this;
  }

}
