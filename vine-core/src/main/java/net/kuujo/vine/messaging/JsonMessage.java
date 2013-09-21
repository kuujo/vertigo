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
package net.kuujo.vine.messaging;

import net.kuujo.vine.Serializeable;

import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

/**
 * An eventbus message.
 *
 * @author Jordan Halterman
 */
public interface JsonMessage extends Serializeable<JsonObject> {

  /**
   * Returns the message tree ID.
   *
   * @return
   *   The message tree ID.
   */
  public String tree();

  /**
   * Returns the message source address.
   *
   * @return
   *   The source address.
   */
  public String source();

  /**
   * Returns the message body.
   *
   * @return
   *   The message body.
   */
  public JsonObject body();

  /**
   * Returns an array of message tags.
   *
   * @return
   *   An array of message tags.
   */
  public JsonArray tags();

  /**
   * Creates a new child of the message.
   *
   * @param body
   *   The child body.
   * @return
   *   A new child message.
   */
  public JsonMessage createChild(JsonObject body);

  /**
   * Creates a new child of the message.
   *
   * @param body
   *   The child body.
   * @param tags
   *   Tags to apply to the child.
   * @return
   *   A new child message.
   */
  public JsonMessage createChild(JsonObject body, JsonArray tags);

  /**
   * Creates a copy of the message.
   *
   * @return
   *   A copy of the message.
   */
  public JsonMessage copy();

}
