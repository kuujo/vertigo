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
package net.kuujo.vertigo.output.connection;

import net.kuujo.vertigo.TypeInfo;
import net.kuujo.vertigo.connection.ConnectionInfo;
import net.kuujo.vertigo.connection.SourceInfo;
import net.kuujo.vertigo.connection.TargetInfo;
import net.kuujo.vertigo.output.connection.impl.OutputConnectionInfoImpl;
import net.kuujo.vertigo.output.stream.OutputStreamInfo;

/**
 * Output connection context represents a single partition's output to
 * an partition of another component.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public interface OutputConnectionInfo extends ConnectionInfo<OutputConnectionInfo> {

  /**
   * Returns a new output connection info builder.
   *
   * @return A new output connection info builder.
   */
  static Builder builder() {
    return new OutputConnectionInfoImpl.Builder();
  }

  /**
   * Returns a new output connection info builder.
   *
   * @param connection An existing output connection info object to wrap.
   * @return An output connection info builder wrapper.
   */
  static Builder builder(OutputConnectionInfo connection) {
    return new OutputConnectionInfoImpl.Builder((OutputConnectionInfoImpl) connection);
  }

  /**
   * Returns the parent output stream context.
   *
   * @return The parent stream context.
   */
  OutputStreamInfo stream();

  /**
   * Output connection info builder.
   */
  public static interface Builder extends TypeInfo.Builder<OutputConnectionInfo> {

    /**
     * Sets the connection address.
     *
     * @param address The connection address.
     * @return The output connection info builder.
     */
    Builder setAddress(String address);

    /**
     * Sets the connection source info.
     *
     * @param source The connection source info.
     * @return The output connection info builder.
     */
    Builder setSource(SourceInfo source);

    /**
     * Sets the connection target info.
     *
     * @param target The connection target info.
     * @return The output connection info builder.
     */
    Builder setTarget(TargetInfo target);

    /**
     * Sets the parent output stream info.
     *
     * @param stream The parent output stream info.
     * @return The output connection info builder.
     */
    Builder setStream(OutputStreamInfo stream);
  }

}
