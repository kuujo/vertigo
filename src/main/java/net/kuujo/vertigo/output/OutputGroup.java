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
package net.kuujo.vertigo.output;


/**
 * Output group.<p>
 *
 * Output groups represent structured groups of output messages. Users can
 * use output groups to batch messages in a manner that will be similarly
 * represented on input connections. When messages are grouped, Vertigo
 * guarantees that the other side of the connection(s) will see each of
 * the messages within the group. Additionally, sub-groups may be created,
 * and Vertigo guarantees that sub-groups are sent and ended prior to parent
 * groups ending. This allows for logical grouping of messages. Note that
 * while groups are guaranteed to be strongly ordered, messages within groups
 * are not guaranteed to be ordered.
 *
 * @author Jordan Halterman
 */
public interface OutputGroup extends Output<OutputGroup> {

  /**
   * Returns the output group name.
   *
   * @return The output group name.
   */
  String name();

  /**
   * Ends the output group.
   *
   * @return The output group.
   */
  OutputGroup end();

}
