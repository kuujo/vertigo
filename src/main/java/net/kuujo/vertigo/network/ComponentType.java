/*
 * Copyright 2013 the original author or authors.
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
package net.kuujo.vertigo.network;

import net.kuujo.vertigo.hooks.ComponentHook;

import org.vertx.java.core.json.JsonObject;

/**
 * Internal class for adapting the old Component API to new polymorphic component types.
 *
 * @author Jordan Halterman
 *
 * @param <T> The component sub type.
 * @param <U> The component type.
 */
@SuppressWarnings("rawtypes")
abstract class ComponentType<T extends ComponentType<T, U>, U extends net.kuujo.vertigo.component.Component> extends Component<U> {

  public ComponentType() {
  }

  public ComponentType(Class<U> type, String address) {
    super(type, address);
  }

  @Override
  @SuppressWarnings("unchecked")
  T setAddress(String address) {
    super.setAddress(address);
    return (T) this;
  }

  @Override
  @SuppressWarnings("unchecked")
  public T setConfig(JsonObject config) {
    super.setConfig(config);
    return (T) this;
  }

  @Override
  @SuppressWarnings("unchecked")
  public T setNumInstances(int numInstances) {
    super.setNumInstances(numInstances);
    return (T) this;
  }

  @Override
  @SuppressWarnings("unchecked")
  public T setHeartbeatInterval(long interval) {
    super.setHeartbeatInterval(interval);
    return (T) this;
  }

  @Override
  @SuppressWarnings("unchecked")
  public T addHook(ComponentHook hook) {
    super.addHook(hook);
    return (T) this;
  }

}
