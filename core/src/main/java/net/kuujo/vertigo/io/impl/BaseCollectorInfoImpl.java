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
package net.kuujo.vertigo.io.impl;

import io.vertx.core.json.JsonObject;
import net.kuujo.vertigo.VertigoException;
import net.kuujo.vertigo.io.CollectorInfo;
import net.kuujo.vertigo.io.port.PortInfo;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * Base collector info implementation.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
abstract class BaseCollectorInfoImpl<T extends PortInfo<T>> implements CollectorInfo<T> {
  private Map<String, T> ports = new HashMap<>();
  private final BiFunction<String, Class<?>, T> factory;

  protected BaseCollectorInfoImpl(JsonObject collector, BiFunction<String, Class<?>, T> factory) {
    this.factory = factory;
    for (String key : collector.fieldNames()) {
      try {
        ports.put(key, factory.apply(key, Class.forName(collector.getString(key))));
      } catch (ClassNotFoundException e) {
        throw new VertigoException(e);
      }
    }
  }

  @Override
  public Collection<T> getPorts() {
    return ports.values();
  }

  @Override
  public CollectorInfo setPorts(Collection<T> ports) {
    this.ports = new HashMap<>(ports.size());
    for (T port : ports) {
      this.ports.put(port.getName(), port);
    }
    return this;
  }

  @Override
  public T getPort(String name) {
    return ports.get(name);
  }

  @Override
  public CollectorInfo setPort(String name, Class<?> type) {
    ports.put(name, factory.apply(name, type));
    return this;
  }

}
