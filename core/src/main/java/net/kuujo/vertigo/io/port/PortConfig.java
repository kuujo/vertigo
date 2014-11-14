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
import net.kuujo.vertigo.TypeConfig;
import net.kuujo.vertigo.component.ComponentConfig;

/**
 * Port info.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public interface PortConfig<T extends PortConfig<T>> extends TypeConfig {

  /**
   * <code>name</code> is a string indicating the port name.
   */
  public static final String PORT_NAME = "name";

  /**
   * <code>type</code> is a string indicating the port type.
   */
  public static final String PORT_TYPE = "type";

  /**
   * <code>codec</code> is a string class name of a port codec.
   */
  public static final String PORT_CODEC = "codec";

  /**
   * <code>persistent</code> is a boolean indicating whether the port is persistent.
   */
  public static final String PORT_PERSISTENT = "persistent";

  /**
   * Returns the parent component.
   *
   * @return The parent component.
   */
  ComponentConfig getComponent();

  /**
   * Sets the parent component.
   *
   * @param component The parent component.
   * @return The port info.
   */
  T setComponent(ComponentConfig component);

  /**
   * Returns the port name.
   *
   * @return The port name.
   */
  String getName();

  /**
   * Sets the port name.
   *
   * @param name The port name.
   * @return The port info.
   */
  T setName(String name);

  /**
   * Returns the port type.
   *
   * @return The port type.
   */
  Class<?> getType();

  /**
   * Sets the port type.
   *
   * @param type The port type.
   * @return The port info.
   */
  T setType(Class<?> type);

  /**
   * Returns the port type message codec class.
   *
   * @return The port type message codec class.
   */
  Class<? extends MessageCodec> getCodec();

  /**
   * Sets the port type message codec class.
   *
   * @param codec The port type message codec class.
   * @return The port info.
   */
  T setCodec(Class<? extends MessageCodec> codec);

  /**
   * Sets whether the port is persistent.
   *
   * @param persistent Whether the port is persistent.
   * @return The port info.
   */
  T setPersistent(boolean persistent);

  /**
   * Returns whether the port is persistent.
   *
   * @return Whether the port is persistent.
   */
  boolean isPersistent();

}
