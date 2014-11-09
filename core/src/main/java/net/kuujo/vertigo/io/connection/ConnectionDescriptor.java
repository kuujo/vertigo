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

import net.kuujo.vertigo.io.partition.Partitioner;

/**
 * Immutable connection descriptor.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public interface ConnectionDescriptor {

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
   * <code>partitioner</code> is an object defining the connection partitioner. The partitioner
   * definition should contain a <code>type</code> which indicates the partitioner type,
   * e.g. <code>round-robin</code>, <code>random</code>, <code>hash</code>, <code>fair</code>,
   * <code>all</code>, or <code>custom</code>. If a <code>custom</code> selector is indicated
   * then an additional <code>selector</code> field must be provided which indicates the
   * custom selector class.
   */
  public static final String CONNECTION_PARTITIONER = "partitioner";

  /**
   * Returns the connection source.
   *
   * @return The connection source.
   */
  SourceDescriptor source();

  /**
   * Returns the connection target.
   *
   * @return The connection target.
   */
  TargetDescriptor target();

  /**
   * Returns the connection partitioner.
   *
   * @return The connection partitioner.
   */
  Partitioner partitioner();

}
