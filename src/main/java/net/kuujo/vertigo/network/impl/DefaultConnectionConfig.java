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

import org.vertx.java.core.json.JsonObject;

import net.kuujo.vertigo.input.grouping.AllGrouping;
import net.kuujo.vertigo.input.grouping.FieldsGrouping;
import net.kuujo.vertigo.input.grouping.Grouping;
import net.kuujo.vertigo.input.grouping.RandomGrouping;
import net.kuujo.vertigo.input.grouping.RoundGrouping;
import net.kuujo.vertigo.network.ComponentConfig;
import net.kuujo.vertigo.network.ConnectionConfig;
import net.kuujo.vertigo.network.ModuleConfig;
import net.kuujo.vertigo.network.NetworkConfig;
import net.kuujo.vertigo.network.VerticleConfig;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Default connection configuration implementation.
 * 
 * @author Jordan Halterman
 */
public class DefaultConnectionConfig implements ConnectionConfig {
  private static final String DEFAULT_OUT_PORT = "out";
  private static final String DEFAULT_IN_PORT = "in";

  private Source source = new DefaultSource();
  private Target target = new DefaultTarget();
  private Grouping grouping;
  @JsonIgnore
  private NetworkConfig network;

  public DefaultConnectionConfig() {
    grouping = new RoundGrouping();
  }

  public DefaultConnectionConfig(String source, String target, NetworkConfig network) {
    this(parseComponent(source), parsePort(source, DEFAULT_OUT_PORT),
        parseComponent(target), parsePort(target, DEFAULT_IN_PORT),
        new RoundGrouping(), network);
  }

  public DefaultConnectionConfig(String source, String target, Grouping grouping, NetworkConfig network) {
    this(parseComponent(source), parsePort(source, DEFAULT_OUT_PORT),
        parseComponent(target), parsePort(target, DEFAULT_IN_PORT), grouping,
        network);
  }

  public DefaultConnectionConfig(String source, String out, String target, String in, NetworkConfig network) {
    this(source, out, target, in, new RoundGrouping(), network);
  }

  public DefaultConnectionConfig(String source, String out, String target, String in, Grouping grouping, NetworkConfig network) {
    this.source.setComponent(source);
    this.source.setPort(out);
    this.target.setComponent(target);
    this.target.setPort(in);
    this.grouping = grouping != null ? grouping : new RoundGrouping();
    this.network = network;
  }

  @Override
  public Source getSource() {
    return source;
  }

  @Override
  public Target getTarget() {
    return target;
  }

  @Override
  public Grouping getGrouping() {
    return grouping;
  }

  @Override
  public ConnectionConfig groupBy(Grouping grouping) {
    this.grouping = grouping;
    return this;
  }

  @Override
  public ConnectionConfig randomGrouping() {
    this.grouping = new RandomGrouping();
    return this;
  }

  @Override
  public ConnectionConfig roundGrouping() {
    this.grouping = new RoundGrouping();
    return this;
  }

  @Override
  public ConnectionConfig fieldsGrouping(String... fields) {
    this.grouping = new FieldsGrouping(fields);
    return this;
  }

  @Override
  public ConnectionConfig allGrouping() {
    this.grouping = new AllGrouping();
    return this;
  }

  @Override
  @SuppressWarnings("rawtypes")
  public <T extends ComponentConfig> T addComponent(T component) {
    return network.addComponent(component);
  }

  @Override
  public <T extends ComponentConfig<T>> T addComponent(String name, String moduleOrMain) {
    return network.addComponent(name, moduleOrMain);
  }

  @Override
  public <T extends ComponentConfig<T>> T addComponent(String name, String moduleOrMain, JsonObject config) {
    return network.addComponent(name, moduleOrMain, config);
  }

  @Override
  public <T extends ComponentConfig<T>> T addComponent(String name, String moduleOrMain, int instances) {
    return network.addComponent(name, moduleOrMain, instances);
  }

  @Override
  public <T extends ComponentConfig<T>> T addComponent(String name, String moduleOrMain, JsonObject config, int instances) {
    return network.addComponent(name, moduleOrMain, config, instances);
  }

  @Override
  public <T extends ComponentConfig<T>> T removeComponent(T component) {
    return network.removeComponent(component);
  }

  @Override
  public <T extends ComponentConfig<T>> T removeComponent(String name) {
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
  public ConnectionConfig createConnection(String source, String target, Grouping grouping) {
    return network.createConnection(source, target, grouping);
  }

  @Override
  public ConnectionConfig createConnection(String source, String out, String target, String in) {
    return network.createConnection(source, out, target, in);
  }

  @Override
  public ConnectionConfig createConnection(String source, String out, String target, String in, Grouping grouping) {
    return network.createConnection(source, out, target, in, grouping);
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

  @Override
  public boolean equals(Object other) {
    if (!(other instanceof ConnectionConfig)) {
      return false;
    }
    ConnectionConfig connection = (ConnectionConfig) other;
    return connection.getSource().getComponent().equals(source.getComponent())
        && connection.getSource().getPort().equals(source.getPort())
        && connection.getTarget().getComponent().equals(target.getComponent())
        && connection.getTarget().getPort().equals(target.getPort());
  }

  /**
   * Default source implementation.
   * 
   * @author Jordan Halterman
   */
  public static class DefaultSource implements Source {
    private String component;
    private String port;

    private DefaultSource() {
    }

    @Override
    public String getComponent() {
      return component;
    }

    @Override
    public Source setComponent(String component) {
      this.component = component;
      return this;
    }

    @Override
    public String getPort() {
      return port;
    }

    @Override
    public Source setPort(String port) {
      this.port = port;
      return this;
    }

  }

  /**
   * Default target implementation.
   * 
   * @author Jordan Halterman
   */
  public static class DefaultTarget implements Target {
    private String component;
    private String port;

    private DefaultTarget() {
    }

    @Override
    public String getComponent() {
      return component;
    }

    @Override
    public Target setComponent(String component) {
      this.component = component;
      return this;
    }

    @Override
    public String getPort() {
      return port;
    }

    @Override
    public Target setPort(String port) {
      this.port = port;
      return this;
    }

  }

  /**
   * Parses a component name from the connection address.
   */
  private static String parseComponent(String address) {
    return !address.contains(":") ? address : address.substring(0,
        address.indexOf(":"));
  }

  /**
   * Parses a port name from the connection address.
   */
  private static String parsePort(String address, String def) {
    if (!address.contains(":")) {
      return def;
    }
    return address.substring(address.indexOf(":") + 1);
  }

}
