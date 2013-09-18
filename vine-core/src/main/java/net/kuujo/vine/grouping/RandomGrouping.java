/*
* Copyright 2013 the original author or authors.
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
package net.kuujo.vine.grouping;

import net.kuujo.vine.definition.GroupingDefinition;
import net.kuujo.vine.messaging.RandomDispatcher;

/**
 * A random grouping implementation.
 *
 * This grouping dispatches messages to workers in a random fashion
 * using the RandomDispatcher dispatcher.
 *
 * @author Jordan Halterman
 */
public class RandomGrouping extends GroupingDefinition {

  public RandomGrouping() {
    super();
    definition.putString("dispatcher", RandomDispatcher.class.getName());
  }

}
