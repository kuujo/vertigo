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
package net.kuujo.vertigo.network;

import net.kuujo.vertigo.input.grouping.MessageGrouping;
import net.kuujo.vertigo.network.impl.DefaultConnectionConfig;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Connection configuration.
 *
 * @author Jordan Halterman
 */
@JsonTypeInfo(
  use=JsonTypeInfo.Id.CLASS,
  include=JsonTypeInfo.As.PROPERTY,
  property="class",
  defaultImpl=DefaultConnectionConfig.class
)
public interface ConnectionConfig extends Config<ConnectionConfig>, ComponentConfigurable, ConnectionConfigurable {

  /**
   * Defines connection message delivery requirements.
   *
   * @author Jordan Halterman
   */
  public static enum Delivery {

    /**
     * Indicates basic at-most-once delivery.
     */
    AT_MOST_ONCE("at-most-once"),

    /**
     * Indicates guaranteed at-least-once delivery.
     */
    AT_LEAST_ONCE("at-least-once"),

    /**
     * Indicates guaranteed exactly-once delivery.
     */
    EXACTLY_ONCE("exactly-once");

    private final String name;

    private Delivery(String name) {
      this.name = name;
    }

    /**
     * Returns the delivery name.
     *
     * @return The delivery method name.
     */
    public String getName() {
      return name;
    }

    @Override
    public String toString() {
      return name;
    }

    /**
     * Parses a delivery method from string.
     *
     * @param name The string name of the delivery method.
     * @return A delivery method.
     * @throws IllegalArgumentException If the delivery method is invalid.
     */
    public static Delivery parse(String name) {
      switch (name) {
        case "at-most-once":
          return AT_MOST_ONCE;
        case "at-least-once":
          return AT_LEAST_ONCE;
        case "exactly-once":
          return EXACTLY_ONCE;
        default:
          throw new IllegalArgumentException(name + " is an invalid delivery method.");
      }
    }

  }

  /**
   * Defines connection message ordering requirements.
   *
   * @author Jordan Halterman
   */
  public static enum Order {

    /**
     * Indicates no enforced order.
     */
    NO_ORDER(false),

    /**
     * Indicates strongly enforced order.
     */
    STRONG_ORDER(true);

    private final boolean ordered;

    private Order(boolean ordered) {
      this.ordered = ordered;
    }

    /**
     * Returns a boolean indicating whether the order is ordered.
     *
     * @return Indicates whether ordered.
     */
    public boolean isOrdered() {
      return ordered;
    }

    /**
     * Parses order type.
     *
     * @param ordered Indicates whether ordered.
     * @return An order type.
     */
    public static Order parse(boolean ordered) {
      return ordered ? STRONG_ORDER : NO_ORDER;
    }

  }

  /**
   * Returns the connection source.
   *
   * @return The connection source info.
   */
  Source getSource();

  /**
   * Returns the connection target.
   *
   * @return The connection target info.
   */
  Target getTarget();

  /**
   * Sets the connection delivery method.
   *
   * @param delivery The connection delivery method.
   * @return The connection configuration.
   */
  ConnectionConfig setDelivery(Delivery delivery);

  /**
   * Returns the connection delivery method.
   *
   * @return The connection delivery method.
   */
  Delivery getDelivery();

  /**
   * Sets the connection order method.
   *
   * @param order The connection order requirements.
   * @return The connection configuration.
   */
  ConnectionConfig setOrder(Order order);

  /**
   * Returns the connection order method.
   *
   * @return The connection order requirements.
   */
  Order getOrder();

  /**
   * Returns the connection grouping.
   *
   * @return The connection grouping. Defaults to round grouping if no grouping
   *         was set on the connection.
   */
  MessageGrouping getGrouping();

  /**
   * Sets the connection grouping.
   *
   * @param grouping The connection grouping.
   * @return The connection configuration.
   */
  ConnectionConfig groupBy(MessageGrouping grouping);

  /**
   * Sets a random connection grouping on the input.
   * 
   * @return The connection instance.
   */
  ConnectionConfig randomGrouping();

  /**
   * Sets a round-robin connection grouping on the input.
   * 
   * @return The connection instance.
   */
  ConnectionConfig roundGrouping();

  /**
   * Sets a fields grouping on the connection.
   * 
   * @param fields The fields on which to hash.
   * @return The connection instance.
   */
  ConnectionConfig fieldsGrouping(String... fields);

  /**
   * Sets an all grouping on the connection.
   * 
   * @return The connection instance.
   */
  ConnectionConfig allGrouping();

  /**
   * Connection source.
   *
   * @author Jordan Halterman
   */
  @JsonTypeInfo(
    use=JsonTypeInfo.Id.CLASS,
    include=JsonTypeInfo.As.PROPERTY,
    property="class",
    defaultImpl=DefaultConnectionConfig.DefaultSource.class
  )
  public static interface Source extends Config<Source> {

    /**
     * Returns the connection source component.
     *
     * @return The source component name.
     */
    String getComponent();

    /**
     * Sets the connection source component.
     *
     * @param component The connection source component.
     * @return The source instance.
     */
    Source setComponent(String component);

    /**
     * Returns the connection source port.
     *
     * @return The connection source port.
     */
    String getPort();

    /**
     * Sets the connection source port.
     *
     * @param port The connection source port.
     * @return The source instance.
     */
    Source setPort(String port);

  }

  /**
   * Connection target.
   *
   * @author Jordan Halterman
   */
  @JsonTypeInfo(
    use=JsonTypeInfo.Id.CLASS,
    include=JsonTypeInfo.As.PROPERTY,
    property="class",
    defaultImpl=DefaultConnectionConfig.DefaultTarget.class
  )
  public static interface Target extends Config<Target> {

    /**
     * Returns the connection target component.
     *
     * @return The target component name.
     */
    String getComponent();

    /**
     * Sets the connection target component.
     *
     * @param component The connection target component.
     * @return The target instance.
     */
    Target setComponent(String component);

    /**
     * Returns the connection target port.
     *
     * @return The connection target port.
     */
    String getPort();

    /**
     * Sets the connection target port.
     *
     * @param port The connection target port.
     * @return The target instance.
     */
    Target setPort(String port);

  }

}
