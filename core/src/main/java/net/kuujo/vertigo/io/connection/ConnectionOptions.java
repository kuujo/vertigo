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
package net.kuujo.vertigo.io.connection;

import io.vertx.codegen.annotations.Options;
import net.kuujo.vertigo.hook.IOHook;
import net.kuujo.vertigo.hook.InputHook;
import net.kuujo.vertigo.hook.OutputHook;
import net.kuujo.vertigo.io.partitioner.Partitioner;

import java.io.Serializable;
import java.util.List;

/**
 * A connection represents a link between two components within a network.<p>
 *
 * When a connection is created, each instance of the source component
 * will be setup to send messages to each instance of the target component.
 * How messages are routed to multiple target instances can be configured
 * using selectors.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
@Options
public interface ConnectionOptions extends Serializable {

  /**
   * <code>source</code> is an object defining the connection source. See the
   * {@link Source} documentation for details on the source structure.
   */
  public static final String CONNECTION_SOURCE = "source";

  /**
   * <code>target</code> is an object defining the connection target. See the
   * {@link Target} documentation for details on the target structure.
   */
  public static final String CONNECTION_TARGET = "target";

  /**
   * <code>hooks</code> is an array defining connection hooks. Each element in the array
   * must be an object containing a <code>hook</code> field which indicates the hook
   * class name.
   */
  public static final String CONNECTION_HOOKS = "hooks";

  /**
   * <code>partitioner</code> is an object defining the connection partitioner. The partitioner
   * definition should contain a <code>type</code> which indicates the partitioner type,
   * e.g. <code>round-robin</code>, <code>random</code>, <code>hash</code>, <code>fair</code>,
   * <code>all</code>, or <code>custom</code>. If a <code>custom</code> selector is indicated
   * then an additional <code>selector</code> field must be provided which indicates the
   * custom selector class.
   */
  public static final String CONNECTION_PARTITIONER = "partitioner";

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
   * Adds an input/output hook to the connection.
   *
   * @param hook The hook to add.
   * @return The connection configuration.
   */
  ConnectionOptions addHook(IOHook hook);

  /**
   * Returns a list of connection hooks.
   *
   * @return A list of hooks for the connection.
   */
  List<IOHook> getHooks();

  /**
   * Returns the connection partitioner.
   *
   * @return The connection selector.
   */
  Partitioner getPartitioner();

  /**
   * Sets the connection partitioner.
   *
   * @param partitioner The partitioner with which to partition individual connections.
   * @return The connection configuration.
   */
  ConnectionOptions setPartitioner(Partitioner partitioner);

  /**
   * Sets a round-robin partitioner on the connection.
   *
   * @return The connection configuration.
   */
  ConnectionOptions roundPartition();

  /**
   * Sets a random partitioner on the connection.
   *
   * @return The connection configuration.
   */
  ConnectionOptions randomPartition();

  /**
   * Sets a mod-hash based partitioner on the connection.
   *
   * @return The connection configuration.
   */
  ConnectionOptions hashPartition();

  /**
   * Sets a fair partitioner on the connection.
   *
   * @return The connection configuration.
   */
  ConnectionOptions fairPartition();

  /**
   * Sets an all partitioner on the connection.
   *
   * @return The connection configuration.
   */
  ConnectionOptions allPartition();

  /**
   * Sets a custom partitioner on the connection.
   *
   * @param partitioner The custom selector to set.
   * @return The connection configuration.
   */
  ConnectionOptions partition(Partitioner partitioner);

  /**
   * Connection source.
   *
   * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
   */
  public static interface Source {

    /**
     * <code>component</code> indicates the source component name.
     */
    public static final String SOURCE_COMPONENT = "component";

    /**
     * <code>port</code> indicates the source output port.
     */
    public static final String SOURCE_PORT = "port";

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

    /**
     * Adds an output hook to the source.
     *
     * @param hook The hook to add.
     * @return The source instance.
     */
    Source addHook(OutputHook hook);

    /**
     * Returns a list of output hooks.
     *
     * @return A list of hooks for the output.
     */
    List<OutputHook> getHooks();

  }

  /**
   * Connection target.
   *
   * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
   */
  public static interface Target {

    /**
     * <code>component</code> indicates the target component name.
     */
    public static final String SOURCE_COMPONENT = "component";

    /**
     * <code>port</code> indicates the target output port.
     */
    public static final String SOURCE_PORT = "port";

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

    /**
     * Adds an input hook to the target.
     *
     * @param hook The hook to add.
     * @return The target instance.
     */
    Target addHook(InputHook hook);

    /**
     * Returns a list of input hooks.
     *
     * @return A list of hooks for the input.
     */
    List<InputHook> getHooks();

  }

}
