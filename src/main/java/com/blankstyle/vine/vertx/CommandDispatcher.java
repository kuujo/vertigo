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
package com.blankstyle.vine.vertx;

import java.util.HashMap;
import java.util.Map;

import org.vertx.java.core.json.JsonObject;

/**
 * An eventbus command dispatcher.
 *
 * @author Jordan Halterman
 */
public class CommandDispatcher {

  private Map<String, Class<? extends Command<?>>> commands = new HashMap<String, Class<? extends Command<?>>>();

  /**
   * Registers a command handler.
   *
   * @param name
   *   The command name.
   * @param command
   *   The command class.
   */
  public void registerCommand(String name, Class<? extends Command<?>> command) {
    commands.put(name, command);
  }

  /**
   * Unregisters a command.
   *
   * @param name
   *   The command name.
   */
  public void unregisterCommand(String name) {
    if (commands.containsKey(name)) {
      commands.remove(name);
    }
  }

  /**
   * Dispatches a command.
   *
   * @param command
   *   The command name.
   * @param args
   *   A JSON object of command arguments.
   */
  public Object dispatch(String command, JsonObject args) {
    if (commands.containsKey(command)) {
      try {
        return commands.get(command).newInstance().execute(args);
      }
      catch (InstantiationException e) {
        return null;
      }
      catch (IllegalAccessException e) {
        return null;
      }
    }
    return null;
  }

}
