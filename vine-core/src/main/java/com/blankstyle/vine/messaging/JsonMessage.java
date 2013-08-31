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
package com.blankstyle.vine.messaging;

import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import com.blankstyle.vine.Serializeable;

/**
 * A JSON message implementation.
 *
 * @author Jordan Halterman
 */
public class JsonMessage implements Serializeable<JsonObject> {

  protected JsonObject json;

  public JsonMessage() {
    this.json = new JsonObject();
  }

  public JsonMessage(JsonObject json) {
    this.json = json;
  }

  /**
   * Sets the unique message identifier.
   *
   * @param id
   *   A unique message identifier.
   * @return
   *   The called message object.
   */
  public JsonMessage setIdentifier(long id) {
    json.putNumber("id", id);
    return this;
  }

  /**
   * Gets the unique message identifier.
   *
   * @return
   *   A unique message identifier.
   */
  public long getIdentifier() {
    return json.getLong("id");
  }

  /**
   * Adds a tag to the message.
   *
   * @param tag
   *   A message tag.
   * @return
   *   The called message object.
   */
  public JsonMessage tag(String tag) {
    JsonArray tags = json.getArray("tags");
    if (tags == null) {
      tags = new JsonArray();
      json.putArray("tags", tags);
    }
    tags.add(tag);
    return this;
  }

  /**
   * Gets an array of message tags.
   *
   * @return
   *   An array of message tags.
   */
  public JsonArray getTags() {
    return json.getArray("tags");
  }

  /**
   * Sets the message body.
   *
   * @param body
   *   The message body.
   * @return
   *   The called message object.
   */
  public JsonMessage setBody(JsonObject body) {
    json.putValue("body", body);
    return this;
  }

  /**
   * Returns the JSON message body.
   *
   * @return
   *   A message body.
   */
  public JsonObject body() {
    return json.getObject("body");
  }

  @Override
  public JsonObject serialize() {
    return json;
  }

  /**
   * Copies the JSON message.
   *
   * @return
   *   A new JSON message.
   */
  public JsonMessage copy() {
    return new JsonMessage(json);
  }

}
