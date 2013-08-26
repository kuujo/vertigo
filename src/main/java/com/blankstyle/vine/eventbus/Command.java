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

/**
 * A remote command.
 *
 * @author Jordan Halterman
 */
public interface Command {

  public static final String ACTION_KEY = "action";

  public static final String ARGUMENTS_KEY = "args";

  /**
   * Returns the command action name.
   *
   * @return
   *   The command action name.
   */
  public String getAction();

  /**
   * Returns the command arguments.
   *
   * @return
   *   The command arguments.
   */
  public Object[] getArguments();

}
