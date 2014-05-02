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
package net.kuujo.vertigo.io.group;

import net.kuujo.vertigo.io.Output;
import net.kuujo.vertigo.io.OutputGroupSupport;

/**
 * Output group represent collections of output messages.<p>
 *
 * Output groups represent structured groups of output messages. Users can
 * use output groups to batch messages in a manner that will be similarly
 * represented on input connections. When messages are grouped, Vertigo
 * guarantees that the other side of the connection(s) will see each of
 * the messages within the group. Additionally, sub-groups may be created,
 * and Vertigo guarantees that sub-groups are sent and ended prior to parent
 * groups ending. This allows for logical grouping of messages.<p>
 *
 * All messages within a root level output group will be sent on the same
 * connection. In other words, connection selection is performed per-group
 * rather than per-message.<p>
 *
 * Grouped messages are guaranteed to be ordered just as all Vertigo messages
 * are. However, some components of a group are not ordered. Specifically, if
 * a group is ended before all of its children (groups) have ended, the parent
 * group will wait for the children to end before sending its own end message.
 * This helps Vertigo guarantee that when all children of a group have completed
 * before a parent completing.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public interface OutputGroup extends Group<OutputGroup>, Output<OutputGroup>, OutputGroupSupport<OutputGroup> {

  /**
   * Ends the output group.
   *
   * @return The output group.
   */
  OutputGroup end();

}
