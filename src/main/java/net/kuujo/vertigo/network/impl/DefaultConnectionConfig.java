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

import net.kuujo.vertigo.input.grouping.AllGrouping;
import net.kuujo.vertigo.input.grouping.FieldsGrouping;
import net.kuujo.vertigo.input.grouping.Grouping;
import net.kuujo.vertigo.input.grouping.RandomGrouping;
import net.kuujo.vertigo.input.grouping.RoundGrouping;
import net.kuujo.vertigo.network.ConnectionConfig;

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

  public DefaultConnectionConfig() {
    grouping = new RoundGrouping();
  }

  public DefaultConnectionConfig(String source, String target) {
    this(parseComponent(source), parsePort(source, DEFAULT_OUT_PORT), parseComponent(target), parsePort(target, DEFAULT_IN_PORT), new RoundGrouping());
  }

  public DefaultConnectionConfig(String source, String target, Grouping grouping) {
    this(parseComponent(source), parsePort(source, DEFAULT_OUT_PORT), parseComponent(target), parsePort(target, DEFAULT_IN_PORT), grouping);
  }

  public DefaultConnectionConfig(String source, String out, String target, String in) {
    this(source, out, target, in, new RoundGrouping());
  }

  public DefaultConnectionConfig(String source, String out, String target, String in, Grouping grouping) {
    this.source.setComponent(source);
    this.source.setPort(out);
    this.target.setComponent(target);
    this.target.setPort(in);
    this.grouping = grouping != null ? grouping : new RoundGrouping();
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
  private static class DefaultSource implements Source {
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
  private static class DefaultTarget implements Target {
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
    return !address.contains(":") ? address : address.substring(0, address.indexOf(":"));
  }

  /**
   * Parses a port name from the connection address.
   */
  private static String parsePort(String address, String def) {
    if (!address.contains(":")) {
      return def;
    }
    return address.substring(address.indexOf(":")+1);
  }

}
