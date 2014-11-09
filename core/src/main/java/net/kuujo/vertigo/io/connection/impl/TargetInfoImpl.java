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

import net.kuujo.vertigo.io.connection.TargetInfo;
import net.kuujo.vertigo.io.connection.TargetDescriptor;

/**
 * Connection target options.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class TargetInfoImpl extends EndpointInfoImpl<TargetInfo> implements TargetInfo {

  /**
   * <code>component</code> indicates the target component name.
   */
  public static final String TARGET_COMPONENT = "component";

  /**
   * <code>port</code> indicates the target output port.
   */
  public static final String TARGET_PORT = "port";

  public TargetInfoImpl() {
    super();
  }

  public TargetInfoImpl(TargetInfo target) {
    super(target);
  }

  public TargetInfoImpl(TargetDescriptor target) {
    super(target);
  }

}
