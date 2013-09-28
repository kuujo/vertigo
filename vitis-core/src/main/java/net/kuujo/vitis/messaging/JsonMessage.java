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
package net.kuujo.vitis.messaging;

import net.kuujo.vitis.Serializeable;

import org.vertx.java.core.json.JsonObject;

/**
 * An eventbus message.
 *
 * @author Jordan Halterman
 */
public interface JsonMessage extends Serializeable<JsonObject> {

  /**
   * Returns the message ID.
   *
   * @return
   *   The message ID.
   */
  public String id();

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
   * Returns the message tag.
   *
   * @return
   *   A message tag.
   */
  public String tag();

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
   * @param id
   *   The child ID.
   * @param body
   *   The child body.
   * @return
   *   A new child message.
   */
  public JsonMessage createChild(String id, JsonObject body);

  /**
   * Creates a new child of the message.
   *
   * @param body
   *   The child body.
   * @param tag
   *   A tag to apply to the child. If no tag is specified then the
   *   parent tag will be inherited.
   * @return
   *   A new child message.
   */
  public JsonMessage createChild(JsonObject body, String tag);

  /**
   * Creates a new child of the message.
   *
   * @param id
   *   The child ID.
   * @param body
   *   The child body.
   * @param tag
   *   A tag to apply to the child. If no tag is specified then the
   *   parent tag will be inherited.
   * @return
   *   A new child message.
   */
  public JsonMessage createChild(String id, JsonObject body, String tag);

  /**
   * Creates a copy of the message.
   *
   * @return
   *   A copy of the message.
   */
  public JsonMessage copy();

}
