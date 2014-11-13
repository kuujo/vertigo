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
import net.kuujo.vertigo.builder.InputBuilder;
import net.kuujo.vertigo.builder.OutputBuilder;
import net.kuujo.vertigo.builder.OutputPortBuilder;
import net.kuujo.vertigo.io.port.OutputPortInfo;

/**
 * Output port builder implementation.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class OutputPortBuilderImpl implements OutputPortBuilder {
  private final ComponentBuilderImpl component;
  private final OutputPortInfo port;

  public OutputPortBuilderImpl(ComponentBuilderImpl component, OutputPortInfo port) {
    this.component = component;
    this.port = port;
  }

  @Override
  public OutputPortBuilder port(String name) {
    return new OutputPortBuilderImpl(component, component.component.getOutput().addPort(name));
  }

  @Override
  public OutputBuilder identifier(String identifier) {
    component.identifier(identifier);
    return this;
  }

  @Override
  public OutputBuilder config(JsonObject config) {
    component.config(config);
    return this;
  }

  @Override
  public OutputBuilder worker() {
    component.worker();
    return this;
  }

  @Override
  public OutputBuilder worker(boolean worker) {
    component.worker(worker);
    return this;
  }

  @Override
  public OutputBuilder multiThreaded() {
    component.multiThreaded();
    return this;
  }

  @Override
  public OutputBuilder multiThreaded(boolean multiThreaded) {
    component.multiThreaded(multiThreaded);
    return this;
  }

  @Override
  public OutputBuilder stateful() {
    component.stateful();
    return this;
  }

  @Override
  public OutputBuilder stateful(boolean stateful) {
    component.stateful(stateful);
    return this;
  }

  @Override
  public OutputBuilder replicas(int replicas) {
    component.replicas(replicas);
    return this;
  }

  @Override
  public OutputPortBuilder type(Class<?> type) {
    port.setType(type);
    return this;
  }

  @Override
  public OutputPortBuilder codec(Class<? extends MessageCodec> codec) {
    port.setCodec(codec);
    return this;
  }

  @Override
  public OutputPortBuilder persistent() {
    port.setPersistent(true);
    return this;
  }

  @Override
  public OutputPortBuilder persistent(boolean persistent) {
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
