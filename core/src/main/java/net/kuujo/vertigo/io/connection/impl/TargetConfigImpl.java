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
import net.kuujo.vertigo.io.connection.TargetConfig;
import net.kuujo.vertigo.io.port.InputPortConfig;

/**
 * Connection target options.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class TargetConfigImpl extends EndpointConfigImpl<TargetConfig> implements TargetConfig {

  public TargetConfigImpl() {
    super();
  }

  public TargetConfigImpl(TargetConfig target) {
    super(target);
  }

  public TargetConfigImpl(InputPortConfig port) {
    super(port.getComponent().getName(), port.getName());
  }

  public TargetConfigImpl(JsonObject target) {
    super(target);
  }

}
