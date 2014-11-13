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
package net.kuujo.vertigo.io.port;

import io.vertx.core.eventbus.MessageCodec;
import net.kuujo.vertigo.TypeContext;
import net.kuujo.vertigo.io.connection.ConnectionContext;
import net.kuujo.vertigo.io.connection.InputConnectionContext;

import java.util.Collection;

/**
 * Port context.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public interface PortContext<T extends PortContext<T, U>, U extends ConnectionContext<U, T>> extends TypeContext<T> {

  /**
   * Returns the port name.
   *
   * @return The port name.
   */
  String name();

  /**
   * Returns the port type.
   *
   * @return The port type.
   */
  Class<?> type();

  /**
   * Returns the port message codec.
   *
   * @return The port message codec.
   */
  Class<? extends MessageCodec> codec();

  /**
   * Returns whether the port is persistent.
   *
   * @return Whether the port is persistent.
   */
  boolean persistent();

  /**
   * Returns a collection of port connections.
   *
   * @return A collection of port connections.
   */
  Collection<U> connections();

  /**
   * Port context builder.
   *
   * @param <T> The builder type.
   * @param <U> The port type.
   */
  public static interface Builder<T extends Builder<T, U, V>, U extends PortContext<U, V>, V extends ConnectionContext<V, U>> extends TypeContext.Builder<T, U> {

    /**
     * Sets the port name.
     *
     * @param name The output port name.
     * @return The output port builder.
     */
    T setName(String name);

    /**
     * Sets the port type.
     *
     * @param type The port type.
     * @return The port context builder.
     */
    T setType(Class<?> type);

    /**
     * Sets the port codec.
     *
     * @param codec The port codec.
     * @return The port context builder.
     */
    T setCodec(Class<? extends MessageCodec> codec);

    /**
     * Sets whether the port is persistent.
     *
     * @param persistent Whether the port is persistent.
     * @return The port context builder.
     */
    T setPersistent(boolean persistent);

    /**
     * Adds a connection to the port.
     *
     * @param connection The input connection context.
     * @return The input port context builder.
     */
    T addConnection(V connection);

    /**
     * Removes a connection from the port.
     *
     * @param connection The input connection context.
     * @return The input port context builder.
     */
    T removeConnection(V connection);

    /**
     * Sets all connections on the port.
     *
     * @param connections A collection of input connection context.
     * @return The input port context builder.
     */
    T setConnections(V... connections);

    /**
     * Sets all connections on the port.
     *
     * @param connections A collection of input connection context.
     * @return The input port context builder.
     */
    T setConnections(Collection<V> connections);

  }

}
