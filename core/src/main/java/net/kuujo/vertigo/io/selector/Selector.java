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
package net.kuujo.vertigo.io.selector;

import java.util.List;

import net.kuujo.vertigo.io.connection.Connection;
import net.kuujo.vertigo.util.serialization.JsonSerializable;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Connection selector.<p>
 *
 * Connection selectors are used to route messages between multiple instances
 * of a component. When a message is sent on an output stream, the stream's
 * selector will select a connection or set of connections to which to copy
 * the message.<p>
 *
 * Vertigo supports custom selector implementations. To implement a custom
 * selector, simply implement the {@link Selector} interface as any selector
 * implementation does.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
@JsonTypeInfo(use=JsonTypeInfo.Id.NAME, include=JsonTypeInfo.As.PROPERTY, property="type", defaultImpl=CustomSelector.class)
@JsonSubTypes({
  @JsonSubTypes.Type(value=RandomSelector.class, name="random"),
  @JsonSubTypes.Type(value=RoundRobinSelector.class, name="round-robin"),
  @JsonSubTypes.Type(value=HashSelector.class, name="hash"),
  @JsonSubTypes.Type(value=FairSelector.class, name="fair"),
  @JsonSubTypes.Type(value=AllSelector.class, name="all")
})
public interface Selector extends JsonSerializable {

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
   * @param message The message being sent.
   * @param targets A list of connections from which to select.
   * @return A list of selected connections.
   */
  @SuppressWarnings("rawtypes")
  <T extends Connection> List<T> select(Object message, List<T> connections);

}
