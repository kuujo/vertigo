package net.kuujo.vertigo.network;

import org.vertx.java.core.json.JsonObject;

import net.kuujo.vertigo.input.grouping.AllGrouping;
import net.kuujo.vertigo.input.grouping.FieldsGrouping;
import net.kuujo.vertigo.input.grouping.Grouping;
import net.kuujo.vertigo.input.grouping.RandomGrouping;
/*
 * Copyright 2013-2014 the original author or authors.
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
import net.kuujo.vertigo.input.grouping.RoundGrouping;
import net.kuujo.vertigo.util.serializer.SerializerFactory;

/**
 * Component-to-component connection.
 *
 * @author Jordan Halterman
 */
public class Connection implements Config {
  private static final String DEFAULT_OUT_PORT = "out";
  private static final String DEFAULT_IN_PORT = "in";
  private Source source;
  private Target target;
  private Grouping grouping;

  public Connection(String source, String target) {
    this(parseComponent(source), parsePort(source, DEFAULT_OUT_PORT), parseComponent(target), parsePort(target, DEFAULT_IN_PORT), new RoundGrouping());
  }

  public Connection(String source, String target, Grouping grouping) {
    this(parseComponent(source), parsePort(source, DEFAULT_OUT_PORT), parseComponent(target), parsePort(target, DEFAULT_IN_PORT), grouping);
  }

  public Connection(String source, String out, String target, String in) {
    this(source, out, target, in, new RoundGrouping());
  }

  public Connection(String source, String out, String target, String in, Grouping grouping) {
    this.source = new Source(source, out);
    this.target = new Target(target, in);
    this.grouping = grouping;
  }

  /**
   * Returns the connection source.
   *
   * @return The connection source.
   */
  public Source getSource() {
    return source;
  }

  /**
   * Returns the connection target.
   *
   * @return The connection target.
   */
  public Target getTarget() {
    return target;
  }

  /**
   * Sets the connection grouping.
   * 
   * The connection grouping indicates how messages should be distributed between multiple
   * instances of the input component.
   * 
   * @param grouping An input grouping.
   * @return The connection instance.
   */
  public Connection groupBy(Grouping grouping) {
    this.grouping = grouping;
    return this;
  }

  /**
   * Sets the connection grouping as a string.
   * 
   * @param grouping The connection grouping type.
   * @return The connection instance.
   */
  public Connection groupBy(String grouping) {
    try {
      this.grouping = SerializerFactory.getSerializer(Grouping.class).deserializeObject(new JsonObject().putString("type", grouping), Grouping.class);
    }
    catch (Exception e) {
      throw new IllegalArgumentException("Invalid input grouping type " + grouping);
    }
    return this;
  }

  /**
   * Sets a random connection grouping on the input.
   * 
   * @return The connection instance.
   */
  public Connection randomGrouping() {
    this.grouping = new RandomGrouping();
    return this;
  }

  /**
   * Sets a round-robin connection grouping on the input.
   * 
   * @return The connection instance.
   */
  public Connection roundGrouping() {
    this.grouping = new RoundGrouping();
    return this;
  }

  /**
   * Sets a fields grouping on the connection.
   * 
   * @param fields The fields on which to hash.
   * @return The connection instance.
   */
  public Connection fieldsGrouping(String... fields) {
    this.grouping = new FieldsGrouping(fields);
    return this;
  }

  /**
   * Sets an all grouping on the connection.
   * 
   * @return The connection instance.
   */
  public Connection allGrouping() {
    this.grouping = new AllGrouping();
    return this;
  }

  /**
   * Returns the current connection grouping.
   * 
   * @return The current connection grouping.
   */
  public Grouping getGrouping() {
    return grouping;
  }

  /**
   * Base endpoint.
   *
   * @author Jordan Halterman
   */
  private static abstract class Endpoint implements Config {
    private String component;
    private String port;
    public Endpoint(String component, String port) {
      this.component = component;
      this.port = port;
    }

    /**
     * Returns the endpoint component name.
     *
     * @return The endpoint component name.
     */
    public String component() {
      return component;
    }

    /**
     * Returns the endpoint port name.
     *
     * @return The endpoint port name.
     */
    public String port() {
      return port;
    }
  }

  /**
   * Source connection endpoint.
   *
   * @author Jordan Halterman
   */
  public static class Source extends Endpoint {
    private Source() {
      super(null, null);
    }
    public Source(String component, String port) {
      super(component, port);
    }
  }

  /**
   * Target connection endpoint.
   *
   * @author Jordan Halterman
   */
  public static class Target extends Endpoint {
    private Target() {
      super(null, null);
    }
    public Target(String component, String port) {
      super(component, port);
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
