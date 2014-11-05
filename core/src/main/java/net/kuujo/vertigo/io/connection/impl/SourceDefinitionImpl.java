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

import io.vertx.core.json.JsonObject;
import net.kuujo.vertigo.io.connection.SourceDefinition;

/**
 * Connection source options.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class SourceDefinitionImpl extends EndpointDefinitionImpl<SourceDefinition> implements SourceDefinition {

  /**
   * <code>component</code> indicates the source component name.
   */
  public static final String SOURCE_COMPONENT = "component";

  /**
   * <code>port</code> indicates the source output port.
   */
  public static final String SOURCE_PORT = "port";

  public SourceDefinitionImpl() {
    super();
  }

  public SourceDefinitionImpl(SourceDefinitionImpl source) {
    super(source);
  }

  public SourceDefinitionImpl(JsonObject source) {
    super(source);
  }

}
