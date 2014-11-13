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

import io.vertx.core.eventbus.MessageCodec;
import net.kuujo.vertigo.impl.BaseContextImpl;
import net.kuujo.vertigo.io.connection.ConnectionContext;
import net.kuujo.vertigo.io.port.PortContext;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Base port context implementation.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
abstract class BasePortContextImpl<T extends PortContext<T, U>, U extends ConnectionContext<U, T>> extends BaseContextImpl<T> implements PortContext<T, U> {
  protected String name;
  protected Class<?> type;
  protected Class<? extends MessageCodec> codec;
  protected boolean persistent;
  protected Collection<U> connections = new ArrayList<>();

  @Override
  public String name() {
    return name;
  }

  @Override
  public Class<?> type() {
    return type;
  }

  @Override
  public Class<? extends MessageCodec> codec() {
    return codec;
  }

  @Override
  public boolean persistent() {
    return persistent;
  }

  @Override
  public Collection<U> connections() {
    return connections;
  }

}
