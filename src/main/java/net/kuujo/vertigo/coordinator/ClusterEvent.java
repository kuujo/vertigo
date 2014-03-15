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
package net.kuujo.vertigo.coordinator;

import net.kuujo.vertigo.util.serializer.Serializable;

/**
 * Cluster event.
 *
 * @author Jordan Halterman
 */
public class ClusterEvent implements Serializable {

  /**
   * Cluster event type.
   *
   * @author Jordan Halterman
   */
  public static enum Type {

    /**
     * Occurs when a key is created.
     */
    CREATE("create"),

    /**
     * Occurs when a key is updated.
     */
    UPDATE("update"),

    /**
     * Occurs when a key is changed (created, updated, or deleted).
     */
    CHANGE("change"),

    /**
     * Occurs when a key is deleted.
     */
    DELETE("delete");

    private final String name;

    private Type(String name) {
      this.name = name;
    }

    @Override
    public String toString() {
      return name;
    }

    /**
     * Parses a string into a cluster event type.
     *
     * @param name The event type name.
     * @return The event type.
     */
    public static Type parse(String name) {
      switch (name) {
        case "create":
          return CREATE;
        case "update":
          return UPDATE;
        case "change":
          return CHANGE;
        case "delete":
          return DELETE;
        default:
          throw new IllegalArgumentException(name + " is not a valid event type.");
      }
    }

  }

  private Type type;
  private String key;
  private Object value;

  public ClusterEvent(Type type, String key, Object value) {
    this.type = type;
    this.key = key;
    this.value = value;
  }

  /**
   * Returns the event type.
   *
   * @return The cluster event type.
   */
  public Type type() {
    return type;
  }

  /**
   * Returns the key on which the event occurred.
   *
   * @return The key on which the event occurred.
   */
  public String key() {
    return key;
  }

  /**
   * Returns the current key value.
   *
   * @return The current key value.
   */
  @SuppressWarnings("unchecked")
  public <T> T value() {
    return (T) value;
  }

}
