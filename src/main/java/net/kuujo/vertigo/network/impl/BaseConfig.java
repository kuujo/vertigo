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
package net.kuujo.vertigo.network.impl;

import net.kuujo.vertigo.io.selector.Selector;
import net.kuujo.vertigo.network.ComponentConfig;
import net.kuujo.vertigo.network.ComponentConfigurable;
import net.kuujo.vertigo.network.ConnectionConfig;
import net.kuujo.vertigo.network.ConnectionConfigurable;
import net.kuujo.vertigo.network.ModuleConfig;
import net.kuujo.vertigo.network.NetworkConfig;
import net.kuujo.vertigo.network.VerticleConfig;

import org.vertx.java.core.json.JsonObject;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Base configuration.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
abstract class BaseConfig implements ComponentConfigurable, ConnectionConfigurable {
  @JsonIgnore
  private NetworkConfig network;

  protected BaseConfig() {
  }

  protected BaseConfig(NetworkConfig network) {
    this.network = network;
  }

  @Override
  @SuppressWarnings("rawtypes")
  public <U extends ComponentConfig> U addComponent(U component) {
    return network.addComponent(component);
  }

  @Override
  public <U extends ComponentConfig<U>> U addComponent(String name, String moduleOrMain) {
    return network.addComponent(name, moduleOrMain);
  }

  @Override
  public <U extends ComponentConfig<U>> U addComponent(String name, String moduleOrMain, JsonObject config) {
    return network.addComponent(name, moduleOrMain, config);
  }

  @Override
  public <U extends ComponentConfig<U>> U addComponent(String name, String moduleOrMain, int instances) {
    return network.addComponent(name, moduleOrMain, instances);
  }

  @Override
  public <U extends ComponentConfig<U>> U addComponent(String name, String moduleOrMain, JsonObject config, int instances) {
    return network.addComponent(name, moduleOrMain, config, instances);
  }

  @Override
  public <U extends ComponentConfig<U>> U removeComponent(U component) {
    return network.removeComponent(component);
  }

  @Override
  public <U extends ComponentConfig<U>> U removeComponent(String name) {
    return network.removeComponent(name);
  }

  @Override
  public ModuleConfig addModule(ModuleConfig module) {
    return network.addModule(module);
  }

  @Override
  public ModuleConfig addModule(String name, String moduleName) {
    return network.addModule(name, moduleName);
  }

  @Override
  public ModuleConfig addModule(String name, String moduleName, JsonObject config) {
    return network.addModule(name, moduleName, config);
  }

  @Override
  public ModuleConfig addModule(String name, String moduleName, int instances) {
    return network.addModule(name, moduleName, instances);
  }

  @Override
  public ModuleConfig addModule(String name, String moduleName, JsonObject config, int instances) {
    return network.addModule(name, moduleName, config, instances);
  }

  @Override
  public ModuleConfig removeModule(ModuleConfig module) {
    return network.removeModule(module);
  }

  @Override
  public ModuleConfig removeModule(String name) {
    return network.removeModule(name);
  }

  @Override
  public VerticleConfig addVerticle(VerticleConfig verticle) {
    return network.addVerticle(verticle);
  }

  @Override
  public VerticleConfig addVerticle(String name, String main) {
    return network.addVerticle(name, main);
  }

  @Override
  public VerticleConfig addVerticle(String name, String main, JsonObject config) {
    return network.addVerticle(name, main, config);
  }

  @Override
  public VerticleConfig addVerticle(String name, String main, int instances) {
    return network.addVerticle(name, main, instances);
  }

  @Override
  public VerticleConfig addVerticle(String name, String main, JsonObject config, int instances) {
    return network.addVerticle(name, main, config, instances);
  }

  @Override
  public VerticleConfig removeVerticle(VerticleConfig verticle) {
    return network.removeVerticle(verticle);
  }

  @Override
  public VerticleConfig removeVerticle(String name) {
    return network.removeVerticle(name);
  }

  @Override
  public ConnectionConfig createConnection(ConnectionConfig connection) {
    return network.createConnection(connection);
  }

  @Override
  public ConnectionConfig createConnection(String source, String target) {
    return network.createConnection(source, target);
  }

  @Override
  public ConnectionConfig createConnection(String source, String target, Selector selector) {
    return network.createConnection(source, target, selector);
  }

  @Override
  public ConnectionConfig createConnection(String source, String out, String target, String in) {
    return network.createConnection(source, out, target, in);
  }

  @Override
  public ConnectionConfig createConnection(String source, String out, String target, String in, Selector selector) {
    return network.createConnection(source, out, target, in, selector);
  }

  @Override
  public NetworkConfig destroyConnection(ConnectionConfig connection) {
    return network.destroyConnection(connection);
  }

  @Override
  public NetworkConfig destroyConnection(String source, String target) {
    return network.destroyConnection(source, target);
  }

  @Override
  public NetworkConfig destroyConnection(String source, String out, String target, String in) {
    return network.destroyConnection(source, out, target, in);
  }

}
