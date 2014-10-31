/*
 * Copyright 2013-2014 the original author or authors.
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
package net.kuujo.vertigo.io.partitioner;

import io.vertx.core.MultiMap;
import net.kuujo.vertigo.io.connection.OutputConnectionInfo;

import java.io.Serializable;
import java.util.List;

/**
 * Connection selector.<p>
 *
 * Connection selectors are used to route messages between multiple instances
 * of a component. When a message is sent on an output stream, the stream's
 * selector will partition a connection or set of connections to which to copy
 * the message.<p>
 *
 * Vertigo supports custom selector implementations. To implement a custom
 * selector, simply implement the {@link Partitioner} interface as any selector
 * implementation does.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public interface Partitioner extends Serializable {

  /**
   * <code>round-robin</code> is a round robin connection selector.
   */
  public static final String ROUND_ROBIN = "round-robin";

  /**
   * <code>random</code> is a random connection selector.
   */
  public static final String RANDOM = "random";

  /**
   * <code>hash</code> is a mod-hash based connection selector.
   */
  public static final String HASH = "hash";

  /**
   * <code>fair</code> is a selector that selects connections based on lowest queue size.
   */
  public static final String FAIR = "fair";

  /**
   * <code>all</code> is a selector that routes messages to all connections.
   */
  public static final String ALL = "all";

  /**
   * <code>custom</code> indicates a custom selector.
   */
  public static final String CUSTOM = "custom";

  /**
   * Selects a list of connections to which to send a message.
   *
   * @param headers The message headers being sent.
   * @param connections A list of connections from which to partition.
   * @return A list of selected connections.
   */
  List<OutputConnectionInfo> partition(MultiMap headers, List<OutputConnectionInfo> connections);

}
