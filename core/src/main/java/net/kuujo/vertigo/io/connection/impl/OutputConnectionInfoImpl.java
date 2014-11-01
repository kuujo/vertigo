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

package net.kuujo.vertigo.io.connection.impl;

import net.kuujo.vertigo.TypeInfo;
import net.kuujo.vertigo.io.connection.OutputConnectionInfo;
import net.kuujo.vertigo.io.connection.SourceInfo;
import net.kuujo.vertigo.io.connection.TargetInfo;
import net.kuujo.vertigo.io.stream.OutputStreamInfo;
import net.kuujo.vertigo.util.Args;

/**
 * Output connection info implementation.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class OutputConnectionInfoImpl extends BaseConnectionInfoImpl<OutputConnectionInfo> implements OutputConnectionInfo {
  private OutputStreamInfo stream;

  @Override
  public OutputStreamInfo stream() {
    return stream;
  }

  /**
   * Output connection info builder.
   */
  public static class Builder implements TypeInfo.Builder<OutputConnectionInfo> {
    private OutputConnectionInfoImpl connection;

    public Builder() {
      connection = new OutputConnectionInfoImpl();
    }

    public Builder(OutputConnectionInfoImpl connection) {
      this.connection = connection;
    }

    /**
     * Sets the connection address.
     *
     * @param address The connection address.
     * @return The output connection info builder.
     */
    public Builder setAddress(String address) {
      Args.checkNotNull(address, "address cannot be null");
      connection.address = address;
      return this;
    }

    /**
     * Sets the connection source info.
     *
     * @param source The connection source info.
     * @return The output connection info builder.
     */
    public Builder setSource(SourceInfo source) {
      Args.checkNotNull(source, "source cannot be null");
      connection.source = source;
      return this;
    }

    /**
     * Sets the connection target info.
     *
     * @param target The connection target info.
     * @return The output connection info builder.
     */
    public Builder setTarget(TargetInfo target) {
      Args.checkNotNull(target, "target cannot be null");
      connection.target = target;
      return this;
    }

    /**
     * Sets the parent output stream info.
     *
     * @param stream The parent output stream info.
     * @return The output connection info builder.
     */
    public Builder setStream(OutputStreamInfo stream) {
      Args.checkNotNull(stream, "stream cannot be null");
      connection.stream = stream;
      return this;
    }

    @Override
    public OutputConnectionInfoImpl build() {
      return connection;
    }
  }

}
