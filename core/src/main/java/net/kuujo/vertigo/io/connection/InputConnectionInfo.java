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

import net.kuujo.vertigo.TypeInfo;
import net.kuujo.vertigo.io.connection.impl.InputConnectionInfoImpl;
import net.kuujo.vertigo.io.port.InputPortInfo;

/**
 * Input connection context represents a single instance's input from
 * an instance of another component.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public interface InputConnectionInfo extends ConnectionInfo<InputConnectionInfo> {

  /**
   * Returns a new input connection info builder.
   *
   * @return A new input connection info builder.
   */
  static Builder builder() {
    return new InputConnectionInfoImpl.Builder();
  }

  /**
   * Returns a new input connection info builder.
   *
   * @param connection An existing input connection info object to wrap.
   * @return An input connection info builder wrapper.
   */
  static Builder builder(InputConnectionInfo connection) {
    return new InputConnectionInfoImpl.Builder((InputConnectionInfoImpl) connection);
  }

  /**
   * Returns the parent input port context.
   *
   * @return The parent port context.
   */
  InputPortInfo port();

  /**
   * Input connection info builder.
   */
  public static interface Builder extends TypeInfo.Builder<InputConnectionInfo> {

    /**
     * Sets the connection address.
     *
     * @param address The connection address.
     * @return The input connection info builder.
     */
    Builder setAddress(String address);

    /**
     * Sets the connection source info.
     *
     * @param source The connection source info.
     * @return The input connection info builder.
     */
    Builder setSource(SourceInfo source);

    /**
     * Sets the connection target info.
     *
     * @param target The connection target info.
     * @return The input connection info builder.
     */
    Builder setTarget(TargetInfo target);

    /**
     * Sets the parent input port info.
     *
     * @param port The parent input port info.
     * @return The input connection info builder.
     */
    Builder setPort(InputPortInfo port);
  }

}
