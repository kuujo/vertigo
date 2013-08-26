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
package com.blankstyle.vine.eventbus;

import java.util.ArrayList;
import java.util.List;

/**
 * An action arguments definition.
 *
 * @author Jordan Halterman
 */
public abstract class ArgumentsDefinition {

  protected List<Argument<?>> args = new ArrayList<Argument<?>>();

  /**
   * Adds an argument to the arguments definition.
   *
   * @param arg
   *   The argument to add.
   * @return
   *   The called arguments definition.
   */
  public ArgumentsDefinition addArgument(Argument<?> arg) {
    if (!args.contains(arg)) {
      args.add(arg);
    }
    return this;
  }

  /**
   * Removes an argument from the arguments definition.
   *
   * @param arg
   *   The argument to remove.
   * @return
   *   The called arguments definition.
   */
  public ArgumentsDefinition removeArgument(Argument<?> arg) {
    if (args.contains(arg)) {
      args.remove(arg);
    }
    return this;
  }

  /**
   * Returns a set of defined arguments.
   *
   * @return
   *   A set of defined arguments.
   */
  public List<Argument<?>> getArguments() {
    return args;
  }

}
