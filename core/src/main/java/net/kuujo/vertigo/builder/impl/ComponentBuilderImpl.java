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

import io.vertx.core.json.JsonObject;
import net.kuujo.vertigo.builder.ComponentBuilder;
import net.kuujo.vertigo.builder.ConnectionSourceBuilder;
import net.kuujo.vertigo.builder.ConnectionSourceComponentBuilder;
import net.kuujo.vertigo.builder.InputBuilder;
import net.kuujo.vertigo.builder.NetworkBuilder;
import net.kuujo.vertigo.builder.OutputBuilder;
import net.kuujo.vertigo.component.ComponentConfig;

/**
 * Component builder implementation.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class ComponentBuilderImpl implements ComponentBuilder {
  private final NetworkBuilder network;
  final ComponentConfig component;

  public ComponentBuilderImpl(NetworkBuilder network, ComponentConfig component) {
    this.network = network;
    this.component = component;
  }

  @Override
  public ComponentBuilder name(String name) {
    component.setName(name);
    return this;
  }

  @Override
  public ComponentBuilder identifier(String identifier) {
    component.setIdentifier(identifier);
    return this;
  }

  @Override
  public ComponentBuilder config(JsonObject config) {
    component.setConfig(config);
    return this;
  }

  @Override
  public ComponentBuilder worker() {
    component.setWorker(true);
    return this;
  }

  @Override
  public ComponentBuilder worker(boolean worker) {
    component.setWorker(worker);
    return this;
  }

  @Override
  public ComponentBuilder multiThreaded() {
    component.setMultiThreaded(true);
    return this;
  }

  @Override
  public ComponentBuilder multiThreaded(boolean multiThreaded) {
    component.setMultiThreaded(multiThreaded);
    return this;
  }

  @Override
  public ComponentBuilder stateful() {
    component.setStateful(true);
    return this;
  }

  @Override
  public ComponentBuilder stateful(boolean stateful) {
    component.setStateful(stateful);
    return this;
  }

  @Override
  public ComponentBuilder replicas(int replicas) {
    component.setReplicas(replicas);
    return this;
  }

  @Override
  public InputBuilder input() {
    return null;
  }

  @Override
  public OutputBuilder output() {
    return null;
  }

  @Override
  public ComponentBuilder component() {
    return network.component();
  }

  @Override
  public ComponentBuilder component(String name) {
    return network.component(name);
  }

  @Override
  public ConnectionSourceBuilder connect() {
    return network.connect();
  }

  @Override
  public ConnectionSourceComponentBuilder connect(String component) {
    return network.connect(component);
  }

  @Override
  public ComponentBuilder component(String name, String identifier) {
    return component(name).identifier(identifier);
  }

}
