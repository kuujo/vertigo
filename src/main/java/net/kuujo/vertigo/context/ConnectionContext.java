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
package net.kuujo.vertigo.context;


/**
 * Connection contexts represent a direct connection between
 * one component and another component.
 *
 * @author Jordan Halterman
 */
public interface ConnectionContext<T extends ConnectionContext<T>> extends Context<T> {

  /**
   * Returns the connection delivery method.
   *
   * @return The connection delivery method.
   */
  Delivery delivery();

  /**
   * Returns the connection order method.
   *
   * @return The connection order method.
   */
  Order order();

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

}
