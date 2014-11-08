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

import io.vertx.codegen.annotations.VertxGen;

/**
 * Connection target builder.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
@VertxGen
public interface ConnectionTargetComponentBuilder extends ConnectionEndpointComponentBuilder<ConnectionTargetComponentBuilder> {

  /**
   * Creates a combined connection target, adding an additional target to the connection.
   *
   * @return The updated connection target builder.
   */
  ConnectionTargetBuilder and();

  /**
   * Creates a combined connection target, adding an additional target to the connection.
   *
   * @param component The target component to add to the connection definition.
   * @return The updated connection target builder.
   */
  ConnectionTargetComponentBuilder and(String component);

}
