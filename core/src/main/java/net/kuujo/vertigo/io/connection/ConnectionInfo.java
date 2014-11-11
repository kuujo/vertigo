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

import io.vertx.codegen.annotations.VertxGen;
import net.kuujo.vertigo.TypeInfo;

/**
 * A connection represents a link between two components within a network.<p>
 *
 * When a connection is created, each partition of the source component
 * will be setup to send messages to each partition of the target component.
 * How messages are routed to multiple target partitions can be configured
 * using selectors.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
@VertxGen
public interface ConnectionInfo extends TypeInfo {

  /**
   * <code>source</code> is an object defining the connection source. See the
   * {@link SourceInfo} documentation for details on the source structure.
   */
  public static final String CONNECTION_SOURCE = "source";

  /**
   * <code>target</code> is an object defining the connection target. See the
   * {@link TargetInfo} documentation for details on the target structure.
   */
  public static final String CONNECTION_TARGET = "target";

  /**
   * Sets the connection source.
   *
   * @param source The connection source.
   * @return The connection info.
   */
  ConnectionInfo setSource(SourceInfo source);

  /**
   * Returns the connection source.
   *
   * @return The connection source.
   */
  SourceInfo getSource();

  /**
   * Sets the connection target.
   *
   * @param target The connection target.
   * @return The connection info.
   */
  ConnectionInfo setTarget(TargetInfo target);

  /**
   * Returns the connection target.
   *
   * @return The connection target.
   */
  TargetInfo getTarget();

}
