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

import org.vertx.java.core.json.JsonObject;

/**
 * A JSON object-based eventbus command.
 *
 * @author Jordan Halterman
 */
public class JsonCommand implements Command {

  private JsonObject json;

  public JsonCommand(JsonObject json) {
    this.json = json;
  }

  @Override
  public String getAction() {
    return json.getString(ACTION_KEY);
  }

  @Override
  public Object[] getArguments() {
    return json.getArray(ARGUMENTS_KEY).toArray();
  }

}
