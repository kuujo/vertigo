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

import net.kuujo.vertigo.io.selector.Selector;
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
   * Returns the connection selector.
   *
   * @return The connection selector.
   */
  Selector getSelector();

  /**
   * Sets the connection selector.
   *
   * @param selector The selector with which to select individual connections.
   * @return The connection configuration.
   */
  ConnectionConfig setSelector(Selector selector);

  /**
   * Sets a round-robin selector on the connection.
   *
   * @return The connection configuration.
   */
  ConnectionConfig roundSelect();

  /**
   * Sets a random selector on the connection.
   *
   * @return The connection configuration.
   */
  ConnectionConfig randomSelect();

  /**
   * Sets a mod-hash based selector on the connection.
   *
   * @return The connection configuration.
   */
  ConnectionConfig hashSelect();

  /**
   * Sets a fair selector on the connection.
   *
   * @return The connection configuration.
   */
  ConnectionConfig fairSelect();

  /**
   * Sets an all selector on the connection.
   *
   * @return The connection configuration.
   */
  ConnectionConfig allSelect();

  /**
   * Sets a custom selector on the connection.
   *
   * @param selector The custom selector to set.
   * @return The connection configuration.
   */
  ConnectionConfig customSelect(Selector selector);

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
