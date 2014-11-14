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
package net.kuujo.vertigo.builder.impl;

import io.vertx.core.eventbus.MessageCodec;
import io.vertx.core.json.JsonObject;
import net.kuujo.vertigo.builder.*;
import net.kuujo.vertigo.io.port.InputPortConfig;

/**
 * Input port builder implementation.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class InputPortBuilderImpl implements InputPortBuilder {
  private final ComponentBuilderImpl component;
  private final InputPortConfig port;

  public InputPortBuilderImpl(ComponentBuilderImpl component, InputPortConfig port) {
    this.component = component;
    this.port = port;
  }

  @Override
  public InputPortBuilder port(String name) {
    return new InputPortBuilderImpl(component, component.component.getInput().addPort(name));
  }

  @Override
  public InputBuilder identifier(String identifier) {
    component.identifier(identifier);
    return this;
  }

  @Override
  public InputBuilder config(JsonObject config) {
    component.config(config);
    return this;
  }

  @Override
  public InputBuilder worker() {
    component.worker();
    return this;
  }

  @Override
  public InputBuilder worker(boolean worker) {
    component.worker(worker);
    return this;
  }

  @Override
  public InputBuilder multiThreaded() {
    component.multiThreaded();
    return this;
  }

  @Override
  public InputBuilder multiThreaded(boolean multiThreaded) {
    component.multiThreaded(multiThreaded);
    return this;
  }

  @Override
  public InputBuilder stateful() {
    component.stateful();
    return this;
  }

  @Override
  public InputBuilder stateful(boolean stateful) {
    component.stateful(stateful);
    return this;
  }

  @Override
  public InputBuilder replicas(int replicas) {
    component.replicas(replicas);
    return this;
  }

  @Override
  public InputPortBuilder type(Class<?> type) {
    port.setType(type);
    return this;
  }

  @Override
  public InputPortBuilder codec(Class<? extends MessageCodec> codec) {
    port.setCodec(codec);
    return this;
  }

  @Override
  public InputPortBuilder persistent() {
    port.setPersistent(true);
    return this;
  }

  @Override
  public InputPortBuilder persistent(boolean persistent) {
    port.setPersistent(persistent);
    return this;
  }

  @Override
  public InputBuilder input() {
    return new InputBuilderImpl(component);
  }

  @Override
  public OutputBuilder output() {
    return new OutputBuilderImpl(component);
  }

}
