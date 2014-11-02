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

import net.kuujo.vertigo.impl.BaseTypeInfoImpl;
import net.kuujo.vertigo.io.connection.TargetInfo;

/**
 * Connection source info implementation.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class TargetInfoImpl extends BaseTypeInfoImpl<TargetInfo> implements TargetInfo {
  private String component;
  private String port;
  private int partition;

  @Override
  public String component() {
    return component;
  }

  @Override
  public String port() {
    return port;
  }

  @Override
  public int partition() {
    return partition;
  }

  /**
   * Target info builder.
   */
  public static class Builder implements TargetInfo.Builder {
    private final TargetInfoImpl target;

    public Builder() {
      target = new TargetInfoImpl();
    }

    public Builder(TargetInfoImpl source) {
      this.target = source;
    }

    @Override
    public Builder setComponent(String component) {
      target.component = component;
      return this;
    }

    @Override
    public Builder setPort(String port) {
      target.port = port;
      return this;
    }

    @Override
    public Builder setPartition(int partition) {
      target.partition = partition;
      return this;
    }

    @Override
    public TargetInfoImpl build() {
      return target;
    }
  }

}
