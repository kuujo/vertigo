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
package net.kuujo.vertigo.builder;

import io.vertx.core.eventbus.MessageCodec;

/**
 * Component port builder.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public interface PortBuilder<T extends PortBuilder<T>> {

  /**
   * Sets the port type.
   *
   * @param type The port type.
   * @return The port builder.
   */
  T type(Class<?> type);

  /**
   * Sets the port codec.
   *
   * @param codec The port codec.
   * @return The port builder.
   */
  T codec(Class<? extends MessageCodec> codec);

  /**
   * Sets the port as persistent.
   *
   * @return The port builder.
   */
  T persistent();

  /**
   * Sets whether the port is persistent.
   *
   * @param persistent Whether the port is persistent.
   * @return The port builder.
   */
  T persistent(boolean persistent);

  /**
   * Returns the component input builder.
   *
   * @return The component input builder.
   */
  InputBuilder input();

  /**
   * Returns the component output builder.
   *
   * @return The component output builder.
   */
  OutputBuilder output();

}
