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
package net.kuujo.vitis.eventbus;

import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

/**
 * An eventbus action helper.
 *
 * @author Jordan Halterman
 */
public class Actions {

  /**
   * Creates an eventbus action.
   *
   * @param action
   *   The action to create.
   * @return
   *   A JSON action.
   */
  public static JsonObject create(String action) {
    return new JsonObject().putString("action", action);
  }

  /**
   * Creates an eventbus action.
   *
   * @param action
   *   The action to create.
   * @param data
   *   A JSON object containing the action configuration.
   * @return
   *   A JSON action.
   */
  public static JsonObject create(String action, JsonObject data) {
    return new JsonObject().putString("action", action).putObject(action, data);
  }

  /**
   * Creates an eventbus action.
   *
   * @param action
   *   The action to create.
   * @param data
   *   A JSON array containing the action configuration.
   * @return
   *   A JSON action.
   */
  public static JsonObject create(String action, JsonArray data) {
    return new JsonObject().putString("action", action).putArray(action, data);
  }

  /**
   * Creates an eventbus action.
   *
   * @param action
   *   The action to create.
   * @param data
   *   A string containing the action configuration.
   * @return
   *   A JSON action.
   */
  public static JsonObject create(String action, String data) {
    return new JsonObject().putString("action", action).putString(action, data);
  }

  /**
   * Creates an eventbus action.
   *
   * @param action
   *   The action to create.
   * @param data
   *   A number containing the action configuration.
   * @return
   *   A JSON action.
   */
  public static JsonObject create(String action, Number data) {
    return new JsonObject().putString("action", action).putNumber(action, data);
  }

}
