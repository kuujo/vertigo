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
package com.blankstyle.vine.eventbus.vine.actions;

import com.blankstyle.vine.context.VineContext;
import com.blankstyle.vine.eventbus.Action;
import com.blankstyle.vine.eventbus.Argument;
import com.blankstyle.vine.eventbus.ArgumentsDefinition;
import com.blankstyle.vine.eventbus.SynchronousAction;

/**
 * A vine verticle ping action.
 *
 * @author Jordan Halterman
 */
public class Ping extends Action<VineContext> implements SynchronousAction<String> {

  public static final String NAME = "ping";

  private static ArgumentsDefinition args = new ArgumentsDefinition() {{
    addArgument(new Argument<String>() {
      @Override
      public String name() {
        return "message";
      }
      @Override
      public boolean isValid(String value) {
        return value instanceof String && value == "ping";
      }
    });
  }};

  @Override
  public ArgumentsDefinition getArgumentsDefinition() {
    return args;
  }

  @Override
  public String execute(Object[] args) {
    return "pong";
  }

}
