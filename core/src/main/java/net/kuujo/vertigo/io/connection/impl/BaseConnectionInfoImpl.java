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
import net.kuujo.vertigo.io.connection.ConnectionInfo;

/**
 * Connection info implementation.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public abstract class BaseConnectionInfoImpl<T extends ConnectionInfo<T>> extends BaseTypeInfoImpl<T> implements ConnectionInfo<T> {
  protected String address;
  protected SourceInfo source;
  protected TargetInfo target;

  @Override
  public String address() {
    return address;
  }

  @Override
  public SourceInfo source() {
    return source;
  }

  @Override
  public TargetInfo target() {
    return target;
  }

}
