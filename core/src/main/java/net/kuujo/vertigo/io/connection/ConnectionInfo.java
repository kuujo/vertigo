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

/**
 * Connection contexts represent a direct connection between two instances
 * of separate components.<p>
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public interface ConnectionInfo<T extends ConnectionInfo<T>> extends TypeInfo<T> {

  /**
   * Returns the unique connection address.
   *
   * @return The unique connection address.
   */
  String address();

  /**
   * Returns the connection source.
   *
   * @return The connection source.
   */
  SourceInfo source();

  /**
   * Returns the connection target.
   *
   * @return The connection target.
   */
  TargetInfo target();

  /**
   * Connection endpoint context.
   *
   * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
   *
   * @param <T> The endpoint type.
   */
  public static interface EndpointInfo<T extends EndpointInfo<T>> extends TypeInfo<T> {

    /**
     * Returns the endpoint component name.
     *
     * @return The endpoint component name.
     */
    String component();

    /**
     * Returns the endpoint port name.
     *
     * @return The endpoint port name.
     */
    String port();

    /**
     * Returns the endpoint instance number.
     *
     * @return The endpoint instance number.
     */
    int instance();

  }

  /**
   * Connection source.
   *
   * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
   */
  public static interface SourceInfo extends EndpointInfo<SourceInfo> {
  }

  /**
   * Connection target.
   *
   * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
   */
  public static interface TargetInfo extends EndpointInfo<TargetInfo> {
  }

}
